package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import lyravega.proxies.lyr_engineBuilder.engineStyle;

import com.fs.starfarer.api.combat.ShipVariantAPI;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public class ehm_ec_midlineEngines extends _ehm_ec_base {
	private static final int style = engineStyle.midline;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		
		variant.setHullSpecAPI(ehm_pimpMyEngineSlots(variant, style));
	}
}
