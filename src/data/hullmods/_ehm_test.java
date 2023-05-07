package data.hullmods;

import java.util.List;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.loading.specs.O00o;
import com.fs.starfarer.loading.specs.Y;

import data.hullmods.ehm._ehm_eventhandler;
import data.hullmods.ehm._ehm_eventmethod;
import lyr.misc.lyr_internals;
import lyr.proxies.lyr_hullSpec;

public class _ehm_test extends _ehm_base implements _ehm_eventmethod {
	//#region LISTENER & EVENT REGISTRATION
	protected _ehm_eventhandler hullModEventHandler;

	@Override	// not used
	public void onInstall(ShipVariantAPI variant) {}

	@Override
	public void onRemove(ShipVariantAPI variant) {}

	@Override
	public void sModCleanUp(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecRefresh(variant));
	}

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		if (this.hullModEventHandler == null) {
			this.hullModEventHandler = new _ehm_eventhandler(this.hullModSpecId, this);
			hullModEventHandler.registerOnInstall(false, true, false);
			hullModEventHandler.registerOnRemove(false, true, false);
			hullModEventHandler.registerSModCleanUp(true, false, true);
		}
	}
	//#endregion
	// END OF LISTENER & EVENT REGISTRATION

	protected static final Logger logger = Logger.getLogger(lyr_internals.logName);
	private static final boolean log = true;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		lyr_hullSpec test2 = new lyr_hullSpec(stats.getVariant().getHullSpec(), true);
		ShipHullSpecAPI hullSpec = stats.getVariant().getHullSpec();
		test2 = test2;
		O00o test = (O00o) stats.getVariant().getHullSpec();
		test = test;

		Y class1 = test.getAllWeaponSlots().iterator().next();
		// if (variant.getSMods().contains(this.hullModSpecId)) {
		// 	lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(hullSpecAPI, false);
		// 	lyr_hullSpec.setOrdnancePoints((int) Math.round(ehm_hullSpecReference(variantAPI).getOrdnancePoints(null)*1.2));
		// 	variantAPI.setHullSpecAPI(lyr_hullSpec.retrieve());
		// } 

		test=test;
	}

	public static void onRemoved() {
		logger.info("Derp");
	}

	@Override
	public boolean affectsOPCosts() {
		return false;
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

	}

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {

	}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {

	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
			boolean isForModSpec) {
		// TODO Auto-generated method stub
		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		// TODO Auto-generated method stub
		return super.isApplicableToShip(ship);
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		// TODO Auto-generated method stub
		return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
	}

	// @Override
	// protected String ehm_unapplicableReason(ShipAPI ship) {
	// 	if (ship == null) return "Ship does not exist";

	// 	// if (!Global.getSector().getPlayerFleet().getFlagship().equals(ship.getFleetMember())) return "This ain't the flagship";

	// 	return null; 
	// }

	// @Override
	// protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
	// 	// HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());

	// 	// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

	// 	return null;
	// }
}
