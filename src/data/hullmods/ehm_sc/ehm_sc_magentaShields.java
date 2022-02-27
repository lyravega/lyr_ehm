package data.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**@category Shield Cosmetic 
 * @author lyravega
 */
public class ehm_sc_magentaShields extends _ehm_sc_base {
	private static final Color inner = new Color(192, 0, 192, 128);
	private static final Color ring = new Color(192, 0, 192, 128);

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_pimpMyShield(variant, inner, ring));
	}
}
