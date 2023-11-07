package lyravega.listeners;

import static lyravega.utilities.lyr_interfaceUtilities.isRefitTab;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.VariantSource;

import lyravega.utilities.lyr_interfaceUtilities;
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

	private final String trackerModId = "lyr_tracker";
	final Map<String, lyr_shipTracker> shipTrackers = new HashMap<String, lyr_shipTracker>();
	final Map<String, FleetMemberAPI> fleetMembers = new HashMap<String, FleetMemberAPI>();

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

	@Override protected void delayedOnOpen() {
		lyr_interfaceUtilities.refreshShipDisplay();
	}

	@Override protected void onAdvance(float amount) {
		if (lyr_interfaceUtilities.clearUndoAfter) lyr_interfaceUtilities.clearUndoAfter();
	}

	private void addTracking(ShipVariantAPI variant, FleetMemberAPI member, String parentTrackerUUID) {
		// if (variant.hasTag(lyr_fleetTracker.uuid.prefix)) return;

		if (!variant.getPermaMods().contains(this.trackerModId))
			variant.addPermaMod(this.trackerModId, false);	// add before constructing tracker or this will be tracked too

		String shipTrackerUUID = this.getTrackerUUID(variant);
		if (shipTrackerUUID == null) shipTrackerUUID = UUID.randomUUID().toString();

		lyr_shipTracker shipTracker = this.getShipTracker(shipTrackerUUID);
		if (shipTracker == null) shipTracker = new lyr_shipTracker(this, variant, member, shipTrackerUUID, parentTrackerUUID);

		// if (!variant.hasTag(lyr_fleetTracker.uuid.prefix))
		// 	variant.addTag(lyr_fleetTracker.uuid.prefix);

		if (shipTrackerUUID != null && !variant.hasTag(lyr_fleetTracker.uuid.shipPrefix+shipTrackerUUID))
			variant.addTag(lyr_fleetTracker.uuid.shipPrefix+shipTrackerUUID);	// ship's uuid

		if (parentTrackerUUID != null && !variant.hasTag(lyr_fleetTracker.uuid.parentPrefix+parentTrackerUUID))
			variant.addTag(lyr_fleetTracker.uuid.parentPrefix+parentTrackerUUID);	// parent's uuid

		for (String moduleSlotId : variant.getStationModules().keySet()) {
			ShipVariantAPI moduleVariant = variant.getModuleVariant(moduleSlotId);
			ShipHullSpecAPI moduleHullSpec = moduleVariant.getHullSpec();

			if (moduleHullSpec.getOrdnancePoints(null) == 0) continue;	// vanilla first checks this then
			if (moduleHullSpec.hasTag(Tags.MODULE_UNSELECTABLE)) continue;	// this to identify unselectables

			if (moduleVariant.getSource() != VariantSource.REFIT) {
				moduleVariant = moduleVariant.clone();
				moduleVariant.setSource(VariantSource.REFIT);
				variant.setModuleVariant(moduleSlotId, moduleVariant);
			}

			this.addTracking(moduleVariant, null, shipTrackerUUID);
		}
	}

	private void removeTracking(ShipVariantAPI variant) {
		// if (!variant.hasTag(lyr_fleetTracker.uuid.prefix)) return;

		if (variant.getPermaMods().contains(this.trackerModId))
			variant.removePermaMod(this.trackerModId);

		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); )
			if (iterator.next().startsWith(lyr_fleetTracker.uuid.prefix)) iterator.remove();

		for (String moduleSlotId : variant.getStationModules().keySet()) {
			this.removeTracking(variant.getModuleVariant(moduleSlotId));
		}
	}

	private void updateFleetTracker() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy())
			if (this.getShipTracker(member) == null) this.addTracking(member.getVariant(), member, null);
	}

	private void terminateFleetTracker() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy())
			if (this.getShipTracker(member) != null) this.removeTracking(member.getVariant());

		this.shipTrackers.clear();
		this.fleetMembers.clear();
	}

	private String getTrackerUUID(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (tag.startsWith(lyr_fleetTracker.uuid.shipPrefix)) return tag.substring(lyr_fleetTracker.uuid.shipPrefix.length());
		};	return null;
	}

	//#region _lyr_abstractTracker IMPLEMENTATION
	@Override
	public lyr_shipTracker getShipTracker(FleetMemberAPI member) {
		return this.shipTrackers.get(this.getTrackerUUID(member.getVariant()));
	}

	@Override
	public lyr_shipTracker getShipTracker(ShipAPI ship) {
		return this.shipTrackers.get(this.getTrackerUUID(ship.getVariant()));
	}

	@Override
	public lyr_shipTracker getShipTracker(MutableShipStatsAPI stats) {
		return this.shipTrackers.get(this.getTrackerUUID(stats.getVariant()));
	}

	@Override
	public lyr_shipTracker getShipTracker(ShipVariantAPI variant) {
		return this.shipTrackers.get(this.getTrackerUUID(variant));
	}

	@Override
	public lyr_shipTracker getShipTracker(String trackerUUID) {
		return this.shipTrackers.get(trackerUUID);
	}

	/** @see {@link lyr_shipTracker#updateVariant(ShipVariantAPI)} */ @Override
	public void updateShipTracker(FleetMemberAPI member) {
		if (isRefitTab()) this.getShipTracker(member).updateVariant(member.getVariant());
	}

	/** @see {@link lyr_shipTracker#updateVariant(ShipVariantAPI)} */ @Override
	public void updateShipTracker(ShipAPI ship) {
		if (isRefitTab()) this.getShipTracker(ship).updateVariant(ship.getVariant());
	}

	/** @see {@link lyr_shipTracker#updateVariant(ShipVariantAPI)} */ @Override
	public void updateShipTracker(MutableShipStatsAPI stats) {
		if (isRefitTab() && ShipAPI.class.isInstance(stats.getEntity())) this.getShipTracker(stats).updateVariant(stats.getVariant());	// the cast check needs to be done because parts of the UI has outdated variant data unless it is a ShipAPI
	}

	/** @see {@link lyr_shipTracker#updateVariant(ShipVariantAPI)} */ @Override @Deprecated
	public void updateShipTracker(ShipVariantAPI variant) {
		if (isRefitTab()) this.getShipTracker(variant).updateVariant(variant);
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
			if (refitShip == null || !ship.getFleetMemberId().equals(refitShip.getFleetMemberId())) return;

			if (instance.getShipTracker(ship) == null) {	// there is an extremely rare case where this is null; the block below is a nuclear option to prevent it
				if (lyr_logger.getLevel() != lyr_levels.DEBUG) {
					lyr_logger.setLevel(lyr_levels.DEBUG); lyr_logger.debug("Lowering logger level to 'DEBUG'");
				};	lyr_logger.warn("FT: Tracker not found, initializing a temporary one");

				instance.addTracking(ship.getVariant(), null, null);
			}

			instance.updateShipTracker(ship);
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
