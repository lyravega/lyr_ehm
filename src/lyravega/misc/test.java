package lyravega.misc;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.plog.SModRecord;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import lunalib.lunaRefit.BaseRefitButton;

public class test extends BaseRefitButton {
	public static class lyr_upgradeCost {
		private final Map<String, Integer> commodityCosts; public Map<String, Integer> getCommodityCosts() { return this.commodityCosts; }

		private final Set<String> specialRequirements; public Set<String> getSpecialRequirements() { return this.specialRequirements; }

		private final int storyPointCost; public int getStoryPointCost() { return this.storyPointCost; }

		private final String id;

		private static Map<String, Integer> convertCommodityCostsArrayToMap(Object[][] commodityCostsArray) {
			Map<String, Integer> commodityCosts = null;

			if (commodityCostsArray != null) for (Object[] commodityCostArray: commodityCostsArray) {
				if (commodityCosts == null) commodityCosts = new HashMap<String, Integer>();

				commodityCosts.put(String.class.cast(commodityCostArray[0]), Integer.class.cast(commodityCostArray[1]));
			}

			return commodityCosts;
		}

		private static Set<String> convertSpecialRequirementsArrayToSet(String[] specialRequirementsArray) {
			Set<String> specialRequirements = null;

			if (specialRequirementsArray != null) for (String specialId : specialRequirementsArray) {
				if (specialRequirements == null) specialRequirements = new HashSet<String>();

				specialRequirements.add(specialId);
			}

			return specialRequirements;
		}

		public lyr_upgradeCost(String id, Map<String, Integer> commodityCosts, Set<String> specialRequirements, Float storyPointCost) {
			this.id = id;
			this.commodityCosts = commodityCosts;
			this.specialRequirements = specialRequirements;
			this.storyPointCost = storyPointCost != null ? Math.max(0, Math.round(storyPointCost)) : 0;
		}

		public lyr_upgradeCost(String id, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost) {
			// this(id, convertCommodityCostsArrayToMap(commodityCostsArray), convertSpecialRequirementsArrayToSet(specialRequirementsArray), Float.class.cast(storyPointCost));

			Map<String, Integer> commodityCosts = null;
			Set<String> specialRequirements = null;

			if (commodityCostsArray != null) for (Object[] commodityCostArray: commodityCostsArray) {
				if (commodityCosts == null) commodityCosts = new HashMap<String, Integer>();

				commodityCosts.put(String.class.cast(commodityCostArray[0]), Integer.class.cast(commodityCostArray[1]));
			}

			if (specialRequirementsArray != null) for (String specialId : specialRequirementsArray) {
				if (specialRequirements == null) specialRequirements = new HashSet<String>();

				specialRequirements.add(specialId);
			}

			this.id = id;
			this.commodityCosts = commodityCosts;
			this.specialRequirements = specialRequirements;
			this.storyPointCost = storyPointCost != null ? Math.max(0, storyPointCost) : 0;
		}

		public boolean canAfford() {
			MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
			CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

			if (this.storyPointCost > 0) {
				if (playerStats.getStoryPoints() < this.storyPointCost) {
					return false;
				}
			}

			if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
				for (String commodityCostId : this.commodityCosts.keySet()) {
					int cost = this.commodityCosts.get(commodityCostId);

					if (playerCargo.getCommodityQuantity(commodityCostId) < cost) {
						return false;
					}
				}
			}

			if (this.specialRequirements != null && !this.specialRequirements.isEmpty()) {
				for (String specialRequirementId : this.specialRequirements) {
					if (playerCargo.getCommodityQuantity(specialRequirementId) < 1) {
						return false;
					}
				}
			}

			return true;
		}

		public void deductCosts() {
			MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
			CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

			if (this.storyPointCost > 0) {
				if (playerStats.getStoryPoints() >= this.storyPointCost) {
					playerStats.spendStoryPoints(this.storyPointCost, true, null, true, 0f, this.id);
				}
			}

			if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
				for (String commodityCostId : this.commodityCosts.keySet()) {
					int cost = this.commodityCosts.get(commodityCostId);

					if (playerCargo.getCommodityQuantity(commodityCostId) >= cost) {
						playerCargo.removeCommodity(commodityCostId, cost);
					}
				}
			}
		}
	}

	private static test instance;
	private static final Map<Integer, lyr_upgradeCost> costsPerLevel = new HashMap<Integer, lyr_upgradeCost>();
	private static final Map<HullSize, Integer> storyPointCostPerHullSize = new HashMap<HullSize, Integer>();

	static {
		costsPerLevel.put(1, new lyr_upgradeCost("ehm_overdrive:1", null, null, 1));
		costsPerLevel.put(2, new lyr_upgradeCost("ehm_overdrive:2", new Object[][]{{"gamma_core", 1}}, null, 1));
		costsPerLevel.put(3, new lyr_upgradeCost("ehm_overdrive:3", new Object[][]{{"beta_core", 1}}, null, 1));
		costsPerLevel.put(4, new lyr_upgradeCost("ehm_overdrive:4", new Object[][]{{"alpha_core", 1}}, null, 1));

		storyPointCostPerHullSize.put(HullSize.FIGHTER, 1);
		storyPointCostPerHullSize.put(HullSize.FRIGATE, 1);
		storyPointCostPerHullSize.put(HullSize.DESTROYER, 2);
		storyPointCostPerHullSize.put(HullSize.CRUISER, 3);
		storyPointCostPerHullSize.put(HullSize.CAPITAL_SHIP, 5);
		storyPointCostPerHullSize.put(HullSize.DEFAULT, 1);
	}

	public static test instance() {
		return instance;
	}

	public static int getOverdriveLevel(ShipVariantAPI variant) {
		return instance.getLevel(variant, false);
	}

	private final int maxLevel = 4;
	private final String gammaCoreIconName;
	private final String betaCoreIconName;
	private final String alphaCoreIconName;

	public test() {
		instance = this;

		this.gammaCoreIconName = Global.getSettings().getCommoditySpec("gamma_core").getIconName();
		this.betaCoreIconName = Global.getSettings().getCommoditySpec("beta_core").getIconName();
		this.alphaCoreIconName = Global.getSettings().getCommoditySpec("alpha_core").getIconName();
	}

	public boolean isCapped(ShipVariantAPI variant) {
		return this.getLevel(variant, false) >= this.maxLevel;
	}

	public int getLevel(ShipVariantAPI variant, boolean upgradeLevel) {
		int overdrive = 0;

		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); ) {
			String tag = iterator.next();
			if (!tag.startsWith("ehm_overdrive:")) continue;

			overdrive = Integer.valueOf(tag.replace("ehm_overdrive:", ""));
			if (upgradeLevel) iterator.remove(); break;
		}

		if (upgradeLevel) variant.addTag("ehm_overdrive:"+(overdrive+1));

		return overdrive;
	}

	public boolean checkRequirements(ShipVariantAPI variant) {
		int storyPoints = Global.getSector().getPlayerStats().getStoryPoints();
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		switch (this.getLevel(variant, false)) {
			case 0: {
				return storyPoints > storyPointCostPerHullSize.get(variant.getHullSize());
			} case 1: {
				// return storyPoints > this.getStoryPointCost(variant)
				// && costsPerLevel.get(2).canAfford();
			} case 2: {
				// return storyPoints > this.getStoryPointCost(variant)
				// && costsPerLevel.get(3).canAfford();
			} case 3: {
				// return storyPoints > this.getStoryPointCost(variant)
				// && costsPerLevel.get(4).canAfford();
			} default: return false;
		}
	}


	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Overdrive S-Mod Capacity";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "graphics/hullmods/integrated_targeting_unit.png";
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return variant.hasHullMod("ehm_base");
	}

	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return this.checkRequirements(variant);
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		if (!event.isShiftDown()) return;

		int overdriveLevel = this.getLevel(variant, true) + 1;
		// int upgradeCost = this.getStoryPointCost(variant);
		// float xp = Misc.getBuildInBonusXP(null, variant.getHullSize()) * 0.1f;
		float xp = 0.0f;

		SModRecord overdriveRecord = new SModRecord(member);
		overdriveRecord.getSMods().add("ehm_overdrive:"+overdriveLevel);
		overdriveRecord.setBonusXPFractionGained(xp);
		overdriveRecord.setSPSpent(overdriveLevel);
		PlaythroughLog.getInstance().getSModsInstalled().add(overdriveRecord);

		Global.getSector().getPlayerStats().spendStoryPoints(overdriveLevel, true, null, true, xp, "Ship Overdriven: "+member.getShipName());

		this.refreshVariant();
		this.refreshButtonList();
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
		boolean hasAlphaCore = playerCargo.getCommodityQuantity("alpha_core") > 0;
		boolean hasBetaCore = playerCargo.getCommodityQuantity("beta_core") > 0;
		boolean hasGammaCore = playerCargo.getCommodityQuantity("gamma_core") > 0;
		boolean hasTriCore = hasAlphaCore && hasBetaCore && hasGammaCore;
		boolean isOverdriveCapped = this.isCapped(variant);
		// int storyPointCost = this.getStoryPointCost(variant);
		int storyPointCost = 5;
		int overdriveLevel = this.getLevel(variant, false);
		int storyPoints = Global.getSector().getPlayerStats().getStoryPoints();

		tooltip.addPara("Increase the maximum amount of s-mods supported by this ship. Maximum of five levels. Requirements are listed below", 5f);

		tooltip.addSectionHeading("CURRENT LEVEL: "+overdriveLevel, Color.CYAN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
		switch (overdriveLevel) {
			case 2: {
				tooltip.addImages(
					tooltip.getWidthSoFar(), 32f, 5f, 10f,
					this.gammaCoreIconName
				);
				break;
			} case 3: {
				tooltip.addImages(
					tooltip.getWidthSoFar(), 32f, 5f, 10f,
					this.gammaCoreIconName, this.betaCoreIconName
				);
				break;
			} case 4: {
				tooltip.addImages(
					tooltip.getWidthSoFar(), 32f, 5f, 10f,
					this.gammaCoreIconName, this.betaCoreIconName, this.alphaCoreIconName
				);
				break;
			} case 5: {
				tooltip.addImages(
					tooltip.getWidthSoFar(), 32f, 5f, 10f,
					this.gammaCoreIconName, this.betaCoreIconName, this.alphaCoreIconName, this.alphaCoreIconName, this.betaCoreIconName, this.gammaCoreIconName
				);
				break;
			} default: break;
		}

		if (isOverdriveCapped) {
			tooltip.addSectionHeading("MAX LEVEL", Color.CYAN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			return;
		}

		tooltip.addSectionHeading("UPGRADE REQUIREMENTS", Color.YELLOW, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);

		switch (overdriveLevel) {
			case 0: {
				LabelAPI req1 = tooltip.addPara(
					"%s: %s & %s",
					2f,
					Color.WHITE,
					"Level 1", storyPointCost+" story points", "no cores"
				);

				req1.setHighlightColors(Color.YELLOW, storyPoints > storyPointCost ? Color.GREEN : Color.RED, Color.GREEN);
			} case 1: {
				LabelAPI req2 = tooltip.addPara(
					"%s: %s & %s",
					2f,
					Color.WHITE,
					"Level 2",
					storyPointCost+" story points",
					"a gamma core"
				);

				if (overdriveLevel < 1) {
					req2.setColor(Color.GRAY);
					req2.setHighlightColor(Color.GRAY);
				} else {
					req2.setHighlightColors(
						Color.YELLOW,
						storyPoints > storyPointCost ? Color.GREEN : Color.RED,
						hasGammaCore ? Color.GREEN : Color.RED
					);
				}
			} case 2: {
				LabelAPI req3 = tooltip.addPara(
					"%s: %s & %s",
					2f,
					Color.WHITE,
					"Level 3",
					storyPointCost+" story points",
					"a beta core"
				);

				if (overdriveLevel < 2) {
					req3.setColor(Color.GRAY);
					req3.setHighlightColor(Color.GRAY);
				} else {
					req3.setHighlightColors(
						Color.YELLOW,
						storyPoints > storyPointCost ? Color.GREEN : Color.RED,
						hasBetaCore ? Color.GREEN : Color.RED
					);
				}
			} case 3: {
				LabelAPI req4 = tooltip.addPara(
					"%s: %s & %s",
					2f,
					Color.WHITE,
					"Level 4",
					storyPointCost+" story points",
					"an alpha core"
				);

				if (overdriveLevel < 3) {
					req4.setColor(Color.GRAY);
					req4.setHighlightColor(Color.GRAY);
				} else {
					req4.setHighlightColors(
						Color.YELLOW,
						storyPoints > storyPointCost ? Color.GREEN : Color.RED,
						hasAlphaCore ? Color.GREEN : Color.RED
					);
				}
			} default: break;
		}

		if (!isOverdriveCapped) {
			if (this.checkRequirements(variant))
				tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", Color.GREEN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			else
				tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", Color.RED, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
		}
	}

}