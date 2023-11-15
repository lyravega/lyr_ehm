package experimentalHullModifications.upgrades;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import lunalib.lunaRefit.BaseRefitButton;
import lyravega.utilities.lyr_interfaceUtilities;

public class _ehmu_test extends BaseRefitButton {
	@Override
	public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) { try {
		if (!event.isShiftDown()) return;

		Global.getSettings().getWeaponSpec("ehm_module_shield").setOrdnancePointCost(20);

		// lyr_hullSpec hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		// lyr_shieldSpec shieldSpec = hullSpec.getShieldSpec();

		// g hs = (g) variant.getHullSpec();
		// OOOo ss = hs.getShieldSpec();

		// variant.setHullSpecAPI(hullSpec.retrieve());

		// this.refreshVariant();
		// this.refreshButtonList();
		lyr_interfaceUtilities.playDrillSound();
	} catch (Throwable t) {

	}}
}
