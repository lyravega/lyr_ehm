package lyravega.listeners.events;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import experimentalHullModifications.hullmods.ehm._ehm_tracker;
import lyravega.listeners.lyr_shipTracker;

/**
 * When a weapon change is detected through the {@link _ehm_tracker tracker}, the event methods
 * in this interface will be called for any hull modification effect that implements this.
 * <p> The hull modifications are registered as having this interface implemented during
 * {@link lyravega.plugin.lyr_ehm#onApplicationLoad() onApplicationLoad()}
 * @see {@link _ehm_tracker Tracker Base} the hull modification base with ship tracking methods and fleet tracking
 * @see {@link lyr_shipTracker Ship Tracker} to cache the old variants and compare them with the newer ones
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
	 */
	public void onWeaponInstall(ShipVariantAPI variant, String weaponId);

	/**
	 * Broadcasted when a weapon is removed from the refit ship, caught by this method.
	 * Further filtering may be necessary depending on the usage 
	 * <p> Effects here will be transient as these methods are called only once after their
	 * events. Should be used mainly to change the variant or to trigger an UI effect
	 * @param variant
	 * @param weaponId of the removed weapon
	 */
	public void onWeaponRemove(ShipVariantAPI variant, String weaponId);
}
