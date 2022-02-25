package data.hullmods.ehm_sc;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods._ehm_base;
import lyr.lyr_hullSpec;
import lyr.lyr_shieldSpec;

/**
 * This class is used by shield cosmetic hullmods. 
 * </p> Reason to split this as another base was primarily maintenance.
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base} for engine cosmetic base
 * @author lyravega
 * @version 0.7
 * @since 0.7
 */
public class _ehm_sc_base extends _ehm_base {
	protected static final ShipHullSpecAPI ehm_pimpMyShield(ShipVariantAPI variant, Color inner, Color ring) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_shieldSpec shieldSpec = hullSpec.getShieldSpec();
		
		shieldSpec.setInnerColor(inner);
		shieldSpec.setRingColor(ring);

		return hullSpec.retrieve();
	}

	public static final ShipHullSpecAPI ehm_restoreShield(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		ShieldSpecAPI stockShieldSpec = ehm_hullSpecReference(variant).getShieldSpec();

		hullSpec.setShieldSpec(stockShieldSpec);
		
		return hullSpec.retrieve();
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.shieldCosmetic, hullModSpecId)) return ehm.excuses.hasShieldCosmetic;

		Set<String> hullModSpecTags = hullModSpec.getTags();
		if (hullModSpecTags.contains(ehm.tag.reqShields) && ship.getShield() == null) return ehm.excuses.noShields;

		return null; 
	}
	//#endregion
}