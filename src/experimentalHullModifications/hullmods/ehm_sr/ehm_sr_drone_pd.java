package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 * @category System Retrofit
 * @author lyravega
 */
public final class ehm_sr_drone_pd extends _ehm_sr_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		stats.getSystemRegenBonus().modifyFlat(this.hullModSpecId, 0.1f);	// extra buff

		this.changeSystem(stats);
	}
}
