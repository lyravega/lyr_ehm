package data.hullmods.ehm_ar;

import static lyr.tools._lyr_uiTools.commitChanges;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import lyr.misc.lyr_internals;
import lyr.proxies.lyr_hullSpec;
import lyr.proxies.lyr_weaponSlot;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public class ehm_ar_stepupadapter extends _ehm_ar_base {
	/**
	 * An inner class to supply the converters with relevant child data
	 */
	private static class childParameters {
		private String childSuffix; // childIds are used as position identifier, and used as a suffix
		private int childCost;
		private WeaponSize childSize;

		protected childParameters(String childSuffix, WeaponSize childSize, int childCost) {
			this.childSuffix = childSuffix;
			this.childCost = childCost;
			this.childSize = childSize;
		}

		public String getChildSuffix() {
			return this.childSuffix;
		}

		private int getChildCost() {
			return this.childCost;
		}

		private WeaponSize getChildSize() {
			return this.childSize;
		}
	}
	
	private static Map<String, childParameters> converters = new HashMap<String, childParameters>();
	private static childParameters mediumToLarge = new childParameters("ML", WeaponSize.LARGE, 2);
	private static childParameters smallToLarge = new childParameters("SL", WeaponSize.LARGE, 3);
	private static childParameters smallToMedium = new childParameters("SM", WeaponSize.MEDIUM, 1);
	static {
		converters.put(lyr_internals.id.shunts.converters.mediumToLarge, mediumToLarge);
		converters.put(lyr_internals.id.shunts.converters.smallToLarge, smallToLarge);
		converters.put(lyr_internals.id.shunts.converters.smallToMedium, smallToMedium);
	}

	private static Map<WeaponSize, Integer> slotValue = new HashMap<WeaponSize, Integer>();
	static {
		slotValue.put(WeaponSize.LARGE, 4);
		slotValue.put(WeaponSize.MEDIUM, 2);
		slotValue.put(WeaponSize.SMALL, 1);
	}

	private static Map<String, Integer> converterCost = new HashMap<String, Integer>();
	static {
		converterCost.put(lyr_internals.id.shunts.converters.mediumToLarge, 2);
		converterCost.put(lyr_internals.id.shunts.converters.smallToLarge, 3);
		converterCost.put(lyr_internals.id.shunts.converters.smallToMedium, 1);
	}

	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them.
	 * Adapters are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 */
	private static final void ehm_stepUpAdapter(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		int slotPoints = 0;
		SortedSet<String> sortedFittedWeaponSlots = new TreeSet<String>(variant.getFittedWeaponSlots());

		// slot conversion for diverters
		for (String slotId: sortedFittedWeaponSlots) {	// need to use a sorted set to keep diversion/conversion in order
			if (variant.getSlot(slotId) == null) continue;	// short-circuit to avoid a potential null pointer (may happen when vanilla hullSpec is (re)loaded)
			// if (slotId.startsWith(lyr_internals.affix.adaptedSlot)) continue;	// short-circuit to prevent diverters working on adapted slots
			if (slotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// short-circuit to prevent diverters working on converted slots

			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (!lyr_internals.id.shunts.diverters.set.contains(weaponId)) continue; // to short-circuit the function if it isn't a shunt

			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId); 

			// slotPoints += slotValue.get(weaponSize);	// needs to be calculated afterwards like mutableStat bonus as this block will execute only on install
			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(slotId, weaponId);
			refreshRefit = true; 
		}

		// slotPoints calculation
		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check decorative

			String slotId = slot.getId();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId); if (weaponSpec == null) continue;	// skip empty slots
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (lyr_internals.id.shunts.diverters.set.contains(weaponId)) slotPoints += slotValue.get(weaponSize); // to short-circuit the function if it isn't a shunt
			else if (lyr_internals.id.shunts.converters.set.contains(weaponId)) slotPoints -= converterCost.get(weaponId); // to short-circuit the function if it isn't a shunt
		}

		// slot conversion for converters
		for (String slotId: sortedFittedWeaponSlots) {	// need to use a sorted set to keep diversion/conversion in order
			if (variant.getSlot(slotId) == null) continue;	// short-circuit to avoid a potential null pointer (may happen when vanilla hullSpec is (re)loaded)
			if (slotId.startsWith(lyr_internals.affix.adaptedSlot)) continue;	// short-circuit to prevent converters working on adapted slots
			if (slotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// short-circuit to prevent converters working on converted slots

			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (!lyr_internals.id.shunts.converters.set.contains(weaponId)) continue; // to short-circuit the function if it isn't a shunt

			childParameters childParameters = converters.get(weaponId);
			int childCost = childParameters.getChildCost(); if (slotPoints - childCost < 0) continue;

			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = lyr_internals.affix.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible
			Vector2f childSlotLocation = parentSlot.retrieve().getLocation();
			WeaponSize childSlotSize = childParameters.getChildSize();

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

			hullSpec.addWeaponSlot(childSlot.retrieve());

			slotPoints -= converterCost.get(weaponId);	// needs to be subtracted from here on initial install to avoid infinite installs
			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(slotId, weaponId);
			refreshRefit = true; 
		}

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ehm_stepUpAdapter(stats); 
	}

	//#region INSTALLATION CHECKS
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		if (ehm_hasWeapons(ship, lyr_internals.affix.convertedSlot)) return false;

		return true;
	}
	//#endregion
}
