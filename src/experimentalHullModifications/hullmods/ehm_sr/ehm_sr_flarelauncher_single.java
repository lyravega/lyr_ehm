package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/**@category System Retrofit 
 * @author lyravega
 */
public final class ehm_sr_flarelauncher_single extends _ehm_sr_base {
	private static final String systemId = "flarelauncher_single";

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (systemId.equals(variant.getHullSpec().getShipSystemId())) return;

		variant.setHullSpecAPI(ehm_systemRetrofit(variant, systemId));
	}
}
