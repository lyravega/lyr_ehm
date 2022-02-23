package data.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**@category Shield Cosmetic 
 * @author lyravega
 * @version 0.7
 * @since 0.6
 */
public class ehm_sc_redShields extends _ehm_sc_base {
	private static final Color inner = new Color(192, 0, 0, 128);
	private static final Color ring = new Color(192, 0, 0, 128);

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_pimpMyShield(variant, inner, ring));
	}
}
