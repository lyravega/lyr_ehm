package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.customEngineData;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods._ehm_customizable;
import lyravega.proxies.lyr_engineBuilder.engineStyle;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public class ehm_cec_greenEngines extends _ehm_ec_base implements _ehm_customizable {
	private static final String customEngineDataId = ehm_cec_greenEngines.class.getSimpleName().toUpperCase();
	private static final int style = engineStyle.custom;
	private static Object engineData;

	@Override
	public void ehm_applyCustomization() {
		String settingIdPrefix = this.getClass().getSimpleName()+"_";

		generateEngineData(settingIdPrefix, customEngineDataId);
		engineData = customEngineData.get(customEngineDataId);
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

		variant.setHullSpecAPI(ehm_pimpMyEngineSlots(variant, style, engineData));
	}
}
