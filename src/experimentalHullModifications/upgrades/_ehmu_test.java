package experimentalHullModifications.upgrades;

import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.negativeText;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import lunalib.lunaRefit.BaseRefitButton;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.ui.lyr_refitTab;
import lyravega.proxies.ui.lyr_refitTab.lyr_parentData;
import lyravega.utilities.lyr_interfaceUtilities;
import lyravega.utilities.lyr_tooltipUtilities;
import lyravega.utilities.lyr_tooltipUtilities.colour;

public class _ehmu_test extends BaseRefitButton {
	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "Remove Module";
	}

	@Override
	public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
		return "data/graphics/experimental_smol.png";
	}

	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return variant.getHullSpec().getHullId().startsWith("ehm_module");
	}

	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		if (!variant.getNonBuiltInHullmods().isEmpty()) return false;
		if (!variant.getNonBuiltInWeaponSlots().isEmpty()) return false;
		if (!variant.getNonBuiltInWings().isEmpty()) return false;

		return true;
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		if (!event.isShiftDown()) return;

		lyr_parentData parentData = lyr_interfaceUtilities.getParentData();
		if (parentData == null) return;

		ShipVariantAPI parentVariant = parentData.getMember().getVariant();
		ShipHullSpecAPI parentHullSpecReference = Global.getSettings().getHullSpec(parentVariant.getHullSpec().getHullId());
		lyr_weaponSlot moduleSlot = new lyr_weaponSlot(parentData.getWeaponSlot());
		lyr_weaponSlot referenceSlot = new lyr_weaponSlot(parentHullSpecReference.getWeaponSlot(moduleSlot.getId()));
		String moduleSlotId = moduleSlot.getId();

		moduleSlot.setSlotType(referenceSlot.getSlotType());	// modules actually conceal a hidden weapon below them
		moduleSlot.setWeaponType(referenceSlot.getWeaponType());
		parentVariant.getStationModules().remove(moduleSlotId);	// actual necessary removal
		parentVariant.clearSlot(moduleSlotId);	// hidden weapon's OP cost is utilized instead of altering parent hull spec; clear to remove OP usage

		lyr_refitTab.proxify().goBackToParentIfNeeded();
		lyr_interfaceUtilities.playDrillSound();

		this.refreshVariant();
		this.refreshButtonList();
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
		tooltip.addSectionHeading("REMOVE MODULE", colour.button, header.invisible_bgColour, Alignment.MID, 2f);
		lyr_tooltipUtilities.addColourizedPara(tooltip, "Removes the mini-module from the parent ship. "+negativeText("This action is irreversible!"), 2f);

		if (this.isClickable(member, variant, market)) {
			tooltip.addSectionHeading("HOLD SHIFT & CLICK TO REMOVE", colour.positive, header.invisible_bgColour, Alignment.MID, 2f);
		} else {
			tooltip.addSectionHeading("MODULE NEEDS TO BE STRIPPED FIRST", colour.negative, header.invisible_bgColour, Alignment.MID, 2f);
		}
	}
}
