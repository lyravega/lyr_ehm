package experimentalHullModifications.misc;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;

import experimentalHullModifications.misc.ehm_internals.hullmods;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.listeners.lyr_eventDispatcher;
import lyravega.utilities.lyr_lunaUtilities;
import lyravega.utilities.logger.lyr_levels;
import lyravega.utilities.logger.lyr_logger;

/**
 * Luna settings listener to utilize anything LunaLib offers for settings
 * management. All settings are registered {@code onApplicationLoad()}.
 * @author lyravega
 */
public final class ehm_settings extends lyr_lunaUtilities {
	public ehm_settings() {
		super(ehm_internals.ids.mod);
	}

	public String getShuntAvailability() { return this.getString("ehm_shuntAvailability"); }
	public boolean getShowInfoForActivators() { return !"None".equals(this.getString("ehm_extraInfoInHullMods")); }
	public boolean getShowFullInfoForActivators() { return "Full".equals(this.getString("ehm_extraInfoInHullMods")); }
	public boolean getCosmeticsOnly() { return this.getBoolean("ehm_cosmeticsOnly"); }
	public boolean getHideAdapters() { return this.getBoolean("ehm_hideAdapters"); }
	public boolean getHideConverters() { return this.getBoolean("ehm_hideConverters"); }
	public int getBaseSlotPointPenalty() { return this.getInt("ehm_baseSlotPointPenalty"); }
	public boolean getShowExperimentalFlavour() { return this.getBoolean("ehm_showExperimentalFlavour"); }
	public boolean getShowFluff() { return this.getBoolean("ehm_showFluff"); }
	public boolean getDebugTooltip() { return this.getBoolean("ehm_debugTooltip"); }
	public boolean getPlayDrillSound() { return !"None".equals(this.getString("ehm_drillSound")); }
	public boolean getPlayDrillSoundForAll() { return "All".equals(this.getString("ehm_drillSound")); }
	public boolean isMirrorFleetEnabled() { return !"Disabled".equals(this.getString("ehm_mirrorFleet")); }
	public boolean replaceSimWithMirrorFleet() { return "Replace Roster".equals(this.getString("ehm_mirrorFleet")); }
	public boolean assignMirrorFleetCommander() { return this.getBoolean("ehm_mirrorFleetCommander"); }
	public boolean assignMirrorFleetCaptains() { return this.getBoolean("ehm_mirrorFleetCaptains"); }
	public float getMirrorFleetReadiness() { return this.getInt("ehm_mirrorFleetReadiness")/100; }
	public boolean getClearUnknownSlots() { return this.getBoolean("ehm_clearUnknownSlots"); }
	public int getLogEventInfo() { return this.getInt("ehm_loggerLevel"); }

	@Override
	protected void initializeSettings() {
		this.loggerLevelChanged();
	}

	@Override
	public void onSettingsChanged() {
		if (this.settingFields.get("ehm_cosmeticsOnly").isChanged()) this.cosmeticsOnlyChanged();
		if (this.settingFields.get("ehm_shuntAvailability").isChanged()) this.shuntAvailabilityChanged();
		if (this.settingFields.get("ehm_loggerLevel").isChanged()) this.loggerLevelChanged();

		if (this.settingGroups.get("ehm_cec_redEngines").isChanged(true)) lyr_eventDispatcher.onSettingsChange(this.modId, hullmods.engineCosmetics.redEngines);
		if (this.settingGroups.get("ehm_cec_greenEngines").isChanged(true)) lyr_eventDispatcher.onSettingsChange(this.modId, hullmods.engineCosmetics.greenEngines);
		if (this.settingGroups.get("ehm_cec_blueEngines").isChanged(true)) lyr_eventDispatcher.onSettingsChange(this.modId, hullmods.engineCosmetics.blueEngines);
		if (this.settingGroups.get("ehm_csc_redShields").isChanged(true)) lyr_eventDispatcher.onSettingsChange(this.modId, hullmods.shieldCosmetics.redShields);
		if (this.settingGroups.get("ehm_csc_greenShields").isChanged(true)) lyr_eventDispatcher.onSettingsChange(this.modId, hullmods.shieldCosmetics.greenShields);
		if (this.settingGroups.get("ehm_csc_blueShields").isChanged(true)) lyr_eventDispatcher.onSettingsChange(this.modId, hullmods.shieldCosmetics.blueShields);

		// lyr_eventDispatcher.onSettingsChange(this.modId, null);	// not necessary as individual ones are targetted

		lyr_logger.info("Settings reapplied");
	}

	private void cosmeticsOnlyChanged() {
		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.updateBlueprints(); lyr_ehm.updateHullMods();
	}

	private void shuntAvailabilityChanged() {
		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.attachShuntAccessListener();
	}

	private void loggerLevelChanged() {
		switch (this.getLogEventInfo()) {
			case 5: lyr_logger.setLevel(lyr_levels.INFO); break;
			case 4: lyr_logger.setLevel(lyr_levels.LSTNR); break;
			case 3: lyr_logger.setLevel(lyr_levels.EVENT); break;
			case 2: lyr_logger.setLevel(lyr_levels.TRCKR); break;
			case 1: lyr_logger.setLevel(lyr_levels.RFLCT); break;
			case 0: lyr_logger.setLevel(lyr_levels.DEBUG); break;
			default: lyr_logger.setLevel(lyr_levels.LSTNR); break;
		}
	}
}