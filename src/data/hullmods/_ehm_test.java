package data.hullmods;

import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.playSound;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.hullmods.ehm.events.normalEvents;
import lyr.misc.lyr_internals;

public class _ehm_test extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_hullSpecReference(variant));
		commitChanges(); playSound();
	}

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public static final Logger logger = Logger.getLogger(lyr_internals.logName);

	@SuppressWarnings("unused")
	private static class hullInfo {
		private ShipHullSpecAPI hullSpec;
		private boolean isDHull_variant;
		private boolean isDHull;
		private boolean isDefaultDHull;
		private boolean isRestoreToBase;
		private String hullId;
		private String baseHullId;
		private String dParentHullId;

		private hullInfo(ShipVariantAPI variant) {
			this.hullSpec = variant.getHullSpec();
			this.isDHull_variant = variant.isDHull();
			this.isDHull = hullSpec.isDHull();
			this.isDefaultDHull = hullSpec.isDefaultDHull();
			this.isRestoreToBase = hullSpec.isRestoreToBase();
			this.hullId = hullSpec.getHullId();
			this.baseHullId = hullSpec.getBaseHullId();
			this.dParentHullId = hullSpec.getDParentHullId();
		}
	}

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
