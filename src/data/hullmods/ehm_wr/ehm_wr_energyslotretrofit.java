package data.hullmods.ehm_wr;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

/**@category Weapon Retrofit 
 * @author lyravega
 * @version 0.5
 * @since 0.2
 */
public class ehm_wr_energyslotretrofit extends _ehm_wr_base {
	private static Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.BALLISTIC, WeaponType.ENERGY);
		conversion.put(WeaponType.MISSILE, WeaponType.SYNERGY);
		conversion.put(WeaponType.COMPOSITE, WeaponType.SYNERGY);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(stats.getVariant(), conversion));
	}
}
