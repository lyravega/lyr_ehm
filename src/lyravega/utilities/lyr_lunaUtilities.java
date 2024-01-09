package lyravega.utilities;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import lyravega.utilities.logger.lyr_logger;

public abstract class lyr_lunaUtilities implements LunaSettingsListener {
	protected final String modId;
	protected final Map<String, settingGroup> settingGroups = new HashMap<String, settingGroup>();
	protected final Map<String, settingField<?>> settingFields = new HashMap<String, settingField<?>>();

	public lyr_lunaUtilities(String modId) {
		this.modId = modId;
	}

	public class settingField<T> {
		private final String settingId;
		private final Class<?> settingClass;
		private T settingValue;
		private boolean isChanged;

		private settingField(String settingId, T settingValue) {
			this.settingId = settingId;
			this.settingClass = settingValue.getClass();
			this.settingValue = settingValue;
		}

		public T getValue() { return this.settingValue; }

		@SuppressWarnings("unchecked")
		private void setValue(Object value) {
			if (!this.settingValue.equals(value)) {
				this.isChanged = true;
				this.settingValue = (T) value;
				lyr_logger.debug(lyr_lunaUtilities.this.modId+": '"+this.settingId+"' changed to '"+this.settingValue+"'");
			}
		}

		public void updateValue() {
			switch (this.settingClass.getSimpleName()) {
				case "Integer": this.setValue(LunaSettings.getInt(lyr_lunaUtilities.this.modId, this.settingId)); break;
				case "Double": this.setValue(LunaSettings.getDouble(lyr_lunaUtilities.this.modId, this.settingId)); break;
				case "Boolean": this.setValue(LunaSettings.getBoolean(lyr_lunaUtilities.this.modId, this.settingId)); break;
				case "String": this.setValue(LunaSettings.getString(lyr_lunaUtilities.this.modId, this.settingId)); break;
				case "Color": this.setValue(LunaSettings.getColor(lyr_lunaUtilities.this.modId, this.settingId)); break;
			}
		}

		public boolean isChanged() {
			if (this.isChanged) {
				this.isChanged = false;
				return true;
			} else return false;
		}
	}

	public class settingGroup {
		private final String settingGroupId;
		private final Map<String, settingField<?>> settingValues;
		private boolean isChanged;

		private settingGroup(String settingGroupId) {
			this.settingGroupId = settingGroupId;
			this.settingValues = new HashMap<String, settingField<?>>();
		}

		public Map<String, settingField<?>> getSettings() { return this.settingValues; }
		private void putSetting(String key, settingField<?> value) { this.settingValues.put(key, value); }

		public void updateGroup() {
			for (settingField<?> settingField : this.settingValues.values()) {
				settingField.updateValue();
				if (settingField.isChanged) this.isChanged = true;
			}
		}

		public boolean isChanged(boolean resetGroup) {
			if (this.isChanged) {
				if (resetGroup) for (settingField<?> settingField : this.settingValues.values())
					settingField.isChanged = false;
				this.isChanged = false;
				return true;
			} else return false;
		}
	}

	private final void updateSettings() {
		for (settingGroup settingGroup : this.settingGroups.values()) {
			settingGroup.updateGroup();
		}
	}

	public void attach() {
		if (LunaSettings.hasSettingsListenerOfClass(this.getClass())) return;

		try {
			JSONArray loadCSV = Global.getSettings().loadCSV("data/config/LunaSettings.csv", this.modId);

			for (int i = 0; i < loadCSV.length(); i++) {
				JSONObject settingRow = loadCSV.getJSONObject(i);

				String settingId = settingRow.getString("fieldID"); if (settingId.isEmpty()) continue;
				String settingType = settingRow.getString("fieldType").toLowerCase();
				settingField<?> settingField;
				switch (settingType) {
					case "int":		settingField = new settingField<Integer>(settingId, LunaSettings.getInt(this.modId, settingId)); break;
					case "double":	settingField = new settingField<Double>(settingId, LunaSettings.getDouble(this.modId, settingId)); break;
					case "string":	settingField = new settingField<String>(settingId, LunaSettings.getString(this.modId, settingId)); break;
					case "boolean":	settingField = new settingField<Boolean>(settingId, LunaSettings.getBoolean(this.modId, settingId)); break;
					case "color":	settingField = new settingField<Color>(settingId, LunaSettings.getColor(this.modId, settingId)); break;
					case "radio":	settingField = new settingField<String>(settingId, LunaSettings.getString(this.modId, settingId)); break;
					// case "keycode":
					// case "text":
					// case "header":
					// case "":
					default: continue;
				}

				String settingGroupId = settingRow.getString("groupId");
				if (this.settingGroups.get(settingGroupId) == null) this.settingGroups.put(settingGroupId, new settingGroup(settingGroupId));
				settingGroup settingGroup = this.settingGroups.get(settingGroupId);

				settingGroup.putSetting(settingId, settingField);
				this.settingFields.put(settingId, settingField);
			}
		} catch (IOException | JSONException e) {
			lyr_logger.error("Failure during reading 'LunaSettings.csv'", e);
		}

		lyr_logger.info("Attached LunaLib settings listener for '"+this.modId+"'");
		LunaSettings.addSettingsListener(this);
		this.initializeSettings();
	}

	@Override
	public final void settingsChanged(String modId) {
		if (!this.modId.equals(modId)) return;

		this.updateSettings();
		this.onSettingsChanged();
	}

	protected abstract void onSettingsChanged();

	protected abstract void initializeSettings();

	public Boolean getBoolean(String settingId) { return (Boolean) this.settingFields.get(settingId).getValue(); }

	public Color getColor(String settingId) { return (Color) this.settingFields.get(settingId).getValue(); }

	public Double getDouble(String settingId) { return (Double) this.settingFields.get(settingId).getValue(); }

	public Float getFloat(String settingId) { return (Float) this.settingFields.get(settingId).getValue(); }

	public Integer getInt(String settingId) { return (Integer) this.settingFields.get(settingId).getValue(); }

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