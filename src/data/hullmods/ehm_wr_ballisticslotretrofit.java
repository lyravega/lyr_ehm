package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

/**@category Weapon Retrofit 
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class ehm_wr_ballisticslotretrofit extends _ehm_wr_base {
	private static Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.ENERGY, WeaponType.BALLISTIC);
		conversion.put(WeaponType.MISSILE, WeaponType.COMPOSITE);
		conversion.put(WeaponType.SYNERGY, WeaponType.COMPOSITE);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(stats.getVariant(), conversion));
	}
}
