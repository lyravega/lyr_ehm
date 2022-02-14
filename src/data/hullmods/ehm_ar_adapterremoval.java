package data.hullmods;

import java.util.Collection;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

// TODO: move this to its base, and clean it up

/**
 * To simply put, grabs a stock hullSpec, and applies it to the variant. As the
 * base retrofit is still there, it will reclone the 
 */
/**@category Adapter Removal 
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class ehm_ar_adapterremoval extends _ehm_ar_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_adapterRemoval(variant)); 
		
		// the screen will refresh without the one below, from the base hullmod,
		// however, this is necessary for a 2nd 'hidden' refresh, to retain the
		// to properly set the refit screen to the correct variant
		refreshRefit(); 
	}

	//#region INSTALLATION CHECKS
	// all checks are reversed for this one, as it is a removal tool
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.adapterRetrofit, hullModSpecId)) return null; 
		
		return ehm.excuses.noAdapterRetrofit; 
	}

	@Override
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { 
		ShipVariantAPI variant = ship.getVariant();
		Collection<String> fittedWeapons = variant.getFittedWeaponSlots();
		fittedWeapons.retainAll(variant.getNonBuiltInWeaponSlots());

		if (!fittedWeapons.isEmpty()) return ehm.excuses.hasWeapons;

		return null;
	}
	//#endregion
}
