package experimentalHullModifications.misc;

import java.awt.Color;

public class ehm_tooltip {
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
			// noShip = {"Ship does not exist", "not"},	// not used
			lacksBase = {"Requires the base 'Experimental Hull Modifications' base to be installed first", "'Experimental Hull Modifications'"},
			lacksActivator = {"Requires the 'Converter/Diverter Activator' hull modification to be installed first", "Converter/Diverter Activator"},
			// hasSystemRetrofit = {"Another system retrofit is already installed", "system retrofit"},	// not used
			// hasWeaponRetrofit = {"Another weapon retrofit is already installed", "weapon retrofit"},	// not used
			// hasAdapterRetrofit = {"Another slot adapter retrofit is already installed", "slot adapter retrofit"},	// not used
			// hasShieldCosmetic = {"Another shield cosmetic modification is already installed", "shield cosmetic modification"},	// not used
			// hasEngineCosmetic = {"Another engine cosmetic modification is already installed", "engine cosmetic modification"},	// not used
			hasAnyExperimental = {"All experimental hull modifications needs to be removed from the ship first", "needs to be removed"},
			hasAnyExperimentalEnhanced = {"Cannot be used due to a built-in experimental hull modification", "built-in"},
			hasLogisticsOverhaul = {"Cannot be installed due to ship having completely overhauled for logistics usage", "overhauled for logistics"},
			isModule = {"Cannot be installed on modules", "modules"},
			isParent = {"Cannot be installed on module ships", "module ships"},
			noShields = {"Cannot be installed on ships that do not utilize shields", "shields"},
			hasPhase = {"Cannot be installed on ships equipped with a phase cloak", "phase cloak"},
			noWings = {"Cannot be installed on ships with no fighter bays", "no fighter bays"},
			notStripped = {"Cannot be installed on ships that are not stripped down to the hull; any flux capacitors or vents, wings, weapons, and modular hull modifications needs to be uninstalled first", "not stripped down"},
			hasWings = {"Cannot be installed or removed as fighter bays have wings occupying them", "fighter bays have wings"},
			hasExtraWings = {"Cannot be removed as added extra fighter bays have wings occupying them", "added extra fighter bays have wings"},
			hasWeapons = {"Cannot be installed or removed as there are slots that have weapons or inert shunts installed on them", "slots that have weapons or inert shunts"},
			hasWeaponsOnAdaptedSlots = {"Cannot be removed as there are adapted slots that have weapons or shunts installed on them", "adapted slots that have weapons or shunts"},
			hasWeaponsOnConvertedSlots = {"Cannot be removed as there are converted slots that have weapons or shunts installed on them", "converted slots that have weapons or shunts"},
			customizable = {"This hull modification can be customized through LunaLib's settings menu. Press F2 in the campaign screen to open the settings menu and adjust them", "F2"},
			// customizableEngine = {"Campaign contrails require an update to be displayed properly. Reloading the game will trigger such an update", "require an update"},	// not used
			integratedAICore = {"The ship has an integrated AI Core which prevents this hull modification from being installed", "integrated AI Core"},
			noAutomatedShipsSkill = {"Cannot be installed without the Automated Ships skill", "Automated Ships"},
			hasCaptain = {"Cannot be installed or removed while the ship has a Captain assigned", "Captain"},
			hasAICore = {"Cannot be installed or removed while the ship has an AI Core installed", "AI Core"};
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
			invisible_bgColour = new Color(0, 0, 0, 0),
			invisible_textColour = new Color(0, 0, 0, 0),
			info_bgColour = Color.decode("0x000000"),
			info_textColour = Color.decode("0x518B9E"),
			warning_bgColour = Color.decode("0x000000"),
			warning_textColour = Color.decode("0xFFEE00"),
			noEffect_bgColour = Color.decode("0x000000"),
			noEffect_textColour = Color.decode("0xFFEE00"),
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