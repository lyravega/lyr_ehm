package data.hullmods.ehm_ar;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.loading.specs.HullVariantSpec;

/**@category Adapter Retrofit 
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class ehm_ar_stepdownadapter extends _ehm_ar_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		HullVariantSpec variant = HullVariantSpec.class.cast(stats.getVariant()); 
		
		variant.setHullSpecAPI(ehm_stepDownAdapter(variant)); 
	}
}
