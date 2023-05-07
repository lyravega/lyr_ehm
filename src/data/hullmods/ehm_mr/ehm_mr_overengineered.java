package data.hullmods.ehm_mr;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods.ehm._ehm_eventhandler;
import data.hullmods.ehm._ehm_eventmethod;
import lyr.misc.lyr_tooltip;
import lyr.proxies.lyr_hullSpec;

public class ehm_mr_overengineered extends _ehm_base implements _ehm_eventmethod {
	//#region LISTENER & EVENT REGISTRATION
	protected _ehm_eventhandler hullModEventHandler;

	@Override	// not used
	public void onInstall(ShipVariantAPI variant) {}

	@Override
	public void onRemove(ShipVariantAPI variant) {}

	@Override
	public void sModCleanUp(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
	}

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		if (this.hullModEventHandler == null) {
			this.hullModEventHandler = new _ehm_eventhandler(this.hullModSpecId, this);
			hullModEventHandler.registerOnInstall(false, true, false);
			hullModEventHandler.registerOnRemove(false, true, false);
			hullModEventHandler.registerSModCleanUp(true, false, true);
		}
	}
	//#endregion
	// END OF LISTENER & EVENT REGISTRATION

	public static final Map<HullSize, Integer> slotPointBonus = new HashMap<HullSize, Integer>();
	static {
		slotPointBonus.put(HullSize.FIGHTER, 0);
		slotPointBonus.put(HullSize.DEFAULT, 0);
		slotPointBonus.put(HullSize.FRIGATE, 0);
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
			case 1: return "" + 15 + "%";
			case 2: return "0/2/4/6 slot points";
			case 3: return "slot point";
			case 4: return "converter shunts";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(lyr_tooltip.header.notApplicable, lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(lyr_tooltip.text.lacksBase, lyr_tooltip.text.padding);
		} else if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
			tooltip.addSectionHeading(lyr_tooltip.header.severeWarning, lyr_tooltip.header.severeWarning_textColour, lyr_tooltip.header.severeWarning_bgColour, Alignment.MID, lyr_tooltip.header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(lyr_tooltip.text.overEngineeredWarning, lyr_tooltip.text.padding);
		}

		// super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "+" + 15 + "% OP";
			case 1: return "" + slotPointBonus.get(hullSize) + " slot points";
			default: return null;
		}
	}

	@Override
	public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {
		if (isApplicableToShip(ship)) {
			if (!ship.getVariant().getSMods().contains(this.hullModSpecId)) {
				tooltip.addSectionHeading(lyr_tooltip.header.noEffect, lyr_tooltip.header.noEffect_textColour, lyr_tooltip.header.noEffect_bgColour, Alignment.MID, lyr_tooltip.header.padding);
				tooltip.addPara(lyr_tooltip.text.overEngineeredNoEffect, lyr_tooltip.text.padding);
			} else {
				tooltip.addSectionHeading(lyr_tooltip.header.sEffect, lyr_tooltip.header.sEffect_textColour, lyr_tooltip.header.sEffect_bgColour, Alignment.MID, lyr_tooltip.header.padding);
				tooltip.addPara(this.hullModSpec.getSModDescription(hullSize).replaceAll("\\%", "%%"), lyr_tooltip.text.padding, lyr_tooltip.header.sEffect_textColour, getSModDescriptionParam(0, hullSize), getSModDescriptionParam(1, hullSize)); 
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
