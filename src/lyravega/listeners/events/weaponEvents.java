package lyravega.listeners.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.listeners.lyr_eventDispatcher;
import lyravega.listeners.lyr_shipTracker;

/**
 * When a weapon change is detected, the event methods in this interface will be called
 * for any hull modification effect that implements this.
 * <p> The hull modifications should be registered in {@code onApplicationLoad()}
 * through {@link lyr_eventDispatcher#registerModsWithEvents(String, String)}
 * @see {@link lyr_shipTracker} to cache the old variants and compare them with the newer ones
 * @author lyravega
 */
public interface weaponEvents {
	/**
	 * Broadcasted when a weapon is installed on the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage 
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 * @param weaponId of the installed weapon
	 * @param slotId of the slot that the weapon is installed at
	 */
	public void onWeaponInstall(ShipVariantAPI variant, String weaponId, String slotId);

	/**
	 * Broadcasted when a weapon is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage 
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 * @param weaponId of the removed weapon
	 * @param slotId of the slot that the weapon is removed from
	 */
	public void onWeaponRemove(ShipVariantAPI variant, String weaponId, String slotId);
}
