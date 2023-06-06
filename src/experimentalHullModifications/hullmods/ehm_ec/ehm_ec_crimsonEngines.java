package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import lyravega.proxies.lyr_engineBuilder.engineStyle;

import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * NOTE: id of this engine in the .csv remains "ehm_ec_torpedoEngines" for save compatibility
 * @category Engine Cosmetic 
 * @author lyravega
 */
public class ehm_ec_crimsonEngines extends _ehm_ec_base {
	private static final int style = engineStyle.torpedo;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		
		variant.setHullSpecAPI(ehm_pimpMyEngineSlots(variant, style));
	}
}