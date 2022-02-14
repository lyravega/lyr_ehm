package data.hullmods;

import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.loading.specs.g;

/**
 * This class is used by system retrofit hullmods. They 
 * are pretty straightforward in their operation; change
 * the system of a hullSpec.
 * Reason to split this as another base was primarily maintenance.
 * @see {@link _ehm_ar_base} for slot adapter base
 * @see {@link _ehm_wr_base} for weapon retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_sr_base extends _ehm_base_master {
	/**
	 * Alters the system on a hullSpec, and returns it.
	 * The returned hullSpec needs to be installed on the variant.
	 * @param variant of the ship that will have its system replaced
	 * @param systemId of the system to be installed on the passed variant
	 * @return a new hullSpec to be installed on the variant
	 */
	protected static final g ehm_systemRetrofit(ShipVariantAPI variant, String systemId) { 
		g hullSpec = (g) variant.getHullSpec();
		hullSpec.setShipSystemId(systemId);
		return hullSpec; 
	}

	public static final g ehm_systemRestore(ShipVariantAPI variant) {
		g hullSpec = (g) variant.getHullSpec();
		hullSpec.setShipSystemId(Global.getSettings().getHullSpec(variant.getHullSpec().getHullId()).getShipSystemId());
		return hullSpec; 
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String unapplicableReason(ShipAPI ship) {
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