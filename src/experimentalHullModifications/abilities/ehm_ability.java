package experimentalHullModifications.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.abilities.listeners.ehm_shuntInjector;
import experimentalHullModifications.abilities.listeners.ehm_submarketInjector;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.plugin.lyr_ehm;

/**
 * A toggle ability that works in conjunction with {@link ehm_submarketInjector
 * interactionListener} or with {@link ehm_shuntInjector tabListener} to determine how to
 * display the slot shunts. The option {@link ehm_settings#shuntAvailability
 * shuntAvailability} controls the display mode.
 * <p> {@link experimentalHullModifications.submarkets.ehm_submarket Slot shunt submarket}
 * will only be displayed if relevant option is selected. Both settings' listeners and
 * the submarket are all transient.
 * @author lyravega
 */
public final class ehm_ability extends BaseToggleAbility {
	@Override
	protected String getActivationText() {
		switch (lyr_ehm.lunaSettings.getShuntAvailability()) {
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
		switch (lyr_ehm.lunaSettings.getShuntAvailability()) {
			case "Always": ehm_shuntInjector.attach(); break;
			case "Submarket": ehm_submarketInjector.attach(); break;
			default: break;
		}
	}

	@Override
	protected void deactivateImpl() {
		switch (lyr_ehm.lunaSettings.getShuntAvailability()) {
			case "Always": ehm_shuntInjector.detach(); break;
			case "Submarket": ehm_submarketInjector.detach(); break;
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
		return this.isActive();
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color highlightColor = Misc.getHighlightColor();
		String desc;

		switch (lyr_ehm.lunaSettings.getShuntAvailability()) {
			case "Always": desc = "While this ability is turned on, an excess amount of slot shunts will be made available in the refit tab. Unused ones will be cleaned-up."; break;
			case "Submarket": desc = "While this ability is turned on, a submarket called Experimental Engineering will be visible on any docked port, and slot shunts will be available in the refit tab. Unused ones will be cleaned-up."; break;
			default: desc = ""; break;
		}

		tooltip.addTitle(this.spec.getName(), highlightColor);
		tooltip.addPara(desc, 10f);
	}

	@Override
	public boolean hasTooltip() {
		return true;
	}

	@Override
	public Color getActiveColor() {
		return Global.getSector().getFaction(ehm_internals.ids.faction).getBrightUIColor();
	}
}