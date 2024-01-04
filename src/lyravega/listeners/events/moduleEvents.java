package lyravega.listeners.events;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
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
 * @category Event Handler
 */
public interface moduleEvents {
	/**
	 * Broadcasted when a module is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage, as the only filter
	 * prior is whether this hull modification is installed on the variant
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param moduleVariant of the installed module
	 * @param moduleSlotId of the slot that the module is installed at
	 */
	public void onModuleInstalled(MutableShipStatsAPI stats, ShipVariantAPI moduleVariant, String moduleSlotId);

	/**
	 * Broadcasted when a module is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage, as the only filter
	 * prior is whether this hull modification is installed on the variant
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param moduleVariant of the removed module
	 * @param moduleSlotId of the slot that the module is removed from
	 */
	public void onModuleRemoved(MutableShipStatsAPI stats, ShipVariantAPI moduleVariant, String moduleSlotId);
}
