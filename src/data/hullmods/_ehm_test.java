package data.hullmods;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.hullmods.ehm._ehm_eventhandler;
import data.hullmods.ehm._ehm_eventmethod;
import lyr.misc.lyr_internals;

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
			hullModEventHandler.registerOnInstall(true, true, true);
			hullModEventHandler.registerOnRemove(true, true, true);
			hullModEventHandler.registerSModCleanUp(true, true, true);
		}
	}
	//#endregion
	// END OF LISTENER & EVENT REGISTRATION

	public static final Logger logger = Logger.getLogger(lyr_internals.logName);

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) { try {

	} catch (Throwable t ) { logger.warn("Test fail in 'applyEffectsBeforeShipCreation()'", t);	}}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) { try {

	} catch (Throwable t ) { logger.warn("Test fail in 'applyEffectsAfterShipCreation()'", t);	}}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) { try {

	} catch (Throwable t ) { logger.warn("Test fail in 'applyEffectsToFighterSpawnedByShip()'", t);	}}

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {

	}

	@Override
	public boolean affectsOPCosts() {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return super.isApplicableToShip(ship);
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
	}
}
