package data.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**@category System Retrofit 
 * @author lyravega
 * @version 0.5
 * @since 0.1
 */
public class ehm_sr_drone_pd_x2 extends _ehm_sr_base {
	private static final String systemId = "drone_pd_x2";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if(variant.getHullSpec().getShipSystemId().equals(systemId)) return;

		variant.setHullSpecAPI(ehm_systemRetrofit(stats.getVariant(), systemId));
	}
}
