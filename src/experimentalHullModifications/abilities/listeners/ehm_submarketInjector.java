package experimentalHullModifications.abilities.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;

import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.listeners._lyr_sectorListener;
import lyravega.misc.lyr_internals;
import lyravega.plugin.lyr_ehm.friend;
import lyravega.tools.logger.lyr_logger;

/**
 * A sector listener class whose sole purpose is to attach/detach the
 * {@link experimentalHullModifications.submarkets.ehm_submarket experimental submarket}
 * when the player interacts with a (valid) market.
 */
public final class ehm_submarketInjector extends _lyr_sectorListener implements ColonyInteractionListener {
	private static _lyr_sectorListener instance = null;

	private ehm_submarketInjector() {}

	public static _lyr_sectorListener get() {
		if (instance == null) instance = new ehm_submarketInjector();

		return instance;
	}

	public static void nullify(friend friend) {
		if (friend == null || instance == null) return;

		instance.detach(); instance = null;
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		if (market == null) return;
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;	// show submarket only if this ability is active
		if (market.hasSubmarket(lyr_internals.id.submarket)) return;

		market.addSubmarket(lyr_internals.id.submarket);

		lyr_logger.listenerInfo("Attached experimental submarket");
	}

	@Override
	public void reportPlayerClosedMarket(MarketAPI market) {
		if (market == null) return;
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;
		if (!market.hasSubmarket(lyr_internals.id.submarket)) return;

		market.removeSubmarket(lyr_internals.id.submarket);

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoItemQuantity<String> weaponCargo : playerCargo.getWeapons()) {
			if (ehm_submarket.shunts.contains(weaponCargo.getItem())) playerCargo.removeWeapons(weaponCargo.getItem(), weaponCargo.getCount());
		}

		lyr_logger.listenerInfo("Detached experimental submarket");
	}

	@Override
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}

	@Override
	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {}
}