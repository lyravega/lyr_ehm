package experimentalHullModifications.hullmods.ehm;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.loading.specs.g;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.stats;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.proxies.ehm_hullSpec;
import lyravega.listeners.events.normalEvents;
import lyravega.utilities.lyr_miscUtilities;

/**
 * Serves as a requirement for all experimental hull modifications, and enables tracking
 * on the ship. Some hull modification effects are also executed from here, and the
 * actual hull modifications only contribute to their tooltips and used for installation
 * checks.
 * @category Base Hull Modification
 * @author lyravega
 */
public final class ehm_module_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		commitVariantChanges(); playDrillSound();
	}

	@Override public void onRemoved(MutableShipStatsAPI stats) {}	// cannot be removed since it becomes a built-in
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();

		ehm_hullSpec hullSpecProxy = new ehm_hullSpec(stats.getVariant().getHullSpec(), false);
		hullSpecProxy.setHullName("Mini Module");
		g test = (g) hullSpec;
		test.setDesignation("");
		Vector2f moduleAnchor = hullSpecProxy.retrieve().getModuleAnchor();

		// if (!lyr_interfaceUtilities.isRefitTab()) {
			// lyr_hullSpec.setHullSize(HullSize.FRIGATE);
		// }

		// for (String tag : variant.getTags()) {
		// 	if (!tag.startsWith("ehm_module_parentShield")) continue;

		// 	Pattern pattern = Pattern.compile(".*:(.*?)/(.*?)/(.*)");
		// 	Matcher matcher = pattern.matcher(tag);

		// 	float shieldCenterX = 0f;
		// 	float shieldCenterY = 0f;
		// 	float shieldRadius = 0f;

		// 	while(matcher.find()) {
		// 		shieldCenterX = Float.parseFloat(matcher.group(1));
		// 		shieldCenterY = Float.parseFloat(matcher.group(2));
		// 		shieldRadius = Float.parseFloat(matcher.group(3));
		// 	};

		// 	lyr_hullSpec.getShieldSpec().setCenterX(shieldCenterX);
		// 	lyr_hullSpec.getShieldSpec().setCenterY(shieldCenterY);
		// 	lyr_hullSpec.getShieldSpec().setRadius(shieldRadius+15);
			// stats.getShieldTurnRateMult().modifyMult(this.hullModSpecId, 10);
		// 	lyr_hullSpec.getShieldSpec().setArc(30f);

		// 	// Object spriteSpec = lyr_hullSpec.getSpriteSpec();
		// 	// MethodHandle testt = null;
		// 	// try {
		// 	// 	testt = lyr_reflectionUtilities.methodReflection.findMethodByClass(spriteSpec, null, float.class).getMethodHandle();
		// 	// 	testt.invoke(spriteSpec, 350f);
		// 	// } catch (Throwable e) {
		// 	// 	e.printStackTrace();
		// 	// }

		// 	break;
		// }

		// _ehm_ar_base.ehm_preProcessShunts(stats);	// at this point, the hull spec should be cloned so proceed and pre-process the shunts
		// lyr_miscUtilities.cleanWeaponGroupsUp(variant);	// when an activator activates shunts on install, so moved this to their 'onInstalled()' method
		variant.setHullSpecAPI(hullSpecProxy.retrieve());
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		// ship.setLayer(CombatEngineLayers.FRIGATES_LAYER);
		// CombatEngineLayers layer = ship.getLayer();

		// ship = ship;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();

		if (ehm_settings.getDebugTooltip()) {
			// tooltip.addSectionHeading("DEBUG INFO: GENERAL", header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding);
			// tooltip.addPara("Mods: "+Global.getSettings().getModManager().getEnabledModsCopy().toString(), 5f).setHighlight("Mods: ");

			tooltip.addSectionHeading("DEBUG INFO: HULL MODIFICATIONS", header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("All: "+variant.getHullMods().toString(), 5f).setHighlight("All:");
			tooltip.addPara("Modular: "+variant.getNonBuiltInHullmods().toString(), 5f).setHighlight("Modular:");
			tooltip.addPara("Modular (Enhanced): "+variant.getSMods().toString(), 5f).setHighlight("Modular (Enhanced):");
			tooltip.addPara("Perma: "+variant.getPermaMods().toString(), 5f).setHighlight("Perma:");
			tooltip.addPara("Built-in: "+hullSpec.getBuiltInMods().toString(), 5f).setHighlight("Built-in:");
			tooltip.addPara("Built-in (Enhanced): "+variant.getSModdedBuiltIns().toString(), 5f).setHighlight("Built-in (Enhanced):");
			tooltip.addPara("Suppressed: "+variant.getSuppressedMods().toString(), 5f).setHighlight("Suppressed:");

			tooltip.addSectionHeading("DEBUG INFO: SHIP DETAILS", header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("isHullCloned: "+(Global.getSettings().getHullSpec(hullSpec.getHullId()) != hullSpec), 5f).setHighlight("isHullCloned:");
			tooltip.addPara("Hull ID: "+hullSpec.getHullId(), 5f).setHighlight("Hull ID:");
			tooltip.addPara("Variant ID: "+variant.getHullVariantId(), 5f).setHighlight("Variant ID:");
			tooltip.addPara("Member ID: "+ship.getFleetMemberId(), 5f).setHighlight("Member ID:");
			tooltip.addPara("Hull Size: "+ship.getHullSize(), 5f).setHighlight("Hull Size:");
			tooltip.addPara("isModule: "+!Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().contains(ship.getFleetMember()), 5f).setHighlight("isModule:");
			tooltip.addPara("isParent: "+lyr_miscUtilities.isParent(ship), 5f).setHighlight("isParent:");
			tooltip.addPara("Hints: "+hullSpec.getHints().toString(), 5f).setHighlight("Hints:");	// variant returns hints from hullspec, so only one is enough
			tooltip.addPara("HullTags: "+hullSpec.getTags().toString(), 5f).setHighlight("HullTags:");
			tooltip.addPara("VariantTags: "+variant.getTags().toString(), 5f).setHighlight("VariantTags:");

			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
			final float launchTubes = dynamicStats.getMod(stats.hangars).computeEffective(0f);
			final float slotPointsFromMods = dynamicStats.getMod(stats.slotPointsFromMods).computeEffective(0f);
			final float slotPointsFromDiverters = dynamicStats.getMod(stats.slotPointsFromDiverters).computeEffective(0f);
			final float slotPointsToConverters = dynamicStats.getMod(stats.slotPointsToConverters).computeEffective(0f);
			final float capacitors = dynamicStats.getMod(stats.capacitors).computeEffective(0f);
			final float dissipators = dynamicStats.getMod(stats.dissipators).computeEffective(0f);
			final float overdrive = dynamicStats.getMod(stats.overdrive).computeEffective(0f);
			tooltip.addSectionHeading("DEBUG INFO: DYNAMIC STATS", header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			if (launchTubes > 0) tooltip.addPara("'"+stats.hangars+"': "+launchTubes, 5f).setHighlight("'"+stats.hangars+"':");
			if (slotPointsFromMods > 0) tooltip.addPara("'"+stats.slotPointsFromMods+"': "+slotPointsFromMods, 5f).setHighlight("'"+stats.slotPointsFromMods+"':");
			if (slotPointsFromDiverters > 0) tooltip.addPara("'"+stats.slotPointsFromDiverters+"': "+slotPointsFromDiverters, 5f).setHighlight("'"+stats.slotPointsFromDiverters+"':");
			if (slotPointsToConverters > 0) tooltip.addPara("'"+stats.slotPointsToConverters+"': "+slotPointsToConverters, 5f).setHighlight("'"+stats.slotPointsToConverters+"':");
			if (capacitors > 0) tooltip.addPara("'"+stats.capacitors+"': "+capacitors, 5f).setHighlight("'"+stats.capacitors+"':");
			if (dissipators > 0) tooltip.addPara("'"+stats.dissipators+"': "+dissipators, 5f).setHighlight("'"+stats.dissipators+"':");
			if (overdrive > 0) tooltip.addPara("'"+stats.overdrive+"': "+overdrive, 5f).setHighlight("'"+stats.overdrive+"':");

			tooltip.addSectionHeading("DEBUG INFO: SCRIPTS", header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			for (EveryFrameScript script : Global.getSector().getScripts()) {
				String scriptSimpleName = script.getClass().getSimpleName();

				if ("FieldRepairsScript".equals(scriptSimpleName)) tooltip.addPara("FieldRepairsScript (vanilla): Running", 5f).setHighlight("FieldRepairsScript (vanilla):");
				if ("lyr_fieldRepairsScript".equals(scriptSimpleName)) tooltip.addPara("FieldRepairsScript (EHM): Running", 5f).setHighlight("FieldRepairsScript (EHM):");
				if ("CaptainsFieldRepairsScript".equals(scriptSimpleName)) tooltip.addPara("FieldRepairsScript (QC): Running", 5f).setHighlight("FieldRepairsScript (QC):");
			}

			return;
		}
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return (lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.hullmods.main.base)) ? false : true;
	}
}
