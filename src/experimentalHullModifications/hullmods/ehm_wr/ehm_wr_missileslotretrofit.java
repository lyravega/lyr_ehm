package experimentalHullModifications.hullmods.ehm_wr;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**@category Weapon Retrofit 
 * @author lyravega
 */
public class ehm_wr_missileslotretrofit extends _ehm_wr_base {
	private static final Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.HYBRID, WeaponType.UNIVERSAL);
		conversion.put(WeaponType.BALLISTIC, WeaponType.COMPOSITE);
		conversion.put(WeaponType.ENERGY, WeaponType.SYNERGY);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(hullModSpecId, 2);
		stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(hullModSpecId, 4);
		stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(hullModSpecId, 8);

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(variant, conversion, null));
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
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
			default: return null;
		}
	}
}
