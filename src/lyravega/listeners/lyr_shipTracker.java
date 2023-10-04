package lyravega.listeners;

import static lyravega.misc.lyr_internals.events.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.listeners.events.*;
import lyravega.misc.lyr_internals;
import lyravega.plugin.lyr_ehm;
import lyravega.tools.lyr_logger;
import lyravega.tools.lyr_uiTools;

/**
 * A class that acts like a listener in a way. Constructor takes a 
 * ship and saves the relevant fields. They are compared later on
 * through updates to see if there are any changes.
 * <p> Not attached to a ship as a listener as the refit ships are
 * rebuilt constantly. Refit and real ships needs to be linked
 * externally in other words, but they can use same instance of
 * this object.
 * @see {@link normalEvents} onInstall / onRemove events for hullmods
 * @see {@link enhancedEvents} onEnhance / onNormalize events for sMods
 * @see {@link suppressedEvents} onSuppress / onRestore events for hullmods
 * @author lyravega
 */
public class lyr_shipTracker implements lyr_logger {
	private ShipVariantAPI variant = null;
	// private ShipHullSpecAPI hullSpec = null;
	private final String memberId;
	private final Set<String> hullMods = new HashSet<String>();
	private final Set<String> enhancedMods = new HashSet<String>();
	private final Set<String> embeddedMods = new HashSet<String>();
	private final Set<String> suppressedMods = new HashSet<String>();
	private final Map<String, String> weapons = new HashMap<String, String>();
	// private Map<String, ShipVariantAPI> moduleVariants = null;
	private Iterator<String> iterator;
	
	//#region CONSTRUCTORS & ACCESSORS

	public lyr_shipTracker(final ShipAPI ship) {
		this.variant = ship.getVariant();
		// this.hullSpec = variant.getHullSpec();
		this.memberId = getId();
		this.hullMods.addAll(variant.getHullMods());
		this.enhancedMods.addAll(variant.getSMods());
		this.embeddedMods.addAll(variant.getSModdedBuiltIns());
		this.suppressedMods.addAll(variant.getSuppressedMods());

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			weapons.put(slot.getId(), variant.getWeaponId(slot.getId()));
		}
	}

	public lyr_shipTracker(final ShipVariantAPI variant) {
		this.variant = variant;
		// this.hullSpec = variant.getHullSpec();
		this.memberId = getId();
		this.hullMods.addAll(variant.getHullMods());
		this.enhancedMods.addAll(variant.getSMods());
		this.embeddedMods.addAll(variant.getSModdedBuiltIns());
		this.suppressedMods.addAll(variant.getSuppressedMods());

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			weapons.put(slot.getId(), variant.getWeaponId(slot.getId()));
		}
	}

	public String getId() {
		for (String tag : this.variant.getTags())
			if (tag.startsWith(lyr_internals.uuid.shipPrefix)) return tag.substring(lyr_internals.uuid.shipPrefix.length());
		return null;
	}

	public void updateVariant(final ShipVariantAPI variant) {
		this.variant = variant;

		checkHullMods();
		checkEnhancedMods();
		checkSuppressedMods();
		checkWeapons();
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS

	// hull modification effects that implement any of the event interfaces are stored in these maps
	public static final Map<String, weaponEvents> weaponEvents = new HashMap<String, weaponEvents>();
	public static final Map<String, normalEvents> normalEvents = new HashMap<String, normalEvents>();
	public static final Map<String, enhancedEvents> enhancedEvents = new HashMap<String, enhancedEvents>();
	public static final Map<String, suppressedEvents> suppressedEvents = new HashMap<String, suppressedEvents>();
	public static final Set<String> allModEvents = new HashSet<String>();

	/**
	 * Executes the hull modification's event method if applicable. They need to implement
	 * the relative interfaces and its methods first
	 * <p> If the hull modification doesn't have any events attached to it, then depending
	 * on the setting of the mod, a drill sound will be played
	 * @param eventName type of the event
	 * @param variant of the ship
	 * @param hullModId of the hull modification
	 */
	private static void onHullModEvent(final String eventName, final ShipVariantAPI variant, final String hullModId) {
		if (allModEvents.contains(hullModId)) switch (eventName) {
			case onInstall:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onInstall(variant); return;
			case onRemove:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onRemove(variant); return;
			case onEnhance:		if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onEnhance(variant); return;
			case onNormalize:	if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onNormalize(variant); return;
			case onSuppress:	if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onSuppress(variant); return;
			case onRestore:		if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onRestore(variant); return;
			default: return;
		} else if (lyr_ehm.settings.getPlayDrillSoundForAll()) switch (eventName) {
			case onInstall:		
			case onRemove:		lyr_uiTools.playDrillSound(); return;
			default: return;
		}
	}

	/**
	 * Broadcasts a weapon event to all installed hull modifications. The hull modifications
	 * need to implement the relative interfaces and methods first
	 * <p> Filtering needs to be done in the event method as this is a global broadcast to
	 * all installed hull modifications
	 * @param eventName type of the event
	 * @param variant of the ship
	 * @param weaponId of the weapon
	 */
	private static void onWeaponEvent(final String eventName, final ShipVariantAPI variant, final String weaponId) {
		switch (eventName) {
			case onWeaponInstall:	for (String hullModId: weaponEvents.keySet()) if (variant.hasHullMod(hullModId)) weaponEvents.get(hullModId).onWeaponInstall(variant, weaponId); return;
			case onWeaponRemove:	for (String hullModId: weaponEvents.keySet()) if (variant.hasHullMod(hullModId)) weaponEvents.get(hullModId).onWeaponRemove(variant, weaponId); return;
			default: return;
		}
	}

	// private void checkHullSpec() {
	// 	if (!variant.getHullSpec().getHullId().equals(this.hullSpec.getHullId())) {
	// 		this.hullSpec = variant.getHullSpec();

	// 		if (log) logger.info(logPrefix+"ST-"+memberId+": Hull Spec of the ship has been changed");
	// 	}
	// }

	private void checkHullMods() {
		for (final String hullModId : variant.getHullMods()) {
			if (hullMods.contains(hullModId)) continue;
			if (suppressedMods.contains(hullModId)) { hullMods.add(hullModId); continue; }

			hullMods.add(hullModId); onHullModEvent(onInstall, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Installed '"+hullModId+"'");
		}

		for (iterator = hullMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.hasHullMod(hullModId)) continue;
			if (variant.getSuppressedMods().contains(hullModId)) { iterator.remove(); continue; }

			iterator.remove(); onHullModEvent(onRemove, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Removed '"+hullModId+"'");
		}
	}

	private void checkEnhancedMods() {
		for (final String hullModId : variant.getSMods()) {
			if (enhancedMods.contains(hullModId)) continue;
			if (embeddedMods.contains(hullModId)) continue;

			enhancedMods.add(hullModId); onHullModEvent(onEnhance, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Enhanced '"+hullModId+"'");
		}

		for (final String hullModId : variant.getSModdedBuiltIns()) {
			if (embeddedMods.contains(hullModId)) continue;
			if (enhancedMods.contains(hullModId)) enhancedMods.remove(hullModId);

			embeddedMods.add(hullModId); onHullModEvent(onEnhance, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Enhanced embedded '"+hullModId+"'");
		}

		for (iterator = enhancedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSMods().contains(hullModId)) continue;

			iterator.remove(); onHullModEvent(onNormalize, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Normalized '"+hullModId+"'");
		}

		for (iterator = embeddedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSModdedBuiltIns().contains(hullModId)) continue;

			iterator.remove(); onHullModEvent(onNormalize, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Normalized embedded '"+hullModId+"'");
		}
	}

	private void checkSuppressedMods() {
		for (final String hullModId : variant.getSuppressedMods()) {
			if (suppressedMods.contains(hullModId)) continue;

			suppressedMods.add(hullModId); onHullModEvent(onSuppress, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Suppressed '"+hullModId+"'");
		}

		for (iterator = suppressedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSuppressedMods().contains(hullModId)) continue;

			iterator.remove(); onHullModEvent(onRestore, variant, hullModId);
			if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Restored '"+hullModId+"'");
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
				onWeaponEvent(onWeaponInstall, variant, newWeaponId);

				if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Installed '"+newWeaponId+"' on '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId == null) {	// weapon removed
				weapons.put(slotId, null);
				onWeaponEvent(onWeaponRemove, variant, oldWeaponId);

				if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Removed '"+oldWeaponId+"' from '"+slotId+"'");
			} else if (oldWeaponId != null && newWeaponId != null && !oldWeaponId.equals(newWeaponId)) {	// weapon changed
				weapons.put(slotId, newWeaponId);
				onWeaponEvent(onWeaponInstall, variant, newWeaponId);
				onWeaponEvent(onWeaponRemove, variant, oldWeaponId);

				if (lyr_ehm.settings.getLogEventInfo()) logger.info(logPrefix+"ST-"+memberId+": Changed '"+oldWeaponId+"' on '"+slotId+"' with '"+newWeaponId+"'");
			}	
		}
	}
}
