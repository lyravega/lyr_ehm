package data.hullmods.ehm_wr;

import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import lyr.proxies.lyr_hullSpec;
import lyr.proxies.lyr_weaponSlot;

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
	 * @see {@link data.hullmods.ehm_base#onRemoved(String, ShipAPI) onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_weaponSlotRestore(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		ShipHullSpecAPI hullSpecReference = ehm_hullSpecReference(variant);

		for (WeaponSlotAPI stockSlot: hullSpecReference.getAllWeaponSlotsCopy()) {
			String slotId = stockSlot.getId();
			String weaponId = variant.getWeaponId(slotId);
			lyr_weaponSlot slot = hullSpec.getWeaponSlot(slotId);
			WeaponType stockSlotWeaponType = stockSlot.getWeaponType();
			
			// TODO take a look at strings, move them to base
			if (slot.retrieve().isDecorative() && ehm.id.adapter.set.contains(weaponId)) {
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"L").setWeaponType(stockSlotWeaponType);
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"R").setWeaponType(stockSlotWeaponType);
				if (weaponId.endsWith("Triple"))
					hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"C").setWeaponType(stockSlotWeaponType);
				else if (weaponId.endsWith("Quad")) {
					hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"FL").setWeaponType(stockSlotWeaponType);
					hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"FR").setWeaponType(stockSlotWeaponType);
				}
			} else {
				slot.setWeaponType(stockSlotWeaponType);
			}
		}

		return hullSpec.retrieve();
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(ehm.tooltip.header.notApplicable, ehm.tooltip.header.notApplicable_textColour, ehm.tooltip.header.notApplicable_bgColour, Alignment.MID, ehm.tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(ehm.tooltip.text.lacksBase, ehm.tooltip.text.padding);
			if (ehm_hasRetrofitTag(ship, ehm.tag.weaponRetrofit, hullModSpecId)) tooltip.addPara(ehm.tooltip.text.hasWeaponRetrofit, ehm.tooltip.text.padding);
		}

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(hullModSpecId) ? " IN" : " OUT";

			tooltip.addSectionHeading(ehm.tooltip.header.locked+inOrOut, ehm.tooltip.header.locked_textColour, ehm.tooltip.header.locked_bgColour, Alignment.MID, ehm.tooltip.header.padding);

			if (ehm_hasWeapons(ship, ehm.id.adapter.set)) tooltip.addPara(ehm.tooltip.text.hasWeapons, ehm.tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.weaponRetrofit, hullModSpecId)) return false; 

		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (ehm_hasWeapons(ship, ehm.id.adapter.set)) return false;

		return true;
	}
	//#endregion
}