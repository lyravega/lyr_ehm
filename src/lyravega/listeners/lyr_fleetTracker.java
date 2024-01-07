package lyravega.listeners;

import static lyravega.utilities.lyr_interfaceUtilities.isRefitTab;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.mission.FleetSide;

import experimentalHullModifications.misc.ehm_settings;
import lyravega.utilities.lyr_interfaceUtilities;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.logger.lyr_levels;
import lyravega.utilities.logger.lyr_logger;

/**
 * A tab listener class that implements several interfaces. There is a
 * method that triggers when the tabs are opened, but not when they are
 * closed so a script is initialized when a tab is opened to determine
 * when it is closed.
 * @author lyravega
 */
public final class lyr_fleetTracker extends _lyr_tabListener implements _lyr_abstractTracker {
	public static final class uuid {
		public static final String
			prefix = "UUID",
			shipPrefix = "UUID-",
			parentPrefix = "UUID+";
	}

	private static final lyr_fleetTracker instance = new lyr_fleetTracker();	//  if this is null and not instantiated before onGameLoad(), will yield a NPE as hullmod effects load earlier

	private lyr_fleetTracker() {
		super(CoreUITabId.REFIT);
	}

	public static _lyr_abstractTracker instance() {
		return instance;
	}

	public static void attach() {
		instance.detachListener();
		instance.attachListener(true);
	}

	private boolean isMirrorInitialized = false;
	final String trackerModId = "lyr_tracker";
	final Map<String, lyr_shipTracker> shipTrackers = new HashMap<String, lyr_shipTracker>();

	@Override
	protected void onOpen() {
		this.updateFleetTracker();

		lyr_logger.trackerInfo("FT: Fleet Tracker initialized");
	}

	@Override
	protected void onClose() {
		this.terminateFleetTracker();

		lyr_logger.trackerInfo("FT: Fleet Tracker terminated");
	}

	@Override protected void onOpenDelayed() {
		lyr_interfaceUtilities.refreshShipDisplay();	// this needs to be done for whatever reason, otherwise refit will show the old(er) variant
	}

	@Override protected void onAdvance(float amount) {
		if (lyr_interfaceUtilities.clearUndoAfter) lyr_interfaceUtilities.clearUndoAfter();

		if (Global.getCombatEngine().isInCampaignSim()) this.createMirrorOpponents();	// this might not be viable for everyone; there are a few frames when this is true before advance is paused
		else this.isMirrorInitialized = false;
	}

	@Override protected void onInterval() {}

	//#region ADVANCED SIMULATION SHIT
	private void createMirrorOpponents() {
		if (this.isMirrorInitialized) return; this.isMirrorInitialized = true;

		CombatFleetManagerAPI enemyFleetManager = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY);
		CombatFleetManagerAPI playerFleetManager = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER);

		if (ehm_settings.replaceSimWithMirrorFleet()) for (FleetMemberAPI member : enemyFleetManager.getReservesCopy()) {
			enemyFleetManager.removeFromReserves(member);
		}

		if (ehm_settings.assignMirrorFleetCommander()) enemyFleetManager.setDefaultCommander(Global.getSector().getPlayerPerson());
		enemyFleetManager.addToReserves(this.createSimMember(playerFleetManager.getDeployedCopy().iterator().next()));	// this is for the selected ship, which starts deployed
		for (FleetMemberAPI member : playerFleetManager.getReservesCopy()) {
			enemyFleetManager.addToReserves(this.createSimMember(member));
		}
	}

	private FleetMemberAPI createSimMember(FleetMemberAPI member) {
		FleetMemberAPI simMember = Global.getFactory().createFleetMember(member.getType(), member.getVariant());
		float mirrorFleetReadiness = ehm_settings.getMirrorFleetReadiness();

		simMember.setOwner(FleetSide.ENEMY.ordinal());
		simMember.getCrewComposition().addCrew(simMember.getNeededCrew());
		simMember.getRepairTracker().setCR(mirrorFleetReadiness == 0 ? member.getRepairTracker().getCR() : mirrorFleetReadiness);
		if (ehm_settings.assignMirrorFleetCaptains()) simMember.setCaptain(member.getCaptain());
		// simMember.setFlagship(true);	// needs investigation
		simMember.updateStats();

		return simMember;
	}
	//#endregion
	// END OF ADVANCED SIMULATION SHIT

	public void startTracking(FleetMemberAPI member, ShipVariantAPI variant) {
		new lyr_shipTracker(this, member, variant).registerTracker();
	}

	public void stopTracking(ShipVariantAPI variant) {
		this.getShipTracker(variant).unregisterTracker();
	}

	private void updateFleetTracker() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			lyr_shipTracker shipTracker = this.getShipTracker(member.getVariant());

			if (shipTracker != null) continue;

			shipTracker = new lyr_shipTracker(this, member, member.getVariant());
			shipTracker.registerTracker();
		}
	}

	private void terminateFleetTracker() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			lyr_shipTracker shipTracker = this.getShipTracker(member.getVariant());

			if (shipTracker == null) continue;

			shipTracker.unregisterTracker();
		}

		this.shipTrackers.clear();
	}

	private String getTrackerUUID(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (tag.startsWith(lyr_fleetTracker.uuid.shipPrefix)) return tag.substring(lyr_fleetTracker.uuid.shipPrefix.length());
		};	return null;
	}

	//#region _lyr_abstractTracker IMPLEMENTATION
	@Override
	public lyr_shipTracker getShipTracker(ShipVariantAPI variant) {
		return this.shipTrackers.get(this.getTrackerUUID(variant));
	}

	/** @see {@link lyr_shipTracker#updateStats(ShipVariantAPI)} */
	@Override
	public void updateShipTracker(FleetMemberAPI member) {
		if (isRefitTab()) this.getShipTracker(member.getVariant()).updateStats(member.getStats());
	}

	/** @see {@link lyr_shipTracker#updateStats(ShipVariantAPI)} */
	@Override
	public void updateShipTracker(ShipAPI ship) {
		if (isRefitTab()) this.getShipTracker(ship.getVariant()).updateStats(ship.getMutableStats());
	}

	/** @see {@link lyr_shipTracker#updateStats(ShipVariantAPI)} */
	@Override
	public void updateShipTracker(MutableShipStatsAPI stats) {
		if (isRefitTab() && ShipAPI.class.isInstance(stats.getEntity())) this.getShipTracker(stats.getVariant()).updateStats(stats);	// the cast check needs to be done because parts of the UI has outdated variant data unless it is a ShipAPI
	}
	//#endregion
	// END OF _lyr_abstractTracker IMPLEMENTATION

	public static class lyr_tracker extends BaseHullMod implements HullModFleetEffect {
		@Override public boolean withAdvanceInCampaign() { return false; }

		@Override public boolean withOnFleetSync() { return true; }

		@Override
		public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {}

		@Override
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ShipAPI refitShip = lyr_interfaceUtilities.getRefitShip();

			if (refitShip == null) return;
			if (ship.getFleetMemberId() != refitShip.getFleetMemberId()) return;
			if (ship.getVariant() != refitShip.getVariant()) return;	// this check here does the same as above but is more guarded against fake ships

			lyr_shipTracker shipTracker = instance.getShipTracker(ship.getVariant());

			if (shipTracker == null) {	// there is an extremely rare case where this is null; the block below is a nuclear option to prevent it
				if (lyr_logger.getLevel() != lyr_levels.DEBUG) {
					lyr_logger.setLevel(lyr_levels.DEBUG); lyr_logger.debug("Lowering logger level to 'DEBUG'");
				};	lyr_logger.warn("FT: Tracker not found, initializing a temporary one");

				shipTracker = new lyr_shipTracker(instance, null, ship.getVariant());
				shipTracker.registerTracker();
			}

			shipTracker.updateStats(ship.getMutableStats());
		}

		@Override public void advanceInCampaign(CampaignFleetAPI fleet) {}

		@Override
		public void onFleetSync(CampaignFleetAPI fleet) {
			if (!isRefitTab()) return;
			if (!fleet.equals(Global.getSector().getPlayerFleet())) return;

			instance.updateFleetTracker();	// ensures ships added while the refit tab is open (through console or something else) are tracked
		}
	}
}
