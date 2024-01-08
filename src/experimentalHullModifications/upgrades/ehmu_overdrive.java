package experimentalHullModifications.upgrades;

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

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.plugin.lyr_ehm;
import lunalib.lunaRefit.BaseRefitButton;
import lyravega.misc._lyr_upgradeEffect;
import lyravega.misc.lyr_upgrade;
import lyravega.utilities.lyr_interfaceUtilities;
import lyravega.utilities.lyr_tooltipUtilities.colour;

public class ehmu_overdrive extends BaseRefitButton implements _lyr_upgradeEffect {
	private final lyr_upgrade upgrade;
	private final String upgradeId = ehm_internals.upgrades.overdrive;

	public ehmu_overdrive() {
		final String gc = "gamma_core";
		final String bc = "beta_core";
		final String ac = "alpha_core";
		final String cn = "corrupted_nanoforge";
		final String pn = "pristine_nanoforge";

		this.upgrade = new lyr_upgrade(this.upgradeId, "Overdrive");

		this.upgrade.addUpgradeLayer(HullSize.FRIGATE, new Object[][]{{gc, 3}}, null, 1, null);
		this.upgrade.addUpgradeLayer(HullSize.FRIGATE, new Object[][]{{gc, 5}, {bc, 2}}, null, 1, null);
		this.upgrade.addUpgradeLayer(HullSize.FRIGATE, new Object[][]{{gc, 8}, {bc, 3}}, new String[]{cn}, 2, null);
		this.upgrade.addUpgradeLayer(HullSize.FRIGATE, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{pn}, 3, null);

		this.upgrade.addUpgradeLayer(HullSize.DESTROYER, new Object[][]{{gc, 3}}, null, 2, null);
		this.upgrade.addUpgradeLayer(HullSize.DESTROYER, new Object[][]{{gc, 5}, {bc, 2}}, null, 2, null);
		this.upgrade.addUpgradeLayer(HullSize.DESTROYER, new Object[][]{{gc, 8}, {bc, 3}}, new String[]{cn}, 3, null);
		this.upgrade.addUpgradeLayer(HullSize.DESTROYER, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{pn}, 4, null);

		this.upgrade.addUpgradeLayer(HullSize.CRUISER, new Object[][]{{gc, 5}}, null, 3, null);
		this.upgrade.addUpgradeLayer(HullSize.CRUISER, new Object[][]{{gc, 8}, {bc, 3}}, null, 3, null);
		this.upgrade.addUpgradeLayer(HullSize.CRUISER, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{cn}, 4, null);
		this.upgrade.addUpgradeLayer(HullSize.CRUISER, new Object[][]{{gc, 21}, {bc, 8}, {ac, 2}}, new String[]{pn}, 5, null);

		this.upgrade.addUpgradeLayer(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 5}}, null, 5, null);
		this.upgrade.addUpgradeLayer(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 8}, {bc, 3}}, null, 5, null);
		this.upgrade.addUpgradeLayer(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{cn}, 6, null);
		this.upgrade.addUpgradeLayer(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 21}, {bc, 8}, {ac, 2}}, new String[]{pn}, 7, null);
	}

	@Override
	public String getUpgradeId() {
		return this.upgradeId;
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
	public void applyUpgradeEffect(MutableShipStatsAPI stats, Integer effectTier) {
		if (effectTier == null) effectTier = this.upgrade.getCurrentTier(stats.getVariant());	// if effect tier is not passed for whatever reason, search the variant

		stats.getDynamic().getMod(this.upgradeId).modifyFlat(this.upgradeId, effectTier);	// for tooltip
		stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(this.upgradeId, effectTier);	// in this upgrade's case, effectTier directly translates to flat mod
	}

	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return this.upgrade.getCurrentLayerName(variant);
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "data/graphics/experimental_smol.png";
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return !lyr_ehm.lunaSettings.getCosmeticsOnly() && variant.hasHullMod(ehm_internals.hullmods.main.base);
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
		lyr_interfaceUtilities.playDrillSound();
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public float getToolipWidth(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return 400.0f;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		final int currentTier = this.upgrade.getCurrentTier(variant);

		tooltip.addSectionHeading("OVERDRIVE", colour.button, header.invisible_bgColour, Alignment.MID, 2f);
		tooltip.addPara("Increase the maximum amount of s-mods supported by this ship by one with each tier", 2f);

		if (currentTier > 0) {
			tooltip.addSectionHeading("CURRENT TIER: "+currentTier, colour.button, header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
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

		this.upgrade.addAllRequirementsToTooltip(variant, tooltip, 2f, 2f);
	}
}