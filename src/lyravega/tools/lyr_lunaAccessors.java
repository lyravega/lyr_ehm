package lyravega.tools;

import java.awt.Color;

import experimentalHullModifications.misc.lyr_internals;
import lunalib.lunaSettings.LunaSettings;

public class lyr_lunaAccessors {
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