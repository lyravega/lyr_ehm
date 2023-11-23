package experimentalHullModifications.misc;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;

import experimentalHullModifications.plugin.lyr_ehm;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
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
public final class ehm_settings implements LunaSettingsListener {
	private ehm_settings() {
		cacheSettings();
	}

	public static void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(ehm_settings.class)) {
			LunaSettings.addSettingsListener(new ehm_settings());

			lyr_logger.info("Attached LunaLib settings listener");
		}
	}

	protected static String shuntAvailability; public static String getShuntAvailability() { return shuntAvailability; }
	protected static boolean showInfoForActivators; public static boolean getShowInfoForActivators() { return showInfoForActivators; }
	protected static boolean showFullInfoForActivators; public static boolean getShowFullInfoForActivators() { return showFullInfoForActivators; }
	protected static boolean cosmeticsOnly; public static boolean getCosmeticsOnly() { return cosmeticsOnly; }
	protected static boolean hideAdapters; public static boolean getHideAdapters() { return hideAdapters; }
	protected static boolean hideConverters; public static boolean getHideConverters() { return hideConverters; }
	protected static int baseSlotPointPenalty; public static int getBaseSlotPointPenalty() { return baseSlotPointPenalty; }
	protected static boolean showExperimentalFlavour; public static boolean getShowExperimentalFlavour() { return showExperimentalFlavour; }
	protected static boolean showFluff; public static boolean getShowFluff() { return showFluff; }
	protected static boolean debugTooltip; public static boolean getDebugTooltip() { return debugTooltip; }
	protected static boolean playDrillSound = true; public static boolean getPlayDrillSound() { return playDrillSound; }
	protected static boolean playDrillSoundForAll = false; public static boolean getPlayDrillSoundForAll() { return playDrillSoundForAll; }
	protected static boolean clearUnknownSlots; public static boolean getClearUnknownSlots() { return clearUnknownSlots; }
	protected static int loggerLevel; public static int getLogEventInfo() { return loggerLevel; }

	private static void cacheSettings() {
		// MAIN SETTINGS
		checkShuntAvailability();	// separate from others as it needs to trigger a method to add/remove listeners only if there's a change
		String extraInfo = lyr_lunaUtilities.getString(ehm_internals.ids.mod, "ehm_extraInfoInHullMods");	// splitting radio into booleans
		showInfoForActivators = !"None".equals(extraInfo);
		showFullInfoForActivators = "Full".equals(extraInfo);
		String drillSound = lyr_lunaUtilities.getString(ehm_internals.ids.mod, "ehm_drillSound");	// splitting radio into booleans
		playDrillSound = !"None".equals(drillSound);
		playDrillSoundForAll = "All".equals(drillSound);
		checkCosmeticsOnly();	// separate from others like the shunt option as it invokes a method to properly update stuff
		hideAdapters = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_hideAdapters");
		hideConverters = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_hideConverters");

		// HULL MODIFICATION SETTINGS
		baseSlotPointPenalty = lyr_lunaUtilities.getInt(ehm_internals.ids.mod, "ehm_baseSlotPointPenalty");

		// FLAVOUR SETTINGS
		showExperimentalFlavour = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_showExperimentalFlavour");
		showFluff = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_showFluff");

		// DEBUG SETTINGS
		clearUnknownSlots = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_clearUnknownSlots");
		debugTooltip = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_debugTooltip");
		checkLoggerLevel();
	}

	private static void checkShuntAvailability() {
		final String temp = lyr_lunaUtilities.getString(ehm_internals.ids.mod, "ehm_shuntAvailability");

		if (shuntAvailability != null && shuntAvailability.equals(temp)) return; else shuntAvailability = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.attachShuntAccessListener();
	}

	protected static void checkLoggerLevel() {
		loggerLevel = lyr_lunaUtilities.getInt(ehm_internals.ids.mod, "ehm_loggerLevel");

		switch (loggerLevel) {
			case 5: lyr_logger.setLevel(lyr_levels.INFO); break;
			case 4: lyr_logger.setLevel(lyr_levels.LSTNR); break;
			case 3: lyr_logger.setLevel(lyr_levels.EVENT); break;
			case 2: lyr_logger.setLevel(lyr_levels.TRCKR); break;
			case 1: lyr_logger.setLevel(lyr_levels.RFLCT); break;
			case 0: lyr_logger.setLevel(lyr_levels.DEBUG); break;
			default: lyr_logger.setLevel(lyr_levels.LSTNR); break;
		}
	}

	private static void checkCosmeticsOnly() {
		final boolean temp = lyr_lunaUtilities.getBoolean(ehm_internals.ids.mod, "ehm_cosmeticsOnly");

		if (cosmeticsOnly == temp) return; else cosmeticsOnly = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.updateBlueprints(); lyr_ehm.updateHullMods();
	}

	@Override
	public void settingsChanged(String modId) {
		if (!ehm_internals.ids.mod.equals(modId)) return;

		cacheSettings();	// order may be important; customizable hull modifications might require these to be cached first
		lyr_eventDispatcher.onSettingsChange(modId, null);
		lyr_interfaceUtilities.refreshFleetView(true);

		lyr_logger.info("Settings reapplied");
	}
}