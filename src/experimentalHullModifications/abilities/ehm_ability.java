package experimentalHullModifications.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lyravega.listeners.lyr_colonyInteractionListener;
import lyravega.misc.lyr_internals;
import lyravega.tools.lyr_logger;

/**
 * A toggle ability that works in conjunction with {@link lyr_colonyInteractionListener interactionListener}
 * to determine whether to display the {@link experimentalHullModifications.submarkets.ehm_submarket shunt submarket} or not.
 * <p> Submarket will only be attached/detached if this ability is toggled to prevent clutter.
 * @author lyravega
 */
public class ehm_ability extends BaseToggleAbility implements lyr_logger {
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
		if (!Global.getSector().getListenerManager().hasListenerOfClass(lyr_colonyInteractionListener.class)) {
			Global.getSector().getListenerManager().addListener(new lyr_colonyInteractionListener(), true);

			if (listenerInfo) logger.info(logPrefix + "Attached colony interaction listener");
		}
	}
	
	@Override
	protected void deactivateImpl() {
		if (Global.getSector().getListenerManager().hasListenerOfClass(lyr_colonyInteractionListener.class)) {
			Global.getSector().getListenerManager().removeListenerOfClass(lyr_colonyInteractionListener.class);

			if (listenerInfo) logger.info(logPrefix + "Detached colony interaction listener");
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