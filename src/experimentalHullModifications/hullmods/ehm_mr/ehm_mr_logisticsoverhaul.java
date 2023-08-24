package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_hullSpec;

/**
 * <p> This category {@code ehm_mr} covers the odd ones since the evens have their own
 * categories, and as such they extend the base effect directly and don't have a base
 * of their own.
 * @category Miscellaneous Retrofit 
 * @author lyravega
 */
public final class ehm_mr_logisticsoverhaul extends _ehm_base implements normalEvents, enhancedEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onEnhance(ShipVariantAPI variant) {
		commitVariantChanges();
	}

	@Override
	public void onNormalize(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
		commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public static final Map<HullSize, Float> logisticsModBonus = new HashMap<HullSize, Float>();
	public static final Map<WeaponSize, Float> logisticsSlotBonus = new HashMap<WeaponSize, Float>();
	static {
		logisticsModBonus.put(HullSize.FRIGATE, 15f);
		logisticsModBonus.put(HullSize.DESTROYER, 30f);
		logisticsModBonus.put(HullSize.CRUISER, 50f);
		logisticsModBonus.put(HullSize.CAPITAL_SHIP, 100f);

		logisticsSlotBonus.put(WeaponSize.SMALL, 5f);
		logisticsSlotBonus.put(WeaponSize.MEDIUM, 10f);
		logisticsSlotBonus.put(WeaponSize.LARGE, 20f);
	}


	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		if (!stats.getVariant().getSMods().contains(this.hullModSpecId)) return;

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		// boolean commitVariantChanges = false;

		float slotLogisticsBonus = 0;
		
		for (WeaponSlotAPI slot : lyr_hullSpec.getAllWeaponSlotsCopy()) {
			slotLogisticsBonus += logisticsSlotBonus.get(slot.getSlotSize());
			
			// commitVariantChanges = ehm_deactivateSlot(hullSpec, null, slot.getId());
			// _ehm_ar_base.ehm_deactivateSlot(lyr_hullSpec, null, slot.getId());
			lyr_hullSpec.getWeaponSlot(slot.getId()).setWeaponType(WeaponType.DECORATIVE);
		}

		if (variant.hasHullMod(HullMods.CIVGRADE)) {
			LinkedHashSet<String> sMods = variant.getSMods();

			if (sMods.contains(HullMods.ADDITIONAL_BERTHING) && !variant.hasHullMod(HullMods.AUTOMATED)) stats.getMaxCrewMod().modifyFlat(this.hullModSpecId, logisticsModBonus.get(hullSize));
			if (sMods.contains(HullMods.EXPANDED_CARGO_HOLDS)) stats.getCargoMod().modifyFlat(this.hullModSpecId, logisticsModBonus.get(hullSize));
			if (sMods.contains(HullMods.AUXILIARY_FUEL_TANKS)) stats.getFuelMod().modifyFlat(this.hullModSpecId, logisticsModBonus.get(hullSize));
		} else {
			lyr_hullSpec.setOrdnancePoints((int) Math.round(ehm_hullSpecReference(variant).getOrdnancePoints(null)*0.25));

			if (!variant.hasHullMod(HullMods.AUTOMATED)) {
				stats.getMinCrewMod().modifyMult(hullModSpecId, 0.10f);
				// stats.getMaxCrewMod().modifyMult(hullModSpecId, 0.50f);
			}
			stats.getCargoMod().modifyFlat(this.hullModSpecId, slotLogisticsBonus);
			stats.getFuelMod().modifyFlat(this.hullModSpecId, slotLogisticsBonus);
		}

		stats.getDynamic().getMod(Stats.MAX_LOGISTICS_HULLMODS_MOD).modifyFlat(this.hullModSpecId, 1);
		stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(this.hullModSpecId, 2);

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
		// if (commitVariantChanges && !isGettingRestored(variant)) { commitVariantChanges = false; commitVariantChanges(); }
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setShield(ShieldType.NONE, 0.0F, 1.0F, 1.0F);
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public boolean hasSModEffect() {
		return true;
	}

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "Herp";
			case 1: return "Derp";
			default: return null;
		}
	}

	@Override
	public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
		if (isApplicableToShip(ship)) {
			if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
				tooltip.addSectionHeading(header.noEffect, header.noEffect_textColour, header.noEffect_bgColour, Alignment.MID, header.padding);
				tooltip.addPara(text.overEngineeredNoEffect[0], text.padding).setHighlight(text.overEngineeredNoEffect[1]);
			} else {
				tooltip.addSectionHeading(header.sEffect, header.sEffect_textColour, header.sEffect_bgColour, Alignment.MID, header.padding);
				tooltip.addPara(this.hullModSpec.getSModDescription(hullSize).replaceAll("\\%", "%%"), text.padding, header.sEffect_textColour, getSModDescriptionParam(0, hullSize), getSModDescriptionParam(1, hullSize)); 
			}
		}
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "10/10/20/20";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
		}

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(header.lockedIn, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		ShipVariantAPI variant = ship.getVariant();

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		if (ehm_hasModularHullmods(ship, this.hullModSpecId)) return false;

		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		return true;
	}
	//#endregion
}
