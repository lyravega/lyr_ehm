package experimentalHullModifications.hullmods.ehm_mr;

import static com.fs.starfarer.api.impl.hullmods.Automated.MAX_CR_PENALTY;
import static com.fs.starfarer.api.impl.hullmods.Automated.isAutomatedNoPenalty;
import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;
import static lyravega.utilities.lyr_tooltipUtilities.regexColour.highlightPattern;
import static lyravega.utilities.lyr_tooltipUtilities.regexColour.negativePattern;
import static lyravega.utilities.lyr_tooltipUtilities.regexColour.positivePattern;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.regexText;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.normalEvents;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

/**
 * Turns normal ships into automated, and automated ones into normal ones.
 * @category Miscellaneous Retrofit
 * @author lyravega
 */
public final class ehm_mr_aiswitch extends _ehm_base implements normalEvents {
	private static final Map<HullSize, int[]> crewMultipliers = new HashMap<HullSize, int[]>();
	static {
		crewMultipliers.put(HullSize.FIGHTER, new int[]{0,0});
		crewMultipliers.put(HullSize.DEFAULT, new int[]{0,0});
		crewMultipliers.put(HullSize.FRIGATE, new int[]{2,3});
		crewMultipliers.put(HullSize.DESTROYER, new int[]{4,6});
		crewMultipliers.put(HullSize.CRUISER, new int[]{6,9});
		crewMultipliers.put(HullSize.CAPITAL_SHIP, new int[]{10,15});
	}

	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		if (!variant.getHullSpec().isBuiltInMod(HullMods.AUTOMATED)) variant.addPermaMod(HullMods.AUTOMATED, false);
		else variant.addSuppressedMod(HullMods.AUTOMATED);	// if this hullmod is suppressed, relevant calculations that look for it won't work properly
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		if (!variant.getSuppressedMods().contains(HullMods.AUTOMATED)) variant.removePermaMod(HullMods.AUTOMATED);
		else variant.removeSuppressedMod(HullMods.AUTOMATED);
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		float dp = stats.getSuppliesToRecover().getBaseValue();
		boolean noAutomatedShipsSkill = Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) < 1;

		if (variant.getHullSpec().isBuiltInMod(HullMods.AUTOMATED) || variant.getSuppressedMods().contains(HullMods.AUTOMATED)) {
			stats.getMinCrewMod().modifyFlat(this.hullModSpecId, dp*crewMultipliers.get(hullSize)[0]);	// after suppression, add crew complement
			stats.getMaxCrewMod().modifyFlat(this.hullModSpecId, dp*crewMultipliers.get(hullSize)[1]);
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(this.hullModSpecId, (int) (dp * 0.25));

			if (noAutomatedShipsSkill) {
				if (!isAutomatedNoPenalty(stats)) {
					stats.getMaxCombatReadiness().modifyFlat(this.hullModSpecId, -MAX_CR_PENALTY, "AI Switch Penalty");
				}
			}
		} else {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(this.hullModSpecId, (int) (dp * -0.10));
			// settings.getHullModSpec(HullMods.AUTOMATED).getEffect().applyEffectsBeforeShipCreation(hullSize, stats, HullMods.AUTOMATED);
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		ShipVariantAPI variant = ship.getVariant();

		if (variant.getHullSpec().isBuiltInMod(HullMods.AUTOMATED) || variant.getSuppressedMods().contains(HullMods.AUTOMATED)) {
			ship.setInvalidTransferCommandTarget(false);
		} else {
			// settings.getHullModSpec(HullMods.AUTOMATED).getEffect().applyEffectsAfterShipCreation(ship, hullModSpecId);
		}
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "crew";
			case 1: return "captain";
			case 2: return "no longer subjected to";
			case 3: return "25% more expensive";
			case 4: return "AI Core";
			case 5: return "no crew";
			case 6: return "become subjected to";
			case 7: return "10% cheaper";
			case 8: return "Automated Ships";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();
		PersonAPI captain = ship.getCaptain();
		boolean noAutomatedShipsSkill = Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) < 1;

		if (variant.getHullSpec().isBuiltInMod(HullMods.AUTOMATED)) {
			float dp = ship.getFleetMember().getUnmodifiedDeploymentPointsCost();
			int minCrew = (int) dp*crewMultipliers.get(hullSize)[0], maxCrew = (int) dp*crewMultipliers.get(hullSize)[1];

			tooltip.addSectionHeading("SUPPRESSION MODE", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			lyr_tooltipUtilities.addColorizedPara(tooltip, "- Crew minimum & maximum: "+highlightPattern+minCrew+") & "+highlightPattern+maxCrew+")",  5);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- A "+highlightPattern+"Captain) may be assigned",  1);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- Subjected to "+positivePattern+"no automated ship penalty)",  1);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- May house any type of wings",  1);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- Deployment point cost "+negativePattern+"increased by 25%)",  1);
			if (noAutomatedShipsSkill) lyr_tooltipUtilities.addColorizedPara(tooltip, "- "+highlightPattern+"Automated Ships) skill "+negativePattern+"not detected)", 1);
		} else {
			tooltip.addSectionHeading("AUTOMATION MODE", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			lyr_tooltipUtilities.addColorizedPara(tooltip, "- No crew",  5);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- An "+highlightPattern+"AI Core) may be installed",  1);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- Subjected to "+negativePattern+"automated ship penalty)",  1);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- May only house automated wings",  1);
			lyr_tooltipUtilities.addColorizedPara(tooltip, "- Deployment point cost "+positivePattern+"decreased by 10%)",  1);
			if (noAutomatedShipsSkill) lyr_tooltipUtilities.addColorizedPara(tooltip, "- "+highlightPattern+"Automated Ships) skill "+negativePattern+"not detected)", 1);
		}

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.lacksBase, text.padding);
			if (!variant.hasHullMod(this.hullModSpecId)) {
				if (Misc.isUnremovable(captain)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.integratedAICore, text.padding);
				if (noAutomatedShipsSkill) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.noAutomatedShipsSkill, text.padding);
			}
		} else if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			if (variant.hasHullMod(this.hullModSpecId)) {
				tooltip.addSectionHeading(header.lockedIn, header.locked_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

				if (captain.isAICore()) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasAICore, text.padding);
				else if (!captain.isDefault()) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasCaptain, text.padding);
			} else {
				tooltip.addSectionHeading(header.lockedOut, header.locked_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

				if (captain.isAICore()) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasAICore, text.padding);
				else if (!captain.isDefault()) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasCaptain, text.padding);
			}

			if (!variant.getNonBuiltInWings().isEmpty()) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasWings, text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) return false;
		if (!ship.getVariant().hasHullMod(this.hullModSpecId)) {
			if (Misc.isUnremovable(ship.getCaptain())) return false;
			if (Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) < 1) return false;
		}

		return true;
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (!ship.getCaptain().isDefault()) return false;
		if (!ship.getVariant().getNonBuiltInWings().isEmpty()) return false;

		return true;
	}
	//#endregion
}
