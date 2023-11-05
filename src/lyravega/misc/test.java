package lyravega.misc;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import lunalib.lunaRefit.BaseRefitButton;
import lyravega.utilities.lyr_miscUtilities;

public class test extends BaseRefitButton {
	private final lyr_upgrade upgrade;

	public test() {
		this.upgrade = new lyr_upgrade("ehm_overdrive");

		this.upgrade.addUpgradeTier(HullSize.FRIGATE, null, null, 1);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{"gamma_core", 1}}, null, 1);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{"beta_core", 1}}, new String[]{"corrupted_nanoforge"}, 2);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{"alpha_core", 1}}, new String[]{"pristine_nanoforge"}, 3);

		this.upgrade.addUpgradeTier(HullSize.DESTROYER, null, null, 2);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{"gamma_core", 1}}, null, 2);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{"beta_core", 1}}, new String[]{"corrupted_nanoforge"}, 3);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{"alpha_core", 1}}, new String[]{"pristine_nanoforge"}, 4);

		this.upgrade.addUpgradeTier(HullSize.CRUISER, null, null, 3);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{"gamma_core", 1}}, null, 3);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{"beta_core", 1}}, new String[]{"corrupted_nanoforge"}, 4);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{"alpha_core", 1}}, new String[]{"pristine_nanoforge"}, 5);

		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, null, null, 5);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{"gamma_core", 1}}, null, 5);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{"beta_core", 1}}, new String[]{"corrupted_nanoforge"}, 6);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{"alpha_core", 1}}, new String[]{"pristine_nanoforge"}, 7);
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
		// return this.checkRequirements(variant);
		return true;
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		if (!event.isShiftDown()) return;
		if (!this.upgrade.canUpgradeTier(variant)) return;

		this.upgrade.upgradeTier(variant, false);

		this.refreshVariant();
		this.refreshButtonList();
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		final int overdriveLevel = this.upgrade.getCurrentTier(variant);
		final boolean isOverdriveCapped = !(overdriveLevel < this.upgrade.getMaxTier(variant.getHullSize()));

		tooltip.addPara("Increase the maximum amount of s-mods supported by this ship by one with each level", 5f);

		if (overdriveLevel > 0) {
			tooltip.addSectionHeading("CURRENT LEVEL: "+overdriveLevel, Color.CYAN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			switch (overdriveLevel) {
				case 2: {
					tooltip.addImages(
						tooltip.getWidthSoFar(), 32f, 0f, 10f,
						Global.getSettings().getCommoditySpec("gamma_core").getIconName()
					);
					break;
				} case 3: {
					tooltip.addImages(
						tooltip.getWidthSoFar(), 32f, 0f, 10f,
						Global.getSettings().getCommoditySpec("gamma_core").getIconName(),
						Global.getSettings().getCommoditySpec("beta_core").getIconName()
					);
					break;
				} case 4: {
					tooltip.addImages(
						tooltip.getWidthSoFar(), 32f, 0f, 10f,
						Global.getSettings().getCommoditySpec("gamma_core").getIconName(),
						Global.getSettings().getCommoditySpec("beta_core").getIconName(),
						Global.getSettings().getCommoditySpec("alpha_core").getIconName()
					);
					break;
				} default: break;
			}
		}

		if (isOverdriveCapped) {
			tooltip.addSectionHeading("MAX LEVEL", Color.CYAN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			return;
		}

		tooltip.addSectionHeading("UPGRADE REQUIREMENTS", Color.YELLOW, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);

		for (lyr_upgradeLayer upgradeLayer : this.upgrade.getUpgradeLayers(variant.getHullSize())) {
			int upgradeTier = upgradeLayer.getTier();
			if (overdriveLevel+1 > upgradeTier) continue;
			this.testsss2(tooltip, upgradeLayer, overdriveLevel == upgradeTier);
		}

		if (!isOverdriveCapped) {
			if (this.upgrade.canUpgradeTier(variant))
				tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", Color.GREEN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			else
				tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", Color.RED, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
		}
	}

	private void testsss2(TooltipMakerAPI tooltip, lyr_upgradeLayer upgradeLayer, boolean isDisabled) {
		MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		int storyPointCost = upgradeLayer.getStoryPointCost();
		Map<String, Integer> commodityCosts = upgradeLayer.getCommodityCosts();
		Set<String> specialRequirements = upgradeLayer.getSpecialRequirements();

		String format = "";

		// String enabledColourHex = "(0xffffff|";
		String disabledColourHex = "(0xff0000|";
		String tierColourHex = "(0xffff00|";
		String availableColourHex = "(0x00ff00|";
		String unavailableColourHex = "(0xff0000|";

		if (isDisabled) {
			tierColourHex = disabledColourHex;
			availableColourHex = disabledColourHex;
			unavailableColourHex = disabledColourHex;
		}

		if (storyPointCost > 0) {
			format = format.isEmpty() ? tierColourHex+"Level "+(upgradeLayer.getTier())+"):" : format+" &";

			format = format+" "
				+(playerStats.getStoryPoints() >= storyPointCost ? availableColourHex : unavailableColourHex)
				+storyPointCost+" SP)";
		}

		if (commodityCosts != null && !commodityCosts.isEmpty()) {
			format = format.isEmpty() ? tierColourHex+"Level "+(upgradeLayer.getTier())+"):" : format+" &";

			for (Iterator<String> iterator = commodityCosts.keySet().iterator(); iterator.hasNext(); ) {
				String commodityCostId = iterator.next();
				int cost = commodityCosts.get(commodityCostId);

				format = format+" "
					+(playerCargo.getCommodityQuantity(commodityCostId) >= cost ? availableColourHex : unavailableColourHex)
					+commodityCosts.get(commodityCostId)+" "
					+Global.getSettings().getCommoditySpec(commodityCostId).getName()+")";

				if (iterator.hasNext()) format = format+", ";
			}
		}

		if (specialRequirements != null && !specialRequirements.isEmpty()) {
			format = format.isEmpty() ? tierColourHex+"Level "+(upgradeLayer.getTier())+"):" : format+" &";

			Set<String> specials = new HashSet<String>();

			for (Iterator<CargoStackAPI> iterator = playerCargo.getStacksCopy().iterator(); iterator.hasNext(); ) {
				CargoStackAPI stack = iterator.next();

				if (stack.getType() != CargoItemType.SPECIAL) { iterator.remove(); continue; }

				specials.add(stack.getSpecialDataIfSpecial().getId());
			}

			for (Iterator<String> iterator = specialRequirements.iterator(); iterator.hasNext(); ) {
				String specialId = iterator.next();

				format = format+" "
					+(specials.contains(specialId) ? availableColourHex : unavailableColourHex)
					+Global.getSettings().getSpecialItemSpec(specialId).getName()+")";

				if (iterator.hasNext()) format = format+", ";
			}
		}

		lyr_miscUtilities.addColorizedPara(tooltip, format, 0f);
	}
}