package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.proxies.lyr_engineBuilder.engineStyleIds;

/**@category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_ec_midlineEngines extends _ehm_ec_base {
	private final int engineStyleId = engineStyleIds.midline;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyEngineCosmetics(variant, this.engineStyleId));
	}
}
