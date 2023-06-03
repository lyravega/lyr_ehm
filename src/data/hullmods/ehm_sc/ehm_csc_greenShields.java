package data.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import data.hullmods._ehm_customizable;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_greenShields" for save compatibility
 * @category Custom Shield Cosmetic 
 * @author lyravega
 */
public class ehm_csc_greenShields extends _ehm_sc_base implements _ehm_customizable {
	private Color innerColour;
	private Color ringColour;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		ehm_applyCustomization();
	}

	@Override
	public void ehm_applyCustomization() {
		String settingIdPrefix = this.getClass().getSimpleName();

		this.innerColour = getLunaRGBAColour(settingIdPrefix+"_inner");
		this.ringColour = getLunaRGBAColour(settingIdPrefix+"_ring");
		this.hullModSpec.setDisplayName(getLunaName(settingIdPrefix));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_pimpMyShield(variant, innerColour, ringColour));
	}
}
