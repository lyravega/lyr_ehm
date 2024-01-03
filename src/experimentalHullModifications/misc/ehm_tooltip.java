package experimentalHullModifications.misc;

import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.*;

import java.awt.Color;

import com.fs.starfarer.api.util.Misc;

public class ehm_tooltip {
	public static class text {
		public static final float padding = 2.0f;
		public static final String
			flavourManufacturer = "Experimental",
			flavourDescription = "This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.";

		public static class colourized {
			public static final float padding = text.padding;
			public static final String
				warning = "Installing or removing Experimental Hull Modifications "+highlightText("will commit the changes immediately")+"; the variant will be saved and any market and/or cargo transactions will be finalized",
				baseRetrofitWarning = "Will become a built-in hull modification "+highlightText("as soon as it is installed")+"",
				// restoreWarning = "This ship has a "+highlightText("base version")+" that it can be restored to. Installing this mod will perform a "+highlightText("visual restoration")+"; the visuals of the ship will be restored to its base version while any d-mods will be retained",
				noEffectUnlessBuiltIn = "Has "+negativeText("reduced or no effects till it is built-in")+" to the ship using a "+storyText("story point")+"",
				willBlockBaseRemoval = highlightText("Removal")+" of base experimental hull modification "+negativeText("will be blocked if built-in")+"",
				// noShip = {"Ship does not exist", "not"},	// old
				lacksBase = negativeText("Requires")+" the "+highlightText("Experimental Hull Modifications")+" base modification to be installed first",
				lacksActivator = negativeText("Requires")+" the "+highlightText("Converter/Diverter Activator")+" hull modification to be installed first",
				// hasSystemRetrofit = {"Another system retrofit is already installed", "system retrofit"},	// old
				// hasWeaponRetrofit = {"Another weapon retrofit is already installed", "weapon retrofit"},	// old
				// hasAdapterRetrofit = {"Another slot adapter retrofit is already installed", "slot adapter retrofit"},	// old
				// hasShieldCosmetic = {"Another shield cosmetic modification is already installed", "shield cosmetic modification"},	// old
				// hasEngineCosmetic = {"Another engine cosmetic modification is already installed", "engine cosmetic modification"},	// old
				hasAnyExperimental = negativeText("Cannot be used")+" while the ship has any "+highlightText("experimental hull modification installed")+"",
				hasAnyExperimentalEnhanced = negativeText("Cannot be used anymore")+" due to a "+highlightText("built-in experimental hull modification")+"",
				hasLogisticsOverhaul = negativeText("Cannot be installed")+" due to ship having completely "+highlightText("overhauled for logistics")+" usage",
				isModule = negativeText("Cannot be installed")+" on "+highlightText("modules")+"",
				isParent = negativeText("Cannot be installed")+" on "+highlightText("ships with modules")+"",
				noShields = negativeText("Cannot be installed")+" on ships with "+highlightText("no shields")+"",
				noEngines = negativeText("Cannot be installed")+" on ships with "+highlightText("no engines")+"",
				hasPhase = negativeText("Cannot be installed")+" on ships with "+highlightText("a phase cloak")+"",
				noWings = negativeText("Cannot be installed")+" on ships with "+highlightText("no fighter bays")+"",
				notStripped = negativeText("Cannot be installed")+" on ships that are "+highlightText("not stripped down to the hull")+"; any flux capacitors or vents, wings, weapons, and modular hull modifications needs to be uninstalled first",
				hasWings = negativeText("Cannot be installed or removed")+" as "+highlightText("fighter bays have wings")+" occupying them",
				hasExtraWings = negativeText("Cannot be removed")+" as the added "+highlightText("extra fighter bays have wings")+" occupying them",
				hasWeapons = negativeText("Cannot be installed or removed")+" as there are "+highlightText("slots")+" that have "+highlightText("weapons or inert shunts installed")+" on them",
				hasWeaponsOnAdaptedSlots = negativeText("Cannot be removed")+" as there are "+highlightText("adapted slots")+" that have "+highlightText("weapons or shunts installed")+" on them",
				hasWeaponsOnConvertedSlots = negativeText("Cannot be removed")+" as there are "+highlightText("converted slots")+" that have "+highlightText("weapons or shunts installed")+" on them",
				customizable = "This hull modification "+positiveText("can be customized")+" through LunaLib's settings menu. Press "+highlightText("F2")+" in the campaign screen to open the settings menu and adjust them",
				// customizableEngine = {"Campaign contrails require an update to be displayed properly. Reloading the game will trigger such an update", "require an update"},	// old
				integratedAICore = negativeText("Cannot be installed")+" as the ship has an integrated "+highlightText("AI Core")+"",
				noAutomatedShipsSkill = negativeText("Cannot be installed")+" without the "+highlightText("Automated Ships")+" skill",
				hasCaptain = negativeText("Cannot be installed or removed")+" while the ship has a "+highlightText("Captain")+" assigned",
				hasAICore = negativeText("Cannot be installed or removed")+" while the ship has an "+highlightText("AI Core")+" installed",
				hasMiniModules = negativeText("Cannot be removed")+" as there are "+highlightText("mini-modules")+" on the ship. These mini-modules needs to be removed individually from their own menu.";
		}
	}

	public static class header {
		public static final float padding = 5.0f;
		public static final String
			info = "INFO",
			warning = "WARNING",
			noEffect = "NO EFFECT",
			sEffect = "S-EFFECT",
			customizable = "CUSTOMIZABLE",	// uses a flashing effect
			// restoreWarning = "BASE DETECTED",
			severeWarning = "WARNING",	// uses a flashing effect
			notApplicable = "NOT APPLICABLE",
			lockedIn = "LOCKED IN",
			lockedOut = "LOCKED OUT";
		public static final Color
			invisible_bgColour = new Color(0, 0, 0, 0),
			invisible_textColour = new Color(0, 0, 0, 0),
			info_textColour = Misc.getButtonTextColor(),
			warning_textColour = Misc.getHighlightColor(),
			noEffect_textColour = Misc.getStoryDarkBrigherColor(),
			sEffect_textColour = Misc.getStoryOptionColor(),
			customizable_textColour = Misc.getPositiveHighlightColor(),
			severeWarning_textColour = Misc.getNegativeHighlightColor(),
			notApplicable_textColour = Misc.getNegativeHighlightColor(),
			locked_textColour = Misc.getNegativeHighlightColor();
	}
}