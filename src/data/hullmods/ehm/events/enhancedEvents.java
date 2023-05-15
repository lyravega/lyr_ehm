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
public interface enhancedEvents {
	/**
	 * Fired if the refit ship has a new s-mod
	 * @param variant
	 */
	public void onEnhance(ShipVariantAPI variant);

	/**
	 * Fired if the refit ship no longer has an s-mod
	 * <p> Is primarily used to remove any lasting effects that the s-mod may introduce
	 * @param variant
	 */
	public void onNormalize(ShipVariantAPI variant);
}
