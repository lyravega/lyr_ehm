package lyravega.listeners;

import static lyravega.listeners.lyr_eventDispatcher.events.*;

import java.util.*;

import org.json.JSONArray;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.plugin.lyr_ehm;	// still connected to other package
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
			onInstalled = "onInstalled",
			onRemoved = "onRemoved",
			onEnhanced = "onEnhanced",
			onNormalized = "onNormalized",
			onSuppressed = "onSuppressed",
			onRestored = "onRestored",
			onWeaponInstalled = "onWeaponInstalled",
			onWeaponRemoved = "onWeaponRemoved",
			onWingAssigned = "onWingAssigned",
			onWingRelieved = "onWingRelieved",
			onModuleInstalled = "onModuleInstalled",
			onModuleRemoved = "onModuleRemoved";
	}

	// hull modification effects that implement any of the event interfaces are stored in these maps
	private static final Map<String, weaponEvents> weaponEvents = new HashMap<String, weaponEvents>();
	private static final Map<String, wingEvents> wingEvents = new HashMap<String, wingEvents>();
	private static final Map<String, moduleEvents> moduleEvents = new HashMap<String, moduleEvents>();
	private static final Map<String, normalEvents> normalEvents = new HashMap<String, normalEvents>();
	private static final Map<String, enhancedEvents> enhancedEvents = new HashMap<String, enhancedEvents>();
	private static final Map<String, suppressedEvents> suppressedEvents = new HashMap<String, suppressedEvents>();
	private static final Map<String, Map<String, customizableMod>> allCustomizableMods = new HashMap<String, Map<String, customizableMod>>();
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
				if (wingEvents.class.isInstance(hullModEffect)) wingEvents.put(hullModSpec.getId(), wingEvents.class.cast(hullModEffect));
				if (moduleEvents.class.isInstance(hullModEffect)) moduleEvents.put(hullModSpec.getId(), moduleEvents.class.cast(hullModEffect));
				if (normalEvents.class.isInstance(hullModEffect)) normalEvents.put(hullModSpec.getId(), normalEvents.class.cast(hullModEffect));
				if (enhancedEvents.class.isInstance(hullModEffect)) enhancedEvents.put(hullModSpec.getId(), enhancedEvents.class.cast(hullModEffect));
				if (suppressedEvents.class.isInstance(hullModEffect)) suppressedEvents.put(hullModSpec.getId(), suppressedEvents.class.cast(hullModEffect));
				if (customizableMod.class.isInstance(hullModEffect)) customizableMods.put(hullModSpec.getId(), customizableMod.class.cast(hullModEffect));

				lyr_logger.debug("Processed hull modification '"+hullModSpec.getId()+"' from '"+modId+"'");
			}

			allCustomizableMods.put(modId, customizableMods);
			allModEvents.addAll(normalEvents.keySet());
			allModEvents.addAll(enhancedEvents.keySet());
			allModEvents.addAll(suppressedEvents.keySet());

			lyr_logger.info("Hull modifications from the mod '"+modId+"' are processed");
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
	 * @param stats of the ship
	 * @param hullModId of the hull modification
	 */
	static void onHullModEvent(final String eventName, final MutableShipStatsAPI stats, final String hullModId) {
		if (allModEvents.contains(hullModId)) switch (eventName) {
			case onInstalled:		if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onInstalled(stats); return;
			case onRemoved:			if (normalEvents.containsKey(hullModId)) normalEvents.get(hullModId).onRemoved(stats); return;
			case onEnhanced:		if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onEnhanced(stats); return;
			case onNormalized:		if (enhancedEvents.containsKey(hullModId)) enhancedEvents.get(hullModId).onNormalized(stats); return;
			case onSuppressed:		if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onSuppressed(stats); return;
			case onRestored:		if (suppressedEvents.containsKey(hullModId)) suppressedEvents.get(hullModId).onRestored(stats); return;
			default: return;
		} else if (lyr_ehm.lunaSettings.getPlayDrillSoundForAll()) switch (eventName) {
			case onInstalled:
			case onRemoved:			lyr_interfaceUtilities.playDrillSound(); return;
			default: return;
		}
	}

	/**
	 * Broadcasts a weapon event to all hull modifications registered with the related interface.
	 * Only the variants that have the hull modification installed will handle the event.
	 * <p> Further filtering needs to be done as the only filtering done on this level is only
	 * a simple check if the variant has a relevant hull modification installed.
	 */
	static void onWeaponEvent(final String eventName, final MutableShipStatsAPI stats, final String weaponId, final String slotId) {
		switch (eventName) {
			case onWeaponInstalled:	for (String hullModId: weaponEvents.keySet()) if (stats.getVariant().hasHullMod(hullModId)) weaponEvents.get(hullModId).onWeaponInstalled(stats, weaponId, slotId); return;
			case onWeaponRemoved:	for (String hullModId: weaponEvents.keySet()) if (stats.getVariant().hasHullMod(hullModId)) weaponEvents.get(hullModId).onWeaponRemoved(stats, weaponId, slotId); return;
			default: return;
		}
	}

	/**
	 * Broadcasts a wing event to all hull modifications registered with the related interface.
	 * Only the variants that have the hull modification installed will handle the event.
	 * <p> Further filtering needs to be done as the only filtering done on this level is only
	 * a simple check if the variant has a relevant hull modification installed.
	 */
	static void onWingEvent(final String eventName, final MutableShipStatsAPI stats, final String weaponId, final int bayNumber) {
		switch (eventName) {
			case onWingAssigned:	for (String hullModId: wingEvents.keySet()) if (stats.getVariant().hasHullMod(hullModId)) wingEvents.get(hullModId).onWingAssigned(stats, weaponId, bayNumber); return;
			case onWingRelieved:	for (String hullModId: wingEvents.keySet()) if (stats.getVariant().hasHullMod(hullModId)) wingEvents.get(hullModId).onWingRelieved(stats, weaponId, bayNumber); return;
			default: return;
		}
	}

	static void onModuleEvent(final String eventName, final MutableShipStatsAPI stats, final ShipVariantAPI moduleVariant, final String moduleSlotId) {
		switch (eventName) {
			case onModuleInstalled:	for (String hullModId: moduleEvents.keySet()) if (stats.getVariant().hasHullMod(hullModId)) moduleEvents.get(hullModId).onModuleInstalled(stats, moduleVariant, moduleSlotId); return;
			case onModuleRemoved:	for (String hullModId: moduleEvents.keySet()) if (stats.getVariant().hasHullMod(hullModId)) moduleEvents.get(hullModId).onModuleRemoved(stats, moduleVariant, moduleSlotId); return;
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
	public static void onSettingsChange(final String modId, final String hullModId) {
		try {
			if (hullModId != null) allCustomizableMods.get(modId).get(hullModId).updateData();
			else for (customizableMod customizableMod : allCustomizableMods.get(modId).values()) customizableMod.updateData();
			lyr_logger.debug("Called 'updateData()' in the event dispatcher"+(hullModId != null ? " for '"+hullModId+"'" : ""));
		} catch (Throwable t) {
			lyr_logger.error("Failure in 'updateData()' in the event dispatcher", t);
		}
	}
}
