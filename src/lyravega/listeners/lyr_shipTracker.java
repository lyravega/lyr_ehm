package lyravega.listeners;

import static lyravega.misc.lyr_internals.events.onEnhance;
import static lyravega.misc.lyr_internals.events.onInstall;
import static lyravega.misc.lyr_internals.events.onNormalize;
import static lyravega.misc.lyr_internals.events.onRemove;
import static lyravega.misc.lyr_internals.events.onRestore;
import static lyravega.misc.lyr_internals.events.onSuppress;

import static lyravega.listeners.lyr_lunaSettingsListener.playDrillSoundForAll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.listeners.events.suppressedEvents;
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
	// private Set<String> weapons = new HashSet<String>();
	// private Map<String, ShipVariantAPI> moduleVariants = null;
	private Iterator<String> iterator;
	
	//#region CONSTRUCTORS & ACCESSORS

	public lyr_shipTracker(final ShipAPI ship) {
		this.variant = ship.getVariant();
		// this.hullSpec = variant.getHullSpec();
		this.memberId = ship.getFleetMemberId();
		this.hullMods.addAll(variant.getHullMods());
		this.enhancedMods.addAll(variant.getSMods());
		this.embeddedMods.addAll(variant.getSModdedBuiltIns());
		this.suppressedMods.addAll(variant.getSuppressedMods());
		// this.weapons.addAll(variant.getFittedWeaponSlots());
	}

	public void updateVariant(final ShipVariantAPI variant) {
		this.variant = variant;

		checkHullMods();
		checkEnhancedMods();
		checkSuppressedMods();
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS

	// hull modification effects that implement any of the event interfaces are stored in these maps
	public static final Map<String, normalEvents> normalEvents = new HashMap<String, normalEvents>();
	public static final Map<String, enhancedEvents> enhancedEvents = new HashMap<String, enhancedEvents>();
	public static final Map<String, suppressedEvents> suppressedEvents = new HashMap<String, suppressedEvents>();
	public static final Set<String> allRegistered = new HashSet<String>();

	private static void onEvent(final String eventName, final ShipVariantAPI variant, final String hullModId) {
		if (allRegistered.contains(hullModId)) switch (eventName) {
			case onInstall:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onInstall(variant); return;
			case onRemove:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onRemove(variant); return;
			case onEnhance:		if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onEnhance(variant); return;
			case onNormalize:	if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onNormalize(variant); return;
			case onSuppress:	if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onSuppress(variant); return;
			case onRestore:		if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onRestore(variant); return;
			default: return;
		} else if (playDrillSoundForAll) switch (eventName) {
			case onInstall:		
			case onRemove:		
			case onEnhance:		
			case onNormalize:	lyr_uiTools.playDrillSound(); return;
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

			hullMods.add(hullModId); onEvent(onInstall, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Installed '"+hullModId+"'");
		}

		for (iterator = hullMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.hasHullMod(hullModId)) continue;

			iterator.remove(); onEvent(onRemove, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Removed '"+hullModId+"'");
		}
	}

	private void checkEnhancedMods() {
		for (final String hullModId : variant.getSMods()) {
			if (enhancedMods.contains(hullModId)) continue;
			if (embeddedMods.contains(hullModId)) continue;

			enhancedMods.add(hullModId); onEvent(onEnhance, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Enhanced '"+hullModId+"'");
		}

		for (final String hullModId : variant.getSModdedBuiltIns()) {
			if (embeddedMods.contains(hullModId)) continue;
			if (enhancedMods.contains(hullModId)) enhancedMods.remove(hullModId);

			embeddedMods.add(hullModId); onEvent(onEnhance, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Enhanced embedded '"+hullModId+"'");
		}

		for (iterator = enhancedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSMods().contains(hullModId)) continue;

			iterator.remove(); onEvent(onNormalize, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Normalized '"+hullModId+"'");
		}

		for (iterator = embeddedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSModdedBuiltIns().contains(hullModId)) continue;

			iterator.remove(); onEvent(onNormalize, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Normalized embedded '"+hullModId+"'");
		}
	}

	private void checkSuppressedMods() {
		for (final String hullModId : variant.getSuppressedMods()) {
			if (suppressedMods.contains(hullModId)) continue;

			suppressedMods.add(hullModId); onEvent(onSuppress, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Suppressed '"+hullModId+"'");
		}

		for (iterator = suppressedMods.iterator(); iterator.hasNext();) { final String hullModId = iterator.next();
			if (variant.getSuppressedMods().contains(hullModId)) continue;

			iterator.remove(); onEvent(onRestore, variant, hullModId);
			if (lyr_lunaSettingsListener.logEventInfo) logger.info(logPrefix+"ST-"+memberId+": Restored '"+hullModId+"'");
		}
	}
}
