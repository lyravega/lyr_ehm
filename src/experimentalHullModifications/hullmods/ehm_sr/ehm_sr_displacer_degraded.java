package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 * @category System Retrofit
 * @author lyravega
 */
public final class ehm_sr_displacer_degraded extends _ehm_sr_base {
	public ehm_sr_displacer_degraded() {
		super();

		this.systemId = "displacer_degraded";
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeSystem(stats);
	}
}
