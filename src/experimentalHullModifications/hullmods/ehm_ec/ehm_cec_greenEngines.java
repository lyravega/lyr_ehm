package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.customEngineStyleSpecs;
import static lyravega.proxies.lyr_engineBuilder.engineStyleIds.custom;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import lyravega.listeners.events.customizableHullMod;
import lyravega.plugin.lyr_settings.lyr_lunaAccessors;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public final class ehm_cec_greenEngines extends _ehm_ec_base implements customizableHullMod {
	private static final String customEngineSpecId = ehm_cec_greenEngines.class.getSimpleName().toUpperCase();
	private static final int engineStyleId = custom;
	private static Object engineStyleSpec;

	@Override
	public void applyCustomization() {
		String settingIdPrefix = this.getClass().getSimpleName()+"_";

		newCustomEngineSpec(settingIdPrefix, customEngineSpecId);
		engineStyleSpec = customEngineStyleSpecs.get(customEngineSpecId);
		this.hullModSpec.setDisplayName(lyr_lunaAccessors.getLunaName(settingIdPrefix));
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyEngineCosmetics(variant, engineStyleId, engineStyleSpec));
	}
}
