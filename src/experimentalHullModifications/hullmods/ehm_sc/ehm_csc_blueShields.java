package experimentalHullModifications.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import lyravega.listeners.events.customizableHullMod;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_blueShields" for save compatibility
 * @category Custom Shield Cosmetic 
 * @author lyravega
 */
public final class ehm_csc_blueShields extends _ehm_sc_base implements customizableHullMod {
	private static Color innerColour;
	private static Color ringColour;

	@Override
	public void applyCustomization() {
		String settingIdPrefix = this.getClass().getSimpleName()+"_";

		innerColour = getLunaRGBAColour(settingIdPrefix+"inner");
		ringColour = getLunaRGBAColour(settingIdPrefix+"ring");
		this.hullModSpec.setDisplayName(getLunaName(settingIdPrefix));
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyShieldCosmetics(variant, innerColour, ringColour));
	}
}
