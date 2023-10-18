package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;

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
	public void onInstalled(ShipVariantAPI variant) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onEnhanced(ShipVariantAPI variant) {
		variant.addPermaMod(HullMods.CIVGRADE, false);
		commitVariantChanges();
	}

	@Override
	public void onNormalized(ShipVariantAPI variant) {
		variant.removePermaMod(HullMods.CIVGRADE);
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
		commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	private static final EnumSet<ShipTypeHints> hintsToRemove = EnumSet.of(ShipTypeHints.CARRIER, ShipTypeHints.COMBAT, ShipTypeHints.NO_AUTO_ESCORT);
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
		if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(ehm_internals.id.hullmods.base)) return;

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(variant.getHullSpec());
		ShipHullSpecAPI originalHullSpec = ehm_hullSpecReference(variant);
		// boolean commitVariantChanges = false;

		float logisticsBonus = 0;

		// bonus from weapon slots
		for (WeaponSlotAPI slot : originalHullSpec.getAllWeaponSlotsCopy()) {
			if (slot.isBuiltIn()) {
				if (slot.getWeaponType() != WeaponType.BUILT_IN) continue;
			} else if (!slot.isWeaponSlot()) continue;

			logisticsBonus += logisticsSlotBonus.get(slot.getSlotSize());

			// commitVariantChanges = ehm_deactivateSlot(hullSpec, null, slot.getId());
			// _ehm_ar_base.ehm_deactivateSlot(lyr_hullSpec, null, slot.getId());
			lyr_hullSpec.getWeaponSlot(slot.getId()).setWeaponType(WeaponType.DECORATIVE);
		}

		// bonus from ship system
		if (originalHullSpec.getShipSystemId() != null) {
			logisticsBonus += logisticsModBonus.get(hullSize);
			lyr_hullSpec.setShipSystemId(null);
		}

		// bonus from defense system
		if (originalHullSpec.getShieldSpec().getType() != ShieldType.NONE) {
			logisticsBonus += logisticsModBonus.get(hullSize);
			lyr_hullSpec.getShieldSpec().setType(ShieldType.NONE);
		}

		// bonus from fighter bays
		float bays = stats.getNumFighterBays().getBaseValue(); if (bays > 0) {
			logisticsBonus += bays * logisticsSlotBonus.get(WeaponSize.LARGE);
			stats.getNumFighterBays().modifyFlat(this.hullModSpecId, -bays);	// game nukes the
		}

		// adjusting hints
		EnumSet<ShipTypeHints> hints = lyr_hullSpec.getHints();
		hints.removeAll(hintsToRemove); hints.add(ShipTypeHints.CIVILIAN);

		if (!stats.getVariant().getSMods().contains(this.hullModSpecId)) {
			variant.setHullSpecAPI(lyr_hullSpec.retrieve()); return;
		}

		// if (!variant.hasHullMod(HullMods.CIVGRADE)) {
		if (!originalHullSpec.getBuiltInMods().contains(HullMods.CIVGRADE)) {
			lyr_hullSpec.setOrdnancePoints((int) Math.round(originalHullSpec.getOrdnancePoints(null)*0.25));

			if (!variant.hasHullMod(HullMods.AUTOMATED)) {
				stats.getMinCrewMod().modifyMult(this.hullModSpecId, 0.25f);
				// stats.getMaxCrewMod().modifyMult(this.hullModSpecId, 0.50f);
			}

			stats.getSuppliesPerMonth().modifyMult(this.hullModSpecId, 0.25f);
			stats.getSuppliesToRecover().modifyMult(this.hullModSpecId, 0.25f);
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(this.hullModSpecId, 0.25f);
		}

		// if (variant.hasHullMod(HullMods.CIVGRADE)) {
		// 	LinkedHashSet<String> sMods = variant.getSMods();

		// 	if (sMods.contains(HullMods.ADDITIONAL_BERTHING) && !variant.hasHullMod(HullMods.AUTOMATED)) stats.getMaxCrewMod().modifyFlat(this.hullModSpecId, logisticsModBonus.get(hullSize));
		// 	if (sMods.contains(HullMods.EXPANDED_CARGO_HOLDS)) stats.getCargoMod().modifyFlat(this.hullModSpecId, logisticsModBonus.get(hullSize));
		// 	if (sMods.contains(HullMods.AUXILIARY_FUEL_TANKS)) stats.getFuelMod().modifyFlat(this.hullModSpecId, logisticsModBonus.get(hullSize));
		// }

		stats.getCargoMod().modifyFlat(this.hullModSpecId, logisticsBonus);
		stats.getFuelMod().modifyFlat(this.hullModSpecId, logisticsBonus);

		stats.getDynamic().getMod(Stats.MAX_LOGISTICS_HULLMODS_MOD).modifyFlat(this.hullModSpecId, 1);
		stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(this.hullModSpecId, 2);

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
		// if (commitVariantChanges && !isGettingRestored(variant)) { commitVariantChanges = false; commitVariantChanges(); }
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		// ship.setShield(ShieldType.NONE, 0.0F, 1.0F, 1.0F);
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override public boolean hasSModEffect() { return true; }

	@Override public boolean hasSModEffectSection(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return true; }

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "logistical bonuses";
			default: return null;
		}
	}

	@Override
	public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
		if (!this.isApplicableToShip(ship)) return;

		if (!ship.getVariant().getHullSpec().getBuiltInMods().contains(ehm_internals.id.hullmods.base)) {
			tooltip.addSectionHeading(header.noEffect, header.noEffect_textColour, header.noEffect_bgColour, Alignment.MID, header.padding);
			tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			return;
		}

		if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.noEffect, header.noEffect_textColour, header.noEffect_bgColour, Alignment.MID, header.padding);
			tooltip.addPara(text.overEngineeredNoEffect[0], text.padding).setHighlight(text.overEngineeredNoEffect[1]);
		} else {
			tooltip.addSectionHeading(header.sEffect, header.sEffect_textColour, header.sEffect_bgColour, Alignment.MID, header.padding);
			tooltip.addPara(this.hullModSpec.getSModDescription(hullSize).replaceAll("\\%", "%%"), text.padding, header.sEffect_textColour, this.getSModDescriptionParam(0, hullSize));

			tooltip.addPara("+1 Built-in & Logistics modification capacity", text.padding, header.sEffect_textColour, "+1");
			String logisticsBonus = "+"+(int) ship.getMutableStats().getCargoMod().getFlatBonus(this.hullModSpecId).value;
			tooltip.addPara(logisticsBonus+" Fuel & Cargo storage", text.padding, header.sEffect_textColour, logisticsBonus);
			if (!ship.getVariant().getHullSpec().getBuiltInMods().contains(HullMods.CIVGRADE)) tooltip.addPara("x0.25 Skeleton Crew, Maintenance & Ordnance Points", text.padding, header.sEffect_textColour, "x0.25");
		}
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "story point";
			case 1: return "an additional built-in and an additional logistics hull modifications";
			case 2: return "5/10/20";
			case 3: return "20";
			case 4: return "15/30/60/100";
			case 5: return "skeleton crew, maintenance costs and ordnance points";
			case 6: return "75%";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (lyr_miscUtilities.isModule(ship)) tooltip.addPara(text.isModule[0], text.padding).setHighlight(text.isModule[1]);
			if (lyr_miscUtilities.isParent(ship)) tooltip.addPara(text.isParent[0], text.padding).setHighlight(text.isParent[1]);
			if (lyr_miscUtilities.hasModularHullmods(ship, this.hullModSpecId, false)
			|| lyr_miscUtilities.hasWeapons(ship)
			|| lyr_miscUtilities.hasAnyFittedWings(ship)
			|| lyr_miscUtilities.hasCapacitorsOrVents(ship)) tooltip.addPara(text.notStripped[0], text.padding).setHighlight(text.notStripped[1]);
		} else if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.overEngineeredWarning[0], text.padding).setHighlight(text.overEngineeredWarning[1]);
		}

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(header.lockedIn, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) return false;
		if (lyr_miscUtilities.isModule(ship)) return false;
		if (lyr_miscUtilities.isParent(ship)) return false;
		if (lyr_miscUtilities.hasWeapons(ship)) return false;
		if (lyr_miscUtilities.hasAnyFittedWings(ship)) return false;
		if (lyr_miscUtilities.hasCapacitorsOrVents(ship)) return false;
		if (lyr_miscUtilities.hasModularHullmods(ship, this.hullModSpecId, false)) return false;

		return true;
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		return true;
	}
	//#endregion
}
