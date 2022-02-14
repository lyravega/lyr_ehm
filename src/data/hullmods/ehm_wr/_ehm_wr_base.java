package data.hullmods.ehm_wr;

import java.util.Collection;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.loading.specs.g;
import com.fs.starfarer.loading.specs.oOoo;

import data.hullmods._ehm_base;
import data.hullmods.ehm_ar._ehm_ar_base;
import data.hullmods.ehm_sr._ehm_sr_base;

/**
 * This class is used by weapon retrofit hullmods. They 
 * are pretty straightforward in their operation; change
 * all of the weapon slots on a ship to a different type.
 * Reason to split this as another base was primarily maintenance.
 * @see {@link _ehm_ar_base} for slot adapter base
 * @see {@link _ehm_sr_base} for system retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_wr_base extends _ehm_base {
	/**
	 * Alters the weapon slots on the passed hullSpec, and returns it.
	 * The returned hullSpec needs to be installed on the variant.
	 * @param variant that will have its weapon slots altered
	 * @param conversions is a map that pairs slot types
	 * @return a hullSpec to be installed on the variant
	 */
	protected static final g ehm_weaponSlotRetrofit(ShipVariantAPI variant, Map<WeaponType, WeaponType> conversions) {
		g hullSpec = (g) variant.getHullSpec(); 

		for (oOoo slot: hullSpec.getAllWeaponSlots()) {
			WeaponType convertFrom = slot.getWeaponType();
			
			if (conversions.containsKey(convertFrom)) {
				WeaponType convertTo = (WeaponType) conversions.get(convertFrom); // Why is the typecast necessary here? Doesn't '.get()' return a 'WeaponType'?!?
				slot.setWeaponType(convertTo);
			}
		}

		return hullSpec;
	}

	public static final g ehm_weaponSlotRestore(ShipVariantAPI variant) {
		g hullSpec = (g) variant.getHullSpec(); 
		g stockHullSpec = ehm_getStockHullSpec(variant, true);

		for (oOoo stockSlot: stockHullSpec.getAllWeaponSlots()) {
			if (!stockSlot.isWeaponSlot()) continue;
			oOoo slot = hullSpec.getWeaponSlot(stockSlot.getId());
			String slotId = slot.getId();
			WeaponType stockSlotWeaponType = stockSlot.getWeaponType();

			if (slot.isDecorative()) {
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"L").setWeaponType(stockSlotWeaponType);
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"R").setWeaponType(stockSlotWeaponType);
			} else {
				hullSpec.getWeaponSlot(slotId).setWeaponType(stockSlotWeaponType);
			}
		}

		return hullSpec;
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.weaponRetrofit, hullModSpecId)) return ehm.excuses.hasWeaponRetrofit; 

		return null; 
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