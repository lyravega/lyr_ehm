package lyravega.listeners;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

/**
 * Abstraction layer for {@link lyr_fleetTracker}
 * @author lyravega
 */
public interface _lyr_abstractTracker {
	public lyr_shipTracker getShipTracker(ShipVariantAPI variant);

	public void updateShipTracker(FleetMemberAPI member);

	public void updateShipTracker(ShipAPI ship);

	public void updateShipTracker(MutableShipStatsAPI stats);
}
