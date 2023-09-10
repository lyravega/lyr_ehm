package experimentalHullModifications.hullmods.ehm_wr;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_expensivemissiles;

/**@category Weapon Retrofit 
 * @see Slave: {@link ehm_mr_expensivemissiles}
 * @author lyravega
 */
public final class ehm_wr_missileslotretrofit extends _ehm_wr_base {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		ehm_mr_expensivemissiles.installExtension(variant);
		super.onInstall(variant);
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		ehm_mr_expensivemissiles.removeExtension(variant);
		super.onRemove(variant);
	}
	//#endregion
	// END OF CUSTOM EVENTS

	private static final Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.HYBRID, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.BALLISTIC, WeaponType.COMPOSITE);
		conversion.put(WeaponType.ENERGY, WeaponType.SYNERGY);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(variant, conversion, null));
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "missile";
			case 1: return "ballistic";
			case 2: return "composite";
			case 3: return "energy";
			case 4: return "synergy";
			case 5: return "hybrid";
			case 6: return "universal";
			case 7: return "2/4/8 OP";
			case 8: return "Heavy Ballistics Integration";
			default: return null;
		}
	}
}
