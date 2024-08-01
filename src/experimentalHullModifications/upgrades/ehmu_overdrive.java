package experimentalHullModifications.upgrades;

import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.highlightText;
import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.storyText;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.upgrades.lyr_upgrade;
import lyravega.utilities.lyr_tooltipUtilities;
import lyravega.utilities.lyr_tooltipUtilities.colour;

public class ehmu_overdrive extends lyr_upgrade {
	public ehmu_overdrive() {
		super(ehm_internals.upgrades.overdrive, "Overdrive");

		final String gc = "gamma_core";
		final String bc = "beta_core";
		final String ac = "alpha_core";
		final String cn = "corrupted_nanoforge";
		final String pn = "pristine_nanoforge";

		// this = new lyr_upgrade(thisId, "Overdrive");

		this.addTierSpec(HullSize.FRIGATE, new Object[][]{{gc, 3}}, null, 1, null);
		this.addTierSpec(HullSize.FRIGATE, new Object[][]{{gc, 5}, {bc, 2}}, null, 1, null);
		this.addTierSpec(HullSize.FRIGATE, new Object[][]{{gc, 8}, {bc, 3}}, new String[]{cn}, 2, null);
		this.addTierSpec(HullSize.FRIGATE, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{pn}, 3, null);

		this.addTierSpec(HullSize.DESTROYER, new Object[][]{{gc, 3}}, null, 2, null);
		this.addTierSpec(HullSize.DESTROYER, new Object[][]{{gc, 5}, {bc, 2}}, null, 2, null);
		this.addTierSpec(HullSize.DESTROYER, new Object[][]{{gc, 8}, {bc, 3}}, new String[]{cn}, 3, null);
		this.addTierSpec(HullSize.DESTROYER, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{pn}, 4, null);

		this.addTierSpec(HullSize.CRUISER, new Object[][]{{gc, 5}}, null, 3, null);
		this.addTierSpec(HullSize.CRUISER, new Object[][]{{gc, 8}, {bc, 3}}, null, 3, null);
		this.addTierSpec(HullSize.CRUISER, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{cn}, 4, null);
		this.addTierSpec(HullSize.CRUISER, new Object[][]{{gc, 21}, {bc, 8}, {ac, 2}}, new String[]{pn}, 5, null);

		this.addTierSpec(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 5}}, null, 5, null);
		this.addTierSpec(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 8}, {bc, 3}}, null, 5, null);
		this.addTierSpec(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 13}, {bc, 5}, {ac, 1}}, new String[]{cn}, 6, null);
		this.addTierSpec(HullSize.CAPITAL_SHIP, new Object[][]{{gc, 21}, {bc, 8}, {ac, 2}}, new String[]{pn}, 7, null);
	}

	@Override
	public void applyUpgradeEffect(MutableShipStatsAPI stats) {
		if (lyr_ehm.lunaSettings.getCosmeticsOnly()) return;

		stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(this.getUpgradeId(), this.getTierSpec(stats).getTier());	// in this upgrade's case, effectTier directly translates to flat mod
	}

	@Override
	public void addUpgradeShortDescription(MutableShipStatsAPI stats, TooltipMakerAPI tooltip, float padding) {
		final int upgradeTier = this.getTierSpec(stats).getTier();

		lyr_tooltipUtilities.addColourizedPara(tooltip, highlightText("Overdrive, Tier "+upgradeTier)+": Increases s-mod capacity by "+storyText(upgradeTier+""), text.padding);
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return !lyr_ehm.lunaSettings.getCosmeticsOnly() && variant.hasHullMod(ehm_internals.hullmods.main.base);
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "data/graphics/experimental_smol.png";
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		final int currentTier = this.getTierSpec(member.getStats()).getTier();

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

		this.addUpgradeRequirementsToTooltip(member, variant, tooltip, 2f, 2f);
	}
}