package lyravega.misc;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm.interfaces.customizable;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.tools.lyr_logger;

public class lyr_lunaSettings implements LunaSettingsListener, lyr_logger {
	private static final Set<customizable> lunaMods = new HashSet<customizable>();
	public static boolean showExperimentalFlavour;
	public static boolean playDrillSound;
	public static boolean showFluff;
	public static String extraInfoInHullMods;

	public static void attachLunaListener() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_lunaSettings.class)) {
			LunaSettings.addSettingsListener(new lyr_lunaSettings());

			logger.info(lyr_internals.logPrefix + "Attached LunaLib settings listener");
		}

		cacheBasicSettings();
		registerCustomizableMods();
	}

	private static void registerCustomizableMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(lyr_internals.tag.customizable)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (customizable.class.isInstance(hullModEffect)) lunaMods.add((customizable) hullModEffect);
		}
	}

	private static void cacheBasicSettings() {
		showExperimentalFlavour = LunaSettings.getBoolean(lyr_internals.id.mod, "ehm_showExperimentalFlavour");
		playDrillSound = LunaSettings.getBoolean(lyr_internals.id.mod, "ehm_playDrillSound");
		showFluff = LunaSettings.getBoolean(lyr_internals.id.mod, "ehm_showFluff");
		extraInfoInHullMods = LunaSettings.getString(lyr_internals.id.mod, "ehm_extraInfoInHullMods");
	}

	@Override
	public void settingsChanged(String modId) {
		if (!modId.equals(lyr_internals.id.mod)) return;
		
		cacheBasicSettings();	

		for (customizable customizableMod: lunaMods) {
			customizableMod.ehm_applyCustomization();
		}

		logger.info(lyr_internals.logPrefix + "Settings changed, reapplying");
	}
}
