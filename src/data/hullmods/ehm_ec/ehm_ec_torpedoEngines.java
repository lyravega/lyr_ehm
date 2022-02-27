package data.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyr.lyr_engineBuilder.engineStyle;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public class ehm_ec_torpedoEngines extends _ehm_ec_base {
	private static final int style = engineStyle.torpedo;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		
		variant.setHullSpecAPI(ehm_pimpMyEngineSlots(variant, style));
	}
}
