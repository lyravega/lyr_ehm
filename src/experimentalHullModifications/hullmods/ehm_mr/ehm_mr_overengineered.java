package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.EnumMap;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

/**
 * First experimental hull modification that can be built-in, with an original effect.
 * Increases the OP limit of the ship and also yields slot points.
 * <p> This category {@code ehm_mr} covers the odd ones since the evens have their own
 * categories, and as such they extend the base effect directly and don't have a base
 * of their own.
 * @category Miscellaneous Retrofit
 * @author lyravega
 */
public final class ehm_mr_overengineered extends _ehm_base implements normalEvents, enhancedEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		playDrillSound();
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

	public static final float ordnancePointBonus = 0.20f;
	public static final EnumMap<HullSize, Integer> slotPointBonus = new EnumMap<HullSize, Integer>(HullSize.class);
	static {
		slotPointBonus.put(HullSize.FIGHTER, 0);
		slotPointBonus.put(HullSize.DEFAULT, 0);
		slotPointBonus.put(HullSize.FRIGATE, 1);
		slotPointBonus.put(HullSize.DESTROYER, 2);
		slotPointBonus.put(HullSize.CRUISER, 3);
		slotPointBonus.put(HullSize.CAPITAL_SHIP, 5);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		if (!stats.getVariant().getSMods().contains(this.hullModSpecId)) return;

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setOrdnancePoints((int) Math.round(lyr_hullSpec.referenceNonDamaged().getOrdnancePoints(null)*(1+ordnancePointBonus)));
		// stats.getDynamic().getMod(ehm_internals.stats.slotPointsFromMods).modifyFlat(this.hullModSpecId, slotPointBonus.get(hullSize));	// done in pre-process
		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override public boolean hasSModEffect() { return true; }

	@Override public boolean hasSModEffectSection(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return true; }

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return Math.round(ordnancePointBonus*100)+"% OP";
			case 1: return slotPointBonus.get(hullSize) + " slot points";
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
			tooltip.addPara(this.hullModSpec.getSModDescription(hullSize).replaceAll("\\%", "%%"), text.padding, header.sEffect_textColour, this.getSModDescriptionParam(0, hullSize), this.getSModDescriptionParam(1, hullSize));
		}
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "story point";
			case 1: return Math.round(ordnancePointBonus*100)+"%";
			case 2: return slotPointBonus.get(HullSize.FRIGATE)+"/"+slotPointBonus.get(HullSize.DESTROYER)+"/"+slotPointBonus.get(HullSize.CRUISER)+"/"+slotPointBonus.get(HullSize.CAPITAL_SHIP)+" slot points";
			case 3: return "slot point";
			case 4: return "converter shunts";
			case 5: return "gained and utilized";
			case 6: return "deployment point";
			case 7: return ehm_settings.getBaseSlotPointPenalty()+"";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.hullmods.main.base)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.lacksBase, text.padding);
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

		return true;
	}
	//#endregion
}
