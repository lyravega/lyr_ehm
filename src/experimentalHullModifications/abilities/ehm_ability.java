package experimentalHullModifications.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.misc.lyr_internals;
import lyravega.tools.lyr_logger;

/**
 * A toggle ability that works in conjunction with {@link ehm_interactionListener interactionListener}
 * to determine whether to display the {@link experimentalHullModifications.submarkets.ehm_submarket shunt submarket} or not.
 * <p> Submarket will only be attached/detached if this ability is toggled to prevent clutter.
 * @author lyravega
 */
public class ehm_ability extends BaseToggleAbility implements lyr_logger {
	public static void attachListener() {	// used in plugin's onLoad()
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;

		if (!Global.getSector().getListenerManager().hasListenerOfClass(ehm_interactionListener.class)) {
			Global.getSector().getListenerManager().addListener(new ehm_interactionListener(), true);

			logger.info(logPrefix + "Attached colony interaction listener");
		}
	}

	/**
	 * An inner listener class whose sole purpose is to attach/detach the
	 * {@link experimentalHullModifications.submarkets.ehm_submarket experimental submarket}
	 */
	private static class ehm_interactionListener implements ColonyInteractionListener {
		@Override
		public void reportPlayerOpenedMarket(MarketAPI market) {
			if (market == null) return;
			if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;	// show submarket only if this ability is active
			if (market.hasSubmarket(lyr_internals.id.submarket)) return;

			market.addSubmarket(lyr_internals.id.submarket);

			logger.info(logPrefix + "Attached experimental submarket");
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

			logger.info(logPrefix + "Detached experimental submarket");
		}

		@Override
		public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}

		@Override
		public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {}
	}
	
	@Override
	protected String getActivationText() {
		return "Looking for a port";
	}
	
	@Override
	protected String getDeactivationText() {
		return null;
	}

	@Override
	protected void applyEffect(float amount, float level) {}

	@Override
	protected void activateImpl() {
		if (!Global.getSector().getListenerManager().hasListenerOfClass(ehm_interactionListener.class)) {
			Global.getSector().getListenerManager().addListener(new ehm_interactionListener(), true);

			logger.info(logPrefix + "Attached colony interaction listener");
		}
	}
	
	@Override
	protected void deactivateImpl() {
		if (Global.getSector().getListenerManager().hasListenerOfClass(ehm_interactionListener.class)) {
			Global.getSector().getListenerManager().removeListenerOfClass(ehm_interactionListener.class);

			logger.info(logPrefix + "Detached colony interaction listener");
		}
	}
	
	@Override
	protected void cleanupImpl() {}

	@Override
	public boolean showProgressIndicator() {
		return false;
	}
	
	@Override
	public boolean showActiveIndicator() {
		return isActive();
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color highlightColor = Misc.getHighlightColor();

		tooltip.addTitle(spec.getName(), highlightColor);
		tooltip.addPara("While this mode is turned on, a submarket called %s where slot shunts can be found will be visible on any port.", 10f,
        highlightColor,
        "Experimental Engineering");
	}

	public boolean hasTooltip() {
		return true;
	}

	@Override
	public Color getActiveColor() {
		return Global.getSector().getFaction(lyr_internals.id.faction).getBrightUIColor();
	}
}