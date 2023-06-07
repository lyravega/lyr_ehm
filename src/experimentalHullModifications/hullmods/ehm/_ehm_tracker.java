package experimentalHullModifications.hullmods.ehm;

import static lyravega.tools.lyr_uiTools.isRefitTab;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import lyravega.listeners.lyr_shipTracker;

/**
 * Bridge class between the ship and trackers, providing utility methods.
 * <p> Actual tracking is done through the unique {@link lyr_shipTracker}
 * objects, which compares the refit variant to the its initial state.
 * <p> Every ship tracker is tracked through an every frame script {@link
 * fleetTrackerScript}, as it is necessary to keep the trackers outside
 * the ships since refit ships don't actually exist to have a listener
 * attached to them.
 * @see {@link ehm_base} base hull modification that enables tracking
 * @author lyravega
 */
public class _ehm_tracker extends _ehm_base {
	protected static void ehm_trackShip(ShipAPI ship) {
		if (!isRefitTab()) return;

		lyr_shipTracker shipTracker = findShipTracker(ship);

		if (shipTracker != null) shipTracker.updateVariant(ship.getVariant());
	}

	protected static void ehm_trackShip(MutableShipStatsAPI stats) {
		if (!isRefitTab()) return;

		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			lyr_shipTracker shipTracker = findShipTracker(ship);
	
			if (shipTracker != null) shipTracker.updateVariant(ship.getVariant());
		}
	}

	protected static void ehm_stopTracking(ShipAPI ship) {
		if (!isRefitTab()) return;

		stopShipTracker(ship);
	}
	
	protected static void ehm_stopTracking(MutableShipStatsAPI stats) {
		if (!isRefitTab()) return;

		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();

			stopShipTracker(ship);
		}
	}

	private static lyr_shipTracker findShipTracker(ShipAPI ship) {
		if (fleetTracker == null) fleetTracker = new fleetTrackerScript();

		return fleetTracker.getShipTracker(ship);
	}

	private static void stopShipTracker(ShipAPI ship) {
		if (fleetTracker != null) fleetTracker.removeShipTracker(ship);
	}
	
	//#region INNER CLASS: fleetTrackerScript
	private static fleetTrackerScript fleetTracker = null;

	/**
	 * An inner class that keeps track of members and trackers; linking refit
	 * ships with the real ones in a way.
	 */
	private static class fleetTrackerScript implements EveryFrameScriptWithCleanup {
		private Map<String, lyr_shipTracker> shipTrackers = new HashMap<String, lyr_shipTracker>();
		private boolean isDone = false;
		private float runTime = 0f;
		
		//#region CONSTRUCTORS & ACCESSORS
		public fleetTrackerScript() {
			if (log) logger.info(logPrefix+"FT: Fleet Tracker initialized");

			Global.getSector().addTransientScript(this);
		}

		private lyr_shipTracker getShipTracker(ShipAPI ship) {
			String memberId = ship.getFleetMemberId();
			lyr_shipTracker shipTracker = this.shipTrackers.get(memberId);

			if (shipTracker == null) {
				shipTracker = new lyr_shipTracker(ship);
	
				this.shipTrackers.put(memberId, shipTracker);
			}

			return shipTracker;
		}

		private void removeShipTracker(ShipAPI ship) {
			this.shipTrackers.remove(ship.getFleetMemberId());
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {	
			if (!isRefitTab() || shipTrackers.size() == 0) { cleanup(); return; }
	
			if (runTime > 30f) {
				runTime = 0f;
				if (log) logger.info(logPrefix+"FT: Tracking "+shipTrackers.size()+" ships");
			} runTime += amount;
		}
	
		@Override
		public boolean runWhilePaused() {
			return true;
		}
	
		@Override
		public boolean isDone() {
			return isDone;
		}
	
		@Override
		public void cleanup() {
			if (log) logger.info(logPrefix+"FT: Fleet Tracker terminated");
			shipTrackers.clear();
			shipTrackers = null;
			fleetTracker = null;
			
			isDone = true;
		}
	}
	//#endregion
	// END OF INNER CLASS: fleetTrackerScript
}