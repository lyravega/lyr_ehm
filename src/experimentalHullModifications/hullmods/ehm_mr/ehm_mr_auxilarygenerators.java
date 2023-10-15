package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
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
import lyravega.listeners.events.normalEvents;
import lyravega.utilities.lyr_miscUtilities;

/**
 * A hullmod to convert OP to SP
 * <p> This category {@code ehm_mr} covers the odd ones since the evens have their own
 * categories, and as such they extend the base effect directly and don't have a base
 * of their own.
 * @category Miscellaneous Retrofit
 * @author lyravega
 */
public final class ehm_mr_auxilarygenerators extends _ehm_base implements normalEvents {
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
	//#endregion
	// END OF CUSTOM EVENTS

	public static final Map<HullSize, Integer> slotPointBonus = new HashMap<HullSize, Integer>();
	static {
		slotPointBonus.put(HullSize.FIGHTER, 0);
		slotPointBonus.put(HullSize.DEFAULT, 0);
		slotPointBonus.put(HullSize.FRIGATE, 1);
		slotPointBonus.put(HullSize.DESTROYER, 1);
		slotPointBonus.put(HullSize.CRUISER, 2);
		slotPointBonus.put(HullSize.CAPITAL_SHIP, 2);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD / DATA CLASS, ACTIONS ARE HANDLED THROUGH ACTIVATOR
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "10/10/20/20";
			case 1: return slotPointBonus.get(HullSize.FRIGATE)+"/"+slotPointBonus.get(HullSize.DESTROYER)+"/"+slotPointBonus.get(HullSize.CRUISER)+"/"+slotPointBonus.get(HullSize.CAPITAL_SHIP);
			case 2: return "gained and utilized";
			case 3: return "deployment point";
			case 4: return ehm_settings.getBaseSlotPointPenalty()+"";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (!ship.getVariant().hasHullMod(ehm_internals.id.hullmods.diverterandconverter)) tooltip.addPara(text.lacksActivator[0], text.padding).setHighlight(text.lacksActivator[1]);
		}

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(header.lockedIn, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);

			if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affix.convertedSlot)) tooltip.addPara(text.hasWeaponsOnConvertedSlots[0], text.padding).setHighlight(text.hasWeaponsOnConvertedSlots[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		ShipVariantAPI variant = ship.getVariant();

		if (!lyr_miscUtilities.hasRetrofitBaseBuiltIn(ship)) return false;
		if (!variant.hasHullMod(ehm_internals.id.hullmods.diverterandconverter)) return false;

		return true;
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (ship.getVariant().hasHullMod(this.hullModSpecId) && lyr_miscUtilities.hasWeapons(ship, ehm_internals.affix.convertedSlot)) return false;	// initial check allows installation, but blocks removal

		return true;
	}
	//#endregion
}
