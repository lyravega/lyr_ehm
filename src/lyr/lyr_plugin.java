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

public class lyr_plugin extends BaseModPlugin {
	private static final Logger logger = Logger.getLogger("lyr");
	public static final String EHM_ID = "lyr_ehm";
	public static final String LOCALIZATION_JSON = "customization/ehm_localization.json";
	public static final String SETTINGS_JSON = "customization/ehm_settings.json";
	public static JSONObject localizationJSON;
	public static JSONObject settingsJSON;	
	
	static {
		try {
			lyr_plugin.localizationJSON = Global.getSettings().getMergedJSONForMod(lyr_plugin.LOCALIZATION_JSON, lyr_plugin.EHM_ID);
			lyr_plugin.settingsJSON = Global.getSettings().getMergedJSONForMod(lyr_plugin.SETTINGS_JSON, lyr_plugin.EHM_ID);
		} catch (IOException | JSONException e) {
			logger.fatal("EHM (Experimental Hull Modifications) - Problem importing configuration JSONS");
		}
	}

	private static void updateBlueprints() {
		FactionAPI playerFaction = Global.getSector().getPlayerFaction();

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			String hullModSpecId = hullModSpec.getId();
			if (hullModSpec.hasTag("ehm") && !playerFaction.knowsHullMod(hullModSpecId)) playerFaction.addKnownHullMod(hullModSpecId);
			else if (hullModSpec.hasTag("ehm_restricted") && playerFaction.knowsHullMod(hullModSpecId)) playerFaction.removeKnownHullMod(hullModSpecId);
		}

		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (weaponSpec.hasTag("ehm_adapters") && !playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.addKnownWeapon(weaponSpec.getWeaponId(), false);
		}
	}

	@Override
	public void onGameLoad(boolean newGame) {
		new lyr.tools._lyr_uiTools._lyr_delayedFinder();
		updateBlueprints();
	}
}
