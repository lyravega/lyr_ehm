package lyravega.plugin;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.listeners.events.customizableHullMod;
import lyravega.misc.lyr_internals.id;
import lyravega.misc.lyr_internals.tag;
import lyravega.misc.lyr_lunaAccessors;
import lyravega.tools.logger.lyr_levels;
import lyravega.tools.logger.lyr_logger;

/**
 * Luna settings listener to utilize anything LunaLib offers for settings
 * management. All settings are registered {@code onApplicationLoad()}.
 * <p> Any hull modification bearing the tag {@link tag#customizable} that
 * also implements the {@link customizableHullMod} interface are registered
 * as such, and any changes will be applied on them without a reload or a
 * restart.
 * @author lyravega
 */
public class lyr_settings implements LunaSettingsListener {
	private lyr_settings() {
		cacheSettings();
		registerModsWithCustomization();
	}

	static void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_settings.class)) {
			LunaSettings.addSettingsListener(new lyr_settings());

			lyr_logger.info("Attached LunaLib settings listener");
		}
	}

	private static final Set<customizableHullMod> lunaMods = new HashSet<customizableHullMod>();

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
		String extraInfo = lyr_lunaAccessors.getString("ehm_extraInfoInHullMods");	// splitting radio into booleans
		showInfoForActivators = !extraInfo.equals("None");
		showFullInfoForActivators = extraInfo.equals("Full");
		String drillSound = lyr_lunaAccessors.getString("ehm_drillSound");	// splitting radio into booleans
		playDrillSound = !drillSound.equals("None");
		playDrillSoundForAll = drillSound.equals("All");
		checkCosmeticsOnly();	// separate from others like the shunt option as it invokes a method to properly update stuff
		hideAdapters = lyr_lunaAccessors.getBoolean("ehm_hideAdapters");
		hideConverters = lyr_lunaAccessors.getBoolean("ehm_hideConverters");

		// HULL MODIFICATION SETTINGS
		baseSlotPointPenalty = lyr_lunaAccessors.getInt("ehm_baseSlotPointPenalty");

		// FLAVOUR SETTINGS
		showExperimentalFlavour = lyr_lunaAccessors.getBoolean("ehm_showExperimentalFlavour");
		showFluff = lyr_lunaAccessors.getBoolean("ehm_showFluff");

		// DEBUG SETTINGS
		debugTooltip = lyr_lunaAccessors.getBoolean("ehm_debugTooltip");
		checkLoggerLevel();
	}

	private static void registerModsWithCustomization() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(tag.customizable)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (customizableHullMod.class.isInstance(hullModEffect)) lunaMods.add((customizableHullMod) hullModEffect);
		}
	}

	private static void checkShuntAvailability() {
		final String temp = lyr_lunaAccessors.getString("ehm_shuntAvailability");

		if (shuntAvailability != null && shuntAvailability.equals(temp)) return; else shuntAvailability = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;
		
		lyr_ehm.attachShuntAccessListener();
	}

	private static void checkLoggerLevel() {
		switch (lyr_lunaAccessors.getInt("ehm_loggerLevel")) {
			case 5: lyr_logger.setLevel(lyr_levels.WARN); break;
			case 4: lyr_logger.setLevel(lyr_levels.LSTNR); break;
			case 3: lyr_logger.setLevel(lyr_levels.INFO); break;
			case 2: lyr_logger.setLevel(lyr_levels.EVENT); break;
			case 1: lyr_logger.setLevel(lyr_levels.RFLCT); break;
			case 0: lyr_logger.setLevel(lyr_levels.DEBUG); break;
			default: lyr_logger.setLevel(lyr_levels.LSTNR); break;
		}
	}

	private static void checkCosmeticsOnly() {
		final boolean temp = lyr_lunaAccessors.getBoolean("ehm_cosmeticsOnly");

		if (cosmeticsOnly == temp) return; else cosmeticsOnly = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;
		
		lyr_ehm.updateBlueprints(); lyr_ehm.updateHullMods();
	}

	@Override
	public void settingsChanged(String modId) {
		if (!modId.equals(id.mod)) return;
		
		cacheSettings();	// order may be important; customizable hull modifications might require these to be cached first

		for (customizableHullMod customizableMod: lunaMods) {
			customizableMod.applyCustomization();
		}

		lyr_logger.info("Settings reapplied");
	}
}