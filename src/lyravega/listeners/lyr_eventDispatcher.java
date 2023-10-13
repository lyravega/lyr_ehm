package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import org.json.JSONArray;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.plugin.ehm_settings;
import lyravega.listeners.events.*;
import lyravega.utilities.lyr_interfaceUtilities;
import lyravega.utilities.logger.lyr_logger;

/**
 * Not a listener on its own, but houses methods for registering classes
 * as having a certain event, store them in the maps, and dispatch the
 * said events when necessary 
 * @author lyravega
 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents} / {@link weaponEvents} / {@link customizableMod}
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
	private static final Map<String, weaponEvents> weaponEvents = new HashMap<String, weaponEvents>();
	private static final Map<String, normalEvents> normalEvents = new HashMap<String, normalEvents>();
	private static final Map<String, enhancedEvents> enhancedEvents = new HashMap<String, enhancedEvents>();
	private static final Map<String, suppressedEvents> suppressedEvents = new HashMap<String, suppressedEvents>();
	private static final Map<String, Map<String, customizableMod>> customizableHullMods = new HashMap<String, Map<String, customizableMod>>();
	private static final Set<String> allModEvents = new HashSet<String>();

	/**
	 * Checks all of the hullmod effects and if they have implemented any events, registers them
	 * in their map. During tracking, if any one of these events are detected, the relevant event
	 * methods will be called
	 * @param hullModCSV path to the {@code hull_mods.csv} file
	 * @param modId of the mod that the file belongs to
	 * @param exclusionTag to skip hull modifications having this tag, may be {@code null}
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents} / {@link weaponEvents}
	 */
	public static void registerModsWithEvents(String hullModCSV, String modId) {
		try {
			JSONArray loadCSV = Global.getSettings().loadCSV(hullModCSV, modId);
			final Map<String, customizableMod> customizableMods = new HashMap<String, customizableMod>();

			for (int i = 0; i < loadCSV.length(); i++) {
				HullModSpecAPI hullModSpec = Global.getSettings().getHullModSpec(loadCSV.getJSONObject(i).getString("id"));
				if (hullModSpec == null) continue;

				HullModEffect hullModEffect = hullModSpec.getEffect();
				if (weaponEvents.class.isInstance(hullModEffect)) weaponEvents.put(hullModSpec.getId(), weaponEvents.class.cast(hullModEffect));
				if (normalEvents.class.isInstance(hullModEffect)) normalEvents.put(hullModSpec.getId(), normalEvents.class.cast(hullModEffect));
				if (enhancedEvents.class.isInstance(hullModEffect)) enhancedEvents.put(hullModSpec.getId(), enhancedEvents.class.cast(hullModEffect));
				if (suppressedEvents.class.isInstance(hullModEffect)) suppressedEvents.put(hullModSpec.getId(), suppressedEvents.class.cast(hullModEffect));
				if (customizableMod.class.isInstance(hullModEffect)) customizableMods.put(hullModSpec.getId(), customizableMod.class.cast(hullModEffect));

				lyr_logger.debug("Processed hull modification '"+hullModSpec.getId()+"'");
			}

			customizableHullMods.put(modId, customizableMods);
			allModEvents.addAll(normalEvents.keySet());
			allModEvents.addAll(enhancedEvents.keySet());
			allModEvents.addAll(suppressedEvents.keySet());

			lyr_logger.info("Experimental hull modifications are registered");
		} catch (Throwable t) {
			lyr_logger.error("Could not load the hull modification .csv file '"+hullModCSV+"' for the mod with the id '"+modId+"'", t);
		}
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
		} else if (ehm_settings.getPlayDrillSoundForAll()) switch (eventName) {
			case onInstall:		
			case onRemove:		lyr_interfaceUtilities.playDrillSound(); return;
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

	/**
	 * Applies any customization that a hull modification has in its {@code applyCustomization()}
	 * method implemented through the interface. This event may be broadcasted for all registered
	 * hullmods for a given {@code modId} or dispatched to a specific one with {@code hullModId}
	 * <p> {@code modId} would be useful along with something like LunaLib to affect all of these
	 * at once. {@code hullModId} might be useful in targeting specific hullmods instead of all.
	 * There is just a simple try-catch block; take care when passing the ids.
	 * @param modId to restrict scope of this event to hullmods from a single mod
	 * @param hullModId may be {@code null} to broadcast this event to all hullmods, otherwise just to a single one  
	 */
	public static void applyCustomization(final String modId, final String hullModId) {
		try {
			if (hullModId != null) customizableHullMods.get(modId).get(hullModId).applyCustomization();
			else for (customizableMod customizableMod : customizableHullMods.get(modId).values()) customizableMod.applyCustomization();
			lyr_logger.debug("Utilized 'applyCustomization()' in the event dispatcher");
		} catch (Throwable t) {
			lyr_logger.error("Failure in 'applyCustomization()' in the event dispatcher", t);
		}
	}
}
