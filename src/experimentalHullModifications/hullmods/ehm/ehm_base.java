package experimentalHullModifications.hullmods.ehm;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;
import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.highlightText;
import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.storyText;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.stats;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.listeners.events.normalEvents;
import lyravega.misc._lyr_upgradeEffect;
import lyravega.misc.lyr_upgradeVault;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

/**
 * Serves as a requirement for all experimental hull modifications, and enables tracking
 * on the ship. Some hull modification effects are also executed from here, and the
 * actual hull modifications only contribute to their tooltips and used for installation
 * checks.
 * @category Base Hull Modification
 * @author lyravega
 */
public final class ehm_base extends _ehm_base implements normalEvents {
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

		this.swapHullSpec(stats);

		if (!lyr_ehm.lunaSettings.getCosmeticsOnly()) for (String tag : variant.getTags()) {
			if (!tag.startsWith(ehm_internals.upgrades.prefix)) continue;

			_lyr_upgradeEffect upgrade = lyr_upgradeVault.getUpgrade(tag.replaceFirst(":.+?", ""));

			if (upgrade != null) upgrade.applyUpgradeEffect(stats, Integer.valueOf(tag.replaceFirst(upgrade.getUpgradeId()+":", "")));
		}

		this.preProcessShunts(stats);	// at this point, the hull spec should be cloned so proceed and pre-process the shunts
		this.preProcessDynamicStats(stats);
		// lyr_miscUtilities.cleanWeaponGroupsUp(variant);	// when an activator activates shunts on install, so moved this to their 'onInstalled()' method
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();

		if (lyr_ehm.lunaSettings.getDebugTooltip()) {
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
			tooltip.addPara("Variant Source: "+ship.getVariant().getSource(), 5f).setHighlight("Variant Source:");
			tooltip.addPara("Hull Size: "+ship.getHullSize(), 5f).setHighlight("Hull Size:");
			tooltip.addPara("isModule: "+!Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().contains(ship.getFleetMember()), 5f).setHighlight("isModule:");
			tooltip.addPara("isParent: "+lyr_miscUtilities.isParent(ship), 5f).setHighlight("isParent:");
			tooltip.addPara("Hints: "+hullSpec.getHints().toString(), 5f).setHighlight("Hints:");	// variant returns hints from hullspec, so only one is enough
			tooltip.addPara("HullTags: "+hullSpec.getTags().toString(), 5f).setHighlight("HullTags:");
			tooltip.addPara("VariantTags: "+variant.getTags().toString(), 5f).setHighlight("VariantTags:");

			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
			final StatBonus overdrive = dynamicStats.getMod(stats.overdrive);
			final StatBonus adapters = dynamicStats.getMod(stats.adapters);
			final StatBonus converters = dynamicStats.getMod(stats.converters);
			final StatBonus diverters = dynamicStats.getMod(stats.diverters);
			final StatBonus capacitors = dynamicStats.getMod(stats.capacitors);
			final StatBonus dissipators = dynamicStats.getMod(stats.dissipators);
			final StatBonus hangars = dynamicStats.getMod(stats.hangars);
			final StatBonus ordnancePoints = dynamicStats.getMod(stats.ordnancePoints);
			final StatBonus slotPoints = dynamicStats.getMod(stats.slotPoints);
			final StatBonus slotPointsNeeded = dynamicStats.getMod(stats.slotPointsNeeded);
			final StatBonus slotPointsUsed = dynamicStats.getMod(stats.slotPointsUsed);
			final StatBonus slotPointsFromMods = dynamicStats.getMod(stats.slotPointsFromMods);
			final StatBonus slotPointsFromDiverters = dynamicStats.getMod(stats.slotPointsFromDiverters);
			final StatBonus slotPointsToConverters = dynamicStats.getMod(stats.slotPointsToConverters);
			final StatBonus engineCosmetics = dynamicStats.getMod(stats.engineCosmetics);
			final StatBonus shieldCosmetics = dynamicStats.getMod(stats.shieldCosmetics);
			final StatBonus weaponRetrofits = dynamicStats.getMod(stats.weaponRetrofits);
			tooltip.addSectionHeading("DEBUG INFO: DYNAMIC STATS", header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			if (!overdrive.getFlatBonuses().isEmpty()) tooltip.addPara("overdrive: "+overdrive.computeEffective(0f)+" / "+overdrive.getFlatBonuses().keySet().toString(), 5f).setHighlight("overdrive:");
			if (!adapters.getFlatBonuses().isEmpty()) tooltip.addPara("adapters: "+adapters.computeEffective(0f)+" / "+adapters.getFlatBonuses().keySet().toString(), 5f).setHighlight("adapters:");
			if (!converters.getFlatBonuses().isEmpty()) tooltip.addPara("converters: "+converters.computeEffective(0f)+" / "+converters.getFlatBonuses().keySet().toString(), 5f).setHighlight("converters:");
			if (!diverters.getFlatBonuses().isEmpty()) tooltip.addPara("diverters: "+diverters.computeEffective(0f)+" / "+diverters.getFlatBonuses().keySet().toString(), 5f).setHighlight("diverters:");
			if (!capacitors.getFlatBonuses().isEmpty()) tooltip.addPara("capacitors: "+capacitors.computeEffective(0f)+" / "+capacitors.getFlatBonuses().keySet().toString(), 5f).setHighlight("capacitors:");
			if (!dissipators.getFlatBonuses().isEmpty()) tooltip.addPara("dissipators: "+dissipators.computeEffective(0f)+" / "+dissipators.getFlatBonuses().keySet().toString(), 5f).setHighlight("dissipators:");
			if (!hangars.getFlatBonuses().isEmpty()) tooltip.addPara("hangars: "+hangars.computeEffective(0f)+" / "+hangars.getFlatBonuses().keySet().toString(), 5f).setHighlight("hangars:");
			if (!ordnancePoints.getFlatBonuses().isEmpty()) tooltip.addPara("ordnancePoints: "+ordnancePoints.computeEffective(0f)+" / "+ordnancePoints.getFlatBonuses().keySet().toString(), 5f).setHighlight("ordnancePoints:");
			if (!slotPoints.getFlatBonuses().isEmpty()) tooltip.addPara("slotPoints: "+slotPoints.computeEffective(0f)+" / "+slotPoints.getFlatBonuses().keySet().toString(), 5f).setHighlight("slotPoints:");
			if (!slotPointsNeeded.getFlatBonuses().isEmpty()) tooltip.addPara("slotPointsNeeded: "+slotPointsNeeded.computeEffective(0f)+" / "+slotPointsNeeded.getFlatBonuses().keySet().toString(), 5f).setHighlight("slotPointsNeeded:");
			if (!slotPointsUsed.getFlatBonuses().isEmpty()) tooltip.addPara("slotPointsUsed: "+slotPointsUsed.computeEffective(0f)+" / "+slotPointsUsed.getFlatBonuses().keySet().toString(), 5f).setHighlight("slotPointsUsed:");
			if (!slotPointsFromMods.getFlatBonuses().isEmpty()) tooltip.addPara("slotPointsFromMods: "+slotPointsFromMods.computeEffective(0f)+" / "+slotPointsFromMods.getFlatBonuses().keySet().toString(), 5f).setHighlight("slotPointsFromMods:");
			if (!slotPointsFromDiverters.getFlatBonuses().isEmpty()) tooltip.addPara("slotPointsFromDiverters: "+slotPointsFromDiverters.computeEffective(0f)+" / "+slotPointsFromDiverters.getFlatBonuses().keySet().toString(), 5f).setHighlight("slotPointsFromDiverters:");
			if (!slotPointsToConverters.getFlatBonuses().isEmpty()) tooltip.addPara("slotPointsToConverters: "+slotPointsToConverters.computeEffective(0f)+" / "+slotPointsToConverters.getFlatBonuses().keySet().toString(), 5f).setHighlight("slotPointsToConverters:");
			if (!engineCosmetics.getFlatBonuses().isEmpty()) tooltip.addPara("engineCosmetics: "+engineCosmetics.computeEffective(0f)+" / "+engineCosmetics.getFlatBonuses().keySet().toString(), 5f).setHighlight("engineCosmetics:");
			if (!shieldCosmetics.getFlatBonuses().isEmpty()) tooltip.addPara("shieldCosmetics: "+shieldCosmetics.computeEffective(0f)+" / "+shieldCosmetics.getFlatBonuses().keySet().toString(), 5f).setHighlight("shieldCosmetics:");
			if (!weaponRetrofits.getFlatBonuses().isEmpty()) tooltip.addPara("weaponRetrofits: "+weaponRetrofits.computeEffective(0f)+" / "+weaponRetrofits.getFlatBonuses().keySet().toString(), 5f).setHighlight("weaponRetrofits:");

			tooltip.addSectionHeading("DEBUG INFO: SCRIPTS", header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			for (EveryFrameScript script : Global.getSector().getScripts()) {
				String scriptSimpleName = script.getClass().getSimpleName();

				switch (scriptSimpleName) {
					case "FieldRepairsScript": tooltip.addPara("FieldRepairsScript (vanilla): Running", 5f).setHighlight("FieldRepairsScript (vanilla):"); break;
					case "lyr_fieldRepairsScript": tooltip.addPara("FieldRepairsScript (EHM): Running", 5f).setHighlight("FieldRepairsScript (EHM):"); break;
					case "CaptainsFieldRepairsScript": tooltip.addPara("FieldRepairsScript (QC): Running", 5f).setHighlight("FieldRepairsScript (QC):"); break;
				}
			}

			return;
		}

		if (!variant.hasHullMod(this.hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.invisible_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.baseRetrofitWarning, text.padding);

			super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		} else {
			final int overdrive = Math.round(ship.getMutableStats().getDynamic().getMod(stats.overdrive).computeEffective(0f));

			if (overdrive > 0) {
				tooltip.addSectionHeading("UPGRADES", header.sEffect_textColour, header.invisible_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				lyr_tooltipUtilities.addColourizedPara(tooltip, highlightText("Overdrive, Tier "+overdrive)+": Increases s-mod capacity by "+storyText(overdrive+""), text.padding);
			}

			if (lyr_ehm.lunaSettings.getShowFluff()) {
				String playerSalutation = Global.getSector().getPlayerPerson().getGender() == Gender.MALE ? Misc.SIR : Misc.MAAM;

				tooltip.addSectionHeading("FLUFF", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
				switch ((int) Math.round(Math.random() * 10)) {
					case 0:
						tooltip.addPara("For slot shunts, we may need to dock in a colony or a spaceport " + playerSalutation, text.padding);
						break;
					case 1:
						tooltip.addPara(playerSalutation + ", if you are unhappy with what I am offering you, I can get rid of the base hull modifications that I've made. Let me know!", text.padding);
						break;
					case 2:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.hullmods.weaponRetrofits.tag, null, true))
							tooltip.addPara(playerSalutation + ", with slot retrofits every weapon slot may be altered all together to make them compatible with other weapon types.", text.padding);
						else tooltip.addPara("The slot retrofits come at a cost, but their main purpose is to allow flexibility, and of course letting you use your favourite weapons, "+ playerSalutation, text.padding);
						break;
					case 3:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.hullmods.systemRetrofits.tag, null, true))
							tooltip.addPara("The ships are designed along with their systems, however with system retrofits, I can change them anytime you want, "+ playerSalutation +".", text.padding);
						else tooltip.addPara("Some system & ship combinations may be powerful. Some may not. No refunds! Just joking...", text.padding);
						break;
					case 4:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.hullmods.engineCosmetics.tag, null, true))
							tooltip.addPara(playerSalutation + ", let me know if you'd like to have this ship's engine exhaust colour get changed. I can even fully customize them to your exact specifications!", text.padding);
						else tooltip.addPara("The engine exhaust cosmetics are looking great, " + playerSalutation, text.padding);
						break;
					case 5:
						if (!lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.hullmods.shieldCosmetics.tag, null, true))
							tooltip.addPara("The shield emitters may be modified to project a shield with different colours, " + playerSalutation + ". The effect is purely cosmetic", text.padding);
						else tooltip.addPara("The shield emitters are modified to project colours of your choice, " + playerSalutation, text.padding);
						break;
					case 6:
						if (!variant.hasHullMod(ehm_internals.hullmods.activatorRetrofits.diverterConverterActivator))
							tooltip.addPara("Power may be diverted from a weapon slot to another with a diverter slot shunt, " + playerSalutation + ". The trade-off is necessary to make such modifications.", text.padding);
						else tooltip.addPara("If a converter remains idle, we might be lacking the necessary power diverted to it " + playerSalutation, text.padding);
						break;
					case 7:
						if (!variant.hasHullMod(ehm_internals.hullmods.activatorRetrofits.mutableShuntActivator))
							tooltip.addPara(playerSalutation + ", slot housings may be replaced with extra flux capacitors or dissipators, or a fighter bay may be fit into a large slot with select slot shunts!", text.padding);
						else tooltip.addPara("The capacitors and dissipators are designed to improve the built-in ones and also support other on-board systems indirectly. An additional fighter bay on the other hand...", text.padding);
						break;
					case 8:
						if (!variant.hasHullMod(ehm_internals.hullmods.activatorRetrofits.adapterShuntActivator))
							tooltip.addPara(playerSalutation + ", if you need more weapon slots of smaller sizes for any reason, bigger slots may be adapted into multiple smaller ones!", text.padding);
						else tooltip.addPara("Any adapters will be activated, " + playerSalutation + ". The additional slots might be smaller, but sometimes having more of something is the answer.", text.padding);
						break;
					case 9:
						if (!variant.getSMods().contains(ehm_internals.hullmods.misc.overengineered))
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
		return (lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.hullmods.main.base)) ? false : true;
	}
}