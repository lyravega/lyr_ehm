package lyravega.listeners.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.listeners.lyr_shipTracker;

/**
 * When a change is detected through the {@link _ehm_tracker tracker}, the event methods
 * in this interface will be called if the hull modification's effect implements it.
 * <p> The hull modifications are registered as having this interface implemented during
 * {@link lyravega.plugin.lyr_ehm#onApplicationLoad() onApplicationLoad()}
 * @see {@link lyr_shipTracker Ship Tracker} to cache the old variants and compare them with the newer ones
 * @author lyravega
 */
public interface normalEvents {
	/**
	 * Called if this hull modification is installed on the refit ship
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 */
	public void onInstall(ShipVariantAPI variant);

	/**
	 * Called if this hull modification is removed from the refit ship
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 */
	public void onRemove(ShipVariantAPI variant);
}
