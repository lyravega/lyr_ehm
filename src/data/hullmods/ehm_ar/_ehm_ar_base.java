package data.hullmods.ehm_ar;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import org.lwjgl.util.vector.Vector2f;

import data.hullmods._ehm_base;
import data.hullmods._ehm_util;
import lyr._lyr_uiTools;
import lyr.lyr_hullSpec;
import lyr.lyr_weaponSlot;


/**
 * This class is used by slot adapter hullmods. Slot adapters are designed 
 * to search the ship for specific weapons, and perform operations on the 
 * hullSpec to yield interesting results, such as creating a new weapon slot. 
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_ar_base extends _ehm_base {
	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them.
	 * @param variant whose hullSpec will be altered
	 * @return an altered hullSpec
	 */
	protected static final ShipHullSpecAPI ehm_stepDownAdapter(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;

		for (String slotId: variant.getFittedWeaponSlots()) {
			if (slotId.startsWith(ehm.affix.adaptedSlot)) continue; // short-circuit to avoid weapons in adapted slots causing an error on load, must be first
			
			//WeaponType slotType = variant.getSlot(slotId).getWeaponType();
			//WeaponSize slotSize = variant.getSlot(slotId).getSlotSize();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			//WeaponType weaponType = weaponSpec.getType();
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // to avoid plugging medium universal to large universal
			if (!ehm.id.adapters.containsKey(weaponId)) continue; // to short-circuit the function if it isn't an adapter
			
			// these are separated in a switch case for now, for future expansions if there will be any
			String childFormation = ehm.id.adapters.get(weaponId);
			Map<String, Vector2f> offsets = new HashMap<String, Vector2f>();
			switch (weaponSize) {
				case LARGE: { 
					if (childFormation.equals("line")) {
						offsets.put("L", new Vector2f(0.0f, 12.0f)); // left
						offsets.put("R", new Vector2f(0.0f, -12.0f)); // right
					}
				}
				break;
				case MEDIUM: { 
					if (childFormation.equals("line")) {
						offsets.put("L", new Vector2f(0.0f, 6.0f)); // left
						offsets.put("R", new Vector2f(0.0f, -6.0f)); // right
					}
				}
				break;
				case SMALL: { 
					// there is no small adapter
				}
				break;
			}
			
			// child size is hardcoded in the loop, could be moved above with more formations in time, right now unimportant
			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId); 
			Vector2f parentSlotLocation = parentSlot.retrieve().getLocation();
			float parentSlotAngle = parentSlot.retrieve().getAngle();
			String parentSlotId = parentSlot.retrieve().getId();
			WeaponSize parentSlotSize = parentSlot.retrieve().getSlotSize();

			for (String position: offsets.keySet()) {
				lyr_weaponSlot childSlot = parentSlot.clone();

				String childSlotId = ehm.affix.adaptedSlot + parentSlotId + position; // also used as nodeId because nodeId isn't visible
				Vector2f childSlotLocation = _ehm_util.generateChildLocation(parentSlotLocation, parentSlotAngle, offsets.get(position));
				WeaponSize childSlotSize = parentSlotSize.equals(WeaponSize.LARGE) ? WeaponSize.MEDIUM : WeaponSize.SMALL;

				childSlot.setId(childSlotId);
				childSlot.setNode(childSlotId, childSlotLocation);
				childSlot.setSlotSize(childSlotSize);

			 	hullSpec.addWeaponSlot(childSlot.retrieve());
			}
			
			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(parentSlotId, weaponId);
			refreshRefit = true; 
		}
		
		if (refreshRefit) { _lyr_uiTools.refreshRefitShip(); refreshRefit = false; }
		return hullSpec.retrieve();
	}

	/** 
	 * Refreshes the hullSpec and returns it.
	 * @param variant whose hullSpec will be restored
	 * @return a restored hullSpec
	 * @see {@link data.hullmods.ehm_base#onRemoved() onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_adapterRemoval(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		variant.setHullSpecAPI(hullSpec);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.adapterRetrofit, hullModSpecId)) return ehm.excuses.hasAdapterRetrofit; 
		
		return null; 
	}
	//#endregion
}