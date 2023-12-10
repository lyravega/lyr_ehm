package lyravega.listeners.events;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

import lyravega.listeners.lyr_eventDispatcher;
import lyravega.listeners.lyr_shipTracker;

/**
 * When a wing change is detected, the event methods in this interface will be called
 * for any hull modification effect that implements this.
 * <p> The hull modifications should be registered in {@code onApplicationLoad()}
 * through {@link lyr_eventDispatcher#registerModsWithEvents(String, String)}
 * @see {@link lyr_shipTracker} to cache the old variants and compare them with the newer ones
 * @author lyravega
 * @category Event Handler
 */
public interface wingEvents {
	/**
	 * Broadcasted when a wing is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage, as the only filter
	 * prior is whether this hull modification is installed on the variant
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param wingId of the assigned wing
	 * @param bayNumber of the bay that the wing is housed at
	 */
	public void onWingAssigned(MutableShipStatsAPI stats, String wingId, int bayNumber);

	/**
	 * Broadcasted when a wing is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage, as the only filter
	 * prior is whether this hull modification is installed on the variant
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param wingId of the relieved wing
	 * @param bayNumber of the bay that the wing was housed at
	 */
	public void onWingRelieved(MutableShipStatsAPI stats, String wingId, int bayNumber);
}
