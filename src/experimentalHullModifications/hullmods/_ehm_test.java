package experimentalHullModifications.hullmods;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import lyravega.listeners.events.normalEvents;
import lyravega.utilities.logger.lyr_logger;

public class _ehm_test extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

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
			this.isDHull = this.hullSpec.isDHull();
			this.isDefaultDHull = this.hullSpec.isDefaultDHull();
			this.isRestoreToBase = this.hullSpec.isRestoreToBase();
			this.hullId = this.hullSpec.getHullId();
			this.baseHullId = this.hullSpec.getBaseHullId();
			this.dParentHullId = this.hullSpec.getDParentHullId();
		}
	}

	@SuppressWarnings("unused")
	private static void addDmods(ShipVariantAPI variant) {
		for (HullModSpecAPI spec : Global.getSettings().getAllHullModSpecs()) {
			if (spec.hasTag(Tags.HULLMOD_DMOD)) variant.addPermaMod(spec.getId(), false);
		}
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) { try {

	} catch (Throwable t ) { lyr_logger.warn("Test fail in 'applyEffectsBeforeShipCreation()'", t);	}}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) { try {

	} catch (Throwable t ) { lyr_logger.warn("Test fail in 'applyEffectsAfterShipCreation()'", t);	}}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String hullModSpecId) { try {

	} catch (Throwable t ) { lyr_logger.warn("Test fail in 'applyEffectsToFighterSpawnedByShip()'", t);	}}

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
		// super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return super.isApplicableToShip(ship);
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return true;
	}
}
