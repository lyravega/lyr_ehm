package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.engineStyleIds.custom;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.customizableMod;
import lyravega.utilities.lyr_lunaUtilities;

/**@category Engine Cosmetic
 * @author lyravega
 */
public final class ehm_cec_greenEngines extends _ehm_ec_base implements customizableMod {
	private final int engineStyleId = custom;
	private Object engineStyleSpec;

	@Override
	public void applyCustomization() {
		String id = this.getClass().getSimpleName();

		this.engineStyleSpec = newCustomEngineStyleSpec(id);
		this.hullModSpec.setDisplayName(lyr_lunaUtilities.getLunaName(ehm_internals.ids.mod, id));
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyEngineCosmetics(variant, this.engineStyleId, this.engineStyleSpec));
	}
}
