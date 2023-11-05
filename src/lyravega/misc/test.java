package lyravega.misc;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import lunalib.lunaRefit.BaseRefitButton;

public class test extends BaseRefitButton {
	private final lyr_upgrade upgrade;

	public test() {
		this.upgrade = new lyr_upgrade("ehm_overdrive");

		this.upgrade.addUpgradeTier(HullSize.FRIGATE, null, null, 1);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{"gamma_core", 1}}, null, 1);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{"beta_core", 1}}, null, 2);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{"alpha_core", 1}}, null, 3);

		this.upgrade.addUpgradeTier(HullSize.DESTROYER, null, null, 2);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{"gamma_core", 1}}, null, 2);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{"beta_core", 1}}, null, 3);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{"alpha_core", 1}}, null, 4);

		this.upgrade.addUpgradeTier(HullSize.CRUISER, null, null, 3);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{"gamma_core", 1}}, null, 3);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{"beta_core", 1}}, null, 4);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{"alpha_core", 1}}, null, 5);

		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, null, null, 5);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{"gamma_core", 1}}, null, 5);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{"beta_core", 1}}, null, 6);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{"alpha_core", 1}}, null, 7);
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
		final int currentTier = this.upgrade.getCurrentTier(variant);
		final boolean isOverdriveCapped = !(currentTier < this.upgrade.getMaxTier(variant.getHullSize()));

		tooltip.addPara("Increase the maximum amount of s-mods supported by this ship by one with each level", 5f);

		if (currentTier > 0) {
			tooltip.addSectionHeading("CURRENT LEVEL: "+currentTier, Color.CYAN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			switch (currentTier) {
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

			if (upgradeTier > currentTier) upgradeLayer.addRequirementsToTooltip(tooltip, upgradeTier != currentTier+1);	// skip purchased tiers, colourize next tier, desaturate rest
		}

		if (!isOverdriveCapped) {
			if (this.upgrade.canUpgradeTier(variant))
				tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", Color.GREEN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			else
				tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", Color.RED, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
		}
	}
}