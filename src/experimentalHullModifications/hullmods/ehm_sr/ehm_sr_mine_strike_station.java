package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**@category System Retrofit 
 * @author lyravega
 */
public class ehm_sr_mine_strike_station extends _ehm_sr_base {
	private static final String systemId = "mine_strike_station";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if(variant.getHullSpec().getShipSystemId().equals(systemId)) return;

		variant.setHullSpecAPI(ehm_systemRetrofit(variant, systemId));
	}
}