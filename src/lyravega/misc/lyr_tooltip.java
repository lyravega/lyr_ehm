package lyravega.misc;

import java.awt.Color;

public class lyr_tooltip {
	public static class text {
		public static final float padding = 5.0f;
		public static final String 
			flavourManufacturer = "Experimental",
			flavourDescription = "This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.",

			warning = "Installing or removing Experimental Hull Modifications will commit the changes immediately; the variant will be saved and any market and/or cargo transactions will be finalized",
			baseRetrofitWarning = "Will become a built-in hull modification as soon as installed",
			restoreWarning = "This ship has a base version that it can be restored to. Installing this mod will perform a visual restoration; the visuals of the ship will be restored to its base version while any d-mods will be retained",
			overEngineeredNoEffect = "Has no effects till it is built into the ship using a story point",
			overEngineeredWarning = "Removal of base experimental hull modification will be blocked if built-in",
			noShip = "Ship does not exist",
			lacksBase = "Requires experimental hull modifications base to be installed first",
			hasSystemRetrofit = "Another system retrofit is already installed",
			hasWeaponRetrofit = "Another weapon retrofit is already installed",
			hasAdapterRetrofit = "Another slot adapter retrofit is already installed",
			hasShieldCosmetic = "Another shield cosmetic modification is already installed",
			hasEngineCosmetic = "Another engine cosmetic modification is already installed",
			hasAnyExperimental = "All experimental hull modifications needs to be removed from the ship first",
			hasAnyExperimentalBuiltIn = "Cannot be used due to an experimental hull modification built-in to the ship with a story point",
			noShields = "Cannot function without shields",
			hasPhase = "Cannot function with a phase cloak",
			noWings = "Cannot function without wings",
			adapterActivated = "An adapter has been activated. Can only be removed with the adapter removal hull mod",
			noAdapterRetrofit = "There are no adapters to remove",
			hasWeapons = "Cannot be installed or uninstalled as long as there are weapons present on the ship",
			hasWeaponsOnAdaptedSlots = "Cannot be uninstalled as long as adapted slots have weapons or shunts on them",
			hasWeaponsOnConvertedSlots = "Cannot be uninstalled as long as converted slots have weapons or shunts on them",
			customizable = "This hull modification can be customized through LunaLib's settings menu. Press 'F2' in the campaign screen to open the settings menu and adjust them",
			customizableEngine = "Campaign contrails require an update to be displayed properly. Reloading the game will trigger such an update";
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