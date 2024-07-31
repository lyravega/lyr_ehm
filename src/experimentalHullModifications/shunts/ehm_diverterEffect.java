package experimentalHullModifications.shunts;

import java.util.HashMap;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.diverters;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.proxies.lyr_hullSpec;

/**
 * This shunt effect class serves as a companion object for its own activator retrofit.
 * See {@link ehm_ar_diverterandconverter} hullmod effect class where these will be utilized.
 * @category Shunt Effect
 * @author lyravega
 */
public final class ehm_diverterEffect extends _ehm_shuntEffect<Integer> {
	public ehm_diverterEffect(_ehm_ar_base activatorEffect) {
		super(activatorEffect, diverters.groupTag, null, affixes.convertedSlot);
	}

	@Override
	public void registerShunt(MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant, WeaponSlotAPI slot, String slotId, String shuntId) {
		if (!variant.hasHullMod(this.shuntActivatorId)) return;
		if (!this.isValidSlot(slotId)) return;

		int mod = this.shuntDataMap.get(shuntId);
		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
		dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, mod);
		dynamicStats.getMod(ehm_internals.statIds.slotPoints).modifyFlat(slotId, mod);
		// dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);	// redundant since stat ids point at the group tag
	}

	@Override
	public void processShunts(lyr_hullSpec hullSpec, MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant) {
		HashMap<String, StatMod> diverterShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!diverterShunts.isEmpty()) {
			for (String slotId : diverterShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);
				float mod = this.shuntDataMap.get(shuntId);
				// float mod = diverterShunts.get(slotId).getValue();

				dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, mod);	// updated on base but used here for self-tracking & to keep stats updated in this class
				dynamicStats.getMod(ehm_internals.statIds.slotPointsFromDiverters).modifyFlat(slotId, mod);	// to have the addition count on the active converter block
				dynamicStats.getMod(ehm_internals.statIds.slotPoints).modifyFlat(slotId, mod);	// to have the addition count on the inactive converter block
				this.activateShunt(hullSpec, slotId, shuntId);
			}
		}
	}

	@Override
	public void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		ShipVariantAPI variant = ship.getVariant();
		DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
		HashMap<String, StatMod> diverterShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!diverterShunts.isEmpty()) {
			tooltip.addSectionHeading("DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			this.printShuntCountsOnTooltip(tooltip, variant, diverterShunts.keySet());
		} else if (lyr_ehm.lunaSettings.getShowFullInfoForActivators()) {
			tooltip.addSectionHeading("NO DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("No diverters are installed. Diverters disable a slot and provide slot points that are used by converters in turn.", 2f);
		}
	}
}