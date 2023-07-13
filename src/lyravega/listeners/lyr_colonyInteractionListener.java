package lyravega.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;

import experimentalHullModifications.abilities.ehm_ability;
import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.misc.lyr_internals;
import lyravega.tools.lyr_logger;

/**
 * An listener class whose sole purpose is to attach/detach the
 * {@link experimentalHullModifications.submarkets.ehm_submarket experimental submarket}
 */
public class lyr_colonyInteractionListener implements ColonyInteractionListener, lyr_logger {
	public static void attach(boolean isTransient) {	// used in plugin's onLoad()
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;
	
		if (!Global.getSector().getListenerManager().hasListenerOfClass(lyr_colonyInteractionListener.class)) {
			Global.getSector().getListenerManager().addListener(new lyr_colonyInteractionListener(), isTransient);
	
			if (listenerInfo) logger.info(logPrefix + "Attached colony interaction listener");
		}
	}

	public static void detach() {
		if (Global.getSector().getListenerManager().hasListenerOfClass(lyr_colonyInteractionListener.class)) {
			Global.getSector().getListenerManager().removeListenerOfClass(lyr_colonyInteractionListener.class);

			if (listenerInfo) logger.info(logPrefix + "Detached colony interaction listener");
		}
	}

	@Override
	public void reportPlayerOpenedMarket(MarketAPI market) {
		if (market == null) return;
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;	// show submarket only if this ability is active
		if (market.hasSubmarket(lyr_internals.id.submarket)) return;

		market.addSubmarket(lyr_internals.id.submarket);

		if (ehm_ability.listenerInfo) ehm_ability.logger.info(ehm_ability.logPrefix + "Attached experimental submarket");
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

		if (ehm_ability.listenerInfo) ehm_ability.logger.info(ehm_ability.logPrefix + "Detached experimental submarket");
	}

	@Override
	public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}

	@Override
	public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {}
}