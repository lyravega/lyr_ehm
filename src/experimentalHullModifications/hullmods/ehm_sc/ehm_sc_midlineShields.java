package experimentalHullModifications.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_purpleShields" for save compatibility
 * @category Shield Cosmetic
 * @author lyravega
 */
public final class ehm_sc_midlineShields extends _ehm_sc_base {
	@Override	// not really customizable; does not implement the interface
	public void updateData() {
		this.innerColour = new Color(255,185,75,75);
		this.ringColour = new Color(255,255,255,255);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeShields(stats);
	}
}
