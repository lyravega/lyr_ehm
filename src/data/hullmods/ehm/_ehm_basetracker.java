package data.hullmods.ehm;

import static lyr.misc.lyr_internals.logPrefix;
import static lyr.misc.lyr_internals.events.onEnhance;
import static lyr.misc.lyr_internals.events.onInstall;
import static lyr.misc.lyr_internals.events.onNormalize;
import static lyr.misc.lyr_internals.events.onRemove;
import static lyr.misc.lyr_internals.events.onRestore;
import static lyr.misc.lyr_internals.events.onSuppress;
import static lyr.tools._lyr_uiTools.isRefitTab;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods._ehm_base;
import data.hullmods.ehm.events.enhancedEvents;
import data.hullmods.ehm.events.normalEvents;
import data.hullmods.ehm.events.suppressedEvents;

/**
 * Provides tools to track any change on a ship, mainly the hull modifications. When a change is
 * detected, will look if the hull modification has a registered handler for the event.
 * <p> If the event has a handler, then additional actions will be taken depending on how the
 * handler is set-up. In addition to some pre-defined actions, custom methods can be executed.
* @see {@link ehm_base Main Modification} Enables & initializes tracking features when installed
* @see {@link _ehm_eventhandler Event Handler} Used by the modifications to register the events
* @see {@link normalEvents Event Method Interface} Implemented by the hull modifications
 * @author lyravega
 */
public class _ehm_basetracker extends _ehm_base {
	/**
	 * Initializes ship tracking in refit tab to detects hullmod changes
	 * @param ship to track
	 */
	protected static void ehm_trackShip(ShipAPI ship) {
		if (isRefitTab()) shipTrackerScript(ship).setVariant(ship.getVariant());
	}

	protected static void ehm_trackShip(MutableShipStatsAPI stats) {
		if (!isRefitTab()) return;

		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();

			shipTrackerScript(ship).setVariant(stats.getVariant());
		}
	}

	protected static void ehm_stopTracking(ShipAPI ship) {
		if (isRefitTab()) shipTrackerScript(ship).cleanup();
	}
	
	protected static void ehm_stopTracking(MutableShipStatsAPI stats) {
		if (!isRefitTab()) return;

		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();

			shipTrackerScript(ship).cleanup();
		}
	}

	// hull modification effects that implement any of the event interfaces are stored in these maps
	public static final Map<String, normalEvents> normalEvents = new HashMap<String, normalEvents>();
	public static final Map<String, enhancedEvents> enhancedEvents = new HashMap<String, enhancedEvents>();
	public static final Map<String, suppressedEvents> suppressedEvents = new HashMap<String, suppressedEvents>();

	private static void onEvent(String eventName, ShipVariantAPI variant, String hullModId) {
		switch (eventName) {
			case onInstall:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onInstall(variant); return;
			case onRemove:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onRemove(variant); return;
			case onEnhance:		if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onEnhance(variant); return;
			case onNormalize:	if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onNormalize(variant); return;
			case onSuppress:	if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onSuppress(variant); return;
			case onRestore:		if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onRestore(variant); return;
			default: return;
		}
	}
	
	//#region INNER CLASS: fleetTrackerScript
	/**
	 * Creates and assigns a {@link fleetTrackerScript}, then returns it. If a
	 * fleet tracker already exists, returns it instead.
	 * @return a {@link fleetTrackerScript} script
	 * @see Callers: {@link #shipTrackerScript(ShipVariantAPI, String)} 
	 */
	protected static fleetTrackerScript fleetTrackerScript() {
		fleetTrackerScript fleetTracker = null;

		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof fleetTrackerScript) {
				fleetTracker = (fleetTrackerScript) script; break; // find the fleet script
			}
		}

		return (fleetTracker == null) ? new fleetTrackerScript() : fleetTracker;
	}

	/**
	 * An inner class with only one purpose: print logging messages. Completely
	 * redundant, but can be made to support other things. Created through the
	 * {@link #fleetTrackerScript()} method that checks for existing trackers
	 * beforehand.
	 */
	private static class fleetTrackerScript implements EveryFrameScriptWithCleanup {
		private Map<String, shipTrackerScript> shipTrackers = new HashMap<String, shipTrackerScript>();
		// private Map<String, FleetMemberAPI> fleetMembers = new HashMap<String, FleetMemberAPI> ();
		// private CampaignFleet playerFleet = (CampaignFleet) Global.getSector().getPlayerFleet();
		private boolean isDone = false;
		private float runTime = 0f;
		
		//#region CONSTRUCTORS & ACCESSORS
		public fleetTrackerScript() {
			// for (FleetMemberAPI fleetMember: Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			// 	fleetMembers.put(fleetMember.getId(), fleetMember);
			// }

			if (log) logger.info(logPrefix+"FT: Fleet Tracker initialized");
			
			Global.getSector().addScript(this);
		}
	
		public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
			shipTrackers.put(memberId, shipTracker);
			if (log) logger.info(logPrefix+"FT: Ship Tracker ST-"+memberId+" starting");
		}
	
		public void removeShipTracker(String memberId) {
			shipTrackers.remove(memberId);
			if (log) logger.info(logPrefix+"FT: Ship Tracker ST-"+memberId+" stopping");
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
			isDone = true;
		}
	}
	//#endregion
	// END OF INNER CLASS: fleetTrackerScript

	//#region INNER CLASS: shipTrackerScript
	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns the 
	 * {@link shipTrackerScript} that is unique to the ship. If a trackers already exists,
	 * returns that one.
	 * @param variant of the ship to track
	 * @param memberId of the ship to track
	 * @return a {@link shipTrackerScript}
	 */
	protected static shipTrackerScript shipTrackerScript(ShipAPI ship) {
		shipTrackerScript shipTracker = null;
		ShipVariantAPI variant = ship.getVariant();
		String memberId = ship.getFleetMemberId();

		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof shipTrackerScript) {
				shipTrackerScript temp = (shipTrackerScript) script; 
				if (!temp.getMemberId().equals(memberId)) continue;
					
				shipTracker = (shipTrackerScript) script; break;
			}
		}

		return (shipTracker == null) ? new shipTrackerScript(variant, memberId, fleetTrackerScript()) : shipTracker;
	}

	/**
	 * An inner class with only one purpose: cache the variant, report any changes.
	 * <p>Created through {@link #shipTrackerScript(ShipAPI)} method that checks 
	 * for existing trackers beforehand
	 * onRemoved()}
	 */
	private static class shipTrackerScript implements EveryFrameScriptWithCleanup {
		private fleetTrackerScript fleetTracker = null;
		private ShipVariantAPI variant = null;
		private String memberId = null;
		private Set<String> hullMods = new HashSet<String>();
		private Set<String> enhancedMods = new HashSet<String>();
		private Set<String> suppressedMods = new HashSet<String>();
		// private Set<String> weapons = new HashSet<String>();
		private Iterator<String> iterator;
		private boolean isDone = false;
		
		//#region CONSTRUCTORS & ACCESSORS
		public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize / a year later, I have no idea what I mean by this
			this.variant = variant;
		}
		
		public shipTrackerScript(ShipVariantAPI variant, String memberId, fleetTrackerScript fleetTracker) {
			fleetTracker.addshipTracker(memberId, this);

			this.fleetTracker = fleetTracker;
			this.variant = variant;
			this.memberId = memberId;
			this.hullMods.addAll(variant.getHullMods());
			this.enhancedMods.addAll(variant.getSMods());
			this.suppressedMods.addAll(variant.getSuppressedMods());
			// this.weapons.addAll(variant.getFittedWeaponSlots());
			
			Global.getSector().addScript(this);
	
			if (log) logger.info(logPrefix+"ST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
		}
	
		public String getMemberId() {
			return this.memberId;
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {
			if (!isRefitTab()) { cleanup(); return; }

			checkHullMods();
			checkEnhancedMods();
			checkSuppressedMods();
		}

		private void checkHullMods() {
			for (String hullModId : variant.getHullMods()) {
				if (hullMods.contains(hullModId)) continue;
	
				if (log) logger.info(logPrefix+"ST-"+memberId+": Installed '"+hullModId+"'");
				hullMods.add(hullModId); onEvent(onInstall, variant, hullModId);
			}

			for (iterator = hullMods.iterator(); iterator.hasNext();) { String hullModId = iterator.next();
				if (variant.hasHullMod(hullModId)) continue;

				if (log) logger.info(logPrefix+"ST-"+memberId+": Removed '"+hullModId+"'");
				iterator.remove(); onEvent(onRemove, variant, hullModId);
			}
		}

		private void checkEnhancedMods() {
			for (String hullModId : variant.getSMods()) {
				if (enhancedMods.contains(hullModId)) continue;
	
				if (log) logger.info(logPrefix+"ST-"+memberId+": Enhanced '"+hullModId+"'");
				enhancedMods.add(hullModId); onEvent(onEnhance, variant, hullModId);
			}

			for (iterator = enhancedMods.iterator(); iterator.hasNext();) { String hullModId = iterator.next();
				if (variant.getSMods().contains(hullModId)) continue;
	
				if (log) logger.info(logPrefix+"ST-"+memberId+": Normalized '"+hullModId+"'");
				iterator.remove(); onEvent(onNormalize, variant, hullModId);
			}
		}

		private void checkSuppressedMods() {
			for (String hullModId : variant.getSuppressedMods()) {
				if (suppressedMods.contains(hullModId)) continue;
	
				if (log) logger.info(logPrefix+"ST-"+memberId+": Suppressed '"+hullModId+"'");
				suppressedMods.add(hullModId); onEvent(onSuppress, variant, hullModId);
			}

			for (iterator = suppressedMods.iterator(); iterator.hasNext();) { String hullModId = iterator.next();
				if (variant.getSMods().contains(hullModId)) continue;
	
				if (log) logger.info(logPrefix+"ST-"+memberId+": Restored '"+hullModId+"'");
				iterator.remove(); onEvent(onRestore, variant, hullModId);
			}
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
			this.fleetTracker.removeShipTracker(memberId);
			this.isDone = true;
		}
	}
	//#endregion
	// END OF INNER CLASS: shipTrackerScript
}