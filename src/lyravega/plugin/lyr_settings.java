package lyravega.plugin;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.listeners.events.customizableHullMod;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_internals.id;
import lyravega.misc.lyr_internals.tag;
import lyravega.tools.lyr_logger;

/**
 * Luna settings listener to utilize anything LunaLib offers for settings
 * management. All settings are registered {@code onApplicationLoad()}.
 * <p> Any hull modification bearing the tag {@link tag#customizable} that
 * also implements the {@link customizableHullMod} interface are registered
 * as such, and any changes will be applied on them without a reload or a
 * restart.
 * @author lyravega
 */
public class lyr_settings implements LunaSettingsListener, lyr_logger {
	private static final Set<customizableHullMod> lunaMods = new HashSet<customizableHullMod>();

	// MAIN SETTINGS
	private String shuntAvailability; public String getShuntAvailability() { return shuntAvailability; }
	// private String extraInfoInHullMods; public String getExtraInfoInHullMods() { return extraInfoInHullMods; }
	private boolean showInfoForActivators; public boolean getShowInfoForActivators() { return showInfoForActivators; }
	private boolean showFullInfoForActivators; public boolean getShowFullInfoForActivators() { return showFullInfoForActivators; }
	// private String drillSound; public String getDrillSound() { return drillSound; }
	private boolean playDrillSound; public boolean getPlayDrillSound() { return playDrillSound; }
	private boolean playDrillSoundForAll; public boolean getPlayDrillSoundForAll() { return playDrillSoundForAll; }
	private boolean cosmeticsOnly; public boolean getCosmeticsOnly() { return cosmeticsOnly; }
	private boolean hideAdapters; public boolean getHideAdapters() { return hideAdapters; }
	private boolean hideConverters; public boolean getHideConverters() { return hideConverters; }

	// HULL MODIFICATION SETTINGS
	private int baseSlotPointPenalty; public int getBaseSlotPointPenalty() { return baseSlotPointPenalty; }

	// FLAVOUR SETTINGS
	private boolean showExperimentalFlavour; public boolean getShowExperimentalFlavour() { return showExperimentalFlavour; }
	private boolean showFluff; public boolean getShowFluff() { return showFluff; }

	// DEBUG SETTINGS
	private boolean debugTooltip; public boolean getDebugTooltip() { return debugTooltip; }
	private boolean logEventInfo; public boolean getLogEventInfo() { return logEventInfo; }
	private boolean logListenerInfo; public boolean getLogListenerInfo() { return logListenerInfo; }
	private boolean logTrackerInfo; public boolean getLogTrackerInfo() { return logTrackerInfo; }

	static void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_settings.class)) {
			LunaSettings.addSettingsListener(lyr_ehm.settings);

			logger.info(logPrefix + "Attached LunaLib settings listener");
		}

		lyr_ehm.settings.cacheSettings();
		registerCustomizableMods();
	}

	private static void registerCustomizableMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(tag.customizable)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (customizableHullMod.class.isInstance(hullModEffect)) lunaMods.add((customizableHullMod) hullModEffect);
		}
	}

	private void cacheSettings() {
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
		logEventInfo = lyr_lunaAccessors.getBoolean("ehm_logEventInfo");
		logListenerInfo = lyr_lunaAccessors.getBoolean("ehm_logListenerInfo");
		logTrackerInfo = lyr_lunaAccessors.getBoolean("ehm_logTrackerInfo");
	}

	private void checkShuntAvailability() {
		final String temp = lyr_lunaAccessors.getString("ehm_shuntAvailability");

		if (shuntAvailability == null || shuntAvailability.equals(temp)) return; else shuntAvailability = temp;

		if (Global.getCurrentState() != GameState.CAMPAIGN) return;
		
		lyr_ehm.attachShuntAccessListener();
	}

	private void checkCosmeticsOnly() {
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

		logger.info(logPrefix + "Settings reapplied");
	}

	public static class lyr_lunaAccessors {
		private static final String modId = lyr_internals.id.mod;

		public static final Boolean getBoolean(String settingId) { return LunaSettings.getBoolean(modId, settingId); }

		public static final Color getColor(String settingId) { return LunaSettings.getColor(modId, settingId); }

		public static final Double getDouble(String settingId) { return LunaSettings.getDouble(modId, settingId); }

		public static final Float getFloat(String settingId) { return LunaSettings.getFloat(modId, settingId); }

		public static final Integer getInt(String settingId) { return LunaSettings.getInt(modId, settingId); }

		public static final String getString(String settingId) { return LunaSettings.getString(modId, settingId); }

		public static final String getLunaName(String settingIdPrefix) { return getString(settingIdPrefix+"name"); }

		public static final int[] getLunaRGBAColourArray(String settingIdPrefix) {
			String colourString = getString(settingIdPrefix+"Colour");
			int[] rgba = {0,0,0,0};
			rgba[0] = Integer.parseInt(colourString.substring(1, 3), 16);
			rgba[1] = Integer.parseInt(colourString.substring(3, 5), 16);
			rgba[2] = Integer.parseInt(colourString.substring(5, 7), 16);
			rgba[3] = getInt(settingIdPrefix+"Alpha");
		
			return rgba;
		}

		public static final Color getLunaRGBAColour(String settingIdPrefix) {
			int[] rgba = getLunaRGBAColourArray(settingIdPrefix);
		
			return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
		}
	}
}
