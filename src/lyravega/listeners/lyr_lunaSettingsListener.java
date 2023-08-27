package lyravega.listeners;

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
import lyravega.tools.lyr_logger;
import lyravega.plugin.lyr_ehm;

/**
 * Luna settings listener to utilize anything LunaLib offers for settings
 * management. All settings are registered {@code onApplicationLoad()}.
 * <p> Any hull modification bearing the tag {@link tag#customizable} that
 * also implements the {@link customizableHullMod} interface are registered
 * as such, and any changes will be applied on them without a reload or a
 * restart.
 * @author lyravega
 */
public class lyr_lunaSettingsListener implements LunaSettingsListener, lyr_logger {
	private static final Set<customizableHullMod> lunaMods = new HashSet<customizableHullMod>();

	// MAIN SETTINGS
	public static String shuntAvailability, _shuntAvailability;
	// public static String extraInfoInHullMods;
	public static boolean showInfoForActivators;
	public static boolean showFullInfoForActivators;
	public static boolean hideAdapters;
	public static boolean hideConverters;

	// HULL MODIFICATION SETTINGS
	public static int baseSlotPointPenalty;

	// FLAVOUR SETTINGS
	public static boolean showExperimentalFlavour;
	// public static String drillSound;
	public static boolean playDrillSound;
	public static boolean playDrillSoundForAll;
	public static boolean showFluff;

	// DEBUG SETTINGS
	public static boolean debugTooltip;
	public static boolean logEventInfo;
	public static boolean logListenerInfo;
	public static boolean logTrackerInfo;

	public static void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_lunaSettingsListener.class)) {
			LunaSettings.addSettingsListener(new lyr_lunaSettingsListener());

			logger.info(logPrefix + "Attached LunaLib settings listener");
		}

		cacheSettings();
		registerCustomizableMods();
	}

	private static void registerCustomizableMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(tag.customizable)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (customizableHullMod.class.isInstance(hullModEffect)) lunaMods.add((customizableHullMod) hullModEffect);
		}
	}

	private static void cacheSettings() {
		// MAIN SETTINGS
		checkShuntAvailability();	// separate from others as it needs to trigger a method to add/remove listeners only if there's a change
		String extraInfo = LunaSettings.getString(id.mod, "ehm_extraInfoInHullMods");	// splitting radio into booleans
		showInfoForActivators = !extraInfo.equals("None");
		showFullInfoForActivators = extraInfo.equals("Full");
		hideAdapters = LunaSettings.getBoolean(id.mod, "ehm_hideAdapters");
		hideConverters = LunaSettings.getBoolean(id.mod, "ehm_hideConverters");

		// HULL MODIFICATION SETTINGS
		baseSlotPointPenalty = LunaSettings.getInt(id.mod, "ehm_baseSlotPointPenalty");

		// FLAVOUR SETTINGS
		showExperimentalFlavour = LunaSettings.getBoolean(id.mod, "ehm_showExperimentalFlavour");
		String drillSound = LunaSettings.getString(id.mod, "ehm_drillSound");	// splitting radio into booleans
		playDrillSound = !drillSound.equals("None");
		playDrillSoundForAll = drillSound.equals("All");
		showFluff = LunaSettings.getBoolean(id.mod, "ehm_showFluff");

		// DEBUG SETTINGS
		debugTooltip = LunaSettings.getBoolean(id.mod, "ehm_debugTooltip");
		logEventInfo = LunaSettings.getBoolean(id.mod, "ehm_logEventInfo");
		logListenerInfo = LunaSettings.getBoolean(id.mod, "ehm_logListenerInfo");
		logTrackerInfo = LunaSettings.getBoolean(id.mod, "ehm_logTrackerInfo");
	}

	private static void checkShuntAvailability() {
		_shuntAvailability = shuntAvailability;
		shuntAvailability = LunaSettings.getString(id.mod, "ehm_shuntAvailability");

		if (_shuntAvailability == null || _shuntAvailability.equals(shuntAvailability)) return;	// null check here ensures return during application load where/when there is no game state
		if (Global.getCurrentState() != GameState.TITLE) lyr_ehm.attachShuntAccessListener();
	}

	@Override
	public void settingsChanged(String modId) {
		if (!modId.equals(id.mod)) return;
		
		cacheSettings();

		for (customizableHullMod customizableMod: lunaMods) {
			customizableMod.applyCustomization();
		}

		logger.info(logPrefix + "Settings reapplied");
	}
}
