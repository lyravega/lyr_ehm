package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

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
	private ShipVariantAPI variant = null;
	private final String trackerUUID;
	private final String parentTrackerUUID;
	private final String logPrefix;
	private final lyr_fleetTracker fleetTracker;
	private final Set<String> hullMods = new HashSet<String>();
	private final Set<String> enhancedMods = new HashSet<String>();
	private final Set<String> embeddedMods = new HashSet<String>();
	private final Set<String> suppressedMods = new HashSet<String>();
	private final Map<String, String> weapons = new HashMap<String, String>();
	private Iterator<String> iterator;

	//#region CONSTRUCTORS & ACCESSORS
	public lyr_shipTracker(lyr_fleetTracker fleetTracker, final ShipVariantAPI variant, final String trackerId, final String parentTrackerId) {
		this.variant = variant;
		this.trackerUUID = trackerId;
		this.parentTrackerUUID = parentTrackerId;
		this.logPrefix = (parentTrackerId != null ? "MT-" : "ST-") + trackerId;
		this.fleetTracker = fleetTracker;
		this.hullMods.addAll(variant.getHullMods());
		this.enhancedMods.addAll(variant.getSMods());
		this.embeddedMods.addAll(variant.getSModdedBuiltIns());
		this.suppressedMods.addAll(variant.getSuppressedMods());

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			this.weapons.put(slot.getId(), variant.getWeaponId(slot.getId()));
		}

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
	void updateVariant(final ShipVariantAPI variant) {
		this.variant = variant;

		this.checkHullMods();
		this.checkEnhancedMods();
		this.checkSuppressedMods();
		this.checkWeapons();
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS

	private void checkHullMods() {
		for (final String hullModId : this.variant.getHullMods()) {
			if (this.hullMods.contains(hullModId)) continue;
			if (this.suppressedMods.contains(hullModId)) { this.hullMods.add(hullModId); continue; }

			this.hullMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onInstall, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Installed '"+hullModId+"'");
		}

		for (this.iterator = this.hullMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.hasHullMod(hullModId)) continue;
			if (this.variant.getSuppressedMods().contains(hullModId)) { this.iterator.remove(); continue; }

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRemove, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Removed '"+hullModId+"'");
		}
	}

	private void checkEnhancedMods() {
		for (final String hullModId : this.variant.getSMods()) {
			if (this.enhancedMods.contains(hullModId)) continue;
			if (this.embeddedMods.contains(hullModId)) continue;

			this.enhancedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhance, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Enhanced '"+hullModId+"'");
		}

		for (final String hullModId : this.variant.getSModdedBuiltIns()) {
			if (this.embeddedMods.contains(hullModId)) continue;
			if (this.enhancedMods.contains(hullModId)) this.enhancedMods.remove(hullModId);

			this.embeddedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhance, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Enhanced embedded '"+hullModId+"'");
		}

		for (this.iterator = this.enhancedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.getSMods().contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalize, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Normalized '"+hullModId+"'");
		}

		for (this.iterator = this.embeddedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.getSModdedBuiltIns().contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalize, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Normalized embedded '"+hullModId+"'");
		}
	}

	private void checkSuppressedMods() {
		for (final String hullModId : this.variant.getSuppressedMods()) {
			if (this.suppressedMods.contains(hullModId)) continue;

			this.suppressedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onSuppress, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Suppressed '"+hullModId+"'");
		}

		for (this.iterator = this.suppressedMods.iterator(); this.iterator.hasNext();) { final String hullModId = this.iterator.next();
			if (this.variant.getSuppressedMods().contains(hullModId)) continue;

			this.iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRestore, this.variant, hullModId);
			lyr_logger.eventInfo(this.logPrefix+": Restored '"+hullModId+"'");
		}
	}

	private void checkWeapons() {
		for (WeaponSlotAPI slot : this.variant.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			String newWeaponId = this.variant.getWeaponId(slotId);
			String oldWeaponId = this.weapons.get(slotId);

			if (oldWeaponId == null && newWeaponId == null) continue;
			else if (oldWeaponId == null && newWeaponId != null) {	// weapon installed
				this.weapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstall, this.variant, newWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Installed '"+newWeaponId+"' on '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId == null) {	// weapon removed
				this.weapons.put(slotId, null);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemove, this.variant, oldWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Removed '"+oldWeaponId+"' from '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId != null && !oldWeaponId.equals(newWeaponId)) {	// weapon changed
				this.weapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstall, this.variant, newWeaponId, slotId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemove, this.variant, oldWeaponId, slotId);

				lyr_logger.eventInfo(this.logPrefix+": Changed '"+oldWeaponId+"' on '"+slotId+"' with '"+newWeaponId+"'");
			}
		}
	}
}
