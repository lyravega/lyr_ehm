package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.EnumMap;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_settings;
import lyravega.listeners.events.normalEvents;

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
	public void onInstalled(MutableShipStatsAPI stats) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		this.restoreHullSpec(stats.getVariant());

		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public static final EnumMap<HullSize, Integer> slotPointBonus = new EnumMap<HullSize, Integer>(HullSize.class);
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
		// stats.getDynamic().getMod(ehm_internals.stats.slotPointsFromMods).modifyFlat(this.hullModSpecId, slotPointBonus.get(hullSize));	// done in pre-process
	}

	//#region DESCRIPTION
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
	//#endregion
}
