package lyravega.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lyravega.plugin.experimentalHullModifications;
import lyravega.tools._lyr_logger;

public class lyr_externals implements _lyr_logger {
	private static final JSONObject settingsJSON = experimentalHullModifications.settingsJSON;
	public static boolean
		showExperimentalFlavour,
		playDrillSound,
		extraActiveInfoInHullMods,
		extraInactiveInfoInHullMods,
		showFluff;
	public static Map<String, lyr_shieldSettings> shieldSettings = new HashMap<String, lyr_shieldSettings>();

	static {
		try {
			showExperimentalFlavour = settingsJSON.getBoolean("showExperimentalFlavour");
			playDrillSound = settingsJSON.getBoolean("playDrillSound");
			extraActiveInfoInHullMods = settingsJSON.getBoolean("extraActiveInfoInHullMods");
			extraInactiveInfoInHullMods = settingsJSON.getBoolean("extraInactiveInfoInHullMods");
			showFluff = settingsJSON.getBoolean("showFluff");
			
			JSONObject shieldSettingsJSON = settingsJSON.getJSONObject("shieldSettings");
			for (int i = 0; i < shieldSettingsJSON.names().length(); i++) {
				String key = shieldSettingsJSON.names().getString(i);
				JSONObject currentJSONObject = shieldSettingsJSON.getJSONObject(key);
				shieldSettings.put(key, new lyr_shieldSettings(currentJSONObject));
			}
		} catch (JSONException e) {
			logger.fatal(lyr_internals.logPrefix+"Problem importing settings JSON");
		}
	}

	public static class lyr_shieldSettings {
		private String name;
		private Color innerColour;
		private Color ringColour;

		public String getName() { return this.name; }
		public Color getInnerColour() { return this.innerColour; }
		public Color getRingColour() { return this.ringColour; }

		private lyr_shieldSettings(JSONObject JSONObject) throws JSONException {
			JSONArray inner = JSONObject.getJSONArray("innerColour");
			JSONArray ring = JSONObject.getJSONArray("ringColour");
			
			this.name = JSONObject.getString("name");
			this.innerColour = new Color(inner.getInt(0),inner.getInt(1),inner.getInt(2),inner.getInt(3));
			this.ringColour = new Color(ring.getInt(0),ring.getInt(1),ring.getInt(2),ring.getInt(3));
		}		
	}
}