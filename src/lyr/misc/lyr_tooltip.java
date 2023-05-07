package lyr.misc;

import java.awt.Color;

import org.json.JSONException;
import org.json.JSONObject;

import lyr.lyr_plugin;

public class lyr_tooltip {
	private static final JSONObject localizationJSON = lyr_plugin.localizationJSON;
	private static JSONObject tooltipJSON;

	static {
		try {
			tooltipJSON = localizationJSON.getJSONObject("tooltip");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class text {
		private static JSONObject textJSON;

		public static float padding;
		public static String 
			flavourManufacturer,
			flavourDescription,
			warning,
			baseRetrofitWarning,
			overEngineeredNoEffect,
			overEngineeredWarning,
			noShip,
			lacksBase,
			hasSystemRetrofit,
			hasWeaponRetrofit,
			hasAdapterRetrofit,
			hasShieldCosmetic,
			hasEngineCosmetic,
			hasAnyExperimental,
			hasAnyExperimentalBuiltIn,
			noShields,
			hasPhase,
			noWings,
			adapterActivated,
			noAdapterRetrofit,
			hasWeapons,
			hasWeaponsOnAdaptedSlots,
			hasWeaponsOnConvertedSlots;

		static {
			try {
				textJSON = tooltipJSON.getJSONObject("text");

				padding = (float) textJSON.getDouble("padding");
				flavourManufacturer = textJSON.getString("flavourManufacturer");
				flavourDescription = textJSON.getString("flavourDescription");
				warning = textJSON.getString("warning");
				baseRetrofitWarning = textJSON.getString("baseRetrofitWarning");
				overEngineeredNoEffect = textJSON.getString("overEngineeredNoEffect");
				overEngineeredWarning = textJSON.getString("overEngineeredWarning");
				noShip = textJSON.getString("noShip");
				lacksBase = textJSON.getString("lacksBase");
				hasSystemRetrofit = textJSON.getString("hasSystemRetrofit");
				hasWeaponRetrofit = textJSON.getString("hasWeaponRetrofit");
				hasAdapterRetrofit = textJSON.getString("hasAdapterRetrofit");
				hasShieldCosmetic = textJSON.getString("hasShieldCosmetic");
				hasEngineCosmetic = textJSON.getString("hasEngineCosmetic");
				hasAnyExperimental = textJSON.getString("hasAnyExperimental");
				hasAnyExperimentalBuiltIn = textJSON.getString("hasAnyExperimentalBuiltIn");
				noShields = textJSON.getString("noShields");
				hasPhase = textJSON.getString("hasPhase");
				noWings = textJSON.getString("noWings");
				adapterActivated = textJSON.getString("adapterActivated");
				noAdapterRetrofit = textJSON.getString("noAdapterRetrofit");
				hasWeapons = textJSON.getString("hasWeapons");
				hasWeaponsOnAdaptedSlots = textJSON.getString("hasWeaponsOnAdaptedSlots");
				hasWeaponsOnConvertedSlots = textJSON.getString("hasWeaponsOnConvertedSlots");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static class header {
		private static JSONObject headerJSON;

		public static float padding;
		public static String 
			info,
			warning,
			noEffect,
			sEffect,
			severeWarning,
			notApplicable,
			lockedIn,
			lockedOut;
		public static Color 
			info_bgColour,
			info_textColour,
			warning_bgColour,
			warning_textColour,
			noEffect_bgColour,
			noEffect_textColour,
			sEffect_bgColour,
			sEffect_textColour,
			severeWarning_bgColour,
			severeWarning_textColour,
			notApplicable_bgColour,
			notApplicable_textColour,
			locked_bgColour,
			locked_textColour;

		static {
			try {
				headerJSON = tooltipJSON.getJSONObject("header");

				padding = (float) headerJSON.getDouble("padding");
				info = headerJSON.getString("info");
				warning = headerJSON.getString("warning");
				noEffect = headerJSON.getString("noEffect");
				sEffect = headerJSON.getString("sEffect");
				severeWarning = headerJSON.getString("severeWarning");
				notApplicable = headerJSON.getString("notApplicable");
				lockedIn = headerJSON.getString("lockedIn");
				lockedOut = headerJSON.getString("lockedOut");
				info_bgColour = Color.decode(headerJSON.getString("info_bgColour"));
				info_textColour = Color.decode(headerJSON.getString("info_textColour"));
				warning_bgColour = Color.decode(headerJSON.getString("warning_bgColour"));
				warning_textColour = Color.decode(headerJSON.getString("warning_textColour"));
				noEffect_bgColour = Color.decode(headerJSON.getString("noEffect_bgColour"));
				noEffect_textColour = Color.decode(headerJSON.getString("noEffect_textColour"));
				sEffect_bgColour = Color.decode(headerJSON.getString("sEffect_bgColour"));
				sEffect_textColour = Color.decode(headerJSON.getString("sEffect_textColour"));
				severeWarning_bgColour = Color.decode(headerJSON.getString("severeWarning_bgColour"));
				severeWarning_textColour = Color.decode(headerJSON.getString("severeWarning_textColour"));
				notApplicable_bgColour = Color.decode(headerJSON.getString("notApplicable_bgColour"));
				notApplicable_textColour = Color.decode(headerJSON.getString("notApplicable_textColour"));
				locked_bgColour = Color.decode(headerJSON.getString("locked_bgColour"));
				locked_textColour = Color.decode(headerJSON.getString("locked_textColour"));
			} catch (NumberFormatException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}