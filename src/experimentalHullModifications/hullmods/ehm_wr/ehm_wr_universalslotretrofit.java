package experimentalHullModifications.hullmods.ehm_wr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

/**@category Weapon Retrofit
 * @author lyravega
 */
public final class ehm_wr_universalslotretrofit extends _ehm_wr_base {
	@Override
	public void updateData() {
		this.typeConversionMap.put(WeaponType.BALLISTIC, WeaponType.UNIVERSAL);
		this.typeConversionMap.put(WeaponType.ENERGY, WeaponType.UNIVERSAL);
		this.typeConversionMap.put(WeaponType.MISSILE, WeaponType.UNIVERSAL);
		this.typeConversionMap.put(WeaponType.COMPOSITE, WeaponType.UNIVERSAL);
		this.typeConversionMap.put(WeaponType.HYBRID, WeaponType.UNIVERSAL);
		this.typeConversionMap.put(WeaponType.SYNERGY, WeaponType.UNIVERSAL);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeWeaponTypes(stats);
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "universal";
			default: return null;
		}
	}
}
