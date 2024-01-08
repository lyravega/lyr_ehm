package experimentalHullModifications.hullmods.ehm_sc;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.listeners.events.customizableMod;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_blueShields" for save compatibility
 * @category Custom Shield Cosmetic
 * @author lyravega
 */
public final class ehm_csc_blueShields extends _ehm_sc_base implements customizableMod {
	@Override
	public void updateData() {
		String id = this.getClass().getSimpleName();

		this.innerColour = lyr_ehm.lunaSettings.getLunaRGBAColour(id+"_inner");
		this.ringColour = lyr_ehm.lunaSettings.getLunaRGBAColour(id+"_ring");
		this.hullModSpec.setDisplayName(lyr_ehm.lunaSettings.getLunaName(id));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeShields(stats);
	}
}
