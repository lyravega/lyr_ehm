package data.hullmods.ehm_ar;

import static data.hullmods._ehm_util.generateChildLocation;
import static lyr.tools._lyr_uiTools.commitChanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import org.lwjgl.util.vector.Vector2f;

import data.hullmods._ehm_base;
import lyr.proxies.lyr_hullSpec;
import lyr.proxies.lyr_weaponSlot;


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
	private static class childrenParameters {
		private Set<String> children; // childIds are used as position identifier, and used as a suffix
		private Map<String, Vector2f> childrenOffsets;
		private Map<String, WeaponSize> childrenSizes;

		protected childrenParameters() {
			children = new HashSet<String>();
			childrenOffsets = new HashMap<String, Vector2f>();
			childrenSizes = new HashMap<String, WeaponSize>();
		}

		private void addChild(String childId, WeaponSize childSize, Vector2f childOffset) {
			this.children.add(childId);
			this.childrenOffsets.put(childId, childOffset);
			this.childrenSizes.put(childId, childSize);
		}

		public Set<String> getChildren() {
			return children;
		}

		private Vector2f getChildOffset(String childPrefix) {
			return this.childrenOffsets.get(childPrefix);
		}

		private WeaponSize getChildSize(String childPrefix) {
			return this.childrenSizes.get(childPrefix);
		}
	}

	private static Map<String, childrenParameters> adapters = new HashMap<String, childrenParameters>();
	private static childrenParameters mediumDual = new childrenParameters();
	private static childrenParameters largeDual = new childrenParameters();
	private static childrenParameters largeTriple = new childrenParameters();
	private static childrenParameters largeQuad = new childrenParameters();
	static {
		mediumDual.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		mediumDual.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		adapters.put(ehm.id.adapter.mediumDual, mediumDual);

		largeDual.addChild("L", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
		largeDual.addChild("R", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
		adapters.put(ehm.id.adapter.largeDual, largeDual);

		largeTriple.addChild("L", WeaponSize.SMALL, new Vector2f(-4.0f, 17.0f)); // left
		largeTriple.addChild("R", WeaponSize.SMALL, new Vector2f(-4.0f, -17.0f)); // right
		largeTriple.addChild("C", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
		adapters.put(ehm.id.adapter.largeTriple, largeTriple);

		largeQuad.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		largeQuad.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 17.0f)); // far left
		largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -17.0f)); // far right
		adapters.put(ehm.id.adapter.largeQuad, largeQuad);
	}
	
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
			if (!ehm.id.adapter.set.contains(weaponId)) continue; // to short-circuit the function if it isn't an adapter

			childrenParameters childrenParameters = adapters.get(weaponId);

			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId); 
			Vector2f parentSlotLocation = parentSlot.retrieve().getLocation();
			float parentSlotAngle = parentSlot.retrieve().getAngle();
			String parentSlotId = parentSlot.retrieve().getId();

			for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
				lyr_weaponSlot childSlot = parentSlot.clone();

				String childSlotId = ehm.affix.adaptedSlot + parentSlotId + childId; // also used as nodeId because nodeId isn't visible
				Vector2f childSlotLocation = generateChildLocation(parentSlotLocation, parentSlotAngle, childrenParameters.getChildOffset(childId));
				WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

				childSlot.setId(childSlotId);
				childSlot.setNode(childSlotId, childSlotLocation);
				childSlot.setSlotSize(childSlotSize);

			 	hullSpec.addWeaponSlot(childSlot.retrieve());
			}

			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(parentSlotId, weaponId);
			refreshRefit = true;
		}

		if (refreshRefit) { commitChanges(); refreshRefit = false; }
		return hullSpec.retrieve();
	}

	/** 
	 * Refreshes the hullSpec and returns it.
	 * @param variant whose hullSpec will be restored
	 * @return a restored hullSpec
	 * @see {@link data.hullmods.ehm_base#onRemoved(String, ShipAPI) onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_adapterRemoval(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(ehm.tooltip.header.notApplicable, ehm.tooltip.header.notApplicable_textColour, ehm.tooltip.header.notApplicable_bgColour, Alignment.MID, ehm.tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(ehm.tooltip.text.lacksBase, ehm.tooltip.text.padding);
			if (ehm_hasRetrofitTag(ship, ehm.tag.adapterRetrofit, hullModSpecId)) tooltip.addPara(ehm.tooltip.text.hasAdapterRetrofit, ehm.tooltip.text.padding);
		}

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(hullModSpecId) ? " IN" : " OUT";

			tooltip.addSectionHeading(ehm.tooltip.header.locked+inOrOut, ehm.tooltip.header.locked_textColour, ehm.tooltip.header.locked_bgColour, Alignment.MID, ehm.tooltip.header.padding);

			if (ehm_hasWeapons(ship, ehm.affix.adaptedSlot)) tooltip.addPara(ehm.tooltip.text.hasWeaponsOnAdaptedSlots, ehm.tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.adapterRetrofit, hullModSpecId)) return false; 
		
		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		if (ehm_hasWeapons(ship, ehm.affix.adaptedSlot)) return false;

		return true;
	}
	//#endregion
}