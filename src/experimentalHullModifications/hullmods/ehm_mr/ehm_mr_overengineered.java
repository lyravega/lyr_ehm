package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.ehm_settings;
import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;

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
	public void onInstall(ShipVariantAPI variant) {
		playDrillSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		playDrillSound();
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

	public static final float ordnancePointBonus = 0.20f;
	public static final Map<HullSize, Integer> slotPointBonus = new HashMap<HullSize, Integer>();
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
		if (!stats.getVariant().getHullSpec().getBuiltInMods().contains(ehm_internals.id.hullmods.base)) return;
		if (!stats.getVariant().getSMods().contains(this.hullModSpecId)) return;

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);

		lyr_hullSpec.setOrdnancePoints((int) Math.round(ehm_hullSpecReference(variant).getOrdnancePoints(null)*(1+ordnancePointBonus)));
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
		if (!isApplicableToShip(ship)) return;

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
			tooltip.addPara(this.hullModSpec.getSModDescription(hullSize).replaceAll("\\%", "%%"), text.padding, header.sEffect_textColour, getSModDescriptionParam(0, hullSize), getSModDescriptionParam(1, hullSize)); 
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

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
		} else if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.overEngineeredWarning[0], text.padding).setHighlight(text.overEngineeredWarning[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!lyr_miscUtilities.hasRetrofitBaseBuiltIn(ship)) return false; 

		return true; 
	}
	//#endregion
}
