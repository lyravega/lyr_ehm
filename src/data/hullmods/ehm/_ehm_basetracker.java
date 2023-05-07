package data.hullmods.ehm;

import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.isRefitTab;
import static lyr.tools._lyr_uiTools.playSound;

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
import lyr.misc.lyr_internals;

/**
 * Provides tools to track any change on a ship, mainly the hull modifications. When a change is
 * detected, will look if the hull modification has a registered handler for the event.
 * <p> If the event has a handler, then additional actions will be taken depending on how the
 * handler is set-up. In addition to some pre-defined actions, custom methods can be executed.
* @see {@link ehm_base Main Modification} Enables & initializes tracking features when installed
* @see {@link _ehm_eventhandler Event Handler} Used by the modifications to register the events
* @see {@link _ehm_eventmethod Event Method Interface} Implemented by the hull modifications
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
		if (isRefitTab()) shipTrackerScript(ship).kill();
	}
	
	protected static void ehm_stopTracking(MutableShipStatsAPI stats) {
		if (!isRefitTab()) return;

		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();

			shipTrackerScript(ship).kill();
		}
	}

	public static Map<String, _ehm_eventhandler> registeredHullMods = new HashMap<String, _ehm_eventhandler>();

	/**
	 * If a change is detected in the ship's {@link shipTrackerScript}, this method is
	 * called. Executes installation actions if there are any for the new hullmods.
	 * @param variant of the ship
	 * @param newHullMods set of new hull mods
	 */
	static void onInstalled(ShipVariantAPI variant, Set<String> newHullMods) {
		String newHullModId;

		for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) {
			newHullModId = i.next();

			if (registeredHullMods.containsKey(newHullModId)) {
				registeredHullMods.get(newHullModId).executeOnInstall(variant);
			} else if (Global.getSettings().getHullModSpec(newHullModId).hasTag(lyr_internals.tag.externalAccess)) { 
				commitChanges(); playSound();
			}
		}
	}

	/**
	 * If a change is detected in the ship's {@link shipTrackerScript}, this method is
	 * called. Executes removal actions if there are any for the old hullmods.
	 * @param variant of the ship
	 * @param removedHullMods set of removed hull mods
	 * @throws Throwable
	 */
	static void onRemoved(ShipVariantAPI variant, Set<String> removedHullMods) {	
		String removedHullModId;

		for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) {
			removedHullModId = i.next(); 

			if (registeredHullMods.containsKey(removedHullModId)) {
				registeredHullMods.get(removedHullModId).executeOnRemove(variant);
			} else if (Global.getSettings().getHullModSpec(removedHullModId).hasTag(lyr_internals.tag.externalAccess)) {
				variant.setHullSpecAPI(ehm_hullSpecRefresh(variant)); commitChanges(); playSound();
			}
		}
	}

	/**
	 * If a change is detected in the ship's {@link shipTrackerScript}, this method is
	 * called. Executes sMod clean-up actions.
	 * @param variant of the ship
	 * @param removedSMods set of removed hull mods
	 * @throws Throwable
	 */
	static void onSModRemoved(ShipVariantAPI variant, Set<String> removedSMods) {	
		String removedSModId;

		for (Iterator<String> i = removedSMods.iterator(); i.hasNext();) {
			removedSModId = i.next(); 

			if (registeredHullMods.containsKey(removedSModId)) {
				registeredHullMods.get(removedSModId).executeSModCleanUp(variant);
			} else if (Global.getSettings().getHullModSpec(removedSModId).hasTag(lyr_internals.tag.externalAccess)) { 
				variant.setHullSpecAPI(ehm_hullSpecRefresh(variant)); commitChanges(); playSound();
			}
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

			if (log) logger.info(lyr_internals.logPrefix+"FT: Fleet Tracker initialized");
			
			Global.getSector().addScript(this);
		}
	
		public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
			shipTrackers.put(memberId, shipTracker);
			if (log) logger.info(lyr_internals.logPrefix+"FT: Ship Tracker ST-"+memberId+" starting");
		}
	
		public void removeShipTracker(String memberId) {
			shipTrackers.remove(memberId);
			if (log) logger.info(lyr_internals.logPrefix+"FT: Ship Tracker ST-"+memberId+" stopping");
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {	
			if (!isRefitTab() || shipTrackers.size() == 0) { kill(); return; }
	
			if (runTime > 30f) {
				runTime = 0f;
				if (log) logger.info(lyr_internals.logPrefix+"FT: Tracking "+shipTrackers.size()+" ships");
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
			if (log) logger.info(lyr_internals.logPrefix+"FT: Fleet Tracker terminated");
			shipTrackers.clear();
			isDone = true;
		}
	
		public void kill() {
			if (log) logger.info(lyr_internals.logPrefix+"FT: Fleet Tracker terminated");
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
	 * An inner class with only one purpose: cache the hullmods installed on the
	 * ship to provide a base for comparisons. Actual changes are handled on the
	 * {@link data.hullmods.ehm_base} class to keep things tidy.
	 * <p>Created through {@link #shipTrackerScript(ShipAPI)} method that checks 
	 * for existing trackers beforehand. Runs / used by the methods located in 
	 * the base hullmod {@link data.hullmods.ehm_base#onInstalled(ShipVariantAPI, Set)
	 * onInstalled()} and {@link data.hullmods.ehm_base#onRemoved(ShipVariantAPI, Set) 
	 * onRemoved()}
	 */
	private static class shipTrackerScript implements EveryFrameScriptWithCleanup {
		private fleetTrackerScript fleetTracker = null;
		private ShipVariantAPI variant = null;
		private String memberId = null;
		private Set<String> hullMods = new HashSet<String>();
		private Set<String> newHullMods = new HashSet<String>();
		private Set<String> removedHullMods = new HashSet<String>();
		private Set<String> sMods = new HashSet<String>();
		private Set<String> newSMods = new HashSet<String>();
		private Set<String> removedSMods = new HashSet<String>();
		// private Set<String> weapons = new HashSet<String>();
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
			this.sMods.addAll(variant.getSMods());
			// this.weapons.addAll(variant.getFittedWeaponSlots());
			
			Global.getSector().addScript(this);
	
			if (log) logger.info(lyr_internals.logPrefix+"ST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
		}
	
		public String getMemberId() {
			return this.memberId;
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {
			if (!isRefitTab()) { kill(); return; }
			
			for (String hullModId : variant.getHullMods()) {
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (hullMods.contains(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"ST-"+memberId+": New hull modification '"+hullModId+"'");
	
				newHullMods.add(hullModId);
			}

			for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (variant.hasHullMod(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"ST-"+memberId+": Removed hull modification '"+hullModId+"'");
	
				removedHullMods.add(hullModId);
			}
			
			if (!newHullMods.isEmpty()) {
				onInstalled(variant, newHullMods);
				hullMods.addAll(newHullMods);
				newHullMods.clear();
			}
			
			if (!removedHullMods.isEmpty()) {
				onRemoved(variant, removedHullMods);
				hullMods.removeAll(removedHullMods);
				removedHullMods.clear();
			}

			for (String hullModId : variant.getSMods()) {
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (sMods.contains(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"ST-"+memberId+": New hull s-modification '"+hullModId+"'");
	
				newSMods.add(hullModId);
			}

			for (Iterator<String> i = sMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (variant.getSMods().contains(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"ST-"+memberId+": Removed hull s-modification '"+hullModId+"'");
	
				removedSMods.add(hullModId);
			}
			
			if (!newSMods.isEmpty()) {
				// onInstalled(variant, newSMods, true);
				sMods.addAll(newSMods);
				newSMods.clear();
			}
			
			if (!removedSMods.isEmpty()) {
				onSModRemoved(variant, removedSMods);
				sMods.removeAll(removedSMods);
				removedSMods.clear();
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
	
		public void kill() {
			this.fleetTracker.removeShipTracker(memberId);
			this.isDone = true;
		}
	}
	//#endregion
	// END OF INNER CLASS: shipTrackerScript
}