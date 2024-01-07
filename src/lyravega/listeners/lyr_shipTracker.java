package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
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
	private final lyr_shipTracker parentTracker;
	private final String trackerUUID;
	private final boolean isShip;
	private final boolean isSelectable;
	private final String logPrefix;
	private final FleetMemberAPI member;
	private FleetMemberAPI refitMember;
	private MutableShipStatsAPI stats;
	private ShipVariantAPI variant;
	private final Map<String, lyr_shipTracker> cachedModules;
	private final Map<String, String> cachedWeapons;
	private final ArrayList<String> cachedWings;
	private final Set<String> cachedHullMods, cachedEnhancedMods, cachedEmbeddedMods, cachedSuppressedMods;
	private Iterator<String> iterator;

	//#region CONSTRUCTORS & ACCESSORS
	/**
	 * Constructs a tracker for a ship. Only the ships with an actual fleet member should construct
	 * a tracker as it will also construct trackers for its modules.
	 * <p> Changes the variant source to {@code REFIT} if necessary, and applies it. This is needed
	 * in cases where the source is {@code HULL} or {@code STOCK}; those variants are not unique.
	 * <p> The trackers needs to be registered using {@link #registerTracker()} method which assigns
	 * the variants their UUID and adds the tracking mod to them. When the trackers are no longer
	 * needed, {@link #unregisterTracker()} method may be used to clean the tag and the hullmod up.
	 * @param fleetTracker that creates these ship trackers
	 * @param member of the ship
	 * @param variant of the ship
	 */
	public lyr_shipTracker(lyr_fleetTracker fleetTracker, FleetMemberAPI member, ShipVariantAPI variant, lyr_shipTracker parentShipTracker) {
		this.fleetTracker = fleetTracker;
		this.parentTracker = parentShipTracker;
		this.trackerUUID = UUID.randomUUID().toString();
		this.isShip = member != null;
		this.isSelectable = isSelectable(variant.getHullSpec());
		this.logPrefix = (this.isShip ? "ST-" : "MT-") + this.trackerUUID;

		this.member = member;
		this.refitMember = member;
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
				if (!this.isSelectable) continue;

				final lyr_shipTracker moduleTracker = new lyr_shipTracker(this.fleetTracker, null, moduleVariant, this);

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

	/**
	 * @return the stored variant
	 */
	public ShipVariantAPI getVariant() { return this.variant; }

	/**
	 * @return the member; if this is a module, will return the refit member; modules do not have actual fleet members in the fleet
	 */
	public FleetMemberAPI getMember() { return this.isShip ? this.member : this.refitMember; }

	/**
	 * @return the parent's tracker; if this is not a module, will return null
	 */
	public lyr_shipTracker getParentTracker() { return this.parentTracker; }

	/**
	 * @return a modules's tracker; if this is a module, will return null
	 */
	public lyr_shipTracker getModuleTracker(String slotId) { return this.isShip ? this.cachedModules.get(slotId) : null; }

	/**
	 * @return ship/module's tracking UUID
	 */
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

		if (!this.variant.getPermaMods().contains(lyr_fleetTracker.trackerModId)) {
			this.variant.addPermaMod(lyr_fleetTracker.trackerModId, false);
			this.cachedHullMods.add(lyr_fleetTracker.trackerModId);
		}

		if (!this.variant.hasTag(lyr_fleetTracker.uuid.shipPrefix+this.trackerUUID))
			this.variant.addTag(lyr_fleetTracker.uuid.shipPrefix+this.trackerUUID);

		this.fleetTracker.shipTrackers.put(this.trackerUUID, this);
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

		if (this.variant.getPermaMods().contains(lyr_fleetTracker.trackerModId))
			this.variant.removePermaMod(lyr_fleetTracker.trackerModId);

		for (Iterator<String> iterator = this.variant.getTags().iterator(); iterator.hasNext(); )
			if (iterator.next().startsWith(lyr_fleetTracker.uuid.prefix)) iterator.remove();

		this.fleetTracker.shipTrackers.remove(this.trackerUUID);
	}

	public static boolean isSelectable(ShipHullSpecAPI hullSpec) {
		if (hullSpec.getOrdnancePoints(null) == 0) return false;	// vanilla first checks this
		if (hullSpec.hasTag(Tags.MODULE_UNSELECTABLE)) return false;	// then this to identify unselectables

		return true;
	}

	/**
	 * Updates the stored variant, and then compares the hullmods and weapons
	 * cached from the old variant. If the relevant hullmods have any event
	 * methods, they will be called upon. See below for details on those
	 * @param variant to update and compare
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents} / {@link weaponEvents}
	 */
	void updateStats(final MutableShipStatsAPI stats) {
		this.refitMember = stats.getFleetMember();
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
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemoved, this.stats, oldWeaponId, slotId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstalled, this.stats, newWeaponId, slotId);

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
				lyr_eventDispatcher.onWingEvent(onWingRelieved, this.stats, oldWingId, bayNumber);
				lyr_eventDispatcher.onWingEvent(onWingAssigned, this.stats, newWingId, bayNumber);

				lyr_logger.eventInfo(this.logPrefix+": Changed wing '"+oldWingId+"' on bay '"+bayNumber+"' with '"+newWingId+"'");
			}
		}
	}

	private void checkModules() {
		final Map<String, String> modules = this.variant.getStationModules();

		for (final String moduleSlotId : modules.keySet()) {
			if (this.cachedModules.containsKey(moduleSlotId)) continue;
			if (!isSelectable(this.variant.getModuleVariant(moduleSlotId).getHullSpec())) continue;

			final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);
			final lyr_shipTracker moduleTracker = new lyr_shipTracker(this.fleetTracker, null, moduleVariant, this);
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
				if (!isSelectable(this.variant.getModuleVariant(moduleSlotId).getHullSpec())) continue;

				final ShipVariantAPI moduleVariant = this.variant.getModuleVariant(moduleSlotId);
				final lyr_shipTracker moduleTracker = new lyr_shipTracker(this.fleetTracker, null, moduleVariant, this);
				final lyr_shipTracker oldModuleTracker = this.cachedModules.get(moduleSlotId);
				final ShipVariantAPI oldModuleVariant = oldModuleTracker.getVariant();
				moduleTracker.registerTracker(); oldModuleTracker.unregisterTracker();

				this.cachedModules.put(moduleSlotId, moduleTracker);
				lyr_eventDispatcher.onModuleEvent(onModuleRemoved, this.stats, oldModuleVariant, moduleSlotId);
				lyr_eventDispatcher.onModuleEvent(onModuleInstalled, this.stats, moduleVariant, moduleSlotId);

				lyr_logger.eventInfo(this.logPrefix+": Changed module '"+oldModuleVariant.getHullVariantId()+"' on slot '"+moduleSlotId+"' with '"+moduleVariant.getHullVariantId()+"'");
			}
		}
	}
}
