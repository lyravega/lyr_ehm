package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.proxies.lyr_engineBuilder.engineStyleIds;

/**
 * NOTE: id of this engine in the .csv remains "ehm_ec_torpedoEngines" for save compatibility
 * @category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_ec_crimsonEngines extends _ehm_ec_base {
	private final int engineStyleId = engineStyleIds.torpedo;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyEngineCosmetics(variant, this.engineStyleId));
	}
}
