package lyr;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import lyr.misc.lyr_internals;
import lyr.tools._lyr_uiTools._lyr_delayedFinder;

public class lyr_plugin extends BaseModPlugin {
	private static final Logger logger = Logger.getLogger(lyr_internals.logName);
	public static final String EHM_ID = "lyr_ehm";
	public static final String LOCALIZATION_JSON = "customization/ehm_localization.json";
	public static final String SETTINGS_JSON = "customization/ehm_settings.json";
	public static JSONObject localizationJSON;
	public static JSONObject settingsJSON;
	
	static {
		try {
			localizationJSON = Global.getSettings().getMergedJSONForMod(LOCALIZATION_JSON, EHM_ID);
			settingsJSON = Global.getSettings().getMergedJSONForMod(SETTINGS_JSON, EHM_ID);
		} catch (IOException | JSONException e) {
			logger.fatal(lyr_internals.logPrefix+"Problem importing configuration JSONs");
		}
	}

	private static void updateBlueprints() {
		FactionAPI playerFaction = Global.getSector().getPlayerFaction();

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			String hullModSpecId = hullModSpec.getId();
			if (hullModSpec.hasTag(lyr_internals.tag.experimental) && !playerFaction.knowsHullMod(hullModSpecId)) playerFaction.addKnownHullMod(hullModSpecId);
			else if (hullModSpec.hasTag(lyr_internals.tag.restricted) && playerFaction.knowsHullMod(hullModSpecId)) playerFaction.removeKnownHullMod(hullModSpecId);
		}

		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (weaponSpec.hasTag(lyr_internals.tag.experimental) && !playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.addKnownWeapon(weaponSpec.getWeaponId(), false);
			else if (weaponSpec.hasTag(lyr_internals.tag.restricted) && playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.removeKnownWeapon(weaponSpec.getWeaponId());
		}
	}

	@Override
	public void onGameLoad(boolean newGame) {
		new _lyr_delayedFinder();
		updateBlueprints();
	}
}
