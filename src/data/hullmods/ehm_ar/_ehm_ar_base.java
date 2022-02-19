package data.hullmods.ehm_ar;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.loading.specs.HullVariantSpec;
import com.fs.starfarer.loading.specs.classsuper;
import com.fs.starfarer.loading.specs.g;
import com.fs.starfarer.loading.specs.oOoo;

import org.lwjgl.util.vector.Vector2f;

import data.hullmods._ehm_base;
import data.hullmods._ehm_util;
import data.hullmods.ehm_sr._ehm_sr_base;
import data.hullmods.ehm_wr._ehm_wr_base;

/**
 * This class is used by slot adapter hullmods. Slot adapters are designed 
 * to search the ship for specific weapons, and perform operations on the 
 * hullSpec to yield interesting results, such as creating a new weapon slot. 
 * </p>
 * Reason to split this as another base was primarily maintenance.
 * @see {@link _ehm_sr_base} for system retrofit base
 * @see {@link _ehm_wr_base} for weapon retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_ar_base extends _ehm_base {
	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them. The returned 
	 * hullSpec needs to be installed on the variant.
	 * @param variant that will have alterations on slots with an adapter
	 * @return a hullSpec to be installed on the variant
	 */
	protected static final g ehm_stepDownAdapter(HullVariantSpec variant) {
		g hullSpec = variant.getHullSpec(); 
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
				classsuper childSlotNode = new classsuper(childSlotId, _ehm_util.generateChildLocation(parentSlotLocation, parentSlotAngle, offsets.get(position)));
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
	@Deprecated // without obfuscated stuff - incomplete
	protected static final ShipHullSpecAPI ehm_stepDownAdapter(ShipVariantAPI variantAPI) {
		HullVariantSpec tempVariant = new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(variantAPI).getHullSpec());
		boolean refreshRefit = false;

		for (String slotId: variantAPI.getFittedWeaponSlots()) {
			if (slotId.startsWith(ehm.affix.adaptedSlot)) continue; // short-circuit to avoid weapons in adapted slots causing an error on load, must be first
			
			//WeaponType slotType = variant.getSlot(slotId).getWeaponType();
			//WeaponSize slotSize = variant.getSlot(slotId).getSlotSize();
			WeaponSpecAPI weaponSpec = variantAPI.getWeaponSpec(slotId);
			//WeaponType weaponType = weaponSpec.getType();
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variantAPI.getSlot(slotId).getSlotSize())) continue; // to avoid plugging medium universal to large universal
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
			WeaponSlotAPI parentSlot = variantAPI.getSlot(slotId); 
			Vector2f parentSlotLocation = parentSlot.getLocation();
			float parentSlotAngle = parentSlot.getAngle();
			String parentSlotId = parentSlot.getId();
			WeaponSize parentSlotSize = parentSlot.getSlotSize();

			for (String position: offsets.keySet()) {
				String childSlotId = ehm.affix.adaptedSlot + parentSlotId + position; // also used as nodeId because nodeId isn't visible
				//lyr_node childSlotNode = new lyr_node(childSlotId, _ehm_util.generateChildLocation(parentSlotLocation, parentSlotAngle, offsets.get(position)));
				WeaponSize childSlotSize = parentSlotSize.equals(WeaponSize.LARGE) ? WeaponSize.MEDIUM : WeaponSize.SMALL;
								
				WeaponSlotAPI childSlot = tempVariant.getHullSpec().getWeaponSlot(slotId).clone();
				tempVariant.getHullSpec().getWeaponSlot(slotId).getClass().cast(childSlot).setId(childSlotId);
				tempVariant.getHullSpec().addWeaponSlot(tempVariant.getHullSpec().getWeaponSlot(slotId).getClass().cast(childSlot));
				//tempVariant.getHullSpec().getWeaponSlot(childSlotId).setNode(tempVariant.getHullSpec().getWeaponSlot(slotId).getNode().getClass().cast(childSlotNode));
				tempVariant.getHullSpec().getWeaponSlot(childSlotId).setSlotSize(childSlotSize);
			}
			
			tempVariant.getHullSpec().getWeaponSlot(slotId).getClass().cast(parentSlot).setWeaponType(WeaponType.DECORATIVE);
			tempVariant.getHullSpec().addBuiltInWeapon(parentSlotId, weaponId);
			refreshRefit = true; 
		}
		
		if (refreshRefit) { refreshRefit(); refreshRefit = false; }
		return tempVariant.getHullSpec();
	}

	/** 
	 * Grabs a stock hullSpec and clones it. As this one removes the dangerous 
	 * changes to the hull, grabbing a stock one and passing it as a new one 
	 * is considered 'safe'.
	 * @param variant that have adapter-altered weapon slots
	 * @return a stock hullSpec to be installed on the variant
	 */
	protected static final g ehm_adapterRemoval(HullVariantSpec variant) {
		return ehm_hullSpecClone(variant, true);
	}
	@Deprecated // without obfuscated stuff
	protected static final ShipHullSpecAPI ehm_adapterRemoval(ShipVariantAPI variantAPI) {
		return ehm_hullSpecClone(variantAPI, true);
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
		HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());
		
		if (variant.hasHullMod(hullModSpec.getId())) for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) 
		if (slot.getId().contains(ehm.affix.adaptedSlot)) return ehm.excuses.adapterActivated;

		return null;
	}
	//#endregion
}