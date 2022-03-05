package data.hullmods.ehm_sr;

import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods._ehm_base;
import lyr.proxies.lyr_hullSpec;

/**
 * This class is used by system retrofit hullmods. They are pretty 
 * straightforward in their operation; change the system of a hullSpec.
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_sr_base extends _ehm_base {
	/**
	 * Alters the system on a hullSpec, and returns it. The returned hullSpec needs 
	 * to be installed on the variant.
	 * @param variant of the ship that will have its system replaced
	 * @param systemId of the system to be installed on the passed variant
	 * @return a new hullSpec to be installed on the variant
	 * @see {@link #ehm_systemRestore()} reverses this process
	 */
	protected static final ShipHullSpecAPI ehm_systemRetrofit(ShipVariantAPI variant, String systemId) { 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false); 
		hullSpec.setShipSystemId(systemId); 
		return hullSpec.retrieve(); 
	}
	
	/**
	 * Restores a system of a hullSpec to its stock one, and returns it. Returned hullSpec 
	 * needs to be installed on the variant.
	 * @param variant that will have its system reset to factory defaults
	 * @return a hullspec to be installed on the variant
	 * @see {@link data.hullmods.ehm_base#onRemoved() onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_systemRestore(ShipVariantAPI variant) { 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		hullSpec.setShipSystemId(ehm_hullSpecReference(variant).getShipSystemId());
		return hullSpec.retrieve(); 
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.systemRetrofit, hullModSpecId)) return ehm.excuses.hasSystemRetrofit; 

		Set<String> hullModSpecTags = hullModSpec.getTags();
		if (hullModSpecTags.contains(ehm.tag.reqShields) && ship.getShield() == null) return ehm.excuses.noShields; 
		if (hullModSpecTags.contains(ehm.tag.reqNoPhase) && ship.getPhaseCloak() != null) return ehm.excuses.hasPhase; 
		if (hullModSpecTags.contains(ehm.tag.reqWings) && ship.getNumFighterBays() == 0) return ehm.excuses.noWings; 
		
		return null; 
	}
	//#endregion
}