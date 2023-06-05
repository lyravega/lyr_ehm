package lyravega.misc;

import java.awt.Color;

public class lyr_tooltip {
	public static class text {
		public static final float padding = 5.0f;
		public static final String 
			flavourManufacturer = "Experimental",
			flavourDescription = "This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.";
		public static final String[]
			warning = {"Installing or removing Experimental Hull Modifications will commit the changes immediately; the variant will be saved and any market and/or cargo transactions will be finalized", "immediately"},
			baseRetrofitWarning = {"Will become a built-in hull modification as soon as installed", "as soon as"},
			restoreWarning = {"This ship has a base version that it can be restored to. Installing this mod will perform a visual restoration; the visuals of the ship will be restored to its base version while any d-mods will be retained", "visual restoration"},
			overEngineeredNoEffect = {"Has no effects till it is built into the ship using a story point", "built into the ship"},
			overEngineeredWarning = {"Removal of base experimental hull modification will be blocked if built-in", "will be blocked"},
			noShip = {"Ship does not exist", "not"},	// not used, when does this happen anyway
			lacksBase = {"Requires base experimental hull modification base to be installed first", "experimental hull modification"},
			lacksActivator = {"Requires diverter and converter activator hull modification to be installed first", "diverter and converter activator"},
			hasSystemRetrofit = {"Another system retrofit is already installed", "system retrofit"},
			hasWeaponRetrofit = {"Another weapon retrofit is already installed", "weapon retrofit"},
			hasAdapterRetrofit = {"Another slot adapter retrofit is already installed", "slot adapter retrofit"},	// not used
			hasShieldCosmetic = {"Another shield cosmetic modification is already installed", "shield cosmetic modification"},
			hasEngineCosmetic = {"Another engine cosmetic modification is already installed", "engine cosmetic modification"},
			hasAnyExperimental = {"All experimental hull modifications needs to be removed from the ship first", "needs to be removed"},
			hasAnyExperimentalBuiltIn = {"Cannot be used due to a built-in experimental hull modification", "built-in"},
			noShields = {"Cannot function without shields", "shields"},
			hasPhase = {"Cannot function with a phase cloak", "a phase cloak"},
			noWings = {"Cannot function without wings", "wings"},
			hasWeapons = {"Cannot be added or removed as long as there are installed weapons on the ship", "installed weapons"},
			hasWeaponsOnAdaptedSlots = {"Cannot be removed as there are weapons or shunts installed on the adapted slots", "adapted slots"},
			hasWeaponsOnConvertedSlots = {"Cannot be removed as there are weapons or shunts installed on the converted slots", "converted slots"},
			customizable = {"This hull modification can be customized through LunaLib's settings menu. Press F2 in the campaign screen to open the settings menu and adjust them", "F2"},
			customizableEngine = {"Campaign contrails require an update to be displayed properly. Reloading the game will trigger such an update", "require"};
	}

	public static class header {
		public static final float padding = 15.0f;
		public static final String 
			info = "INFO",
			warning = "WARNING",
			noEffect = "NO EFFECT",
			sEffect = "S-EFFECT",
			customizable = "CUSTOMIZABLE",	// uses a flashing effect
			restoreWarning = "BASE DETECTED",
			severeWarning = "WARNING",	// uses a flashing effect
			notApplicable = "NOT APPLICABLE",
			lockedIn = "LOCKED IN",
			lockedOut = "LOCKED OUT";
		public static final Color 
			info_bgColour = Color.decode("0x000000"),
			info_textColour = Color.decode("0x518B9E"),
			warning_bgColour = Color.decode("0x000000"),
			warning_textColour = Color.decode("0xFFFF00"),
			noEffect_bgColour = Color.decode("0x000000"),
			noEffect_textColour = Color.decode("0xFFFF00"),
			sEffect_bgColour = Color.decode("0x000000"),
			sEffect_textColour = Color.decode("0xA2ED7F"),
			customizable_bgColour = Color.decode("0x000000"),
			customizable_textColour = Color.decode("0xA2ED7F"),
			severeWarning_bgColour = Color.decode("0x000000"),
			severeWarning_textColour = Color.decode("0xFF0000"),
			notApplicable_bgColour = Color.decode("0x000000"),
			notApplicable_textColour = Color.decode("0xFF0000"),
			locked_bgColour = Color.decode("0x000000"),
			locked_textColour = Color.decode("0xFF6600");
	}
}