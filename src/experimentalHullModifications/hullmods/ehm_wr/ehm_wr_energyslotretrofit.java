package experimentalHullModifications.hullmods.ehm_wr;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_heavyenergyintegration;
import lyravega.misc.lyr_internals;

/**@category Weapon Retrofit
 * @see Slave: {@link ehm_mr_heavyenergyintegration}
 * @author lyravega
 */
public class ehm_wr_energyslotretrofit extends _ehm_wr_base {
	private static final String extension = lyr_internals.id.hullmods.extensions.heavyenergyintegration;
	private static final String hbi = "hbi";

	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		if (variant.getHullSpec().getBuiltInMods().contains(hbi) || variant.getPermaMods().contains(hbi)) {
			variant.addSuppressedMod(hbi);
			variant.addPermaMod(extension, false);
		}
		super.onInstall(variant);
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		if (variant.getSuppressedMods().contains(hbi)) {
			variant.removeSuppressedMod(hbi);
			variant.removePermaMod(extension);
		}
		super.onRemove(variant);
	}
	//#endregion
	// END OF CUSTOM EVENTS

	private static final Map<WeaponType,WeaponType> conversion = new HashMap<WeaponType,WeaponType>();
	static {
		conversion.put(WeaponType.BALLISTIC, WeaponType.ENERGY);
		conversion.put(WeaponType.MISSILE, WeaponType.SYNERGY);
		conversion.put(WeaponType.COMPOSITE, WeaponType.SYNERGY);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_weaponSlotRetrofit(variant, conversion, null));
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "ballistic";
			case 1: return "energy";
			case 2: return "missile";
			case 3: return "composite";
			case 4: return "synergy";
			case 5: return "Heavy Ballistics Integration";
			default: return null;
		}
	}
}
