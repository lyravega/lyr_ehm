package experimentalHullModifications.hullmods.ehm_wr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

/**@category Weapon Retrofit
 * @author lyravega
 */
public final class ehm_wr_ballisticslotretrofit extends _ehm_wr_base {
	@Override
	public void updateData() {
		this.typeConversionMap.put(WeaponType.ENERGY, WeaponType.BALLISTIC);
		this.typeConversionMap.put(WeaponType.MISSILE, WeaponType.COMPOSITE);
		this.typeConversionMap.put(WeaponType.SYNERGY, WeaponType.COMPOSITE);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeWeaponTypes(stats);
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
