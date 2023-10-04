package experimentalHullModifications.abilities.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.CoreUITabId;

import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.listeners._lyr_sectorListener;
import lyravega.listeners._lyr_tabListener;
import lyravega.misc.lyr_internals;

/**
 * A sector listener class that adds/removes slot shunts to/from the player
 * inventory when the refit tab is opened/closed.
 * @author lyravega
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

	@Override
	public void onOpen() {
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		for (String shuntId : ehm_submarket.shunts) {
			playerCargo.addWeapons(shuntId, 1000);
		}
	}

	@Override
	public void onClose() {
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		for (CargoItemQuantity<String> weaponCargo : playerCargo.getWeapons()) {
			if (ehm_submarket.shunts.contains(weaponCargo.getItem())) playerCargo.removeWeapons(weaponCargo.getItem(), weaponCargo.getCount());
		}
	}

	@Override public void onAdvance(float amount) {}
}
