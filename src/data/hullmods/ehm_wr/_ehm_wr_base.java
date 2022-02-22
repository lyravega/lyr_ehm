package data.hullmods.ehm_wr;

import java.util.Collection;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import data.hullmods._ehm_base;
import data.hullmods.ehm_ar._ehm_ar_base;
import data.hullmods.ehm_sr._ehm_sr_base;
import lyr.lyr_hullSpec;
import lyr.lyr_weaponSlot;

/**
 * This class is used by weapon retrofit hullmods. They are pretty 
 * straightforward in their operation; change all of the weapon slots 
 * on a ship to a different type. 
 * </p>
 * Reason to split this as another base was primarily maintenance.
 * @see {@link _ehm_ar_base} for slot adapter base
 * @see {@link _ehm_sr_base} for system retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_wr_base extends _ehm_base {
	/**
	 * Alters the weapon slots on the passed hullSpec, and returns it. The returned 
	 * hullSpec needs to be installed on the variant.
	 * @param variant that will have its weapon slots altered
	 * @param conversions is a map that pairs slot types
	 * @return a hullSpec to be installed on the variant
	 * @see {@link #ehm_weaponSlotRestore()} reverses this process one slot at a time
	 */
	protected static final ShipHullSpecAPI ehm_weaponSlotRetrofit(ShipVariantAPI variant, Map<WeaponType, WeaponType> conversions) {	
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);

		for (WeaponSlotAPI slot: hullSpec.retrieve().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			WeaponType convertFrom = slot.getWeaponType();
			
			if (conversions.containsKey(convertFrom)) {
				WeaponType convertTo = (WeaponType) conversions.get(convertFrom); // Why is the typecast necessary here? Doesn't '.get()' return a 'WeaponType'?!?
				hullSpec.getWeaponSlot(slotId).setWeaponType(convertTo);
			} 
		}
		
		return hullSpec.retrieve();
	}

	/**
	 * Compares the weapon slots of a stock hullSpec to the variant's hullspec. Restores 
	 * altered slots to the originals, ignoring the decorative slots that an adapter 
	 * might have altered. As there might be other things that alter these slots, 
	 * restoring only the necessary ones is preferable.
	 * @param variant with the altered weapon slots
	 * @return the restored hullSpec in near-mint condition
	 * @see {@link data.scripts.shipTrackerScript} only called externally by this script
	 */
	public static final ShipHullSpecAPI ehm_weaponSlotRestore(ShipVariantAPI variant) {
		ShipHullSpecAPI stockHullSpec = ehm_hullSpecClone(variant, true);
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);

		for (WeaponSlotAPI stockSlot: stockHullSpec.getAllWeaponSlotsCopy()) {
			String slotId = stockSlot.getId();
			lyr_weaponSlot slot = hullSpec.getWeaponSlot(slotId);

			if (!slot.isWeaponSlot()) continue;
			WeaponType stockSlotWeaponType = stockSlot.getWeaponType();

			if (slot.retrieve().isDecorative()) {
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"L").setWeaponType(stockSlotWeaponType);
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"R").setWeaponType(stockSlotWeaponType);
			} else {
				slot.setWeaponType(stockSlotWeaponType);
			}
		}

		return hullSpec.retrieve();
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