package experimentalHullModifications.plugin;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.id;
import experimentalHullModifications.misc.ehm_internals.tag;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.listeners.lyr_eventDispatcher;
import lyravega.listeners.events.customizableMod;
import lyravega.utilities.lyr_lunaUtilities;
import lyravega.utilities.logger.lyr_levels;
import lyravega.utilities.logger.lyr_logger;

/**
 * Luna settings listener to utilize anything LunaLib offers for settings
 * management. All settings are registered {@code onApplicationLoad()}.
 * <p> Any hull modification bearing the tag {@link tag#customizable} that
 * also implements the {@link customizableMod} interface are registered
 * as such, and any changes will be applied on them without a reload or a
 * restart.
 * @author lyravega
 */
public class ehm_settings implements LunaSettingsListener {
	private ehm_settings() {
		cacheSettings();
	}

	static void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(ehm_settings.class)) {
			LunaSettings.addSettingsListener(new ehm_settings());

			lyr_logger.info("Attached LunaLib settings listener");
		}
	}

	private static String shuntAvailability; public static String getShuntAvailability() { return shuntAvailability; }
	// private static String extraInfoInHullMods; public static String getExtraInfoInHullMods() { return extraInfoInHullMods; }
	private static boolean showInfoForActivators; public static boolean getShowInfoForActivators() { return showInfoForActivators; }
	private static boolean showFullInfoForActivators; public static boolean getShowFullInfoForActivators() { return showFullInfoForActivators; }
	// private static String drillSound; public static String getDrillSound() { return drillSound; }
	private static boolean playDrillSound; public static boolean getPlayDrillSound() { return playDrillSound; }
	private static boolean playDrillSoundForAll; public static boolean getPlayDrillSoundForAll() { return playDrillSoundForAll; }
	private static boolean cosmeticsOnly; public static boolean getCosmeticsOnly() { return cosmeticsOnly; }
	private static boolean hideAdapters; public static boolean getHideAdapters() { return hideAdapters; }
	private static boolean hideConverters; public static boolean getHideConverters() { return hideConverters; }
	private static int baseSlotPointPenalty; public static int getBaseSlotPointPenalty() { return baseSlotPointPenalty; }
	private static boolean showExperimentalFlavour; public static boolean getShowExperimentalFlavour() { return showExperimentalFlavour; }
	private static boolean showFluff; public static boolean getShowFluff() { return showFluff; }
	private static boolean debugTooltip; public static boolean getDebugTooltip() { return debugTooltip; }
//	private static int loggerLevel; public static int getLogEventInfo() { return loggerLevel; }

	private static void cacheSettings() {
		// MAIN SETTINGS
		checkShuntAvailability();	// separate from others as it needs to trigger a method to add/remove listeners only if there's a change
		String extraInfo = lyr_lunaUtilities.getString("ehm_extraInfoInHullMods");	// splitting radio into booleans
		showInfoForActivators = !extraInfo.equals("None");
		showFullInfoForActivators = extraInfo.equals("Full");
		String drillSound = lyr_lunaUtilities.getString("ehm_drillSound");	// splitting radio into booleans
		playDrillSound = !drillSound.equals("None");
		playDrillSoundForAll = drillSound.equals("All");
		checkCosmeticsOnly();	// separate from others like the shunt option as it invokes a method to properly update stuff
		hideAdapters = lyr_lunaUtilities.getBoolean("ehm_hideAdapters");
		hideConverters = lyr_lunaUtilities.getBoolean("ehm_hideConverters");

		// HULL MODIFICATION SETTINGS
		baseSlotPointPenalty = lyr_lunaUtilities.getInt("ehm_baseSlotPointPenalty");

		// FLAVOUR SETTINGS
		showExperimentalFlavour = lyr_lunaUtilities.getBoolean("ehm_showExperimentalFlavour");
		showFluff = lyr_lunaUtilities.getBoolean("ehm_showFluff");

		// DEBUG SETTINGS
		debugTooltip = lyr_lunaUtilities.getBoolean("ehm_debugTooltip");
		checkLoggerLevel();
	}

	private static void checkShuntAvailability() {
		final String temp = lyr_lunaUtilities.getString("ehm_shuntAvailability");

		if (shuntAvailability != null && shuntAvailability.equals(temp)) return; else shuntAvailability = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.attachShuntAccessListener();
	}

	private static void checkLoggerLevel() {
		switch (lyr_lunaUtilities.getInt("ehm_loggerLevel")) {
			case 4: lyr_logger.setLevel(lyr_levels.INFO); break;
			case 3: lyr_logger.setLevel(lyr_levels.LSTNR); break;
			case 2: lyr_logger.setLevel(lyr_levels.EVENT); break;
			case 1: lyr_logger.setLevel(lyr_levels.RFLCT); break;
			case 0: lyr_logger.setLevel(lyr_levels.DEBUG); break;
			default: lyr_logger.setLevel(lyr_levels.LSTNR); break;
		}
	}

	private static void checkCosmeticsOnly() {
		final boolean temp = lyr_lunaUtilities.getBoolean("ehm_cosmeticsOnly");

		if (cosmeticsOnly == temp) return; else cosmeticsOnly = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;

		lyr_ehm.updateBlueprints(); lyr_ehm.updateHullMods();
	}

	@Override
	public void settingsChanged(String modId) {
		if (!modId.equals(id.mod)) return;

		cacheSettings();	// order may be important; customizable hull modifications might require these to be cached first
		lyr_eventDispatcher.onSettingsChange(ehm_internals.id.mod, null);

		lyr_logger.info("Settings reapplied");
	}
}