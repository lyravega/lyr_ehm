package data.hullmods.ehm.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import data.hullmods.ehm._ehm_basetracker;

/**
 * When a change is detected through the {@link _ehm_basetracker tracker}, the event methods
 * in this interface will be fired if the hull modification's effect implements it.
 * <p> The hull modifications are registered as having this interface implemented during
 * {@link lyr.lyr_plugin#onApplicationLoad() onApplicationLoad()}
 * @see {@link _ehm_basetracker Tracker Base} Provides tracking features for the fleet/ship
 * @author lyravega
 */
public interface suppressedEvents {
	/**
	 * Fired if this hull modification is suppressed on the refit ship.
	 * <p> Will also be detected as a removed hull modification (!)
	 * @param variant
	 */
	public void onSuppress(ShipVariantAPI variant);

	/**
	 * Fired if this hull modification is no longer suppressed on the refit ship.
	 * <p> Will also be detected as an installed hull modification (!)
	 * @param variant
	 */
	public void onRestore(ShipVariantAPI variant);
}
