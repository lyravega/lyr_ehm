package experimentalHullModifications.hullmods.ehm_wr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_heavyenergyintegration;

/**@category Weapon Retrofit
 * @see Slave: {@link ehm_mr_heavyenergyintegration}
 * @author lyravega
 */
public final class ehm_wr_energyslotretrofit extends _ehm_wr_base {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		ehm_mr_heavyenergyintegration.installExtension(stats.getVariant());

		super.onInstalled(stats);
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		ehm_mr_heavyenergyintegration.removeExtension(stats.getVariant());

		super.onRemoved(stats);
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void updateData() {
		this.typeConversionMap.put(WeaponType.BALLISTIC, WeaponType.ENERGY);
		this.typeConversionMap.put(WeaponType.MISSILE, WeaponType.SYNERGY);
		this.typeConversionMap.put(WeaponType.COMPOSITE, WeaponType.SYNERGY);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeWeaponTypes(stats);
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
