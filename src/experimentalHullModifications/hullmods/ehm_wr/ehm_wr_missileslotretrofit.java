package experimentalHullModifications.hullmods.ehm_wr;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_expensivemissiles;
import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.companionMod;

/**@category Weapon Retrofit
 * @see Companion: {@link ehm_mr_expensivemissiles}
 * @author lyravega
 */
public final class ehm_wr_missileslotretrofit extends _ehm_wr_base {
	@Override
	public void updateData() {
		this.typeConversionMap.put(WeaponType.HYBRID, WeaponType.UNIVERSAL);
		this.typeConversionMap.put(WeaponType.BALLISTIC, WeaponType.COMPOSITE);
		this.typeConversionMap.put(WeaponType.ENERGY, WeaponType.SYNERGY);

		this.companionMod = (companionMod) Global.getSettings().getHullModSpec(ehm_internals.hullmods.extensions.expensivemissiles).getEffect();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		this.changeWeaponTypes(stats);
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
