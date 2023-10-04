package lyravega.listeners.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.listeners.lyr_shipTracker;

/**
 * When a change is detected, the event methods in this interface will be called if the
 * hull modification's effect implements it.
 * <p> The hull modifications are registered as having this interface implemented during
 * {@link lyravega.plugin.lyr_ehm#onApplicationLoad() onApplicationLoad()}
 * @see {@link lyr_shipTracker} to cache the old variants and compare them with the newer ones
 * @author lyravega
 */
public interface enhancedEvents {
	/**
	 * Called if this hull modification is enhanced with a story point on the refit ship
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 */
	public void onEnhance(ShipVariantAPI variant);

	/**
	 * Called if this hull modification is no longer enhanced on the refit ship
	 * <p> Is primarily used to remove any lasting effects that the s-mod effect may introduce
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 */
	public void onNormalize(ShipVariantAPI variant);
}
