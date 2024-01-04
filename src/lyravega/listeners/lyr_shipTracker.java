package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.loading.specs.HullVariantSpec;

import lyravega.listeners.events.*;
import lyravega.utilities.logger.lyr_logger;

/**
 * A class that acts like a listener in a way. Constructor takes a
 * ship and saves the relevant fields. They are compared later on
 * through updates to see if there are any changes.
 * <p> Not attached to a ship as a listener as the refit ships are
 * rebuilt constantly. Refit and real ships needs to be linked
 * externally in other words, but they can use same instance of
 * this object.
 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents} / {@link weaponEvents}
 * @author lyravega
 */
public final class lyr_shipTracker {
	private MutableShipStatsAPI stats = null;
	private ShipVariantAPI variant = null;
	private final lyr_fleetTracker fleetTracker;
	private final boolean isShip;
	private final String logPrefix;
	private final String trackerUUID;
	private final String parentTrackerUUID;
	private final Set<String> cachedHullMods;
	private final Set<String> cachedEnhancedMods;
	private final Set<String> cachedEmbeddedMods;
	private final Set<String> cachedSuppressedMods;
	private final Map<String, String> cachedWeapons;
	private final ArrayList<String> cachedWings;
	private final Map<String, ShipVariantAPI> cachedModules;
	private Iterator<String> iterator;

	//#region CONSTRUCTORS & ACCESSORS
	public lyr_shipTracker(lyr_fleetTracker fleetTracker, final ShipVariantAPI variant, final FleetMemberAPI member, final String trackerUUID, final String parentTrackerUUID) {
		// this.stats = stats;
		this.variant = variant;
		this.fleetTracker = fleetTracker;
		this.isShip = member != null;
		this.logPrefix = (this.isShip ? "ST-" : "MT-") + trackerUUID;
		this.trackerUUID = trackerUUID;
		this.parentTrackerUUID = parentTrackerUUID;

		this.cachedHullMods = new HashSet<String>(variant.getHullMods());
		this.cachedEnhancedMods = new HashSet<String>(variant.getSMods());
		this.cachedEmbeddedMods = new HashSet<String>(variant.getSModdedBuiltIns());
		this.cachedSuppressedMods = new HashSet<String>(variant.getSuppressedMods());

		this.cachedWeapons = new HashMap<String, String>(); for (final WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy())
			this.cachedWeapons.put(slot.getId(), variant.getWeaponId(slot.getId()));

		this.cachedWings = new ArrayList<String>(variant.getWings());

		if (this.isShip) {
			Map<String, HullVariantSpec> moduleVariants = HullVariantSpec.class.cast(variant).getModuleVariants();
			this.cachedModules = moduleVariants != null ? new HashMap<String, ShipVariantAPI>(moduleVariants) : new HashMap<String, ShipVariantAPI>();
		} else this.cachedModules = null;

		fleetTracker.shipTrackers.put(trackerUUID, this);
		if (this.isShip) fleetTracker.fleetMembers.put(parentTrackerUUID, member);

		lyr_logger.trackerInfo(this.logPrefix+": Tracker initialized");
	}

	public ShipVariantAPI getOwnVariant() { return this.variant; }

	public ShipVariantAPI getParentVariant() { return this.fleetTracker.shipTrackers.get(this.parentTrackerUUID).getOwnVariant(); }

	public FleetMemberAPI getOwnMember() { return this.fleetTracker.fleetMembers.get(this.trackerUUID); }

	public FleetMemberAPI getParentMember() { return this.fleetTracker.fleetMembers.get(this.parentTrackerUUID); }

	public String getOwnTrackerUUID() { return this.trackerUUID; }

	public String getParentTrackerUUID() { return this.parentTrackerUUID; }

	/**
	 * Updates the stored variant, and then compares the hullmods and weapons
	 * cached from the old variant. If the relevant hullmods have any event
	 * methods, they will be called upon. See below for details on those
	 * @param variant to update and compare
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents} / {@link weaponEvents}
	 */
	void updateStats(final MutableShipStatsAPI stats) {
		this.stats = stats;
		this.variant = stats.getVariant();

		this.checkHullMods();
		this.checkEnhancedMods();
		this.checkSuppressedMods();
		this.checkWeapons();
		this.checkWings();
		if (this.isShip) this.checkModules();
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS

	private void checkHullMods() {
		final Collection<String> hullMods = this.variant.getHullMods();
		final Set<String> suppressedMods = this.variant.getSuppressedMods();

		for (final String hullModId : hullMods) {
			if (this.cachedHullMods.contains(hullModId)) continue;
			if (this.cachedSuppressedMods.contains(hullModId)) { this.cachedHullMods.add(hullModId); continue; }

			this.cachedHullMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onInstalled, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Installed '"+hullModId+"'");
		}

		for (this.iterator = this.cachedHullMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (hullMods.contains(hullModId)) continue;
			if (suppressedMods.contains(hullModId)) { this.iterator.remove(); continue; }

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRemoved, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Removed '"+hullModId+"'");
		}
	}

	private void checkEnhancedMods() {
		final LinkedHashSet<String> enhancedMods = this.variant.getSMods();
		final LinkedHashSet<String> embeddedMods = this.variant.getSModdedBuiltIns();

		for (final String hullModId : enhancedMods) {
			if (this.cachedEnhancedMods.contains(hullModId)) continue;
			if (this.cachedEmbeddedMods.contains(hullModId)) continue;

			this.cachedEnhancedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhanced, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Enhanced '"+hullModId+"'");
		}

		for (final String hullModId : embeddedMods) {
			if (this.cachedEmbeddedMods.contains(hullModId)) continue;
			if (this.cachedEnhancedMods.contains(hullModId)) this.cachedEnhancedMods.remove(hullModId);

			this.cachedEmbeddedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhanced, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Enhanced embedded '"+hullModId+"'");
		}

		for (this.iterator = this.cachedEnhancedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (enhancedMods.contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalized, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Normalized '"+hullModId+"'");
		}

		for (this.iterator = this.cachedEmbeddedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (embeddedMods.contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalized, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Normalized embedded '"+hullModId+"'");
		}
	}

	private void checkSuppressedMods() {
		final Set<String> suppressedMods = this.variant.getSuppressedMods();

		for (final String hullModId : suppressedMods) {
			if (this.cachedSuppressedMods.contains(hullModId)) continue;

			this.cachedSuppressedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onSuppressed, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Suppressed '"+hullModId+"'");
		}

		for (this.iterator = this.cachedSuppressedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (suppressedMods.contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRestored, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Restored '"+hullModId+"'");
		}
	}

	private void checkWeapons() {
		for (WeaponSlotAPI slot : this.variant.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			String newWeaponId = this.variant.getWeaponId(slotId);
			String oldWeaponId = this.cachedWeapons.get(slotId);

			if (this.variant.getModuleSlots().contains(slotId)) {
				this.cachedWeapons.put(slotId, null);
				continue;	// not sure if this is needed but just in case to short-circuit the weapon events and let module events overtake them
			}

			if (oldWeaponId == null && newWeaponId == null) continue;
			else if (oldWeaponId == null && newWeaponId != null) {	// weapon installed
				this.cachedWeapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstalled, this.stats, newWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Installed weapon '"+newWeaponId+"' on slot '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId == null) {	// weapon removed
				this.cachedWeapons.put(slotId, null);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemoved, this.stats, oldWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Removed weapon '"+oldWeaponId+"' from slot '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId != null && !oldWeaponId.equals(newWeaponId)) {	// weapon changed
				this.cachedWeapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstalled, this.stats, newWeaponId, slotId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemoved, this.stats, oldWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Changed weapon '"+oldWeaponId+"' on slot '"+slotId+"' with '"+newWeaponId+"'");
			}
		}
	}

	private void checkWings() {
		if (this.cachedWings.size() < this.variant.getWings().size()) {	// needed because 'getWings()' grows when needed
			this.cachedWings.addAll(Collections.nCopies(this.variant.getWings().size() - this.cachedWings.size(), ""));
		}

		for (int bayNumber = 0; bayNumber < this.cachedWings.size(); bayNumber++) {	// iterating over this instead of 'getWings()' because it may get cleared
			String oldWingId = this.cachedWings.get(bayNumber);
			String newWingId = this.variant.getWingId(bayNumber);

			if (oldWingId.isEmpty() && newWingId == null) continue;
			else if (oldWingId.isEmpty() && newWingId != null) {	// wing installed
				this.cachedWings.set(bayNumber, newWingId);
				lyr_eventDispatcher.onWingEvent(onWingAssigned, this.stats, newWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Installed wing '"+newWingId+"' on bay '"+bayNumber+"'");
			} else if (!oldWingId.isEmpty() && newWingId == null) {	// wing removed
				this.cachedWings.set(bayNumber, "");
				lyr_eventDispatcher.onWingEvent(onWingRelieved, this.stats, oldWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Removed wing '"+oldWingId+"' from bay '"+bayNumber+"'");
			} else if (!oldWingId.isEmpty() && newWingId != null && !oldWingId.equals(newWingId)) {	// wing changed
				this.cachedWings.set(bayNumber, newWingId);
				lyr_eventDispatcher.onWingEvent(onWingAssigned, this.stats, newWingId, bayNumber);
				lyr_eventDispatcher.onWingEvent(onWingRelieved, this.stats, oldWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Changed wing '"+oldWingId+"' on bay '"+bayNumber+"' with '"+newWingId+"'");
			}
		}
	}

	private void checkModules() {
		final Map<String, String> modules = this.variant.getStationModules();
		// final Map<String, HullVariantSpec> modules = HullVariantSpec.class.cast(this.variant).getModuleVariants();	// TODO: rig this up?

		for (final String moduleSlotId : modules.keySet()) {
			if (this.cachedModules.containsKey(moduleSlotId)) continue;
			final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);

			this.fleetTracker.addTracking(moduleVariant, null, this.trackerUUID);
			this.cachedModules.put(moduleSlotId, moduleVariant);
			lyr_eventDispatcher.onModuleEvent(onModuleInstalled, this.stats, moduleVariant, moduleSlotId);

			lyr_logger.eventInfo(this.logPrefix+": Installed module '"+moduleVariant.getHullVariantId()+"' on slot '"+moduleSlotId+"'");
		}

		for (this.iterator = this.cachedModules.keySet().iterator(); this.iterator.hasNext();) { final String moduleSlotId = this.iterator.next();
			if (!modules.containsKey(moduleSlotId)) {
				final ShipVariantAPI cachedModuleVariant = this.cachedModules.get(moduleSlotId);

				// TODO: remove it from fleet tracker? also, rebuild status from here?
				this.iterator.remove();
				lyr_eventDispatcher.onModuleEvent(onModuleRemoved, this.stats, cachedModuleVariant, moduleSlotId);

				lyr_logger.eventInfo(this.logPrefix+": Removed module '"+cachedModuleVariant.getHullVariantId()+"' from slot '"+moduleSlotId+"'");
			} else if (!modules.get(moduleSlotId).equals(this.cachedModules.get(moduleSlotId).getHullVariantId())) {
				final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);
				final ShipVariantAPI cachedModuleVariant = this.cachedModules.get(moduleSlotId);

				this.fleetTracker.addTracking(moduleVariant, null, this.trackerUUID);
				this.cachedModules.put(moduleSlotId, moduleVariant);
				lyr_eventDispatcher.onModuleEvent(onModuleInstalled, this.stats, moduleVariant, moduleSlotId);
				lyr_eventDispatcher.onModuleEvent(onModuleRemoved, this.stats, cachedModuleVariant, moduleSlotId);

				lyr_logger.eventInfo(this.logPrefix+": Changed module '"+cachedModuleVariant.getHullVariantId()+"' on slot '"+moduleSlotId+"' with '"+moduleVariant.getHullVariantId()+"'");
			}
		}
	}
}
