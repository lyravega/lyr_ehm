package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import lyravega.proxies.lyr_engineBuilder.engineStyleIds;

/**@category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_ec_highTechEngines extends _ehm_ec_base {
	@Override	// not really customizable; does not implement the interface
	public void updateData() {
		this.engineStyleId = engineStyleIds.highTech;
		this.engineStyleSpec = null;
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeEngines(stats);
	}
}
