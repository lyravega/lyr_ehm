package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.shunts.converters;
import experimentalHullModifications.misc.ehm_internals.shunts.converters.converterParameters;
import experimentalHullModifications.misc.ehm_internals.shunts.diverters;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_diverterandconverter extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (comboSet.contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (comboSet.contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	static final Map<String, converterParameters> converterMap = converters.dataMap;
	static final Map<String, Integer> diverterMap = diverters.dataMap;
	static final Set<String> comboSet = new HashSet<String>();
	static {
		comboSet.addAll(converters.idSet);
		comboSet.addAll(diverters.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		HashMap<String, StatMod> diverterShunts = stats.getDynamic().getMod(diverters.groupTag).getFlatBonuses();
		if (diverterShunts != null && !diverterShunts.isEmpty()) {
			for (String slotId : diverterShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				float mod = diverterShunts.get(slotId).getValue();

				stats.getDynamic().getMod(ehm_internals.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);	// to have the addition count on the active converter block
				stats.getDynamic().getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, mod);	// to have the addition count on the inactive converter block
				ehm_deactivateSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}
		}

		HashMap<String, StatMod> inactiveConverterShunts = stats.getDynamic().getMod(converters.groupTag+"_inactive").getFlatBonuses();	// inactive converters, only to activate them here
		if (inactiveConverterShunts != null && !inactiveConverterShunts.isEmpty()) {
			float slotPoints = stats.getDynamic().getMod(ehm_internals.stats.slotPoints).computeEffective(0f);

			for (String slotId : inactiveConverterShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				float slotPointCost = inactiveConverterShunts.get(slotId).getValue();
				float slotPointsUsed = stats.getDynamic().getMod(ehm_internals.stats.slotPointsUsed).computeEffective(0f);

				if (slotPointCost + slotPointsUsed > slotPoints) continue;

				// stats.getDynamic().getMod(ehm_internals.stats.slotPointsToConverters).modifyFlat(slotId, slotPoints);	// redundant in this method block; base pre-process method will update it
				stats.getDynamic().getMod(ehm_internals.stats.slotPointsUsed).modifyFlat(slotId, slotPointCost);	// only this is necessary at this stage to keep track, rest of the stats will be processed externally
				ehm_convertSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}
		}

		HashMap<String, StatMod> converterShunts = stats.getDynamic().getMod(converters.groupTag).getFlatBonuses();	// active converters, only to apply the penalty
		if (converterShunts != null && !converterShunts.isEmpty()) {
			float slotPointsUsed = stats.getDynamic().getMod(ehm_internals.stats.slotPointsUsed).computeEffective(0f);
			float slotPointsFromDiverters = stats.getDynamic().getMod(ehm_internals.stats.slotPointsFromDiverters).computeEffective(0f);

			stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(this.hullModSpecId, ehm_settings.getBaseSlotPointPenalty()*Math.max(0, slotPointsUsed - slotPointsFromDiverters));
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "converters";
			case 1: return "diverters";
			case 2: return "gained and utilized";
			case 3: return "deployment point";
			case 4: return ehm_settings.getBaseSlotPointPenalty()+"";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (ship.getVariant().hasHullMod(this.hullModSpecId)) {
			final DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			int slotPoints = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPoints).computeEffective(0f));
			int slotPointsNeeded = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).computeEffective(0f));
			int slotPointsUsed = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).computeEffective(0f));
			int slotPointsFromMods = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).computeEffective(0f));
			int slotPointsFromDiverters = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).computeEffective(0f));
			int slotPointsToConverters = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).computeEffective(0f));
			int slotPointsPenalty = ehm_settings.getBaseSlotPointPenalty()*Math.max(0, slotPointsUsed - slotPointsFromDiverters);

			tooltip.addSectionHeading(slotPointsUsed+"/"+slotPoints+(slotPointsNeeded > slotPoints ? " ("+slotPointsNeeded+") " : " ")+"SLOT POINTS", (slotPointsUsed != slotPoints) ? header.notApplicable_textColour : header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			if (slotPointsFromMods > 0) tooltip.addPara("Hull modifications are providing " + slotPointsFromMods + " slot points", 2f, header.sEffect_textColour, slotPointsFromMods + " slot points");
			if (slotPointsFromDiverters > 0) tooltip.addPara("Diverter shunts are providing " + slotPointsFromDiverters + " slot points", 2f, header.sEffect_textColour, slotPointsFromDiverters + " slot points");
			if (slotPointsToConverters > 0) tooltip.addPara("Converter shunts are utilizing " + slotPointsToConverters + " slot points", 2f, header.notApplicable_textColour, slotPointsToConverters + " slot points");
			if (slotPointsPenalty > 0) tooltip.addPara("Ship will require an additional " + slotPointsPenalty + " deployment points", 2f, header.notApplicable_textColour, slotPointsPenalty + " deployment points");

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> converterShunts = dynamicStats.getMod(converters.groupTag).getFlatBonuses();
				if (converterShunts != null && !converterShunts.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, dynamicStats, converters.idSet);
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
				}

				HashMap<String, StatMod> diverterShunts = dynamicStats.getMod(diverters.groupTag).getFlatBonuses();
				if (diverterShunts != null && !diverterShunts.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, dynamicStats, diverters.idSet);
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No diverters are installed. Diverters disable a slot and provide slot points that are used by converters in turn.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
