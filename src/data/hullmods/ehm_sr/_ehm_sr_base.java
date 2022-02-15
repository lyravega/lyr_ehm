package data.hullmods.ehm_sr;

import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.loading.specs.HullVariantSpec;
import com.fs.starfarer.loading.specs.g;

import data.hullmods._ehm_base;
import data.hullmods.ehm_ar._ehm_ar_base;
import data.hullmods.ehm_wr._ehm_wr_base;

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
public class _ehm_sr_base extends _ehm_base {
	/**
	 * Alters the system on a hullSpec, and returns it.
	 * The returned hullSpec needs to be installed on the variant.
	 * @param variant of the ship that will have its system replaced
	 * @param systemId of the system to be installed on the passed variant
	 * @return a new hullSpec to be installed on the variant
	 */
	protected static final g ehm_systemRetrofit(HullVariantSpec variant, String systemId) { 
		g hullSpec = variant.getHullSpec();
		hullSpec.setShipSystemId(systemId);
		return hullSpec; 
	}
	@Deprecated // without obfuscated stuff
	protected static final ShipHullSpecAPI ehm_systemRetrofit(ShipVariantAPI variantAPI, String systemId) { 
		HullVariantSpec tempVariant = new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(variantAPI).getHullSpec());
		tempVariant.getHullSpec().setShipSystemId(systemId);
		return tempVariant.getHullSpec(); 
	}
	
	public static final g ehm_systemRestore(HullVariantSpec variant) {
		g hullSpec = variant.getHullSpec();
		hullSpec.setShipSystemId(Global.getSettings().getHullSpec(variant.getHullSpec().getHullId()).getShipSystemId());
		return hullSpec; 
	}
	@Deprecated // without obfuscated stuff
	public static final ShipHullSpecAPI ehm_systemRestore(ShipVariantAPI variantAPI) { 
		HullVariantSpec tempVariant = new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(variantAPI).getHullSpec());
		tempVariant.getHullSpec().setShipSystemId(Global.getSettings().getHullSpec(variantAPI.getHullSpec().getHullId()).getShipSystemId());
		return tempVariant.getHullSpec(); 
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