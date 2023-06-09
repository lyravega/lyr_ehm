package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.tools.lyr_uiTools.commitChanges;
import static lyravega.tools.lyr_uiTools.playSound;
import static com.fs.starfarer.api.impl.hullmods.Automated.isAutomatedNoPenalty;
import static com.fs.starfarer.api.impl.hullmods.Automated.MAX_CR_PENALTY;

import java.awt.Color;
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
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;

public class ehm_mr_aiswitch extends _ehm_base implements normalEvents {
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
	public void onInstall(ShipVariantAPI variant) {
		if (!variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED)) variant.addPermaMod(HullMods.AUTOMATED, false);
		else variant.addSuppressedMod(HullMods.AUTOMATED);	// if this hullmod is suppressed, relevant calculations that look for it won't work properly
		commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		if (!variant.getSuppressedMods().contains(HullMods.AUTOMATED)) variant.removePermaMod(HullMods.AUTOMATED);
		else variant.removeSuppressedMod(HullMods.AUTOMATED);
		commitChanges(); playSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		float dp = stats.getSuppliesToRecover().getBaseValue();
		boolean noAutomatedShipsSkill = Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) < 1;

		if (variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED) || variant.getSuppressedMods().contains(HullMods.AUTOMATED)) {
			stats.getMinCrewMod().modifyFlat(hullModSpecId, dp*crewMultipliers.get(hullSize)[0]);	// after suppression, add crew complement
			stats.getMaxCrewMod().modifyFlat(hullModSpecId, dp*crewMultipliers.get(hullSize)[1]);
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(hullModSpecId, (int) (dp * 0.25));

			if (noAutomatedShipsSkill) {
				if (isInPlayerFleet(stats) && !isAutomatedNoPenalty(stats)) {
					stats.getMaxCombatReadiness().modifyFlat(hullModSpecId, -MAX_CR_PENALTY, "AI Switch Penalty");
				}
			}
		} else {
			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(hullModSpecId, (int) (dp * -0.10));
			// settings.getHullModSpec(HullMods.AUTOMATED).getEffect().applyEffectsBeforeShipCreation(hullSize, stats, HullMods.AUTOMATED);
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShipVariantAPI variant = ship.getVariant();

		if (variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED) || variant.getSuppressedMods().contains(HullMods.AUTOMATED)) {
			ship.setInvalidTransferCommandTarget(false);
		} else {
			// settings.getHullModSpec(HullMods.AUTOMATED).getEffect().applyEffectsAfterShipCreation(ship, id);
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

	private static final Color[] highlightArray = new Color[]{Misc.getHighlightColor(), Misc.getNegativeHighlightColor()};

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();
		PersonAPI captain = ship.getCaptain();
		boolean noAutomatedShipsSkill = Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) < 1;

		if (variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED)) {
			float dp = ship.getFleetMember().getUnmodifiedDeploymentPointsCost();
			int minCrew = (int) dp*crewMultipliers.get(hullSize)[0], maxCrew = (int) dp*crewMultipliers.get(hullSize)[1];

			tooltip.addSectionHeading("SUPPRESSION MODE", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);

			tooltip.addPara("- Crew minimum & maximum: "+minCrew+" & "+maxCrew, 5).setHighlight(minCrew+" & "+maxCrew);
			tooltip.addPara("- A Captain may be assigned", 1).setHighlight("Captain");
			tooltip.addPara("- Not subjected to automated ship penalty", 1);
			tooltip.addPara("- May house any type of wings", 1);
			tooltip.addPara("- Deployment point cost increased by 25%", 1).setHighlight("25%");
			if (noAutomatedShipsSkill) tooltip.addPara("- Automated Ships skill not detected!", 1, highlightArray, "Automated Ships", "not detected");
		} else {
			tooltip.addSectionHeading("AUTOMATION MODE", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);

			tooltip.addPara("- No crew", 5);
			tooltip.addPara("- An AI Core may be installed", 1).setHighlight("AI Core");
			tooltip.addPara("- Subjected to automated ship penalty", 1);
			tooltip.addPara("- May only house automated wings", 1);
			tooltip.addPara("- Deployment point cost decreased by 10%", 1).setHighlight("10%");
			if (noAutomatedShipsSkill) tooltip.addPara("- Automated Ships skill not detected!", 1, highlightArray, "Automated Ships", "not detected");
		}

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			else if (!variant.hasHullMod(this.hullModSpecId) && Misc.isUnremovable(captain)) tooltip.addPara(text.integratedAICore[0], text.padding).setHighlight(text.integratedAICore[1]);
			else if (!variant.hasHullMod(this.hullModSpecId) && noAutomatedShipsSkill) tooltip.addPara(text.noAutomatedShipsSkill[0], text.padding).setHighlight(text.noAutomatedShipsSkill[1]);
		} else if (!canBeAddedOrRemovedNow(ship, null, null)) {
			boolean isFakeCaptain = captain.getNameString().isEmpty();	// as captain always returns something, checking the name to see if it's empty
			boolean isAICore = captain.isAICore();

			if (variant.hasHullMod(hullModSpecId)) {
				tooltip.addSectionHeading(header.lockedIn, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);

				if (isAICore) tooltip.addPara(text.hasAICore[0], text.padding).setHighlight(text.hasAICore[1]);
				else if (!isFakeCaptain) tooltip.addPara(text.hasCaptain[0], text.padding).setHighlight(text.hasCaptain[1]);
			} else {
				tooltip.addSectionHeading(header.lockedOut, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);

				if (isAICore) tooltip.addPara(text.hasAICore[0], text.padding).setHighlight(text.hasAICore[1]);
				else if (!isFakeCaptain) tooltip.addPara(text.hasCaptain[0], text.padding).setHighlight(text.hasCaptain[1]);
			}
			
			if (!variant.getNonBuiltInWings().isEmpty()) tooltip.addPara(text.hasWings[0], text.padding).setHighlight(text.hasWings[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		ShipVariantAPI variant = ship.getVariant();
		PersonAPI captain = ship.getCaptain();
		boolean noAutomatedShipsSkill = Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) < 1;

		if (!ehm_hasRetrofitBaseBuiltIn(variant)) return false;
		if (!variant.hasHullMod(this.hullModSpecId) && Misc.isUnremovable(captain)) return false;
		if (!variant.hasHullMod(this.hullModSpecId) && noAutomatedShipsSkill) return false;

		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (!ship.getCaptain().getNameString().isEmpty()) return false;
		if (!ship.getVariant().getNonBuiltInWings().isEmpty()) return false;

		return true;
	}
	//#endregion
}
