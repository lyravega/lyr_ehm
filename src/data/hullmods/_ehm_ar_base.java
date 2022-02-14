package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.loading.specs.classsuper;
import com.fs.starfarer.loading.specs.g;
import com.fs.starfarer.loading.specs.oOoo;

import org.lwjgl.util.vector.Vector2f;

/**
 * This class is used by slot adapter hullmods. Slot adapters 
 * are designed to search the ship for specific weapons, and
 * perform operations on the hullSpec to yield interesting
 * results, such as creating a new weapon slot. 
 * Reason to split this as another base was primarily maintenance.
 * @see {@link _ehm_sr_base} for system retrofit base
 * @see {@link _ehm_wr_base} for weapon retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_ar_base extends _ehm_base_master {
	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them.
	 * The returned hullSpec needs to be installed on the variant.
	 * @param variant that will have alterations on slots with an adapter
	 * @return a hullSpec to be installed on the variant
	 */
	// TODO: Clean this up
	protected static final g ehm_stepDownAdapter(ShipVariantAPI variant) {
		g hullSpec = (g) variant.getHullSpec(); 
		boolean refreshRefit = false;

		// getFittedWeaponSlots() vs getNonBuiltInWeaponSlots()
		// both gets slots with weapons in them
		// getFittedWeaponSlots() doesn't get WeaponType.DECORATIVE, but gets WeaponType.BUILTIN
		// getNonBuiltInWeaponSlots() doesn't get WeaponType.BUILTIN, but gets WeaponType.DECORATIVE
		// WeaponType.DECORATIVE is used for the adapter slots, because it blocks UI interaction except strip
		// as such, getFittedWeaponSlots() is preferable to reduce the checks in the logic below

		// when an adapter is first added to the ship, its slot is still a proper weapon slot
		// it will create two new slots, and its type will be changed to WeaponType.DECORATIVE
		// as its slot type is changed, getFittedWeaponSlots() will ignore it
		// as such, the function will be short-circuited; it will not create further slots
		// the new slots have an affix in their slot tags that is also used to short-circuit the function

		// DECORATIVE vs BUILTIN vs SYSTEM as a slot choice
		// built-in shows up as a weapon, is interactable in the UI, is excluded in getNonBuiltInWeaponSlots()
		// system shows up as a weapon, is not interactable in the UI
		// decorative does not show up anywhere, is excluded in getFittedWeaponSlots() - best choice
		for (String slotId: variant.getFittedWeaponSlots()) {
			if (slotId.contains(ehm.affix.adaptedSlot)) continue; // short-circuit to avoid weapons in adapted slots causing an error on load, must be first
			
			//WeaponType slotType = variant.getSlot(slotId).getWeaponType();
			//WeaponSize slotSize = variant.getSlot(slotId).getSlotSize();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			//WeaponType weaponType = weaponSpec.getType();
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // to avoid plugging medium universal to large universal
			if (!ehm.id.weapons.containsKey(weaponId)) continue; // to short-circuit the function if it isn't an adapter
			
			// these are separated in a switch case for now, for future expansions if there will be any
			String childFormation = ehm.id.weapons.get(weaponId);
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
			oOoo parentSlot = hullSpec.getWeaponSlot(slotId); 
			Vector2f parentSlotLocation = parentSlot.getLocation();
			float parentSlotAngle = parentSlot.getAngle();
			String parentSlotId = parentSlot.getId();
			WeaponSize parentSlotSize = parentSlot.getSlotSize();

			for (String position: offsets.keySet()) {
				oOoo childSlot = parentSlot.clone();
				String childSlotId = ehm.affix.adaptedSlot + parentSlotId + position; // also used as nodeId because nodeId isn't visible
				classsuper childSlotNode = new classsuper(childSlotId, _ehm_base_util.generateChildLocation(parentSlotLocation, parentSlotAngle, offsets.get(position)));
				WeaponSize childSlotSize = parentSlotSize.equals(WeaponSize.LARGE) ? WeaponSize.MEDIUM : WeaponSize.SMALL;

				childSlot.setId(childSlotId);
				childSlot.setNode(childSlotNode);
				childSlot.setSlotSize(childSlotSize);

				hullSpec.addWeaponSlot(childSlot);
			}
			
			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(parentSlotId, weaponId);
			refreshRefit = true; 
		}
		
		if (refreshRefit) { refreshRefit(); refreshRefit = false; }
		return hullSpec;
	}

	/** 
	 * Just an extra call to {@link #ehm_getStockHullSpec(ShipVariantAPI, boolean)}.
	 * Used to do all kinds of stuff, now just retrieves a stock hull. Due to
	 * the persistence of 
	 * @param variant that will have alterations on slots with an adapter
	 * @return a hullSpec to be installed on the variant
	 */
	protected static final g ehm_adapterRemoval(ShipVariantAPI variant) {
		g newHullSpec = (g) Global.getSettings().getHullSpec(variant.getHullSpec().getHullId());

		return ehm_hullSpecClone(newHullSpec);
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.adapterRetrofit, hullModSpecId)) return ehm.excuses.hasAdapterRetrofit; 
		
		return null; 
	}

	@Override
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		ShipVariantAPI variant = ship.getVariant();
		
		if (variant.hasHullMod(hullModSpec.getId())) for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) 
		if (slot.getId().contains(ehm.affix.adaptedSlot)) return ehm.excuses.adapterActivated;

		return null;
	}
	//#endregion
}