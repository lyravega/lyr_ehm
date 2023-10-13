package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import com.fs.starfarer.api.combat.ShipVariantAPI;
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
public class lyr_shipTracker {
	private ShipVariantAPI variant = null;
	// private ShipHullSpecAPI hullSpec = null;
	private final String trackerId;
	private final Set<String> hullMods = new HashSet<String>();
	private final Set<String> enhancedMods = new HashSet<String>();
	private final Set<String> embeddedMods = new HashSet<String>();
	private final Set<String> suppressedMods = new HashSet<String>();
	private final Map<String, String> weapons = new HashMap<String, String>();
	// private Map<String, ShipVariantAPI> moduleVariants = null;
	private Iterator<String> iterator;
	
	//#region CONSTRUCTORS & ACCESSORS
	public lyr_shipTracker(final ShipVariantAPI variant, final String trackerId) {
		this.variant = variant;
		// this.hullSpec = variant.getHullSpec();
		this.trackerId = trackerId;
		// this.parentId = getUUID(variant);
		this.hullMods.addAll(variant.getHullMods());
		this.enhancedMods.addAll(variant.getSMods());
		this.embeddedMods.addAll(variant.getSModdedBuiltIns());
		this.suppressedMods.addAll(variant.getSuppressedMods());

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			weapons.put(slot.getId(), variant.getWeaponId(slot.getId()));
		}
	}

	ShipVariantAPI getVariant() { return this.variant; }

	/**
	 * Updates the stored variant, and then compares the hullmods and weapons
	 * cached from the old variant. If the relevant hullmods have any event
	 * methods, they will be called upon. See below for details on those
	 * @param variant to update and compare
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents} / {@link weaponEvents}
	 */
	public void updateVariant(final ShipVariantAPI variant) {
		this.variant = variant;

		checkHullMods();
		checkEnhancedMods();
		checkSuppressedMods();
		checkWeapons();
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS

	private void checkHullMods() {
		for (final String hullModId : variant.getHullMods()) {
			if (hullMods.contains(hullModId)) continue;
			if (suppressedMods.contains(hullModId)) { hullMods.add(hullModId); continue; }

			hullMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onInstall, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Installed '"+hullModId+"'");
		}

		for (iterator = hullMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.hasHullMod(hullModId)) continue;
			if (variant.getSuppressedMods().contains(hullModId)) { iterator.remove(); continue; }

			iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRemove, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Removed '"+hullModId+"'");
		}
	}

	private void checkEnhancedMods() {
		for (final String hullModId : variant.getSMods()) {
			if (enhancedMods.contains(hullModId)) continue;
			if (embeddedMods.contains(hullModId)) continue;

			enhancedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhance, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Enhanced '"+hullModId+"'");
		}

		for (final String hullModId : variant.getSModdedBuiltIns()) {
			if (embeddedMods.contains(hullModId)) continue;
			if (enhancedMods.contains(hullModId)) enhancedMods.remove(hullModId);

			embeddedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onEnhance, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Enhanced embedded '"+hullModId+"'");
		}

		for (iterator = enhancedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSMods().contains(hullModId)) continue;

			iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalize, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Normalized '"+hullModId+"'");
		}

		for (iterator = embeddedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSModdedBuiltIns().contains(hullModId)) continue;

			iterator.remove(); lyr_eventDispatcher.onHullModEvent(onNormalize, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Normalized embedded '"+hullModId+"'");
		}
	}

	private void checkSuppressedMods() {
		for (final String hullModId : variant.getSuppressedMods()) {
			if (suppressedMods.contains(hullModId)) continue;

			suppressedMods.add(hullModId); lyr_eventDispatcher.onHullModEvent(onSuppress, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Suppressed '"+hullModId+"'");
		}

		for (iterator = suppressedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSuppressedMods().contains(hullModId)) continue;

			iterator.remove(); lyr_eventDispatcher.onHullModEvent(onRestore, variant, hullModId);
			lyr_logger.eventInfo("ST-"+trackerId+": Restored '"+hullModId+"'");
		}
	}

	private void checkWeapons() {
		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			String newWeaponId = variant.getWeaponId(slotId);
			String oldWeaponId = weapons.get(slotId);

			if (oldWeaponId == null && newWeaponId == null) continue;
			else if (oldWeaponId == null && newWeaponId != null) {	// weapon installed
				weapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstall, variant, newWeaponId, slotId);

				lyr_logger.eventInfo("ST-"+trackerId+": Installed '"+newWeaponId+"' on '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId == null) {	// weapon removed
				weapons.put(slotId, null);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemove, variant, oldWeaponId, slotId);

				lyr_logger.eventInfo("ST-"+trackerId+": Removed '"+oldWeaponId+"' from '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId != null && !oldWeaponId.equals(newWeaponId)) {	// weapon changed
				weapons.put(slotId, newWeaponId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponInstall, variant, newWeaponId, slotId);
				lyr_eventDispatcher.onWeaponEvent(onWeaponRemove, variant, oldWeaponId, slotId);

				lyr_logger.eventInfo("ST-"+trackerId+": Changed '"+oldWeaponId+"' on '"+slotId+"' with '"+newWeaponId+"'");
			}	
		}
	}
}
