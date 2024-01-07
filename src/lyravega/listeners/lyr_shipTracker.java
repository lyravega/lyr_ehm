package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
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
	private final lyr_fleetTracker fleetTracker;
	private final String trackerUUID;
	private final boolean isShip;
	private final String logPrefix;
	private FleetMemberAPI member, parentMember;
	private MutableShipStatsAPI stats;
	private ShipVariantAPI variant, parentVariant;
	private final Map<String, lyr_shipTracker> cachedModules;
	private final Map<String, String> cachedWeapons;
	private final ArrayList<String> cachedWings;
	private final Set<String> cachedHullMods, cachedEnhancedMods, cachedEmbeddedMods, cachedSuppressedMods;
	private Iterator<String> iterator;

	//#region CONSTRUCTORS & ACCESSORS
	public lyr_shipTracker(lyr_fleetTracker fleetTracker, FleetMemberAPI member, ShipVariantAPI variant) {
		this.fleetTracker = fleetTracker;
		this.trackerUUID = UUID.randomUUID().toString();
		this.isShip = member != null;
		this.logPrefix = (this.isShip ? "ST-" : "MT-") + this.trackerUUID;

		this.member = member;
		this.stats = this.isShip ? member.getStats() : null;
		this.variant = variant;

		if (this.variant.getSource() != VariantSource.REFIT) {
			lyr_logger.debug(this.logPrefix+this.trackerUUID+": Changing variant source from "+variant.getSource().name()+" to REFIT");
			this.variant = this.variant.clone();
			this.variant.setSource(VariantSource.REFIT);	// is because stock ships cause problems till they're saved once
			if (this.isShip) this.member.setVariant(this.variant, false, false);
		}

		if (this.isShip) {
			this.cachedModules = new HashMap<String, lyr_shipTracker>();

			for (final String moduleSlotId : this.variant.getModuleSlots()) {
				final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);
				// final ShipHullSpecAPI moduleHullSpec = moduleVariant.getHullSpec();

				// if (moduleHullSpec.getOrdnancePoints(null) == 0) continue;	// vanilla first checks this
				// if (moduleHullSpec.hasTag(Tags.MODULE_UNSELECTABLE)) continue;	// then this to identify unselectables

				final lyr_shipTracker moduleTracker = new lyr_shipTracker(moduleVariant, this, this.fleetTracker);

				this.variant.setModuleVariant(moduleSlotId, moduleTracker.getVariant());
				this.cachedModules.put(moduleSlotId, moduleTracker);
			}
		} else this.cachedModules = null;
		this.cachedWeapons = new HashMap<String, String>();
		for (final WeaponSlotAPI slot : this.variant.getHullSpec().getAllWeaponSlotsCopy())
			this.cachedWeapons.put(slot.getId(), this.variant.getWeaponId(slot.getId()));
		this.cachedWings = new ArrayList<String>(this.variant.getWings());
		this.cachedHullMods = new HashSet<String>(this.variant.getHullMods());
		this.cachedEnhancedMods = new HashSet<String>(this.variant.getSMods());
		this.cachedEmbeddedMods = new HashSet<String>(this.variant.getSModdedBuiltIns());
		this.cachedSuppressedMods = new HashSet<String>(this.variant.getSuppressedMods());
	}

	private lyr_shipTracker(ShipVariantAPI moduleVariant, lyr_shipTracker parentTracker, lyr_fleetTracker fleetTracker) {
		this(fleetTracker, null, moduleVariant);

		this.parentMember = parentTracker.getMember();
		this.parentVariant = parentTracker.getVariant();
	}

	public ShipVariantAPI getVariant() { return this.variant; }

	public ShipVariantAPI getParentVariant() { return this.parentVariant; }

	public FleetMemberAPI getMember() { return this.member; }

	public FleetMemberAPI getParentMember() { return this.parentMember; }

	public String getTrackerUUID() { return this.trackerUUID; }

	void registerTracker() {
		if (this.isShip) {
			this.variant = this.member.getVariant();

			for (String moduleSlotId : this.cachedModules.keySet()) {
				final lyr_shipTracker moduleTracker = this.cachedModules.get(moduleSlotId);

				moduleTracker.variant = this.variant.getModuleVariant(moduleSlotId);
				moduleTracker.registerTracker();
			}
		}

		if (!this.variant.getPermaMods().contains(this.fleetTracker.trackerModId)) {
			this.variant.addPermaMod(this.fleetTracker.trackerModId, false);
			this.cachedHullMods.add(this.fleetTracker.trackerModId);
		}

		if (!this.variant.hasTag(lyr_fleetTracker.uuid.shipPrefix+this.trackerUUID))
			this.variant.addTag(lyr_fleetTracker.uuid.shipPrefix+this.trackerUUID);

		this.fleetTracker.shipTrackers.put(this.trackerUUID, this);
		lyr_logger.debug(this.logPrefix+": Registering tracker");
	}

	void unregisterTracker() {
		if (this.isShip) {
			this.variant = this.member.getVariant();	// this is needed as on exit the game will update the member variants, at which point tracker's variant will reference older one

			for (String moduleSlotId : this.cachedModules.keySet()) {
				final lyr_shipTracker moduleTracker = this.cachedModules.get(moduleSlotId);

				moduleTracker.variant = this.variant.getModuleVariant(moduleSlotId);	// same reason as above; at this point, tracker variants will be outdated
				moduleTracker.unregisterTracker();
			}
		}

		if (this.variant.getPermaMods().contains(this.fleetTracker.trackerModId))
			this.variant.removePermaMod(this.fleetTracker.trackerModId);

		for (Iterator<String> iterator = this.variant.getTags().iterator(); iterator.hasNext(); )
			if (iterator.next().startsWith(lyr_fleetTracker.uuid.prefix)) iterator.remove();

		this.fleetTracker.shipTrackers.remove(this.trackerUUID);
		lyr_logger.debug(this.logPrefix+": Unregistering tracker");
	}

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

		for (final String moduleSlotId : modules.keySet()) {
			if (this.cachedModules.containsKey(moduleSlotId)) continue;
			final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);
			final lyr_shipTracker moduleTracker = new lyr_shipTracker(moduleVariant, this, this.fleetTracker);

			moduleTracker.registerTracker();
			this.cachedModules.put(moduleSlotId, moduleTracker);
			lyr_eventDispatcher.onModuleEvent(onModuleInstalled, this.stats, moduleVariant, moduleSlotId);

			lyr_logger.eventInfo(this.logPrefix+": Installed module '"+moduleVariant.getHullVariantId()+"' on slot '"+moduleSlotId+"'");
		}

		for (this.iterator = this.cachedModules.keySet().iterator(); this.iterator.hasNext();) { final String moduleSlotId = this.iterator.next();
			if (!modules.containsKey(moduleSlotId)) {
				final lyr_shipTracker moduleTracker = this.cachedModules.get(moduleSlotId);
				final ShipVariantAPI moduleVariant = moduleTracker.getVariant();

				moduleTracker.unregisterTracker();
				this.iterator.remove();
				lyr_eventDispatcher.onModuleEvent(onModuleRemoved, this.stats, moduleVariant, moduleSlotId);

				lyr_logger.eventInfo(this.logPrefix+": Removed module '"+moduleVariant.getHullVariantId()+"' from slot '"+moduleSlotId+"'");
			} else if (!modules.get(moduleSlotId).equals(this.cachedModules.get(moduleSlotId).getVariant().getHullVariantId())) {
				final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);
				final lyr_shipTracker moduleTracker = new lyr_shipTracker(moduleVariant, this, this.fleetTracker);
				final lyr_shipTracker oldModuleTracker = this.cachedModules.get(moduleSlotId);
				final ShipVariantAPI oldModuleVariant = oldModuleTracker.getVariant();

				moduleTracker.registerTracker(); oldModuleTracker.unregisterTracker();
				this.cachedModules.put(moduleSlotId, moduleTracker);
				lyr_eventDispatcher.onModuleEvent(onModuleInstalled, this.stats, moduleVariant, moduleSlotId);
				lyr_eventDispatcher.onModuleEvent(onModuleRemoved, this.stats, oldModuleVariant, moduleSlotId);

				lyr_logger.eventInfo(this.logPrefix+": Changed module '"+oldModuleVariant.getHullVariantId()+"' on slot '"+moduleSlotId+"' with '"+moduleVariant.getHullVariantId()+"'");
			}
		}
	}
}
