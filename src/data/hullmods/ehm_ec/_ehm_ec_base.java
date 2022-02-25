package data.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods._ehm_base;
import lyr.lyr_engineBuilder;
import lyr.lyr_hullSpec;

/**
 * This class is used by weapon retrofit hullmods. They are pretty 
 * straightforward in their operation; change all of the weapon slots 
 * on a ship to a different type. 
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base} for shield cosmetic base
 * @author lyravega
 * @version 0.7
 * @since 0.3
 */
public class _ehm_ec_base extends _ehm_base {
	protected static final ShipHullSpecAPI ehm_pimpMyEngineSlots(ShipVariantAPI variant, int styleEnum) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyle(styleEnum);
		}

		return hullSpec.retrieve();
	}

	public static final ShipHullSpecAPI ehm_restoreEngineSlots(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		variant.setHullSpecAPI(hullSpec);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.engineCosmetic, hullModSpecId)) return ehm.excuses.hasEngineCosmetic;

		return null; 
	}
	//#endregion
}