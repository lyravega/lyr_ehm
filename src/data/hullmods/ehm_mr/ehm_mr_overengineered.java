package data.hullmods.ehm_mr;

import static lyravega.tools._lyr_uiTools.commitChanges;
import static lyravega.tools._lyr_uiTools.playSound;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods.ehm.events.enhancedEvents;
import data.hullmods.ehm.events.normalEvents;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_hullSpec;

/**
 * First experimental hull modification that can be built-in, with an original effect.
 * Increases the OP limit of the ship and also yields slot points.
 * <p> This category {@code ehm_mr} covers the odd ones since the evens have their own
 * categories, and as such they extend the base effect directly and don't have a base
 * of their own.
 * @category Miscellaneous Retrofit 
 * @author lyravega
 */
public class ehm_mr_overengineered extends _ehm_base implements normalEvents, enhancedEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		playSound();
	}

	@Override
	public void onEnhance(ShipVariantAPI variant) {}

	@Override
	public void onNormalize(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
		commitChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public static final Map<HullSize, Integer> slotPointBonus = new HashMap<HullSize, Integer>();
	static {
		slotPointBonus.put(HullSize.FIGHTER, 0);
		slotPointBonus.put(HullSize.DEFAULT, 0);
		slotPointBonus.put(HullSize.FRIGATE, 1);
		slotPointBonus.put(HullSize.DESTROYER, 2);
		slotPointBonus.put(HullSize.CRUISER, 4);
		slotPointBonus.put(HullSize.CAPITAL_SHIP, 6);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (variant.getSMods().contains(this.hullModSpecId)) {
			lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
			lyr_hullSpec.setOrdnancePoints((int) Math.round(ehm_hullSpecReference(variant).getOrdnancePoints(null)*1.2));
			variant.setHullSpecAPI(lyr_hullSpec.retrieve());
		}
	}

	@Override
	public boolean affectsOPCosts() {
		return false;
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public boolean hasSModEffect() {
		return true;
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "story point";
			case 1: return "" + 20 + "%";
			case 2: return "1/2/4/6 slot points";
			case 3: return "slot point";
			case 4: return "converter shunts";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
		} else if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.overEngineeredWarning[0], text.padding).setHighlight(text.overEngineeredWarning[1]);
		}

		// super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "+" + 20 + "% OP";
			case 1: return "" + slotPointBonus.get(hullSize) + " slot points";
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
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) return false; 

		return true; 
	}
	//#endregion
}
