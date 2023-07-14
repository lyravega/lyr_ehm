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
	public static boolean showExperimentalFlavour;
	public static boolean playDrillSound;
	public static boolean showFluff;
	public static String extraInfoInHullMods;
	public static String shuntAvailability, _shuntAvailability;
	public static int baseSlotPointPenalty;

	public static void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_lunaSettingsListener.class)) {
			LunaSettings.addSettingsListener(new lyr_lunaSettingsListener());

			logger.info(logPrefix + "Attached LunaLib settings listener");
		}

		cacheBasicSettings();
		checkShuntAvailability();
		registerCustomizableMods();
	}

	private static void registerCustomizableMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(tag.customizable)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (customizableHullMod.class.isInstance(hullModEffect)) lunaMods.add((customizableHullMod) hullModEffect);
		}
	}

	private static void cacheBasicSettings() {
		showExperimentalFlavour = LunaSettings.getBoolean(id.mod, "ehm_showExperimentalFlavour");
		playDrillSound = LunaSettings.getBoolean(id.mod, "ehm_playDrillSound");
		showFluff = LunaSettings.getBoolean(id.mod, "ehm_showFluff");
		extraInfoInHullMods = LunaSettings.getString(id.mod, "ehm_extraInfoInHullMods");
		baseSlotPointPenalty = LunaSettings.getInt(id.mod, "ehm_baseSlotPointPenalty");
	}

	private static void checkShuntAvailability() {
		_shuntAvailability = shuntAvailability;
		shuntAvailability = LunaSettings.getString(id.mod, "ehm_shuntAvailability");

		if (_shuntAvailability == null || _shuntAvailability.equals(shuntAvailability)) return;	// null check here ensures return during application load where/when there is no game state
		if (!Global.getCurrentState().equals(GameState.TITLE)) lyr_ehm.attachShuntAccessListener();
	}

	@Override
	public void settingsChanged(String modId) {
		if (!modId.equals(id.mod)) return;
		
		cacheBasicSettings();
		checkShuntAvailability();	// to reset any possible changes on the shuntAvailability setting

		for (customizableHullMod customizableMod: lunaMods) {
			customizableMod.applyCustomization();
		}

		logger.info(logPrefix + "Settings reapplied");
	}
}
