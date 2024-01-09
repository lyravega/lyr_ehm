package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import lyravega.proxies.lyr_engineBuilder.engineStyleIds;
import lyravega.utilities.lyr_interfaceUtilities;

/**@category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_ec_midlineEngines extends _ehm_ec_base {
	@Override	// not really customizable; does not implement the interface
	public void updateData() {
		this.engineStyleId = engineStyleIds.midline;
		this.engineStyleSpec = null;

		lyr_interfaceUtilities.refreshPlayerFleetView(true);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeEngines(stats);
	}
}
