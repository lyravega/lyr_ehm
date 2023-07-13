package lyravega.listeners;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.listeners.events.customizableHullMod;
import lyravega.misc.lyr_internals.id;
import lyravega.misc.lyr_internals.tag;
import lyravega.tools.lyr_logger;

public class lyr_lunaSettingsListener implements LunaSettingsListener, lyr_logger {
	private static final Set<customizableHullMod> lunaMods = new HashSet<customizableHullMod>();
	public static boolean showExperimentalFlavour;
	public static boolean playDrillSound;
	public static boolean showFluff;
	public static String extraInfoInHullMods;

	public static void attachLunaListener() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_lunaSettingsListener.class)) {
			LunaSettings.addSettingsListener(new lyr_lunaSettingsListener());

			logger.info(logPrefix + "Attached LunaLib settings listener");
		}

		cacheBasicSettings();
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
	}

	@Override
	public void settingsChanged(String modId) {
		if (!modId.equals(id.mod)) return;
		
		cacheBasicSettings();	

		for (customizableHullMod customizableMod: lunaMods) {
			customizableMod.applyCustomization();
		}

		logger.info(logPrefix + "Settings changed, reapplying");
	}
}
