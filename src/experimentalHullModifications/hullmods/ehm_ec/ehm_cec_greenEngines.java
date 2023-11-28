package experimentalHullModifications.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.customizableMod;
import lyravega.proxies.lyr_engineBuilder.engineStyleIds;
import lyravega.utilities.lyr_lunaUtilities;

/**@category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_cec_greenEngines extends _ehm_ec_base implements customizableMod {
	@Override
	public void applyCustomization() {
		String id = this.getClass().getSimpleName();

		this.engineStyleId = engineStyleIds.custom;
		this.engineStyleSpec = newCustomEngineStyleSpec(id);
		this.hullModSpec.setDisplayName(lyr_lunaUtilities.getLunaName(ehm_internals.ids.mod, id));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyEngineCosmetics(variant, this.engineStyleId, this.engineStyleSpec));
	}
}
