package experimentalHullModifications.hullmods;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.proxies.ehm_hullSpec;
import lyravega.listeners.events.normalEvents;
import lyravega.utilities.logger.lyr_logger;

public class _ehm_test extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
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
			if (!spec.hasTag(Tags.HULLMOD_DMOD)) continue;

			variant.addPermaMod(spec.getId(), false); return;
		}
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) { try {
		if (!ShipAPI.class.isInstance(stats.getEntity())) return;
		ShipAPI ship = ShipAPI.class.cast(stats.getEntity());

		FleetMemberAPI fleetMember = stats.getFleetMember();
		Vector2f shieldCenterEvenIfNoShield = ship.getShieldCenterEvenIfNoShield();

		// below are related to sprite spec, and here in this beforeShipCreation block, it haven't been applied yet
		// float collisionRadius = ship.getCollisionRadius();
		// Vector2f shipCenter = new Vector2f(ship.getSpriteAPI().getCenterX(), ship.getSpriteAPI().getCenterY());

		ship = ship;

		// int bays = ship.getLaunchBaysCopy().size();
		// float cost = getRateCost(bays);
	} catch (Throwable t ) { lyr_logger.warn("Test fail in 'applyEffectsBeforeShipCreation()'", t);	}}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) { try {
		FleetMemberAPI fleetMemberStats = ship.getMutableStats().getFleetMember();
		FleetMemberAPI fleetMember = ship.getFleetMember();
		Vector2f shieldCenterEvenIfNoShield = ship.getShieldCenterEvenIfNoShield();

		float collisionRadius = ship.getCollisionRadius();
		Vector2f shipCenter = new Vector2f(ship.getSpriteAPI().getCenterX(), ship.getSpriteAPI().getCenterY());
		Object spriteSpec = new ehm_hullSpec(ship.getHullSpec(), false).getSpriteSpec();

		ship = ship;

		List<ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();
		List<Vector2f> shipEngineLocations = new ArrayList<>();

		for (ShipEngineAPI engine : shipEngines) {
			shipEngineLocations.add(engine.getLocation());
		}

		ship = ship;

		// ShipSystemAPI phaseCloak = ship.getPhaseCloak();
		// if (phaseCloak != null) phaseCloak.getSpecAPI().addTag("uses_damper_ai");

		// fighter test shit
		// List<FighterWingAPI> allWings = ship.getAllWings();

		List<String> wings = ship.getVariant().getWings();	// launch bays are empty here
		List<FighterLaunchBayAPI> launchBays = ship.getLaunchBaysCopy();	// so using variant data instead

		final int extraFighters = 5;
		int bayIndex = 0;
		for (String wingId : wings) {
			if (wingId.isEmpty()) continue;

			FighterLaunchBayAPI bay = launchBays.get(bayIndex);
			FighterWingSpecAPI wingSpec = Global.getSettings().getFighterWingSpec(wingId);

			// bay.makeCurrentIntervalFast();
			// bay.setFastReplacements(bay.getFastReplacements()+wingSpec.getNumFighters()+extraFighters);
			// bay.setExtraDeployments(wingSpec.getNumFighters()+extraFighters);
			// bay.setExtraDeployments(100);	// this works like a bank; these are used constantly and drain over time, better to give just one in advance
			bay.setExtraDeploymentLimit(wingSpec.getNumFighters()+extraFighters);
			bay.setExtraDuration(3600f);
			bayIndex++;
		}
	} catch (Throwable t ) { lyr_logger.warn("Test fail in 'applyEffectsAfterShipCreation()'", t);	}}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String hullModSpecId) { try {

	} catch (Throwable t ) { lyr_logger.warn("Test fail in 'applyEffectsToFighterSpawnedByShip()'", t);	}}

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		List<ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();
		List<Vector2f> shipEngineLocations = new ArrayList<>();

		for (ShipEngineAPI engine : shipEngines) {
			shipEngineLocations.add(engine.getLocation());
		}

		Vector2f shipLocation = ship.getLocation();

		ship = ship;

		// fighter test shit
		List<String> wings = ship.getVariant().getWings();
		List<FighterLaunchBayAPI> launchBays = ship.getLaunchBaysCopy();

		final int extraFighters = 5;
		// for (String wingId : wings) {
		for (FighterLaunchBayAPI bay : launchBays) {
			if (bay.getWing() == null) continue;

			FighterWingAPI wing = bay.getWing();
			FighterWingSpecAPI wingSpec = wing.getSpec();
			final int targetSize = wingSpec.getNumFighters()+extraFighters;
			final int currentSize = wing.getWingMembers().size();

			// bay.makeCurrentIntervalFast();
			// bay.setFastReplacements(bay.getFastReplacements()+extraFighters);
			// bay.setExtraDeployments(wingSpec.getNumFighters()+extraFighters);
			if (currentSize < targetSize) bay.setExtraDeployments(1);	// give an extra fighter to deploy
			else bay.makeCurrentIntervalFast();	// this is a fix; even though the target is achieved, bay will keep itself busy for another cycle
			// bay.setExtraDeploymentLimit(targetFighters);
			// bay.setExtraDuration(3600f);

			int extraDeployments = bay.getExtraDeployments();
			int extraDeploymentLimit = bay.getExtraDeploymentLimit();
			float extraDuration = bay.getExtraDuration();

			bay = bay;
		}
	}

	@Override
	public boolean affectsOPCosts() {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		// super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		List<ShipEngineAPI> shipEngines = ship.getEngineController().getShipEngines();

		ship = ship;
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
