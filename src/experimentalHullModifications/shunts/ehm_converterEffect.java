package experimentalHullModifications.shunts;

import java.util.HashMap;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.converters;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.shunts.ehm_converterEffect.converterParameters;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;

/**
 * This shunt effect class serves as a companion object for its own activator retrofit.
 * See {@link ehm_ar_diverterandconverter} hullmod effect class where these will be utilized.
 * @category Shunt Effect
 * @author lyravega
 */
public final class ehm_converterEffect extends _ehm_shuntEffect<converterParameters> {
	public static final class converterParameters {
		private final String childSuffix; public String getChildSuffix() { return this.childSuffix; }
		private final int childCost; public int getChildCost() { return this.childCost; }
		private final WeaponSize childSize; public WeaponSize getChildSize() { return this.childSize; }

		public converterParameters(String childSuffix, WeaponSize childSize, int childCost) {
			this.childSuffix = childSuffix;
			this.childCost = childCost;
			this.childSize = childSize;
		}
	}

	public ehm_converterEffect(_ehm_ar_base activatorEffect) {
		super(activatorEffect, converters.groupTag, affixes.convertedSlot, affixes.adaptedSlot, affixes.convertedSlot);
	}

	@Override
	public void registerShunt(MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant, WeaponSlotAPI slot, String slotId, String shuntId) {
		if (!variant.hasHullMod(this.shuntActivatorId)) return;
		if (!this.isValidSlot(slotId)) return;

		int mod = this.shuntDataMap.get(shuntId).getChildCost();
		if (!slot.isDecorative()) {
			dynamicStats.getMod(shuntId+"_inactive").modifyFlat(slotId, 1);
			dynamicStats.getMod(this.shuntGroupTag+"_inactive").modifyFlat(slotId, mod);
			dynamicStats.getMod(ehm_internals.statIds.slotPointsNeeded).modifyFlat(slotId, mod);
		} else {
			dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
			dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, mod);
			dynamicStats.getMod(ehm_internals.statIds.slotPointsNeeded).modifyFlat(slotId, mod);
			dynamicStats.getMod(ehm_internals.statIds.slotPointsUsed).modifyFlat(slotId, mod);
			// dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).modifyFlat(slotId, mod);	// redundant since stat ids point at the group tag
		}
	}

	@Override
	public void processShunts(lyr_hullSpec hullSpec, MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant) {
		HashMap<String, StatMod> inactiveConverterShunts = dynamicStats.getMod(this.shuntGroupTag+"_inactive").getFlatBonuses();	// inactive converters, only to activate them here

		if (!inactiveConverterShunts.isEmpty()) {
			float slotPoints = dynamicStats.getMod(ehm_internals.statIds.slotPoints).computeEffective(0f);

			for (String slotId : inactiveConverterShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);
				float slotPointCost = this.shuntDataMap.get(shuntId).getChildCost();
				// float slotPointCost = inactiveConverterShunts.get(slotId).getValue();
				float slotPointsUsed = dynamicStats.getMod(ehm_internals.statIds.slotPointsUsed).computeEffective(0f);

				if (slotPointCost + slotPointsUsed > slotPoints) continue;

				dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, slotPointCost);	// updated on base but used here for self-tracking & to keep stats updated in this class
				dynamicStats.getMod(ehm_internals.statIds.slotPointsUsed).modifyFlat(slotId, slotPointCost);	// only this is necessary at this stage to keep track, rest of the stats will be processed externally
				this.activateShunt(hullSpec, slotId, shuntId);
			}
		}

		HashMap<String, StatMod> converterShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();	// active converters, only to apply the penalty

		if (!converterShunts.isEmpty() && lyr_ehm.lunaSettings.getBaseSlotPointPenalty() > 0) {
			float slotPointsUsed = dynamicStats.getMod(ehm_internals.statIds.slotPointsUsed).computeEffective(0f);
			float slotPointsFromDiverters = dynamicStats.getMod(ehm_internals.statIds.slotPointsFromDiverters).computeEffective(0f);
			float deploymentPointsMod = lyr_ehm.lunaSettings.getBaseSlotPointPenalty()*Math.max(0, slotPointsUsed - slotPointsFromDiverters);

			dynamicStats.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(this.shuntActivatorId, deploymentPointsMod);
		}
	}

	/**
	 * Activates the converter shunts on the slot. The parent slot is turned into a built-in decorative
	 * in the process. The data that is populated through the activator will be utilized as child
	 * slot parameters.
	 * @param hullSpec proxy of the ship that will be altered
	 * @param shuntId to determine the shunt type and to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot while deriving info for child
	 */
	@Override
	public final void activateShunt(lyr_hullSpec hullSpec, String slotId, String shuntId) {
		converterParameters childParameters = this.shuntDataMap.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = this.childSlotPrefix + slotId + childParameters.getChildSuffix(); // also used as nodeId

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, parentSlot.getLocation());
		childSlot.setSlotSize(childParameters.getChildSize());

		hullSpec.addWeaponSlot(childSlot);

		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.lunaSettings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);
	}

	@Override
	public void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		ShipVariantAPI variant = ship.getVariant();
		DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
		HashMap<String, StatMod> converterShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!converterShunts.isEmpty()) {
			tooltip.addSectionHeading("CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			this.printShuntCountsOnTooltip(tooltip, variant, converterShunts.keySet());
		} else if (lyr_ehm.lunaSettings.getShowFullInfoForActivators()) {
			tooltip.addSectionHeading("NO CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
		}
	}
}