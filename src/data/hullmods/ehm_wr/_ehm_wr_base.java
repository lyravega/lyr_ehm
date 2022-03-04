package data.hullmods.ehm_wr;

import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import data.hullmods._ehm_base;
import lyr.lyr_hullSpec;
import lyr.lyr_weaponSlot;

/**
 * This class is used by weapon retrofit hullmods. They are pretty 
 * straightforward in their operation; change all of the weapon slots 
 * on a ship to a different type. 
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_wr_base extends _ehm_base {
	/**
	 * Alters the weapon slots on the passed variant's hullSpec, and returns it.
	 * @param variant whose hullSpec will be altered
	 * @param conversions is a map that pairs slot types
	 * @return an altered hullSpec with different weaponSlots
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
	 * Refers to a stock hullSpec, and restores the slots on the passed variant's
	 * hullSpec one by one. Ignores activated adapters, and affects adapted slots.
	 * @param variant whose hullSpec will have its weaponSlots restored
	 * @return an altered hullSpec with restored weaponSlots
	 * @see {@link data.hullmods.ehm_base#onRemoved() onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_weaponSlotRestore(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		ShipHullSpecAPI hullSpecReference = ehm_hullSpecReference(variant);

		for (WeaponSlotAPI stockSlot: hullSpecReference.getAllWeaponSlotsCopy()) {
			String slotId = stockSlot.getId();
			lyr_weaponSlot slot = hullSpec.getWeaponSlot(slotId);
			WeaponType stockSlotWeaponType = stockSlot.getWeaponType();

			if (slot.retrieve().isDecorative() && ehm.id.adapters.containsKey(variant.getWeaponId(slotId))) {
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
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.weaponRetrofit, hullModSpecId)) return ehm.excuses.hasWeaponRetrofit; 

		return null; 
	}

	@Override
	protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		ShipVariantAPI variant = ship.getVariant();

		if (ehm_hasWeapons(variant, ehm.id.adapters.keySet())) return ehm.excuses.hasWeapons;

		return null;
	}
	//#endregion
}