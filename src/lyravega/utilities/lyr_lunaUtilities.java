package lyravega.utilities;

import java.awt.Color;

import experimentalHullModifications.misc.ehm_settings;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.utilities.logger.lyr_logger;

public abstract class lyr_lunaUtilities implements LunaSettingsListener {
	protected final String modId;

	public lyr_lunaUtilities(String modId) {
		this.modId = modId;
	}

	public void attach() {
		if (!LunaSettings.hasSettingsListenerOfClass(ehm_settings.class)) {
			LunaSettings.addSettingsListener(this);

			lyr_logger.info("Attached LunaLib settings listener for '"+this.modId+"'");
		}

		this.cacheSettings();
	}

	@Override
	public final void settingsChanged(String modId) {
		if (!this.modId.equals(modId)) return;

		this.onSettingsChanged();
	}

	protected abstract void onSettingsChanged();

	protected abstract void cacheSettings();

	public Boolean getBoolean(String settingId) { return LunaSettings.getBoolean(this.modId, settingId); }

	public Color getColor(String settingId) { return LunaSettings.getColor(this.modId, settingId); }

	public Double getDouble(String settingId) { return LunaSettings.getDouble(this.modId, settingId); }

	public Float getFloat(String settingId) { return LunaSettings.getFloat(this.modId, settingId); }

	public Integer getInt(String settingId) { return LunaSettings.getInt(this.modId, settingId); }

	public String getString(String settingId) { return LunaSettings.getString(this.modId, settingId); }

	/**
	 * Gets a string setting from LunaLib with {@code prefix+"_name"} id and returns it
	 */
	public String getLunaName(String settingIdPrefix) { return LunaSettings.getString(this.modId, settingIdPrefix+"_name"); }

	/**
	 * Gets a colour setting from LunaLib with {@code prefix+"Colour"} id along with an
	 * 0-255 integer setting with {@code prefix+"Alpha"} id and returns a RGBA array
	 */
	public int[] getLunaRGBAColourArray(String settingIdPrefix) {
		String colourString = LunaSettings.getString(this.modId, settingIdPrefix+"Colour");
		int[] rgba = {0,0,0,0};
		rgba[0] = Integer.parseInt(colourString.substring(1, 3), 16);
		rgba[1] = Integer.parseInt(colourString.substring(3, 5), 16);
		rgba[2] = Integer.parseInt(colourString.substring(5, 7), 16);
		rgba[3] = LunaSettings.getInt(this.modId, settingIdPrefix+"Alpha");

		return rgba;
	}

	/**
	 * Gets a colour setting from LunaLib with {@code prefix+"Colour"} id along with an
	 * 0-255 integer setting with {@code prefix+"Alpha"} id and returns a RGBA colour
	 */
	public Color getLunaRGBAColour(String settingIdPrefix) {
		int[] rgba = this.getLunaRGBAColourArray(settingIdPrefix);

		return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
	}
}