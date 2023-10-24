package experimentalHullModifications.hullmods.ehm;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.id.stats;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.weaponEvents;
import lyravega.utilities.lyr_miscUtilities;

/**
 * Serves as a requirement for all experimental hull modifications, and enables tracking
 * on the ship. Some hull modification effects are also executed from here, and the
 * actual hull modifications only contribute to their tooltips and used for installation
 * checks.
 * @category Base Hull Modification
 * @author lyravega
 */
public final class ehm_base extends _ehm_base implements weaponEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {

	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {

	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();

		if (!hullSpec.isBuiltInMod(ehm_internals.id.hullmods.base) || !Misc.getDHullId(hullSpec).equals(hullSpec.getHullId())) {
			variant.setHullSpecAPI(ehm_hullSpecClone(variant));

			if (!variant.getPermaMods().contains(ehm_internals.id.hullmods.base)) {	// to make this a one-time commit, and to avoid re-committing if/when the ship is getting restored
				// for (String moduleSlot : variant.getStationModules().keySet()) {
				// 	ShipVariantAPI moduleVariant = variant.getModuleVariant(moduleSlot);

				// 	if (!moduleVariant.getPermaMods().contains(lyr_internals.id.hullmods.base)) moduleVariant.addPermaMod(lyr_internals.id.hullmods.base, false);
				// }

				variant.addPermaMod(ehm_internals.id.hullmods.base, false);
				commitVariantChanges(); playDrillSound();
			}
		}

		_ehm_ar_base.ehm_preProcessShunts(stats);	// at this point, the hull spec should be cloned so proceed and pre-process the shunts
		// lyr_miscUtilities.cleanWeaponGroupsUp(variant);	// when an activator activates shunts on install, so moved this to their 'onInstalled()' method
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();

		if (!variant.hasHullMod(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.baseRetrofitWarning[0], text.padding).setHighlight(text.baseRetrofitWarning[1]);

			if (hullSpec.isRestoreToBase()) {
				tooltip.addSectionHeading(header.restoreWarning, header.warning_textColour, header.warning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				tooltip.addPara(text.restoreWarning[0], text.padding).setHighlight(text.restoreWarning[1]);
			}

			super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		} else {
			if (ehm_settings.getDebugTooltip()) {
				// tooltip.addSectionHeading("DEBUG INFO: GENERAL", header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				// tooltip.addPara("Mods: "+Global.getSettings().getModManager().getEnabledModsCopy().toString(), 5f).setHighlight("Mods: ");

				tooltip.addSectionHeading("DEBUG INFO: HULL MODIFICATIONS", header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				tooltip.addPara("All: "+variant.getHullMods().toString(), 5f).setHighlight("All:");
				tooltip.addPara("Modular: "+variant.getNonBuiltInHullmods().toString(), 5f).setHighlight("Modular:");
				tooltip.addPara("Modular (Enhanced): "+variant.getSMods().toString(), 5f).setHighlight("Modular (Enhanced):");
				tooltip.addPara("Perma: "+variant.getPermaMods().toString(), 5f).setHighlight("Perma:");
				tooltip.addPara("Built-in: "+hullSpec.getBuiltInMods().toString(), 5f).setHighlight("Built-in:");
				tooltip.addPara("Built-in (Enhanced): "+variant.getSModdedBuiltIns().toString(), 5f).setHighlight("Built-in (Enhanced):");
				tooltip.addPara("Suppressed: "+variant.getSuppressedMods().toString(), 5f).setHighlight("Suppressed:");

				tooltip.addSectionHeading("DEBUG INFO: SHIP DETAILS", header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				tooltip.addPara("Hull ID: "+hullSpec.getHullId(), 5f).setHighlight("Hull ID:");
				tooltip.addPara("Variant ID: "+variant.getHullVariantId(), 5f).setHighlight("Variant ID:");
				tooltip.addPara("Member ID: "+ship.getFleetMemberId(), 5f).setHighlight("Member ID:");
				tooltip.addPara("isModule: "+!Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().contains(ship.getFleetMember()), 5f).setHighlight("isModule:");
				tooltip.addPara("isParent: "+lyr_miscUtilities.isParent(ship), 5f).setHighlight("isParent:");
				tooltip.addPara("HullHints: "+hullSpec.getHints().toString(), 5f).setHighlight("HullHints:");
				tooltip.addPara("VariantHints: "+variant.getHints().toString(), 5f).setHighlight("VariantHints:");
				tooltip.addPara("HullTags: "+hullSpec.getTags().toString(), 5f).setHighlight("HullTags:");
				tooltip.addPara("VariantTags: "+variant.getTags().toString(), 5f).setHighlight("VariantTags:");

				DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
				tooltip.addSectionHeading("DEBUG INFO: DYNAMIC STATS", header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				tooltip.addPara("'"+stats.launchTubes+"': "+(dynamicStats.getMod(stats.launchTubes).computeEffective(0f)), 5f).setHighlight("'"+stats.launchTubes+"':");
				tooltip.addPara("'"+stats.slotPointsFromMods+"': "+(dynamicStats.getMod(stats.slotPointsFromMods).computeEffective(0f)), 5f).setHighlight("'"+stats.slotPointsFromMods+"':");
				tooltip.addPara("'"+stats.slotPointsFromDiverters+"': "+(dynamicStats.getMod(stats.slotPointsFromDiverters).computeEffective(0f)), 5f).setHighlight("'"+stats.slotPointsFromDiverters+"':");
				tooltip.addPara("'"+stats.slotPointsToConverters+"': "+(dynamicStats.getMod(stats.slotPointsToConverters).computeEffective(0f)), 5f).setHighlight("'"+stats.slotPointsToConverters+"':");

				tooltip.addSectionHeading("DEBUG INFO: SCRIPTS", header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				for (EveryFrameScript script : Global.getSector().getScripts()) {
					String scriptSimpleName = script.getClass().getSimpleName();

					if ("FieldRepairsScript".equals(scriptSimpleName)) tooltip.addPara("FieldRepairsScript (vanilla): Running", 5f).setHighlight("FieldRepairsScript (vanilla):");
					if ("lyr_fieldRepairsScript".equals(scriptSimpleName)) tooltip.addPara("FieldRepairsScript (EHM): Running", 5f).setHighlight("FieldRepairsScript (EHM):");
					if ("CaptainsFieldRepairsScript".equals(scriptSimpleName)) tooltip.addPara("FieldRepairsScript (QC): Running", 5f).setHighlight("FieldRepairsScript (QC):");
				}
			} else if (ehm_settings.getShowFluff()) {
				String playerSalutation = Global.getSector().getPlayerPerson().getGender() == Gender.MALE ? Misc.SIR : Misc.MAAM;

				tooltip.addSectionHeading("FLUFF", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
				switch ((int) Math.round(Math.random() * 10)) {
					case 0:
						tooltip.addPara("For slot shunts, we may need to dock in a colony or a spaceport " + playerSalutation, text.padding);
						break;
					case 1:
						tooltip.addPara(playerSalutation + ", if you are unhappy with what I am offering you, I can get rid of the base hull modifications that I've made. Let me know!", text.padding);
						break;
					case 2:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.weaponRetrofit, null, true))
							tooltip.addPara(playerSalutation + ", with slot retrofits every weapon slot may be altered all together to make them compatible with other weapon types.", text.padding);
						else tooltip.addPara("The slot retrofits come at a cost, but their main purpose is to allow flexibility, and of course letting you use your favourite weapons, "+ playerSalutation, text.padding);
						break;
					case 3:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.systemRetrofit, null, true))
							tooltip.addPara("The ships are designed along with their systems, however with system retrofits, I can change them anytime you want, "+ playerSalutation +".", text.padding);
						else tooltip.addPara("Some system & ship combinations may be powerful. Some may not. No refunds! Just joking...", text.padding);
						break;
					case 4:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.engineCosmetic, null, true))
							tooltip.addPara(playerSalutation + ", let me know if you'd like to have this ship's engine exhaust colour get changed. I can even fully customize them to your exact specifications!", text.padding);
						else tooltip.addPara("The engine exhaust cosmetics are looking great, " + playerSalutation, text.padding);
						break;
					case 5:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.shieldCosmetic, null, true))
							tooltip.addPara("The shield emitters may be modified to project a shield with different colours, " + playerSalutation + ". The effect is purely cosmetic", text.padding);
						else tooltip.addPara("The shield emitters are modified to project colours of your choice, " + playerSalutation, text.padding);
						break;
					case 6:
						if (!variant.hasHullMod(ehm_internals.id.hullmods.diverterandconverter))
							tooltip.addPara("Power may be diverted from a weapon slot to another with a diverter slot shunt, " + playerSalutation + ". The trade-off is necessary to make such modifications.", text.padding);
						else tooltip.addPara("If a converter remains idle, we might be lacking the necessary power diverted to it " + playerSalutation, text.padding);
						break;
					case 7:
						if (!variant.hasHullMod(ehm_internals.id.hullmods.mutableshunt))
							tooltip.addPara(playerSalutation + ", slot housings may be replaced with extra flux capacitors or dissipators, or a fighter bay may be fit into a large slot with select slot shunts!", text.padding);
						else tooltip.addPara("The capacitors and dissipators are designed to improve the built-in ones and also support other on-board systems indirectly. An additional fighter bay on the other hand...", text.padding);
						break;
					case 8:
						if (!variant.hasHullMod(ehm_internals.id.hullmods.stepdownadapter))
							tooltip.addPara(playerSalutation + ", if you need more weapon slots of smaller sizes for any reason, bigger slots may be adapted into multiple smaller ones!", text.padding);
						else tooltip.addPara("Any adapters will be activated, " + playerSalutation + ". The additional slots might be smaller, but sometimes having more of something is the answer.", text.padding);
						break;
					case 9:
						if (!variant.getSMods().contains(ehm_internals.id.hullmods.overengineered))
							tooltip.addPara(playerSalutation + ", have you thought about letting me over-engineer the ship? You might find the benefits interesting!", text.padding);
						else tooltip.addPara("This over-engineered ship is a beast, " + playerSalutation + "! Every internal system, even the bulkheads were replaced, while keeping the structural integrity intact! A mir... *cough* masterpiece!", text.padding);
						break;
					default: tooltip.addPara("All systems operational, " + playerSalutation, text.padding); break;
				}
			}
		}
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return (lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) ? false : true;
	}
}
