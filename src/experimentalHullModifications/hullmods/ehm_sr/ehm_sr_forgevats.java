package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 * @category System Retrofit
 * @author lyravega
 */
public final class ehm_sr_forgevats extends _ehm_sr_base {
	public ehm_sr_forgevats() {
		super();

		this.systemId = "forgevats";
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		stats.getSystemCooldownBonus().modifyFlat(this.hullModSpecId, 45.0f);	// extra buff

		this.changeSystem(stats);
	}
}
