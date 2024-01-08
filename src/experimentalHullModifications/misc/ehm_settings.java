package experimentalHullModifications.misc;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;

import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.listeners.lyr_eventDispatcher;
import lyravega.utilities.lyr_interfaceUtilities;
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

	private String shuntAvailability; public String getShuntAvailability() { return this.shuntAvailability; }
	private boolean showInfoForActivators; public boolean getShowInfoForActivators() { return this.showInfoForActivators; }
	private boolean showFullInfoForActivators; public boolean getShowFullInfoForActivators() { return this.showFullInfoForActivators; }
	private boolean cosmeticsOnly; public boolean getCosmeticsOnly() { return this.cosmeticsOnly; }
	private boolean hideAdapters; public boolean getHideAdapters() { return this.hideAdapters; }
	private boolean hideConverters; public boolean getHideConverters() { return this.hideConverters; }
	private int baseSlotPointPenalty; public int getBaseSlotPointPenalty() { return this.baseSlotPointPenalty; }
	private boolean showExperimentalFlavour; public boolean getShowExperimentalFlavour() { return this.showExperimentalFlavour; }
	private boolean showFluff; public boolean getShowFluff() { return this.showFluff; }
	private boolean debugTooltip; public boolean getDebugTooltip() { return this.debugTooltip; }
	private boolean playDrillSound = true; public boolean getPlayDrillSound() { return this.playDrillSound; }
	private boolean playDrillSoundForAll = false; public boolean getPlayDrillSoundForAll() { return this.playDrillSoundForAll; }
	private boolean isMirrorFleetEnabled = false; public boolean isMirrorFleetEnabled() { return this.isMirrorFleetEnabled; }
	private boolean replaceMirrorFleet = false; public boolean replaceSimWithMirrorFleet() { return this.replaceMirrorFleet; }
	private boolean mirrorFleetCommander = false; public boolean assignMirrorFleetCommander() { return this.mirrorFleetCommander; }
	private boolean mirrorFleetCaptains = false; public boolean assignMirrorFleetCaptains() { return this.mirrorFleetCaptains; }
	private float mirrorFleetReadiness = 0; public float getMirrorFleetReadiness() { return this.mirrorFleetReadiness/100; }
	private boolean clearUnknownSlots; public boolean getClearUnknownSlots() { return this.clearUnknownSlots; }
	private int loggerLevel; public int getLogEventInfo() { return this.loggerLevel; }

	private void checkShuntAvailability() {
		final String temp = this.getString("ehm_shuntAvailability");

		if (this.shuntAvailability != null && this.shuntAvailability.equals(temp)) return; else this.shuntAvailability = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.attachShuntAccessListener();
	}

	private void checkLoggerLevel() {
		this.loggerLevel = this.getInt("ehm_loggerLevel");

		switch (this.loggerLevel) {
			case 5: lyr_logger.setLevel(lyr_levels.INFO); break;
			case 4: lyr_logger.setLevel(lyr_levels.LSTNR); break;
			case 3: lyr_logger.setLevel(lyr_levels.EVENT); break;
			case 2: lyr_logger.setLevel(lyr_levels.TRCKR); break;
			case 1: lyr_logger.setLevel(lyr_levels.RFLCT); break;
			case 0: lyr_logger.setLevel(lyr_levels.DEBUG); break;
			default: lyr_logger.setLevel(lyr_levels.LSTNR); break;
		}
	}

	private void checkCosmeticsOnly() {
		final boolean temp = this.getBoolean("ehm_cosmeticsOnly");

		if (this.cosmeticsOnly == temp) return; else this.cosmeticsOnly = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.updateBlueprints(); lyr_ehm.updateHullMods();
	}

	@Override
	protected void cacheSettings() {
		// MAIN SETTINGS
		this.checkShuntAvailability();	// separate from others as it needs to trigger a method to add/remove listeners only if there's a change
		String extraInfo = this.getString("ehm_extraInfoInHullMods");	// splitting radio into booleans
		this.showInfoForActivators = !"None".equals(extraInfo);
		this.showFullInfoForActivators = "Full".equals(extraInfo);
		String drillSound = this.getString("ehm_drillSound");	// splitting radio into booleans
		this.playDrillSound = !"None".equals(drillSound);
		this.playDrillSoundForAll = "All".equals(drillSound);
		this.checkCosmeticsOnly();	// separate from others like the shunt option as it invokes a method to properly update stuff
		this.hideAdapters = this.getBoolean("ehm_hideAdapters");
		this.hideConverters = this.getBoolean("ehm_hideConverters");

		// HULL MODIFICATION SETTINGS
		this.baseSlotPointPenalty = this.getInt("ehm_baseSlotPointPenalty");

		// FLAVOUR SETTINGS
		this.showExperimentalFlavour = this.getBoolean("ehm_showExperimentalFlavour");
		this.showFluff = this.getBoolean("ehm_showFluff");

		// ADVANCED SIMULATION SETTINGS
		String mirrorFleet = this.getString("ehm_mirrorFleet");	// splitting radio into booleans
		this.isMirrorFleetEnabled = !"Disabled".equals(mirrorFleet);
		this.replaceMirrorFleet = "Replace Roster".equals(mirrorFleet);
		this.mirrorFleetCommander = this.getBoolean("ehm_mirrorFleetCommander");
		this.mirrorFleetCaptains = this.getBoolean("ehm_mirrorFleetCaptains");
		this.mirrorFleetReadiness = this.getFloat("ehm_mirrorFleetReadiness");

		// DEBUG SETTINGS
		this.clearUnknownSlots = this.getBoolean("ehm_clearUnknownSlots");
		this.debugTooltip = this.getBoolean("ehm_debugTooltip");
		this.checkLoggerLevel();
	}

	@Override
	public void onSettingsChanged() {
		this.cacheSettings();	// order may be important; customizable hull modifications might require these to be cached first
		lyr_eventDispatcher.onSettingsChange(this.modId, null);
		lyr_interfaceUtilities.refreshFleetView(true);	// needed for the engine cosmetics

		lyr_logger.info("Settings reapplied");
	}
}