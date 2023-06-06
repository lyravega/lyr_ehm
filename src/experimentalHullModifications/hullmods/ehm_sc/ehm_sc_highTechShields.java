package experimentalHullModifications.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_cyanShields" for save compatibility
 * @category Shield Cosmetic 
 * @author lyravega
 */
public class ehm_sc_highTechShields extends _ehm_sc_base {
	private Color innerColour = new Color(125,125,255,75);
	private Color ringColour = new Color(255,255,255,255);
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_pimpMyShield(variant, innerColour, ringColour));
	}
}