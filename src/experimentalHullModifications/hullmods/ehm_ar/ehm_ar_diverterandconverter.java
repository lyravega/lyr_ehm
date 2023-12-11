package experimentalHullModifications.hullmods.ehm_ar;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts;
import experimentalHullModifications.misc.ehm_internals.shunts.converters;
import experimentalHullModifications.misc.ehm_internals.shunts.diverters;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_tooltipUtilities.colour;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_diverterandconverter extends _ehm_ar_base {
	static final class converterData {
		public static final class ids {
			public static final String
				mediumToLarge = converters.ids.mediumToLarge,
				smallToLarge = converters.ids.smallToLarge,
				smallToMedium = converters.ids.smallToMedium;
		}
		public static final String activatorId = converters.activatorId;
		public static final String tag = converters.groupTag;
		public static final String groupTag = converters.groupTag;
		public static final Map<String, converterParameters> dataMap = new HashMap<String, converterParameters>();
		public static final Set<String> idSet = dataMap.keySet();
		private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.adaptedSlot, affixes.convertedSlot});

		public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
			return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
		}

		static {
			dataMap.put(ids.mediumToLarge, new converterParameters("ML", WeaponSize.LARGE, shunts.slotValues.get(WeaponSize.LARGE) - shunts.slotValues.get(WeaponSize.MEDIUM)));
			dataMap.put(ids.smallToLarge, new converterParameters("SL", WeaponSize.LARGE, shunts.slotValues.get(WeaponSize.LARGE) - shunts.slotValues.get(WeaponSize.SMALL)));
			dataMap.put(ids.smallToMedium, new converterParameters("SM", WeaponSize.MEDIUM, shunts.slotValues.get(WeaponSize.MEDIUM) - shunts.slotValues.get(WeaponSize.SMALL)));
		}

		public static final class converterParameters {
			private final String childSuffix; public String getChildSuffix() { return this.childSuffix; }
			private final int childCost; public int getChildCost() { return this.childCost; }
			private final WeaponSize childSize; public WeaponSize getChildSize() { return this.childSize; }

			private converterParameters(String childSuffix, WeaponSize childSize, int childCost) {
				this.childSuffix = childSuffix;
				this.childCost = childCost;
				this.childSize = childSize;
			}
		}
	}

	static final class diverterData {
		public static final class ids {
			public static final String
				large = diverters.ids.large,
				medium = diverters.ids.medium,
				small = diverters.ids.small;
		}
		public static final String activatorId = diverters.activatorId;
		public static final String tag = diverters.groupTag;
		public static final String groupTag = diverters.groupTag;
		public static final Map<String, Integer> dataMap = new HashMap<String, Integer>();
		public static final Set<String> idSet = dataMap.keySet();
		private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.convertedSlot});

		public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
			return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
		}

		static {
			dataMap.put(ids.large, shunts.slotValues.get(WeaponSize.LARGE));
			dataMap.put(ids.medium, shunts.slotValues.get(WeaponSize.MEDIUM));
			dataMap.put(ids.small, shunts.slotValues.get(WeaponSize.SMALL));
		}
	}

	public ehm_ar_diverterandconverter() {
		super();

		this.shuntSet.addAll(converterData.idSet);
		this.shuntSet.addAll(diverterData.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		HashMap<String, StatMod> diverterShunts = dynamicStats.getMod(diverterData.groupTag).getFlatBonuses();
		if (!diverterShunts.isEmpty()) {
			for (String slotId : diverterShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				float mod = diverterShunts.get(slotId).getValue();

				dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);	// to have the addition count on the active converter block
				dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, mod);	// to have the addition count on the inactive converter block
				ehm_deactivateSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}
		}

		HashMap<String, StatMod> inactiveConverterShunts = dynamicStats.getMod(converterData.groupTag+"_inactive").getFlatBonuses();	// inactive converters, only to activate them here
		if (!inactiveConverterShunts.isEmpty()) {
			float slotPoints = dynamicStats.getMod(ehm_internals.stats.slotPoints).computeEffective(0f);

			for (String slotId : inactiveConverterShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				float slotPointCost = inactiveConverterShunts.get(slotId).getValue();
				float slotPointsUsed = dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).computeEffective(0f);

				if (slotPointCost + slotPointsUsed > slotPoints) continue;

				// dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).modifyFlat(slotId, slotPoints);	// redundant in this method block; base pre-process method will update it
				dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).modifyFlat(slotId, slotPointCost);	// only this is necessary at this stage to keep track, rest of the stats will be processed externally
				ehm_convertSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}
		}

		HashMap<String, StatMod> converterShunts = dynamicStats.getMod(converterData.groupTag).getFlatBonuses();	// active converters, only to apply the penalty
		if (!converterShunts.isEmpty() && ehm_settings.getBaseSlotPointPenalty() > 0) {
			float slotPointsUsed = dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).computeEffective(0f);
			float slotPointsFromDiverters = dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).computeEffective(0f);
			float deploymentPointsMod = ehm_settings.getBaseSlotPointPenalty()*Math.max(0, slotPointsUsed - slotPointsFromDiverters);

			dynamicStats.getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(this.hullModSpecId, deploymentPointsMod);
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
		ShipVariantAPI variant = ship.getVariant();

		if (ship.getVariant().hasHullMod(this.hullModSpecId)) {
			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			int slotPoints = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPoints).computeEffective(0f));
			int slotPointsNeeded = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).computeEffective(0f));
			int slotPointsUsed = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).computeEffective(0f));
			int slotPointsFromMods = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).computeEffective(0f));
			int slotPointsFromDiverters = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).computeEffective(0f));
			int slotPointsToConverters = Math.round(dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).computeEffective(0f));
			int slotPointsPenalty = ehm_settings.getBaseSlotPointPenalty()*Math.max(0, slotPointsUsed - slotPointsFromDiverters);

			tooltip.addSectionHeading(slotPointsUsed+"/"+slotPoints+(slotPointsNeeded > slotPoints ? " ("+slotPointsNeeded+") " : " ")+"SLOT POINTS", (slotPointsUsed != slotPoints) ? colour.negative : colour.highlight, header.invisible_bgColour, Alignment.MID, header.padding);
			if (slotPointsPenalty > 0) tooltip.addPara("Ship will require an additional %s", 2f, colour.negative, slotPointsPenalty + " deployment points");
			if (slotPointsFromMods > 0) tooltip.addPara("Hull modifications are providing %s", 2f, colour.positive, slotPointsFromMods + " slot points");
			if (slotPointsFromDiverters > 0) tooltip.addPara("Diverter shunts are providing %s", 2f, colour.positive, slotPointsFromDiverters + " slot points");
			if (slotPointsToConverters > 0) tooltip.addPara("Converter shunts are utilizing %s", 2f, colour.highlight, slotPointsToConverters + " slot points");
			if (slotPointsNeeded > slotPoints) tooltip.addPara("%s required for inactive converters", 2f, colour.highlight,  (slotPointsNeeded - slotPoints) + " additional slot points");
			else if (slotPointsUsed < slotPoints) tooltip.addPara("%s may be utilized", 2f, colour.highlight,  (slotPoints - slotPointsUsed) + " additional slot points");

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> converterShunts = dynamicStats.getMod(converterData.groupTag).getFlatBonuses();
				if (!converterShunts.isEmpty()) {
					tooltip.addSectionHeading("CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, variant, converterShunts.keySet());
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
				}

				HashMap<String, StatMod> diverterShunts = dynamicStats.getMod(diverterData.groupTag).getFlatBonuses();
				if (!diverterShunts.isEmpty()) {
					tooltip.addSectionHeading("DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, variant, diverterShunts.keySet());
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
