package experimentalHullModifications.abilities.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.plugin.lyr_ehm.friend;
import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.listeners._lyr_sectorListener;
import lyravega.utilities.logger.lyr_logger;

/**
 * A sector listener class whose sole purpose is to attach/detach a submarket
 * when the player interacts with a (valid) market while the ability is toggled on
 * @author lyravega
 * @see {@link experimentalHullModifications.abilities.ehm_ability Control Ability}
 * @see {@link experimentalHullModifications.submarkets.ehm_submarket Experimental Submarket}
 */
public final class ehm_submarketInjector extends _lyr_sectorListener implements ColonyInteractionListener {
	private static _lyr_sectorListener instance = null;

	private ehm_submarketInjector() {}

	public static void attach() {
		if (instance == null) instance = new ehm_submarketInjector();

		instance.attachListener(true);
	}

	public static void detach() {
		if (instance == null) return;

		instance.detachListener();
	}

	public static void nullify(friend friend) {
		if (friend == null || instance == null) return;

		instance.detachListener(); instance = null;
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		if (market == null) return;
		if (!Global.getSector().getPlayerFleet().getAbility(ehm_internals.id.ability).isActive()) return;	// show submarket only if this ability is active
		if (market.hasSubmarket(ehm_internals.id.submarket)) return;

		market.addSubmarket(ehm_internals.id.submarket);

		lyr_logger.debug("Attached experimental submarket");
	}

	@Override
	public void reportPlayerClosedMarket(MarketAPI market) {
		if (market == null) return;
		if (!Global.getSector().getPlayerFleet().getAbility(ehm_internals.id.ability).isActive()) return;
		if (!market.hasSubmarket(ehm_internals.id.submarket)) return;

		market.removeSubmarket(ehm_internals.id.submarket);

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
		for (CargoItemQuantity<String> weaponCargo : playerCargo.getWeapons()) {
			if (ehm_submarket.shunts.contains(weaponCargo.getItem())) playerCargo.removeWeapons(weaponCargo.getItem(), weaponCargo.getCount());
		}

		lyr_logger.debug("Detached experimental submarket");
	}

	@Override
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}

	@Override
	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {}
}