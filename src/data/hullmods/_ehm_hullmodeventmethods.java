package data.hullmods;

import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * A simple interface whose sole purpose is providing guidance for the
 * event methods. As these methods will be invocated remotely, they are
 * not that flexible; this interface is to prevent any mistakes mostly.
 * <p> These methods' name can be anything, they can be static, they can
 * return anything, however their arguments MUST match below, and they
 * MUST be visible from anywhere (public).
 * <p> Change detection & event firing happens on the {@link
 * data.hullmods.ehm_base base hullmod}, which uses {@link 
 * data.hullmods._ehm_basetracker}. The events will be fired for the 
 * hullMods only if they are registered, and the event exists.
 * @author lyravega
 */
public interface _ehm_hullmodeventmethods {
	public void onInstall(ShipVariantAPI variant);
	public void onRemove(ShipVariantAPI variant);
	public void sModCleanUp(ShipVariantAPI variant);
}
