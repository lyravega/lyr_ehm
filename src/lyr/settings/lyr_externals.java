package lyr.settings;

import org.json.JSONException;

import lyr.lyr_plugin;

public class lyr_externals {
	public static boolean showFlavour = true;

	static {
		try {
			showFlavour = lyr_plugin.settingsJSON.getBoolean("showFlavour");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}