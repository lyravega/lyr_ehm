package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import experimentalHullModifications.misc.ehm_internals.hullmods.engineCosmetics;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.listeners.events.customizableMod;
import lyravega.proxies.lyr_engineBuilder.engineStyleIds;
import lyravega.utilities.lyr_interfaceUtilities;

/**@category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_cec_blueEngines extends _ehm_ec_base implements customizableMod {
	@Override
	public void updateData() {
		this.engineStyleId = engineStyleIds.custom;
		this.engineStyleSpec = this.newCustomEngineStyleSpec();
		this.hullModSpec.setDisplayName(lyr_ehm.lunaSettings.getLunaName(engineCosmetics.blueEngines));

		lyr_interfaceUtilities.refreshPlayerFleetView(true);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeEngines(stats);
	}
}
