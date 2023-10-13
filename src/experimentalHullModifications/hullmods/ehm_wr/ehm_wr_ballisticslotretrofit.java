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
public final class ehm_wr_ballisticslotretrofit extends _ehm_wr_base {
	private static final Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.ENERGY, WeaponType.BALLISTIC);
		conversion.put(WeaponType.MISSILE, WeaponType.COMPOSITE);
		conversion.put(WeaponType.SYNERGY, WeaponType.COMPOSITE);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(variant, conversion, null));
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "energy";
			case 1: return "ballistic";
			case 2: return "missile";
			case 3: return "synergy";
			case 4: return "composite";
			default: return null;
		}
	}
}
