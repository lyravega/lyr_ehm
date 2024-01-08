package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 * @category System Retrofit
 * @author lyravega
 */
public final class ehm_sr_drone_pd_x2 extends _ehm_sr_base {
	public ehm_sr_drone_pd_x2() {
		super();

		this.systemId = "drone_pd_x2";
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		stats.getSystemRegenBonus().modifyFlat(this.hullModSpecId, 0.1f);	// extra buff

		this.changeSystem(stats);
	}
}
