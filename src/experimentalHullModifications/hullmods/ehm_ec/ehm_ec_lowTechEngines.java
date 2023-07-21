package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.engineStyleIds.lowTech;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public final class ehm_ec_lowTechEngines extends _ehm_ec_base {
	private static final int engineStyleId = lowTech;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		
		variant.setHullSpecAPI(ehm_applyEngineCosmetics(variant, engineStyleId));
	}
}
