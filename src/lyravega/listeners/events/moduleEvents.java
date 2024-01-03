package lyravega.listeners.events;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

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
	 * Broadcasted when a weapon is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage, as the only filter
	 * prior is whether this hull modification is installed on the variant
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param weaponId of the installed weapon
	 * @param slotId of the slot that the weapon is installed at
	 */
	public void onModuleInstalled(MutableShipStatsAPI stats, String moduleVariantId, String moduleSlotId);

	/**
	 * Broadcasted when a weapon is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage, as the only filter
	 * prior is whether this hull modification is installed on the variant
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param stats
	 * @param weaponId of the removed weapon
	 * @param slotId of the slot that the weapon is removed from
	 */
	public void onModuleRemoved(MutableShipStatsAPI stats, String moduleVariantId, String moduleSlotId);
}
