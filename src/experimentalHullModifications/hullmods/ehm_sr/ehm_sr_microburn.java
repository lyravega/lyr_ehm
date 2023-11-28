package experimentalHullModifications.hullmods.ehm_sr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * @category System Retrofit
 * @author lyravega
 */
public final class ehm_sr_microburn extends _ehm_sr_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (this.systemId.equals(variant.getHullSpec().getShipSystemId())) return;

		variant.setHullSpecAPI(ehm_systemRetrofit(variant, this.systemId));
	}
}
