package data.hullmods.ehm_ec;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.hullmods._ehm_customizable;
import lyravega.proxies.lyr_engineBuilder.engineStyle;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

/**@category Engine Cosmetic 
 * @author lyravega
 */
public class ehm_cec_redEngines extends _ehm_ec_base implements _ehm_customizable {
	private static final int style = engineStyle.custom;

	@Override
	public void ehm_applyCustomization() {
		// TODO Auto-generated method stub
		// throw new UnsupportedOperationException("Unimplemented method 'ehm_applyCustomization'");
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		ehm_applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		
		variant.setHullSpecAPI(ehm_pimpMyEngineSlots(variant, style));
	}
}
