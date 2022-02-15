package data.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.loading.specs.HullVariantSpec;

/**@category System Retrofit 
 * @author lyravega
 * @version 0.5
 * @since 0.1
 */
public class ehm_sr_displacer extends _ehm_sr_base {
	private static final String systemId = "displacer";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		HullVariantSpec variant = HullVariantSpec.class.cast(stats.getVariant()); 

		if(variant.getHullSpec().getShipSystemId().equals(systemId)) return;

		variant.setHullSpecAPI(ehm_systemRetrofit(variant, systemId));
	}
}
