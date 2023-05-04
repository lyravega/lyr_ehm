package data.hullmods.ehm;

import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * When a change is detected through the {@link _ehm_basetracker tracker}, additional events can
 * be fired. This interface provides custom methods, that can be used by those events' handler.
 * <p> Even if the hull modifications are not going to utilize the custom methods, they still
 * need to implement the interface as the handler stores the hull modification's object as one.
* @see {@link _ehm_basetracker Tracker Base} Provides tracking features for the fleet/ship
* @see {@link _ehm_eventhandler Event Handler} Used by the modifications to register the events
 * @author lyravega
 */
public interface _ehm_eventmethod {
	public void onInstall(ShipVariantAPI variant);
	public void onRemove(ShipVariantAPI variant);
	public void sModCleanUp(ShipVariantAPI variant);
}
