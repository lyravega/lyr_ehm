package lyravega.utilities;

import java.awt.Color;
import java.io.IOException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.utilities.logger.lyr_logger;

public abstract class lyr_lunaUtilities implements LunaSettingsListener {
	private static enum settingType {
		Integer, Double, Boolean, String, Color;
	}

	protected final String modId;
	private final Map<String, settingField<?>> settingFields = new HashMap<String, settingField<?>>();
	private final Set<String> hasChanged = new HashSet<String>();

	public lyr_lunaUtilities(String modId) {
		this.modId = modId;
	}

	public class settingField<T> {
		private final String id;
		private final String group;
		private final settingType type;
		private T settingValue;

		private settingField(String settingId, String settingGroup, T settingValue) {
			this.id = settingId;
			this.group = settingGroup;
			this.type = settingType.valueOf(settingValue.getClass().getSimpleName());
			this.settingValue = settingValue;
		}

		public T getValue() { return this.settingValue; }

		@SuppressWarnings("unchecked")
		private void setValue(Object value) {
			if (!this.settingValue.equals(value)) {
				this.settingValue = (T) value;
				lyr_lunaUtilities.this.hasChanged.add(this.id);
				if (this.group != null) lyr_lunaUtilities.this.hasChanged.add(this.group);
				lyr_logger.debug(lyr_lunaUtilities.this.modId+": '"+this.id+"' changed to '"+this.settingValue+"'");
			}
		}

		public void updateValue() {
			switch (this.type) {
				case Integer: this.setValue(LunaSettings.getInt(lyr_lunaUtilities.this.modId, this.id)); return;
				case Double: this.setValue(LunaSettings.getDouble(lyr_lunaUtilities.this.modId, this.id)); return;
				case Boolean: this.setValue(LunaSettings.getBoolean(lyr_lunaUtilities.this.modId, this.id)); return;
				case String: this.setValue(LunaSettings.getString(lyr_lunaUtilities.this.modId, this.id)); return;
				case Color: this.setValue(LunaSettings.getColor(lyr_lunaUtilities.this.modId, this.id)); return;
			}
		}
	}

	/**
	 * Attaches the settings listener to LunaLib, caches the settings, and then calls a method for
	 * extra initialization {@link #initializeSettings()} if necessary. If the listener is being added
	 * manually and this method is not utilized, {@link #cacheSettings()} must be called after it.
	 */
	public final void attach() {
		if (LunaSettings.hasSettingsListenerOfClass(this.getClass())) return;
		LunaSettings.addSettingsListener(this);

		this.cacheSettings();
		this.initializeSettings();

		lyr_logger.info("Attached LunaLib settings listener for '"+this.modId+"'");
	}

	/**
	 * Caches all of the settings belonging to the {@link #modId} in an internal map, which is
	 * kept updated during {@link #settingsChanged()}. Getters will refer to this map, though the
	 * static getters of LunaLib may still be utilized.
	 */
	protected final void cacheSettings() {
		try {
			JSONArray loadCSV = Global.getSettings().loadCSV("data/config/LunaSettings.csv", this.modId);

			for (int i = 0; i < loadCSV.length(); i++) {
				JSONObject settingRow = loadCSV.getJSONObject(i);

				String settingId = settingRow.getString("fieldID"); if (settingId.isEmpty()) continue;
				String settingType = settingRow.getString("fieldType").toLowerCase();
				String settingGroupId = settingRow.optString("groupId"); if (settingGroupId.isEmpty()) settingGroupId = null;
				settingField<?> settingField;
				switch (settingType) {
					case "int":		settingField = new settingField<Integer>(settingId, settingGroupId, LunaSettings.getInt(this.modId, settingId)); break;
					case "double":	settingField = new settingField<Double>(settingId, settingGroupId, LunaSettings.getDouble(this.modId, settingId)); break;
					case "string":	settingField = new settingField<String>(settingId, settingGroupId, LunaSettings.getString(this.modId, settingId)); break;
					case "boolean":	settingField = new settingField<Boolean>(settingId, settingGroupId, LunaSettings.getBoolean(this.modId, settingId)); break;
					case "color":	settingField = new settingField<Color>(settingId, settingGroupId, LunaSettings.getColor(this.modId, settingId)); break;
					case "radio":	settingField = new settingField<String>(settingId, settingGroupId, LunaSettings.getString(this.modId, settingId)); break;
					// case "keycode":
					// case "text":
					// case "header":
					// case "":
					default: continue;
				}

				this.settingFields.put(settingId, settingField);
			}
		} catch (IOException | JSONException e) {
			lyr_logger.error("Failure during reading 'LunaSettings.csv' for '"+this.modId+"'", e);
		}
	}

	/**
	 * Updates and checks all of the cached settings. If there are any changes, they'll be added to
	 * a set and may be queried with the {@link #hasChanged(String)} method.
	 */
	private final void updateSettings() {
		for (settingField<?> settingField : this.settingFields.values()) {
			settingField.updateValue();
		}
	}

	/**
	 * If a setting requires additional checks to do something else, this method may be used to query
	 * if it has been changed or not. Should be used in {@link #onSettingsChanged()}
	 * <p> The settings csv file may have an additional {@code groupId} column which may be used to
	 * check changes to a group as a whole, eliminating the need to check individual settings.
	 * @param settingOrGroupId setting or group id to query
	 * @return {@code true} if there's a change, {@code false} otherwise
	 */
	public final boolean hasChanged(String settingOrGroupId) {
		return this.hasChanged.contains(settingOrGroupId);
	}

	/**
	 * Called if there's a change in a setting. If there's none, won't be called. Used instead of the
	 * abstracted default {@link #settingsChanged(String)} method. Individual changes may be queried
	 * with the {@link #hasChanged(String)} method.
	 */
	protected abstract void onSettingsChanged();

	/**
	 * Called after this is attached to {@link LunaSettings}, and provides a place where additional
	 * initialization actions may be taken.
	 */
	protected abstract void initializeSettings();

	/**
	 * Abstracted version of LunaLib's method. If the {@link #modId} is not relevant, does nothing.
	 * Else, updates the internal cache, and if there are any changes, calls {@link #onSettingsChanged()}
	 * @param modId
	 */
	@Override
	public final void settingsChanged(String modId) {
		if (!this.modId.equals(modId)) return;
		this.updateSettings();

		if (this.hasChanged.isEmpty()) return;
		this.onSettingsChanged();

		this.hasChanged.retainAll(this.settingFields.keySet());	// this is just to have a purty output below, filtering groupIds out
		lyr_logger.info("Settings reapplied ("+this.hasChanged.size()+" changes)");
		this.hasChanged.clear();
	}

	public boolean getBoolean(String settingId) { return (boolean) this.settingFields.get(settingId).getValue(); }

	public Color getColor(String settingId) { return (Color) this.settingFields.get(settingId).getValue(); }

	public double getDouble(String settingId) { return (double) this.settingFields.get(settingId).getValue(); }

	public float getFloat(String settingId) { return (float) this.settingFields.get(settingId).getValue(); }

	public int getInt(String settingId) { return (int) this.settingFields.get(settingId).getValue(); }

	public String getString(String settingId) { return (String) this.settingFields.get(settingId).getValue(); }

	/**
	 * Gets a string setting from LunaLib with {@code prefix+"_name"} id and returns it
	 */
	public String getLunaName(String settingIdPrefix) { return this.getString(settingIdPrefix+"_name"); }

	/**
	 * Gets a colour setting from LunaLib with {@code prefix+"Colour"} id along with an
	 * 0-255 integer setting with {@code prefix+"Alpha"} id and returns a RGBA array
	 */
	public int[] getLunaRGBAColourArray(String settingIdPrefix) {
		Color colour = this.getColor(settingIdPrefix+"Colour");
		int[] rgba = {0,0,0,0};
		rgba[0] = colour.getRed();
		rgba[1] = colour.getGreen();
		rgba[2] = colour.getBlue();
		rgba[3] = this.getInt(settingIdPrefix+"Alpha");

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