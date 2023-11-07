package experimentalHullModifications.upgrades;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lunalib.lunaRefit.BaseRefitButton;
import lyravega.misc._lyr_upgradeEffect;
import lyravega.misc.lyr_upgrade;
import lyravega.misc.lyr_upgradeLayer;

public class ehmu_overdrive extends BaseRefitButton implements _lyr_upgradeEffect {
	private final lyr_upgrade upgrade;

	public ehmu_overdrive() {
		final String gc = "gamma_core";
		final String bc = "beta_core";
		final String ac = "alpha_core";
		final String cn = "corrupted_nanoforge";
		final String pn = "pristine_nanoforge";

		this.upgrade = new lyr_upgrade(ehm_internals.id.upgrades.overdrive, "Overdrive");

		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{gc, 3}}, null, 1);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{gc, 6}, {bc, 1}}, null, 1);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{gc, 9}, {bc, 2}}, new String[]{cn}, 2);
		this.upgrade.addUpgradeTier(HullSize.FRIGATE, new Object[][]{{gc, 12}, {bc, 3}, {ac, 1}}, new String[]{pn}, 3);

		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{gc, 3}}, null, 2);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{gc, 6}, {bc, 1}}, null, 2);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{gc, 9}, {bc, 2}}, new String[]{cn}, 3);
		this.upgrade.addUpgradeTier(HullSize.DESTROYER, new Object[][]{{gc, 12}, {bc, 3}, {ac, 1}}, new String[]{pn}, 4);

		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{gc, 4}}, null, 3);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{gc, 8}, {bc, 2}}, null, 3);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{gc, 12}, {bc, 4}, {ac, 1}}, new String[]{cn}, 4);
		this.upgrade.addUpgradeTier(HullSize.CRUISER, new Object[][]{{gc, 16}, {bc, 6}, {ac, 2}}, new String[]{pn}, 5);

		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 4}}, null, 5);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 8}, {bc, 2}}, null, 5);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 12}, {bc, 4}, {ac, 1}}, new String[]{cn}, 6);
		this.upgrade.addUpgradeTier(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 16}, {bc, 6}, {ac, 2}}, new String[]{pn}, 7);
	}

	@Override
	public String getUpgradeId() {
		return this.upgrade.getId();
	}

	@Override
	public String getUpgradeName() {
		return this.upgrade.getName();
	}

	@Override
	public int getUpgradeTier(ShipVariantAPI variant) {
		return this.upgrade.getCurrentTier(variant);
	}

	@Override
	public void applyUpgradeEffect(MutableShipStatsAPI stats, String tag) {
		final int mod = tag == null ? this.upgrade.getCurrentTier(stats.getVariant()) : Integer.valueOf(tag.replace(this.upgrade.getId()+":", ""));

		stats.getDynamic().getMod(this.upgrade.getId()).modifyFlat(this.upgrade.getId(), mod);	// for tooltip
		stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(this.upgrade.getId(), mod);
	}

	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return this.upgrade.getName();
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "data/graphics/experimental_smol.png";
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return variant.hasHullMod(ehm_internals.id.hullmods.base);
	}

	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return this.upgrade.canUpgradeTier(variant);
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		if (!event.isShiftDown()) return;
		if (!this.upgrade.canUpgradeTier(variant)) return;

		this.upgrade.upgradeTier(variant);

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

		tooltip.addSectionHeading("OVERDRIVE", Misc.getHighlightColor(), header.invisible_bgColour, Alignment.MID, 2f);
		tooltip.addPara("Increase the maximum amount of s-mods supported by this ship by one with each level", 2f);

		if (currentTier > 0) {
			tooltip.addSectionHeading("CURRENT LEVEL: "+currentTier, Color.CYAN, header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
			switch (currentTier) {
				case 1: {
					tooltip.addImages(
						tooltip.getWidthSoFar(), 32f, 0f, 10f,
						Global.getSettings().getCommoditySpec("gamma_core").getIconName()
					);
					break;
				} case 2: {
					tooltip.addImages(
						tooltip.getWidthSoFar(), 32f, 0f, 10f,
						Global.getSettings().getCommoditySpec("gamma_core").getIconName(),
						Global.getSettings().getCommoditySpec("beta_core").getIconName()
					);
					break;
				} case 3: case 4: {
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

		if (!(currentTier < this.upgrade.getMaxTier(variant.getHullSize()))) {
			tooltip.addSectionHeading("MAX LEVEL", Color.CYAN, header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
			return;
		}

		if (this.upgrade.canUpgradeTier(variant)) {
			tooltip.addSectionHeading("UPGRADE REQUIREMENTS", Misc.getHighlightColor(), header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
		} else {
			tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", Misc.getNegativeHighlightColor(), header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
		}

		for (lyr_upgradeLayer upgradeLayer : this.upgrade.getUpgradeLayers(variant.getHullSize())) {
			int upgradeTier = upgradeLayer.getTier();

			if (upgradeTier > currentTier) upgradeLayer.addRequirementsToTooltip(tooltip, upgradeTier != currentTier+1);	// skip purchased tiers, colourize next tier, desaturate rest
		}

		if (this.upgrade.canUpgradeTier(variant)) {
			tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", Misc.getPositiveHighlightColor(), header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
			tooltip.addPara("Any special item requirements will not be consumed, while the rest will be", 2f);
		}
	}
}