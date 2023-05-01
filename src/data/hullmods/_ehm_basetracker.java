package data.hullmods;

import static lyr.tools._lyr_reflectionTools.inspectMethod;
import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.isRefitTab;
import static lyr.tools._lyr_uiTools.playSound;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyr.misc.lyr_internals;
import lyr.tools._lyr_reflectionTools.methodMap;

/**
 * This class is used by primary hull modification, and contains relevant
 * methods, inner classes and whatnot to keep track of the new and old 
 * ones, and execute actions if the hull modification in question has any.
 * <p> Actions are only taken if they are registered with this classes'
 * {@link hullModEventListener}, for this mod it happens during initialization
 * {@link data.hullmods._ehm_base#init() init()} but might as well be done 
 * during initial game start-up.
 * <p> Basic actions include committing the changes on a variant and
 * refreshing the refit screen, and playing a sound after a hull modification
 * is added or removed. Advanced actions can be taken by providing a method
 * name, which are by default {@code onInstall()} and {@code onRemove()}.
 * <p> {@link data.hullmods._ehm_hullmodeventmethods} interface provides guidance
 * for any hull modification, however it is not necessary. As long as the
 * methods are public and they only take a variant as their argument, any
 * method can be used for more specific tasks.
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

	public static Map<String, hullModEventListener> registeredHullMods = new HashMap<String, hullModEventListener>();

	/**
	 * Inner class that stores listener specific data
	 */
	protected static class hullModEventListener {
		private String hullModId;
		private Object hullModObject;
		private event onRemove;
		private event onInstall;

		/**
		 * Inner class that stores event specific data
		 */
		private static class event {
			private static final int STATIC = 0x00000008;
			private boolean hasEventMethod;
			private String eventMethodName;
			private MethodHandle eventMethodHandle;
			private boolean isEventMethodStatic;
			private boolean commitChanges;
			private boolean playSound;
		
			/**
			 * Adds a new event to be used by the listener, with a custom methodName
			 * located in the hullMod's object/class to be invoked when the event occurs
			 * @param commitChanges whether the changes will be committed in the event
			 * @param playSound whether a sound will be played in the event
			 * @param eventMethodName name of the method to be invoked in the event
			 * @param hullModObject object of the hull modification for invocation purposes
			 */
			private event(boolean commitChanges, boolean playSound, String eventMethodName, Object hullModObject) {
				this.eventMethodName = eventMethodName;
				this.commitChanges = commitChanges;
				this.playSound = playSound;
				try {
					methodMap methodMap = inspectMethod(false, eventMethodName, hullModObject.getClass(), ShipVariantAPI.class);
					this.eventMethodHandle = methodMap.getMethodHandle();
					this.isEventMethodStatic = (methodMap.getModifiers() & STATIC) != 0;
					this.hasEventMethod = true;
				} catch (Throwable e) {
					this.hasEventMethod = false;
				}
			}

			/**
			 * Adds a new simpler event to be used by the listener
			 * @param commitChanges whether the changes will be committed in the event
			 * @param playSound whether a sound will be played in the event
			 */
			private event(boolean commitChanges, boolean playSound) {
				this.commitChanges = commitChanges;
				this.playSound = playSound;
				this.hasEventMethod = false;
			}
		
			private boolean hasEventMethod() { return this.hasEventMethod; }
			private String getEventMethodName() { return this.eventMethodName; }
			private MethodHandle getEventMethodHandle() { return this.eventMethodHandle; }
			private boolean isEventMethodStatic() { return this.isEventMethodStatic; }
			private boolean shouldCommitChanges() { return this.commitChanges; }
			private boolean shouldPlaySound() { return this.playSound; }
		}

		/**
		 * Creates a new listener and stores it among the others. {@link 
		 * #registerRemoveEvent()} and/or {@link #registerInstallEvent()}
		 * should be set/used right after initialization of this object. 
		 * <p> Change detection & event firing happens on the {@link
		 * data.hullmods.ehm_base base hullmod}, which uses {@link 
		 * data.hullmods._ehm_basetracker}. The events will be fired for 
		 * the hullMods only if they are registered.
		 * @param hullModId
		 * @param hullModObject
		 */
		protected hullModEventListener(String hullModId, Object hullModObject) {
			this.hullModId = hullModId;
			this.hullModObject = hullModObject; 
			registeredHullMods.put(hullModId, this);
		}

		/**
		 * Registers the remove event; what happens when this hull mod is
		 * removed from the variant on the refit screen. A custom method's
		 * name can be given to execute that method's actions after the
		 * removal.
		 * @param commitChanges changes will be committed and refit screen
		 * will be refreshed after removal
		 * @param playSound a basic sound will be played after removal
		 * @param onRemoveMethodName name of the method whose actions can be
		 * utilized, defaults to "onRemove", is completely optional
		 * @see data.hullmods.ehm_sr._ehm_sr_base#onRemove(ShipVariantAPI)
		 * @see #executeRemoveEvent(ShipVariantAPI)
		 */
		protected void registerRemoveEvent(boolean commitChanges, boolean playSound, String onRemoveMethodName) {
			this.onRemove = new event(commitChanges, playSound, onRemoveMethodName == null ? "onRemove" : onRemoveMethodName, this.hullModObject);
		}

		/**
		 * Registers the install event; what happens when this hull mod is
		 * removed to the variant on the refit screen.
		 * @param commitChanges changes will be committed and refit screen
		 * will be refreshed after removal
		 * @param playSound a basic sound will be played after removal
		 * @see #executeRemoveEvent(ShipVariantAPI)
		 */
		protected void registerRemoveEvent(boolean commitChanges, boolean playSound) {
			this.onRemove = new event(commitChanges, playSound);
		}

		/**
		 * Executes the stored "onRemove" event on the main method
		 * @param variant passed to the invoked method, if any
		 * @see #executeEvent() for the main method
		 */
		protected void executeRemoveEvent(ShipVariantAPI variant) {
			executeEvent(this.onRemove, variant);
		}

		/**
		 * Registers the install event; what happens when this hull mod is
		 * installed to the variant on the refit screen. A custom method's
		 * name can be given to execute that method's actions after the
		 * installation.
		 * @param commitChanges changes will be committed and refit screen
		 * will be refreshed after installation
		 * @param playSound a basic sound will be played after installation
		 * @param onInstallMethodName name of the method whose actions can be
		 * utilized, defaults to "onInstall", is completely optional
		 * @see data.hullmods.ehm_sr._ehm_sr_base#onInstall(ShipVariantAPI)
		 * @see #executeInstallEvent(ShipVariantAPI)
		 */
		protected void registerInstallEvent(boolean commitChanges, boolean playSound, String onInstallMethodName) {
			this.onInstall = new event(commitChanges, playSound, onInstallMethodName == null ? "onInstall" : onInstallMethodName, this.hullModObject);
		}

		/**
		 * Registers the install event; what happens when this hull mod is
		 * installed to the variant on the refit screen.
		 * @param commitChanges changes will be committed and refit screen
		 * will be refreshed after installation
		 * @param playSound a basic sound will be played after installation
		 * @see #executeInstallEvent(ShipVariantAPI)
		 */
		protected void registerInstallEvent(boolean commitChanges, boolean playSound) {
			this.onInstall = new event(commitChanges, playSound);
		}

		/**
		 * Executes the stored "onInstall" event on the main method
		 * @param variant passed to the invoked method, if any
		 * @see #executeEvent() for the main method
		 */
		protected void executeInstallEvent(ShipVariantAPI variant) {
			executeEvent(this.onInstall, variant);
		}

		/**
		 * Executes the received event ("onRemove" or "onInstall").
		 * <p> If there is a custom method, it will be invoked first. If any
		 * problems occur during this invocation, an error message will be
		 * displayed. Basic problems are, name mismatch, visibility and
		 * argument mismatch.
		 * <p> The method invocation somewhat puts a limitation on what can
		 * be called remotely, as the arguments and the method name must 
		 * match. As long as the name is correct, method only takes a
		 * variant as an argument, and it is visible (public), it should be
		 * invoked without any problems.
		 * <p> After the custom method, the pre-made ones will be executed
		 * if their value is registered as true. Refit screen will be
		 * refreshed and a sound effect will be played after the event. 
		 * @param event	either "onRemove" or "onInstall" (for now)
		 * @param variant of the ship that may be used in custom method
		 */
		protected void executeEvent(event event, ShipVariantAPI variant) {
			if (event.hasEventMethod()) {
				try {
					if (event.isEventMethodStatic()) event.getEventMethodHandle().invoke(variant);	// since static methods belong to the class, no need for an instancing object
					else event.getEventMethodHandle().invoke(hullModObject, variant);	// if the check fails, that means method is not static and requires an object
				} catch (Throwable t) {
					logger.error(lyr_internals.logPrefix+"Failure while trying to invoke '"+event.getEventMethodName()+"(...)' of the '"+this.hullModId+"'", t);
				}
			}
			if (event.shouldCommitChanges()) commitChanges();
			if (event.shouldPlaySound()) playSound();
		}
	}

	/**
	 * If a change is detected in the ship's {@link shipTrackerScript}, this method is
	 * called. Executes installation actions if there are any for the new hullmods.
	 * @param variant of the ship
	 * @param newHullMods set of new hull mods
	 */
	private static void onInstalled(ShipVariantAPI variant, Set<String> newHullMods) {
		String newHullModId;

		for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) {
			newHullModId = i.next(); 

			if (registeredHullMods.containsKey(newHullModId)) {
				registeredHullMods.get(newHullModId).executeInstallEvent(variant);
			} else if (Global.getSettings().getHullModSpec(newHullModId).hasTag(lyr_internals.tag.externalAccess)) { 
				commitChanges(); playSound(); break;
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
	private static void onRemoved(ShipVariantAPI variant, Set<String> removedHullMods) {	
		String removedHullModId;

		for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) {
			removedHullModId = i.next(); 

			if (registeredHullMods.containsKey(removedHullModId)) {
				registeredHullMods.get(removedHullModId).executeRemoveEvent(variant); break;
			} else if (Global.getSettings().getHullModSpec(removedHullModId).hasTag(lyr_internals.tag.externalAccess)) { 
				variant.setHullSpecAPI(ehm_hullSpecRefresh(variant)); commitChanges(); playSound(); break;
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
		// private Set<FleetMember> members = new HashSet<FleetMember>();
		// private CampaignFleet playerFleet = (CampaignFleet) Global.getSector().getPlayerFleet();
		private boolean isDone = false;
		private float runTime = 0f;
		
		//#region CONSTRUCTORS & ACCESSORS
		public fleetTrackerScript() {
			if (log) logger.info(lyr_internals.logPrefix+"xFT: Initialized fleet tracking");
			
			Global.getSector().addScript(this);
		}
	
		public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
			shipTrackers.put(memberId, shipTracker);
			if (log) logger.info(lyr_internals.logPrefix+"xFT: Keeping track of ST-"+memberId);
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {	
			if (!isRefitTab()) { if (log) logger.info(lyr_internals.logPrefix+"xFT: Stopping fleet tracking"); isDone = true; return; }
	
			if (runTime > 30f) {
				runTime = 0f;
				if (log) logger.info(lyr_internals.logPrefix+"xFT: Tracking "+shipTrackers.size()+" ships");
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
			shipTrackers.clear();
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
		// private fleetTrackerScript fleetTracker = null;
		private ShipVariantAPI variant = null;
		private String memberId = null;
		private Set<String> hullMods = new HashSet<String>();
		private Set<String> newHullMods = new HashSet<String>();
		private Set<String> removedHullMods = new HashSet<String>();
		private boolean isDone = false;
		
		//#region CONSTRUCTORS & ACCESSORS
		public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize / a year later, I have no idea what I mean by this
			this.variant = variant;
		}
		
		public shipTrackerScript(ShipVariantAPI variant, String memberId, fleetTrackerScript fleetTracker) {
			this.variant = variant;
			this.memberId = memberId;
			this.hullMods.addAll(variant.getHullMods());
			// this.fleetTracker = fleetTracker;

			fleetTracker.addshipTracker(memberId, this);
			
			Global.getSector().addScript(this); 
	
			if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
		}
	
		public String getMemberId() {
			return this.memberId;
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {
			if (!isRefitTab()) { if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Stopping ship tracking"); isDone = true; return; }
			
			for (String hullModId : variant.getHullMods()) {
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (hullMods.contains(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": New hull modification '"+hullModId+"'");
	
				newHullMods.add(hullModId);
			}

			for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (variant.hasHullMod(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Removed hull modification '"+hullModId+"'");
	
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
			this.isDone = true;
		}
	}
	//#endregion
	// END OF INNER CLASS: shipTrackerScript
}