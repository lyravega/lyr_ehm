package experimentalHullModifications.hullmods.ehm_wr;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

/**@category Weapon Retrofit 
 * @author lyravega
 */
public class ehm_wr_universalslotretrofit extends _ehm_wr_base {
	private static final Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.BALLISTIC, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.ENERGY, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.MISSILE, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.COMPOSITE, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.HYBRID, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.SYNERGY, WeaponType.UNIVERSAL);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(variant, conversion, null));
	}
}
