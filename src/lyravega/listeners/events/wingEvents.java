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
	 * An event is broadcasted when a wing is changed on the refit ship, which is then caught by
	 * this method for the hull modifications that implement this interface and are installed on the
	 * ships. Further filtering may be necessary depending on the usage.
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param wingId of the assigned wing
	 * @param bayNumber of the bay that the wing is housed at
	 */
	public void onWingAssigned(MutableShipStatsAPI stats, String wingId, int bayNumber);

	/**
	 * An event is broadcasted when a wing is changed on the refit ship, which is then caught by
	 * this method for the hull modifications that implement this interface and are installed on the
	 * ships. Further filtering may be necessary depending on the usage.
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param wingId of the relieved wing
	 * @param bayNumber of the bay that the wing was housed at
	 */
	public void onWingRelieved(MutableShipStatsAPI stats, String wingId, int bayNumber);
}
