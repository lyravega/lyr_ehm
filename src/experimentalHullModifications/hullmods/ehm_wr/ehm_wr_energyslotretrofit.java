package experimentalHullModifications.hullmods.ehm_wr;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_heavyenergyintegration;
import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.companionMod;

/**@category Weapon Retrofit
 * @see Companion: {@link ehm_mr_heavyenergyintegration}
 * @author lyravega
 */
public final class ehm_wr_energyslotretrofit extends _ehm_wr_base {
	@Override
	public void updateData() {
		this.typeConversionMap.put(WeaponType.BALLISTIC, WeaponType.ENERGY);
		this.typeConversionMap.put(WeaponType.MISSILE, WeaponType.SYNERGY);
		this.typeConversionMap.put(WeaponType.COMPOSITE, WeaponType.SYNERGY);

		this.companionMod = (companionMod) Global.getSettings().getHullModSpec(ehm_internals.hullmods.extensions.heavyenergyintegration).getEffect();
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
