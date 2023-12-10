package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import lyravega.proxies.lyr_engineBuilder.engineStyleIds;

/**
 * NOTE: id of this engine in the .csv remains "ehm_ec_torpedoEngines" for save compatibility
 * @category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_ec_crimsonEngines extends _ehm_ec_base {
	@Override	// not really customizable; does not implement the interface
	public void updateData() {
		this.engineStyleId = engineStyleIds.torpedo;
		this.engineStyleSpec = null;
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeEngines(stats);
	}
}
