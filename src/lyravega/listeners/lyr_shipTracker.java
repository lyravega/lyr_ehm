package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

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
	private final String logPrefix;
	private final String trackerUUID;
	private final String parentTrackerUUID;
	private final Set<String> hullMods;
	private final Set<String> enhancedMods;
	private final Set<String> embeddedMods;
	private final Set<String> suppressedMods;
	private final Map<String, String> weapons;
	private final ArrayList<String> wings;
	private final Map<String, ShipVariantAPI> modules;
	private Iterator<String> iterator;

	//#region CONSTRUCTORS & ACCESSORS
	public lyr_shipTracker(lyr_fleetTracker fleetTracker, final ShipVariantAPI variant, final FleetMemberAPI member, final String trackerUUID, final String parentTrackerUUID) {
		// this.stats = stats;
		this.variant = variant;
		this.fleetTracker = fleetTracker;
		this.logPrefix = (member == null ? "MT-" : "ST-") + trackerUUID;
		this.trackerUUID = trackerUUID;
		this.parentTrackerUUID = parentTrackerUUID;
		this.hullMods = new HashSet<String>(variant.getHullMods());
		this.enhancedMods = new HashSet<String>(variant.getSMods());
		this.embeddedMods = new HashSet<String>(variant.getSModdedBuiltIns());
		this.suppressedMods = new HashSet<String>(variant.getSuppressedMods());
		this.weapons = new HashMap<String, String>(); for (final WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy())
			this.weapons.put(slot.getId(), variant.getWeaponId(slot.getId()));
		this.wings = new ArrayList<String>(variant.getWings());
		this.modules = new HashMap<String, ShipVariantAPI>(); for (final String moduleSlotId : variant.getModuleSlots())
			this.modules.put(moduleSlotId, variant.getModuleVariant(moduleSlotId));

		fleetTracker.shipTrackers.put(trackerUUID, this);
		if (member != null) fleetTracker.fleetMembers.put(parentTrackerUUID, member);

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
		this.checkModules();
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS

	private void checkHullMods() {
		for (final String hullModId : this.variant.getHullMods()) {
			if (this.hullMods.contains(hullModId)) continue;
			if (this.suppressedMods.contains(hullModId)) { this.hullMods.add(hullModId); continue; }

			this.hullMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onInstalled, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Installed '"+hullModId+"'");
		}

		for (this.iterator = this.hullMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.hasHullMod(hullModId)) continue;
			if (this.variant.getSuppressedMods().contains(hullModId)) { this.iterator.remove(); continue; }

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRemoved, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Removed '"+hullModId+"'");
		}
	}

	private void checkEnhancedMods() {
		for (final String hullModId : this.variant.getSMods()) {
			if (this.enhancedMods.contains(hullModId)) continue;
			if (this.embeddedMods.contains(hullModId)) continue;

			this.enhancedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhanced, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Enhanced '"+hullModId+"'");
		}

		for (final String hullModId : this.variant.getSModdedBuiltIns()) {
			if (this.embeddedMods.contains(hullModId)) continue;
			if (this.enhancedMods.contains(hullModId)) this.enhancedMods.remove(hullModId);

			this.embeddedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhanced, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Enhanced embedded '"+hullModId+"'");
		}

		for (this.iterator = this.enhancedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.getSMods().contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalized, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Normalized '"+hullModId+"'");
		}

		for (this.iterator = this.embeddedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.getSModdedBuiltIns().contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalized, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Normalized embedded '"+hullModId+"'");
		}
	}

	private void checkSuppressedMods() {
		for (final String hullModId : this.variant.getSuppressedMods()) {
			if (this.suppressedMods.contains(hullModId)) continue;

			this.suppressedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onSuppressed, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Suppressed '"+hullModId+"'");
		}

		for (this.iterator = this.suppressedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.getSuppressedMods().contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRestored, this.stats, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Restored '"+hullModId+"'");
		}
	}

	private void checkWeapons() {
		for (WeaponSlotAPI slot : this.variant.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			String newWeaponId = this.variant.getWeaponId(slotId);
			String oldWeaponId = this.weapons.get(slotId);

			if (this.variant.getModuleSlots().contains(slotId)) {
				this.weapons.put(slotId, null);
				continue;	// TODO: this is a shortcut to avoid firing the removed event
			}

			if (oldWeaponId == null && newWeaponId == null) continue;
			else if (oldWeaponId == null && newWeaponId != null) {	// weapon installed
				this.weapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstalled, this.stats, newWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Installed weapon '"+newWeaponId+"' on '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId == null) {	// weapon removed
				this.weapons.put(slotId, null);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemoved, this.stats, oldWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Removed weapon '"+oldWeaponId+"' from '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId != null && !oldWeaponId.equals(newWeaponId)) {	// weapon changed
				this.weapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstalled, this.stats, newWeaponId, slotId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemoved, this.stats, oldWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Changed weapon '"+oldWeaponId+"' on '"+slotId+"' with '"+newWeaponId+"'");
			}
		}
	}

	private void checkWings() {
		if (this.wings.size() < this.variant.getWings().size()) {	// needed because 'getWings()' grows when needed
			this.wings.addAll(Collections.nCopies(this.variant.getWings().size() - this.wings.size(), ""));
		}

		for (int bayNumber = 0; bayNumber < this.wings.size(); bayNumber++) {	// iterating over this instead of 'getWings()' because it may get cleared
			String oldWingId = this.wings.get(bayNumber);
			String newWingId = this.variant.getWingId(bayNumber);

			if (oldWingId.isEmpty() && newWingId == null) continue;
			else if (oldWingId.isEmpty() && newWingId != null) {	// wing installed
				this.wings.set(bayNumber, newWingId);
				lyr_eventDispatcher.onWingEvent(onWingAssigned, this.stats, newWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Installed wing '"+newWingId+"' on '"+bayNumber+"'");
			} else if (!oldWingId.isEmpty() && newWingId == null) {	// wing removed
				this.wings.set(bayNumber, "");
				lyr_eventDispatcher.onWingEvent(onWingRelieved, this.stats, oldWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Removed wing '"+oldWingId+"' from '"+bayNumber+"'");
			} else if (!oldWingId.isEmpty() && newWingId != null && !oldWingId.equals(newWingId)) {	// wing changed
				this.wings.set(bayNumber, newWingId);
				lyr_eventDispatcher.onWingEvent(onWingAssigned, this.stats, newWingId, bayNumber);
				lyr_eventDispatcher.onWingEvent(onWingRelieved, this.stats, oldWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Changed wing '"+oldWingId+"' on '"+bayNumber+"' with '"+newWingId+"'");
			}
		}
	}

	private void checkModules() {
		for (final String moduleSlotId : this.variant.getStationModules().keySet()) {
			if (this.modules.containsKey(moduleSlotId)) continue;
			final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);

			this.fleetTracker.addTracking(moduleVariant, null, this.trackerUUID);
			this.modules.put(moduleSlotId, moduleVariant); lyr_eventDispatcher.onModuleEvent(onModuleInstalled, this.stats, moduleVariant, moduleSlotId);

			lyr_logger.eventInfo(this.logPrefix+": Installed module '"+moduleVariant.getHullVariantId()+"' on '"+moduleSlotId+"'");
		}

		for (this.iterator = this.modules.keySet().iterator(); this.iterator.hasNext();) { final String moduleSlotId = this.iterator.next();
			if (this.variant.getStationModules().containsKey(moduleSlotId)) continue;
			final ShipVariantAPI moduleVariant = this.modules.get(moduleSlotId);

			// TODO: remove it from fleet tracker?
			this.iterator.remove(); lyr_eventDispatcher.onModuleEvent(onModuleRemoved, this.stats, moduleVariant, moduleSlotId);

			lyr_logger.eventInfo(this.logPrefix+": Removed module '"+moduleVariant.getHullVariantId()+"' from '"+moduleSlotId+"'");
		}
	}
}
