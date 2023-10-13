package experimentalHullModifications.abilities.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.CoreUITabId;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.plugin.lyr_ehm.friend;
import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.listeners._lyr_sectorListener;
import lyravega.listeners._lyr_tabListener;
import lyravega.utilities.logger.lyr_logger;

/**
 * A sector listener class that adds/removes slot shunts to/from the player
 * inventory when the refit tab is opened/closed while the ability is toggled on
 * @author lyravega
 * @see {@link experimentalHullModifications.abilities.ehm_ability Control Ability}
 */
public final class ehm_shuntInjector extends _lyr_tabListener {
	private static _lyr_sectorListener instance = null;

	private ehm_shuntInjector() {
		super(CoreUITabId.REFIT);
	}

	public static _lyr_sectorListener get() {
		if (instance == null) instance = new ehm_shuntInjector();

		return instance;
	}

	public static void nullify(friend friend) {
		if (friend == null || instance == null) return;

		instance.detach(); instance = null;
	}

	@Override
	public void onOpen() {
		if (!Global.getSector().getPlayerFleet().getAbility(ehm_internals.id.ability).isActive()) return;

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		for (String shuntId : ehm_submarket.shunts) {
			playerCargo.addWeapons(shuntId, 1000);
		}

		lyr_logger.debug("Adding slot shunts to player cargo");
	}

	@Override
	public void onClose() {
		if (!Global.getSector().getPlayerFleet().getAbility(ehm_internals.id.ability).isActive()) return;

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		for (CargoItemQuantity<String> weaponCargo : playerCargo.getWeapons()) {
			if (ehm_submarket.shunts.contains(weaponCargo.getItem())) playerCargo.removeWeapons(weaponCargo.getItem(), weaponCargo.getCount());
		}

		lyr_logger.debug("Removing slot shunts from player cargo");
	}

	@Override public void onAdvance(float amount) {}
}
