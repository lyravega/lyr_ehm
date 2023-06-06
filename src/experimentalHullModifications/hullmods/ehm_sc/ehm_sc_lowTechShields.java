package experimentalHullModifications.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_magentaShields" for save compatibility
 * @category Shield Cosmetic 
 * @author lyravega
 */
public class ehm_sc_lowTechShields extends _ehm_sc_base {
	private static Color innerColour = new Color(255,125,125,75);
	private static Color ringColour = new Color(255,255,255,255);

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyShieldCosmetics(variant, innerColour, ringColour));
	}
}
