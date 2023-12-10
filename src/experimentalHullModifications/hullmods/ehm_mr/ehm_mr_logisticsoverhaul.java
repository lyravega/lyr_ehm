package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.EnumMap;
import java.util.EnumSet;

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
import lyravega.utilities.lyr_tooltipUtilities;

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
	public void onInstalled(MutableShipStatsAPI stats) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();

		this.restoreHullSpec(variant);
		variant.removeMod(HullMods.CIVGRADE);	// to clean-up the variant if it was added as a built-in through this mod

		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onEnhanced(MutableShipStatsAPI stats) {
		commitVariantChanges();
	}

	@Override
	public void onNormalized(MutableShipStatsAPI stats) {
		this.restoreHullSpec(stats.getVariant());

		commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	private static final EnumSet<ShipTypeHints> hintsToRemove = EnumSet.of(ShipTypeHints.CARRIER, ShipTypeHints.COMBAT, ShipTypeHints.NO_AUTO_ESCORT, ShipTypeHints.PHASE);
	private static final EnumSet<ShipTypeHints> hintsToAdd = EnumSet.of(ShipTypeHints.CIVILIAN, ShipTypeHints.ALWAYS_PANIC);

	public static final EnumMap<HullSize, Float> logisticsModBonus = new EnumMap<HullSize, Float>(HullSize.class);
	public static final EnumMap<WeaponSize, Float> logisticsSlotBonus = new EnumMap<WeaponSize, Float>(WeaponSize.class);
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
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		ShipHullSpecAPI originalHullSpec = lyr_hullSpec.referenceNonDamaged();

		float logisticsBonus = 0;

		// bonus from weapon slots
		for (WeaponSlotAPI slot : originalHullSpec.getAllWeaponSlotsCopy()) {
			if (!slot.isWeaponSlot()) continue;	// TODO: needs to affect built-in weapons

			logisticsBonus += logisticsSlotBonus.get(slot.getSlotSize());
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
			stats.getNumFighterBays().modifyFlat(this.hullModSpecId, -bays);
		}

		// adjusting hints & adding civgrade if needed
		EnumSet<ShipTypeHints> hints = lyr_hullSpec.getHints();	hints.removeAll(hintsToRemove); hints.addAll(hintsToAdd);
		if (!lyr_hullSpec.retrieve().isBuiltInMod(HullMods.CIVGRADE)) lyr_hullSpec.addBuiltInMod(HullMods.CIVGRADE);

		if (!stats.getVariant().getSMods().contains(this.hullModSpecId)) {
			variant.setHullSpecAPI(lyr_hullSpec.retrieve()); return;	// cut-off point for non s-mod effects
		}

		if (!lyr_miscUtilities.hasCivilianHintsOrMod(originalHullSpec, true)) {
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

		if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.noEffect, header.noEffect_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.overEngineeredNoEffect, text.padding);
		} else {
			tooltip.addSectionHeading(header.sEffect, header.sEffect_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara(this.hullModSpec.getSModDescription(hullSize).replaceAll("\\%", "%%"), text.padding, header.sEffect_textColour, this.getSModDescriptionParam(0, hullSize));

			tooltip.addPara("+1 Built-in & Logistics modification capacity", text.padding, header.sEffect_textColour, "+1");
			String logisticsBonus = "+"+(int) ship.getMutableStats().getCargoMod().getFlatBonus(this.hullModSpecId).value;
			tooltip.addPara(logisticsBonus+" Fuel & Cargo storage", text.padding, header.sEffect_textColour, logisticsBonus);
			if (!lyr_miscUtilities.hasCivilianHintsOrMod(ship.getHullSpec(), true)) tooltip.addPara("x0.25 Skeleton Crew, Maintenance & Ordnance Points", text.padding, header.sEffect_textColour, "x0.25");
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
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.hullmods.main.base)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.lacksBase, text.padding);
			if (lyr_miscUtilities.isModule(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.isModule, text.padding);
			if (lyr_miscUtilities.isParent(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.isParent, text.padding);
			if (!lyr_miscUtilities.isStripped(ship, this.hullModSpecId)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.notStripped, text.padding);
		} else if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.overEngineeredWarning, text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.hullmods.main.base)) return false;
		if (lyr_miscUtilities.isModule(ship)) return false;
		if (lyr_miscUtilities.isParent(ship)) return false;
		if (!lyr_miscUtilities.isStripped(ship, this.hullModSpecId)) return false;

		return true;
	}
	//#endregion
}
