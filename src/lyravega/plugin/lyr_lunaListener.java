package lyravega.plugin;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import data.hullmods._ehm_customizable;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.misc.lyr_internals;
import lyravega.tools._lyr_logger;

public class lyr_lunaListener implements LunaSettingsListener, _lyr_logger {
	private static final Map<String, _ehm_customizable> lunaMods = new HashMap<String, _ehm_customizable>();

    public static void attachLunaListener() {
		if (!LunaSettings.hasSettingsListenerOfClass(lyr_lunaListener.class)) {
            LunaSettings.addSettingsListener(new lyr_lunaListener());

			logger.info(lyr_internals.logPrefix + "Attached LunaLib settings listener");
		}

        registerCustomizableMods();
    }

    private static void registerCustomizableMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(lyr_internals.tag.customizable)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (_ehm_customizable.class.isInstance(hullModEffect)) lunaMods.put(hullModSpec.getId(), (_ehm_customizable) hullModEffect);
		}
    }

	@Override
	public void settingsChanged(String modId) {
        if (!modId.equals(lyr_internals.id.mod)) return;

        for (_ehm_customizable customizableMod: lunaMods.values()) {
            customizableMod.ehm_applyCustomization();

			// logger.info(lyr_internals.logPrefix + "Settings changed, reapplying");
        }
	}
}
