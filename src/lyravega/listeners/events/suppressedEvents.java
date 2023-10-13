package lyravega.listeners.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.listeners.lyr_eventDispatcher;
import lyravega.listeners.lyr_shipTracker;

/**
 * When a change is detected, the event methods in this interface will be called if the
 * hull modification's effect implements it.
 * <p> The hull modifications should be registered in {@code onApplicationLoad()}
 * through {@link lyr_eventDispatcher#registerModsWithEvents(String, String)}
 * @see {@link lyr_shipTracker} to cache the old variants and compare them with the newer ones
 * @author lyravega
 */
public interface suppressedEvents {
	/**
	 * Called if this hull modification is suppressed on the refit ship
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 */
	public void onSuppress(ShipVariantAPI variant);

	/**
	 * Called if this hull modification is no longer suppressed on the refit ship
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 */
	public void onRestore(ShipVariantAPI variant);
}
