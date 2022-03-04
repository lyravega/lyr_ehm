package data.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods._ehm_base;
import lyr.lyr_engineBuilder;
import lyr.lyr_hullSpec;

/**
 * This class is used by engine cosmetic hullmods. The changes are 
 * permanent, and does not use {@code advanceInCombat()}.
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_ec_base extends _ehm_base {
	/**
	 * Alters the engine visuals of the ship. Uses the vanilla engine styles
	 * (as I haven't found a way to alter engine colours directly)
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum somewhat hardcoded {@link lyr.lyr_engineBuilder.engineStyle engineStyle}
	 * @return a hullSpec with the altered engine visuals
	 */
	protected static final ShipHullSpecAPI ehm_pimpMyEngineSlots(ShipVariantAPI variant, int styleEnum) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyle(styleEnum);
		}

		return hullSpec.retrieve();
	}

	/**
	 * Restores the engine visuals of the ship by applying a stock hullSpec
	 * on the variant.
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum somewhat hardcoded {@link lyr.lyr_engineBuilder.engineStyle engineStyle}
	 * @return a hullSpec with restored engine visuals
	 * @see {@link data.hullmods.ehm_base#onRemoved() onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_restoreEngineSlots(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		variant.setHullSpecAPI(hullSpec);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.engineCosmetic, hullModSpecId)) return ehm.excuses.hasEngineCosmetic;

		return null; 
	}
	//#endregion
}