package lyravega.utilities;

import java.awt.Color;

import lunalib.lunaSettings.LunaSettings;

public class lyr_lunaUtilities {
	public static final Boolean getBoolean(String modId, String settingId) { return LunaSettings.getBoolean(modId, settingId); }

	public static final Color getColor(String modId, String settingId) { return LunaSettings.getColor(modId, settingId); }

	public static final Double getDouble(String modId, String settingId) { return LunaSettings.getDouble(modId, settingId); }

	public static final Float getFloat(String modId, String settingId) { return LunaSettings.getFloat(modId, settingId); }

	public static final Integer getInt(String modId, String settingId) { return LunaSettings.getInt(modId, settingId); }

	public static final String getString(String modId, String settingId) { return LunaSettings.getString(modId, settingId); }

	/**
	 * Gets a string setting from LunaLib with {@code prefix+"Name"} id and returns it
	 */
	public static final String getLunaName(String modId, String settingIdPrefix) { return LunaSettings.getString(modId, settingIdPrefix+"name"); }

	/**
	 * Gets a colour setting from LunaLib with {@code prefix+"Colour"} id along with an
	 * 0-255 integer setting with {@code prefix+"Alpha"} id and returns a RGBA array
	 */
	public static final int[] getLunaRGBAColourArray(String modId, String settingIdPrefix) {
		String colourString = LunaSettings.getString(modId, settingIdPrefix+"Colour");
		int[] rgba = {0,0,0,0};
		rgba[0] = Integer.parseInt(colourString.substring(1, 3), 16);
		rgba[1] = Integer.parseInt(colourString.substring(3, 5), 16);
		rgba[2] = Integer.parseInt(colourString.substring(5, 7), 16);
		rgba[3] = LunaSettings.getInt(modId, settingIdPrefix+"Alpha");

		return rgba;
	}

	/**
	 * Gets a colour setting from LunaLib with {@code prefix+"Colour"} id along with an
	 * 0-255 integer setting with {@code prefix+"Alpha"} id and returns a RGBA colour
	 */
	public static final Color getLunaRGBAColour(String modId, String settingIdPrefix) {
		int[] rgba = getLunaRGBAColourArray(modId, settingIdPrefix);

		return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
	}
}