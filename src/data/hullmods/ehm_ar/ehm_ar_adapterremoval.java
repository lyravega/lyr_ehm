package data.hullmods.ehm_ar;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * To simply put, grabs a stock hullSpec, and applies it to the variant. The 
 * removal of mods is caused by hullMod incompatibility. Both belong to the
 * same group, and as such, when this is installed alongside another adapter,
 * game will remove both mods at the same time, but not before applying the 
 * effects of the adapter hullmod.
 * @category Adapter Removal 
 * @author lyravega
 */
@Deprecated // deprecated yet again, due to how the refresh script is now being a UI hack
public class ehm_ar_adapterremoval extends _ehm_ar_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant(); 

		variant.setHullSpecAPI(ehm_adapterRemoval(variant)); 
	}

	//#region INSTALLATION CHECKS
	// all checks are reversed for this one, as it is a removal tool
	// @Override
	// protected String ehm_unapplicableReason(ShipAPI ship) {
	// 	if (ship == null) return ehm.tooltip.excuses.noShip; 

	// 	if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.tooltip.excuses.lacksBase; 
	// 	if (ehm_hasRetrofitTag(ship, ehm.tag.adapterRetrofit, hullModSpecId)) return null; 
		
	// 	return ehm.tooltip.excuses.noAdapterRetrofit; 
	// }

	// @Override
	// protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { 
	// 	ShipVariantAPI variant = ship.getVariant();

	// 	if (ehm_hasWeapons(variant, ehm.affix.adaptedSlot)) return ehm.tooltip.excuses.hasWeaponsOnAdaptedSlots;

	// 	return null;
	// }
	//#endregion
}
