package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.plugin.lyr_settings;
import lyravega.listeners.events.*;
import lyravega.tools._ehm_helpers;
import lyravega.tools.lyr_uiTools;
import lyravega.tools.logger.lyr_logger;

/**
 * Not a listener on its own, but houses methods for registering classes
 * as having a certain event, store them in the maps, and dispatch the
 * said events when necessary 
 * @author lyravega
 */
public final class lyr_eventDispatcher {
	public static final class events {
		public static final String 
			onInstall = "onInstall",
			onRemove = "onRemove",
			onEnhance = "onEnhance",
			onNormalize = "onNormalize",
			onSuppress = "onSuppress",
			onRestore = "onRestore",
			onWeaponInstall = "onWeaponInstall",
			onWeaponRemove = "onWeaponRemove";
	}

	// hull modification effects that implement any of the event interfaces are stored in these maps
	public static final Map<String, weaponEvents> weaponEvents = new HashMap<String, weaponEvents>();
	public static final Map<String, normalEvents> normalEvents = new HashMap<String, normalEvents>();
	public static final Map<String, enhancedEvents> enhancedEvents = new HashMap<String, enhancedEvents>();
	public static final Map<String, suppressedEvents> suppressedEvents = new HashMap<String, suppressedEvents>();
	public static final Set<String> allModEvents = new HashSet<String>();

	/**
	 * Checks all of the hullmod effects and if they have implemented any events, registers them
	 * in their map. During tracking, if any one of these events are detected, the relevant event
	 * methods will be called
	 * @see {@link lyr_ehm.hullmods.ehm.ehm_base ehm_base} base hull modification that enables tracking
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents}
	 */
	public static void registerModsWithEvents() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!_ehm_helpers.isExperimentalMod(hullModSpec, true)) continue;
	
			HullModEffect hullModEffect = hullModSpec.getEffect();
	
			if (weaponEvents.class.isInstance(hullModEffect)) weaponEvents.put(hullModSpec.getId(), (weaponEvents) hullModEffect);
			if (normalEvents.class.isInstance(hullModEffect)) normalEvents.put(hullModSpec.getId(), (normalEvents) hullModEffect);
			if (enhancedEvents.class.isInstance(hullModEffect)) enhancedEvents.put(hullModSpec.getId(), (enhancedEvents) hullModEffect);
			if (suppressedEvents.class.isInstance(hullModEffect)) suppressedEvents.put(hullModSpec.getId(), (suppressedEvents) hullModEffect);
		}

		allModEvents.addAll(normalEvents.keySet());
		allModEvents.addAll(enhancedEvents.keySet());
		allModEvents.addAll(suppressedEvents.keySet());
	
		lyr_logger.info("Experimental hull modifications are registered");
	}

	/**
	 * Executes the hull modification's event method if applicable. They need to implement
	 * the relative interfaces and its methods first
	 * <p> If the hull modification doesn't have any events attached to it, then depending
	 * on the setting of the mod, a drill sound will be played
	 * @param eventName type of the event
	 * @param variant of the ship
	 * @param hullModId of the hull modification
	 */
	public static void onHullModEvent(final String eventName, final ShipVariantAPI variant, final String hullModId) {
		if (allModEvents.contains(hullModId)) switch (eventName) {
			case onInstall:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onInstall(variant); return;
			case onRemove:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onRemove(variant); return;
			case onEnhance:		if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onEnhance(variant); return;
			case onNormalize:	if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onNormalize(variant); return;
			case onSuppress:	if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onSuppress(variant); return;
			case onRestore:		if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onRestore(variant); return;
			default: return;
		} else if (lyr_settings.getPlayDrillSoundForAll()) switch (eventName) {
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
	public static void onWeaponEvent(final String eventName, final ShipVariantAPI variant, final String weaponId, final String slotId) {
		switch (eventName) {
			case onWeaponInstall:	for (String hullModId: weaponEvents.keySet()) if (variant.hasHullMod(hullModId)) weaponEvents.get(hullModId).onWeaponInstall(variant, weaponId, slotId); return;
			case onWeaponRemove:	for (String hullModId: weaponEvents.keySet()) if (variant.hasHullMod(hullModId)) weaponEvents.get(hullModId).onWeaponRemove(variant, weaponId, slotId); return;
			default: return;
		}
	}
}
