package experimentalHullModifications.hullmods.ehm_mr;

import static lyravega.tools.lyr_uiTools.commitChanges;
import static lyravega.tools.lyr_uiTools.playSound;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;

public class ehm_mr_aiswitch extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		if (variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED)) variant.addSuppressedMod(HullMods.AUTOMATED);
		else variant.addPermaMod(HullMods.AUTOMATED, false);
		commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		if (variant.getSuppressedMods().contains(HullMods.AUTOMATED)) variant.removeSuppressedMod(HullMods.AUTOMATED);
		else variant.removePermaMod(HullMods.AUTOMATED);
		commitChanges(); playSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED) || variant.getSuppressedMods().contains(HullMods.AUTOMATED)) {
			if (stats.getFleetMember() != null) {
				float dp = stats.getFleetMember().getUnmodifiedDeploymentPointsCost();

				stats.getMinCrewMod().modifyFlat(hullModSpecId, dp*10);
				stats.getMaxCrewMod().modifyFlat(hullModSpecId, dp*12);
			}
		} else {
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
			case 0: return "automated ships";
			case 1: return "crew";
			case 2: return "captain";
			case 3: return "other ships";
			case 4: return "AI Core";
			case 5: return "no crew";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();
		PersonAPI captain = ship.getCaptain();

		if (variant.getHullSpec().getBuiltInMods().contains(HullMods.AUTOMATED)) {
			float dp = ship.getFleetMember().getUnmodifiedDeploymentPointsCost();
			float minCrew = dp*10, maxCrew = dp*12;

			tooltip.addSectionHeading("SUPPRESSION MODE", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);

			tooltip.addPara("- Crew minimum / maximum: "+minCrew+" / "+maxCrew, 1).setHighlight(minCrew+" / "+maxCrew);
			tooltip.addPara("- A Captain may be assigned", 1).setHighlight("Captain");
			tooltip.addPara("- Not subjected to automated ship penalty", 1).setHighlight("Not subjected to");
		} else {
			tooltip.addSectionHeading("AUTOMATION MODE", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);

			tooltip.addPara("- Crew minimum / maximum: 0 / 0", 1).setHighlight("0 / 0");
			tooltip.addPara("- An AI Core may be installed", 1).setHighlight("AI Core");
			tooltip.addPara("- Subjected to automated ship penalty", 1).setHighlight("Subjected to");
		}

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			else if (!variant.hasHullMod(this.hullModSpecId) && Misc.isUnremovable(captain)) tooltip.addPara(text.integratedAICore[0], text.padding).setHighlight(text.integratedAICore[1]);
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
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		ShipVariantAPI variant = ship.getVariant();
		PersonAPI captain = ship.getCaptain();

		if (!ehm_hasRetrofitBaseBuiltIn(variant)) return false;
		if (!variant.hasHullMod(this.hullModSpecId) && Misc.isUnremovable(captain)) return false;

		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (!ship.getCaptain().getNameString().isEmpty()) return false;

		return true;
	}
	//#endregion
}
