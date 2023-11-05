package lyravega.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
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
	private static test instance;

	public static test instance() {
		return instance;
	}

	// public static int getOverdriveLevel(ShipVariantAPI variant) {
	// 	return instance.getLevel(variant, false);
	// }

	private final String gammaCoreIconName;
	private final String betaCoreIconName;
	private final String alphaCoreIconName;
	private final lyr_upgrade upgrade;

	public test() {
		instance = this;

		this.gammaCoreIconName = Global.getSettings().getCommoditySpec("gamma_core").getIconName();
		this.betaCoreIconName = Global.getSettings().getCommoditySpec("beta_core").getIconName();
		this.alphaCoreIconName = Global.getSettings().getCommoditySpec("alpha_core").getIconName();

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
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
		final int overdriveLevel = this.upgrade.getCurrentTier(variant);
		final boolean isOverdriveCapped = !(overdriveLevel < this.upgrade.getMaxTier(variant.getHullSize()));
		final int storyPoints = Global.getSector().getPlayerStats().getStoryPoints();

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

			int storyPointCost = upgradeLayer.getStoryPointCost();
			boolean hasCoreRequirement = upgradeLayer.getCommodityCosts() != null;
			Entry<String, Integer> coreRequirement = hasCoreRequirement ? upgradeLayer.getCommodityCosts().entrySet().iterator().next() : null;

			Object[] obj = {
				Color.YELLOW, "Level "+upgradeTier,
				null, ": ",
				Color.GREEN, storyPointCost+" SP",
				null, hasCoreRequirement ? "&" : "",
				hasCoreRequirement ? (playerCargo.getCommodityQuantity(coreRequirement.getKey()) >= coreRequirement.getValue() ? Color.GREEN : Color.RED) : null, hasCoreRequirement ? coreRequirement.getValue()+" "+Global.getSettings().getCommoditySpec(coreRequirement.getKey()).getName() : "",
			};
			this.testsss(tooltip, Color.WHITE, obj);

			// LabelAPI req = tooltip.addPara(
			// 	"%s: %s %s %s",
			// 	2f,
			// 	Color.WHITE,
			// 	"Level "+upgradeTier,
			// 	storyPointCost+" SP",
			// 	hasCoreRequirement ? "&" : "",
			// 	hasCoreRequirement ? coreRequirement.getValue()+" "+Global.getSettings().getCommoditySpec(coreRequirement.getKey()).getName() : ""
			// );

			// if (overdriveLevel+1 == upgradeTier) {
			// 	req.setHighlightColors(
			// 		Color.YELLOW,
			// 		storyPoints > storyPointCost ? Color.GREEN : Color.RED,
			// 		Color.WHITE,
			// 		hasCoreRequirement ? (playerCargo.getCommodityQuantity(coreRequirement.getKey()) >= coreRequirement.getValue() ? Color.GREEN : Color.RED) : null
			// 	);
			// } else {
			// 	req.setColor(Color.GRAY);
			// 	req.setHighlightColor(Color.GRAY);
			// }
		}

		if (!isOverdriveCapped) {
			if (this.upgrade.canUpgradeTier(variant))
				tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", Color.GREEN, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
			else
				tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", Color.RED, header.invisible_bgColour, Alignment.MID, 5f).flash(1f, 1f);
		}
	}

	private void testsss(TooltipMakerAPI tooltip, Color defaultColour, Object[] objects) {
		String format = "";
		ArrayList<String> replaceList = new ArrayList<String>();
		ArrayList<Color> colourList = new ArrayList<Color>();

		// jesus christ... where are Lua tables when you need them
		// why the fuck I can't just do 'array[array.length] = blah' to add something to the array

		for (Object o : objects) {
			if (o == null) colourList.add(defaultColour);
			else if (Color.class.isInstance(o)) colourList.add(Color.class.cast(o));
			else if (String.class.isInstance(o)) { replaceList.add(String.class.cast(o)); format = format+"%s "; }
		}

		tooltip.addPara(format, 0, colourList.toArray(new Color[]{}), replaceList.toArray(new String[]{}));
	}
}