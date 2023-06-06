package experimentalHullModifications.hullmods.ehm.interfaces;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import experimentalHullModifications.hullmods.ehm._ehm_tracker;

/**
 * When a change is detected through the {@link _ehm_tracker tracker}, the event methods
 * in this interface will be fired if the hull modification's effect implements it.
 * <p> The hull modifications are registered as having this interface implemented during
 * {@link lyravega.plugin.lyr_ehm#onApplicationLoad() onApplicationLoad()}
 * @see {@link _ehm_tracker Tracker Base} Provides tracking features for the fleet/ship
 * @author lyravega
 */
public interface enhancedEvents {
	/**
	 * Fired if this hull modification is enhanced with a story point on the refit ship.
	 * @param variant
	 */
	public void onEnhance(ShipVariantAPI variant);

	/**
	 * Fired if this hull modification is no longer enhanced on the refit ship.
	 * <p> Is primarily used to remove any lasting effects that the s-mod effect may introduce
	 * @param variant
	 */
	public void onNormalize(ShipVariantAPI variant);
}
