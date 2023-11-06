package experimentalHullModifications.misc;

import static lyravega.utilities.lyr_tooltipUtilities.regexColour.*;

import java.awt.Color;

import com.fs.starfarer.api.util.Misc;

public class ehm_tooltip {
	public static class text {
		public static final float padding = 5.0f;
		public static final String
			flavourManufacturer = "Experimental",
			flavourDescription = "This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.";
	}

	public static class regexText {
		public static final String
			warning = "Installing or removing Experimental Hull Modifications "+highlightPattern+"will commit the changes immediately); the variant will be saved and any market and/or cargo transactions will be finalized",
			baseRetrofitWarning = "Will become a built-in hull modification "+highlightPattern+"as soon as it is installed)",
			restoreWarning = "This ship has a "+highlightPattern+"base version) that it can be restored to. Installing this mod will perform a "+highlightPattern+"visual restoration); the visuals of the ship will be restored to its base version while any d-mods will be retained",
			overEngineeredNoEffect = "Has "+negativePattern+"no effects till it is built-in) to the ship using a "+storyPattern+"story point)",
			overEngineeredWarning = highlightPattern+"Removal) of base experimental hull modification "+negativePattern+"will be blocked if built-in)",
			// noShip = {"Ship does not exist", "not"},	// not used, old
			lacksBase = negativePattern+"Requires) the "+highlightPattern+"Experimental Hull Modifications) base modification to be installed first",
			lacksActivator = negativePattern+"Requires) the "+highlightPattern+"Converter/Diverter Activator) hull modification to be installed first",
			// hasSystemRetrofit = {"Another system retrofit is already installed", "system retrofit"},	// not used, old
			// hasWeaponRetrofit = {"Another weapon retrofit is already installed", "weapon retrofit"},	// not used, old
			// hasAdapterRetrofit = {"Another slot adapter retrofit is already installed", "slot adapter retrofit"},	// not used, old
			// hasShieldCosmetic = {"Another shield cosmetic modification is already installed", "shield cosmetic modification"},	// not used, old
			// hasEngineCosmetic = {"Another engine cosmetic modification is already installed", "engine cosmetic modification"},	// not used, old
			hasAnyExperimental = negativePattern+"Cannot be used) while the ship has any "+highlightPattern+"experimental hull modification installed)",
			hasAnyExperimentalEnhanced = negativePattern+"Cannot be used anymore) due to a "+highlightPattern+"built-in experimental hull modification)",
			hasLogisticsOverhaul = negativePattern+"Cannot be installed) due to ship having completely "+highlightPattern+"overhauled for logistics) usage",
			isModule = negativePattern+"Cannot be installed) on "+highlightPattern+"modules)",
			isParent = negativePattern+"Cannot be installed) on "+highlightPattern+"ships with modules)",
			noShields = negativePattern+"Cannot be installed) on ships with "+highlightPattern+"no shields)",
			hasPhase = negativePattern+"Cannot be installed) on ships with "+highlightPattern+"a phase cloak)",
			noWings = negativePattern+"Cannot be installed) on ships with "+highlightPattern+"no fighter bays)",
			notStripped = negativePattern+"Cannot be installed) on ships that are "+highlightPattern+"not stripped down to the hull); any flux capacitors or vents, wings, weapons, and modular hull modifications needs to be uninstalled first",
			hasWings = negativePattern+"Cannot be installed or removed) as "+highlightPattern+"fighter bays have wings) occupying them",
			hasExtraWings = negativePattern+"Cannot be removed) as the added "+highlightPattern+"extra fighter bays have wings) occupying them",
			hasWeapons = negativePattern+"Cannot be installed or removed) as there are "+highlightPattern+"slots) that have "+highlightPattern+"weapons or inert shunts installed) on them",
			hasWeaponsOnAdaptedSlots = negativePattern+"Cannot be removed) as there are "+highlightPattern+"adapted slots) that have "+highlightPattern+"weapons or shunts installed) on them",
			hasWeaponsOnConvertedSlots = negativePattern+"Cannot be removed) as there are "+highlightPattern+"converted slots) that have "+highlightPattern+"weapons or shunts installed) on them",
			customizable = "This hull modification "+positivePattern+"can be customized) through LunaLib's settings menu. Press "+highlightPattern+"F2) in the campaign screen to open the settings menu and adjust them",
			// customizableEngine = {"Campaign contrails require an update to be displayed properly. Reloading the game will trigger such an update", "require an update"},	// not used, old
			integratedAICore = negativePattern+"Cannot be installed) as the ship has an integrated "+highlightPattern+"AI Core)",
			noAutomatedShipsSkill = negativePattern+"Cannot be installed) without the "+highlightPattern+"Automated Ships) skill",
			hasCaptain = negativePattern+"Cannot be installed or removed) while the ship has a "+highlightPattern+"Captain) assigned",
			hasAICore = negativePattern+"Cannot be installed or removed) while the ship has an "+highlightPattern+"AI Core) installed";
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
			info_textColour = Misc.getHighlightColor(),
			warning_textColour = Misc.getHighlightColor(),
			noEffect_textColour = Misc.getStoryDarkBrigherColor(),
			sEffect_textColour = Misc.getStoryOptionColor(),
			customizable_textColour = Misc.getPositiveHighlightColor(),
			severeWarning_textColour = Misc.getNegativeHighlightColor(),
			notApplicable_textColour = Misc.getNegativeHighlightColor(),
			locked_textColour = Misc.getNegativeHighlightColor();
	}
}