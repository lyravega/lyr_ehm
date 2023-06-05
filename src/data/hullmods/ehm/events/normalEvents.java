package data.hullmods.ehm.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods.ehm._ehm_basetracker;

/**
 * When a change is detected through the {@link _ehm_basetracker tracker}, the event methods
 * in this interface will be fired if the hull modification's effect implements it.
 * <p> The hull modifications are registered as having this interface implemented during
 * {@link lyravega.plugin.experimentalHullModifications#onApplicationLoad() onApplicationLoad()}
 * @see {@link _ehm_basetracker Tracker Base} Provides tracking features for the fleet/ship
 * @author lyravega
 */
public interface normalEvents {
	/**
	 * Fired if this hull modification is installed on the refit ship.
	 * @param variant
	 */
	public void onInstall(ShipVariantAPI variant);

	/**
	 * Fired if this hull modification is removed from the refit ship.
	 * @param variant
	 */
	public void onRemove(ShipVariantAPI variant);
}
