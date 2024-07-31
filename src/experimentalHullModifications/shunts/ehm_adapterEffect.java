package experimentalHullModifications.shunts;

import java.util.*;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.adapters;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.shunts.ehm_adapterEffect.adapterParameters;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;
import lyravega.utilities.lyr_vectorUtilities;

/**
 * This shunt effect class serves as a companion object for its own activator retrofit.
 * See {@link ehm_ar_stepdownadapter} hullmod effect class where these will be utilized.
 * @category Shunt Effect
 * @author lyravega
 */
public final class ehm_adapterEffect extends _ehm_shuntEffect<adapterParameters> {
	public static class adapterParameters {
		private final Set<String> children; public Set<String> getChildren() { return this.children; }
		private final Map<String, Vector2f> childrenOffsets; public Vector2f getChildOffset(String childPrefix) { return this.childrenOffsets.get(childPrefix); }
		private final Map<String, WeaponSize> childrenSizes; public WeaponSize getChildSize(String childPrefix) { return this.childrenSizes.get(childPrefix); }

		public adapterParameters() {
			this.children = new HashSet<String>();
			this.childrenOffsets = new HashMap<String, Vector2f>();
			this.childrenSizes = new HashMap<String, WeaponSize>();
		}

		public void addChild(String childId, WeaponSize childSize, Vector2f childOffset) {
			this.children.add(childId);
			this.childrenOffsets.put(childId, childOffset);
			this.childrenSizes.put(childId, childSize);
		}
	}

	public ehm_adapterEffect(_ehm_ar_base activatorEffect) {
		super(activatorEffect, adapters.groupTag, affixes.adaptedSlot, affixes.adaptedSlot, affixes.convertedSlot);
	}

	@Override
	public void registerShunt(MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant, WeaponSlotAPI slot, String slotId, String shuntId) {
		if (!variant.hasHullMod(this.shuntActivatorId)) return;
		if (!this.isValidSlot(slotId)) return;

		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
		dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, 1);
	}

	@Override
	public void processShunts(lyr_hullSpec hullSpec, MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant) {
		HashMap<String, StatMod> adapterShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!adapterShunts.isEmpty()) {
			for (String slotId : adapterShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);

				stats.getDynamic().getMod(this.shuntGroupTag).modifyFlat(slotId, 1);
				this.activateShunt(hullSpec, slotId, shuntId);
			}
		}
	}

	/**
	 * Activates the adapter shunts on the slot. The parent slot is turned into a built-in decorative
	 * in the process. The data that is populated through the activator will be utilized as child
	 * slot parameters.
	 * @param hullSpec proxy of the ship that will be altered
	 * @param shuntId to determine the shunt type and to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot while deriving info for children
	 */
	@Override
	public final void activateShunt(lyr_hullSpec hullSpec, String slotId, String shuntId) {
		adapterParameters childrenParameters = this.shuntDataMap.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = this.childSlotPrefix + slotId + childId; // also used as nodeId
			Vector2f childSlotLocation = lyr_vectorUtilities.calculateRelativePoint(parentSlot.getLocation(), parentSlot.getAngle(), childrenParameters.getChildOffset(childId));
			WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

			hullSpec.addWeaponSlot(childSlot);
		}

		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.lunaSettings.getHideAdapters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);	// sometimes the activated shunts (decoratives) on these new slots (especially hardpoint ones) are rendered below the adapter, hence the change
	}

	@Override
	public void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		ShipVariantAPI variant = ship.getVariant();
		DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
		HashMap<String, StatMod> adapterShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!adapterShunts.isEmpty()) {
			tooltip.addSectionHeading("ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			this.printShuntCountsOnTooltip(tooltip, variant, adapterShunts.keySet());
		} else if (lyr_ehm.lunaSettings.getShowFullInfoForActivators()) {
			tooltip.addSectionHeading("NO ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("No adapters are installed. Adapters turn bigger slots into smaller ones.", text.padding);
		}
	}
}