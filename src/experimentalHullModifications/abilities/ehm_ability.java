package experimentalHullModifications.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lyravega.listeners.lyr_colonyInteractionListener;
import lyravega.listeners.lyr_lunaSettingsListener;
import lyravega.listeners.lyr_tabListener;
import lyravega.misc.lyr_internals;
import lyravega.tools.lyr_logger;

/**
 * A toggle ability that works in conjunction with {@link lyr_colonyInteractionListener
 * interactionListener} or with {@link lyr_tabListener tabListener} to determine how to
 * display the slot shunts. The option {@link lyr_lunaSettingsListener#shuntAvailability
 * shuntAvailability} controls the display mode.
 * <p> {@link experimentalHullModifications.submarkets.ehm_submarket Slot shunt submarket}
 * will only be displayed if relevant option is selected. Both settings' listeners and
 * the submarket are all transient.
 * @author lyravega
 */
public class ehm_ability extends BaseToggleAbility implements lyr_logger {
	@Override
	protected String getActivationText() {
		switch (lyr_lunaSettingsListener.shuntAvailability) {
			case "Always": return "Ready to experiment";
			case "Submarket": return "Looking for a port";
			default: return null;
		}
	}
	
	@Override
	protected String getDeactivationText() {
		return null;
	}

	@Override
	protected void applyEffect(float amount, float level) {}

	@Override
	protected void activateImpl() {
		switch (lyr_lunaSettingsListener.shuntAvailability) {
			case "Always": lyr_tabListener.attach(true); break;
			case "Submarket": lyr_colonyInteractionListener.attach(true); break;
			default: break;
		}
	}
	
	@Override
	protected void deactivateImpl() {
		switch (lyr_lunaSettingsListener.shuntAvailability) {
			case "Always": lyr_tabListener.detach(); break;
			case "Submarket": lyr_colonyInteractionListener.detach(); break;
			default: break;
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
		String desc;

		switch (lyr_lunaSettingsListener.shuntAvailability) {
			case "Always": desc = "While this ability is turned on, an excess amount of slot shunts will be made available in the refit tab. Unused ones will be cleaned-up."; break;
			case "Submarket": desc = "While this ability is turned on, a submarket called Experimental Engineering will be visible on any docked port, and slot shunts will be available in the refit tab. Unused ones will be cleaned-up."; break;
			default: desc = ""; break;
		}

		tooltip.addTitle(spec.getName(), highlightColor);
		tooltip.addPara(desc, 10f);
	}

	public boolean hasTooltip() {
		return true;
	}

	@Override
	public Color getActiveColor() {
		return Global.getSector().getFaction(lyr_internals.id.faction).getBrightUIColor();
	}
}