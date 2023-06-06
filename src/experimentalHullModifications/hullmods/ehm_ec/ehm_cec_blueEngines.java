package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.customEngineStyleSpecs;
import static lyravega.proxies.lyr_engineBuilder.engineStyleIds.custom;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm.interfaces.customizableHullMod;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public class ehm_cec_blueEngines extends _ehm_ec_base implements customizableHullMod {
	private static final String customEngineSpecId = ehm_cec_blueEngines.class.getSimpleName().toUpperCase();
	private static final int engineStyleId = custom;
	private static Object engineData;

	@Override
	public void ehm_applyCustomization() {
		String settingIdPrefix = this.getClass().getSimpleName()+"_";

		newCustomEngineSpec(settingIdPrefix, customEngineSpecId);
		engineData = customEngineStyleSpecs.get(customEngineSpecId);
		this.hullModSpec.setDisplayName(getLunaName(settingIdPrefix));
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		ehm_applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_pimpMyEngineSlots(variant, engineStyleId, engineData));
	}
}
