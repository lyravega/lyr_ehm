package experimentalHullModifications.hullmods.ehm_ar;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts;
import experimentalHullModifications.misc.ehm_internals.shunts.capacitors;
import experimentalHullModifications.misc.ehm_internals.shunts.dissipators;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.proxies.ehm_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_mutableshunt extends _ehm_ar_base {
	public static final class capacitorData {
		public static final class ids {
			public static final String
				large = capacitors.ids.large,
				medium = capacitors.ids.medium,
				small = capacitors.ids.small;
		}
		public static final String activatorId = capacitors.activatorId;
		public static final String tag = capacitors.groupTag;
		public static final String groupTag = capacitors.groupTag;
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

		public static final class mods {
			public static final float flat = 1.5f * Misc.FLUX_PER_CAPACITOR;
			public static final float mult = 0.01f;
		}
	}

	public static final class dissipatorData {
		public static final class ids {
			public static final String
				large = dissipators.ids.large,
				medium = dissipators.ids.medium,
				small = dissipators.ids.small;
		}
		public static final String activatorId = dissipators.activatorId;
		public static final String tag = dissipators.groupTag;
		public static final String groupTag = dissipators.groupTag;
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

		public static final class mods {
			public static final float flat = 1.5f * Misc.DISSIPATION_PER_VENT;
			public static final float mult = 0.01f;
		}
	}

	public ehm_ar_mutableshunt() {
		super();

		this.statSet.add(capacitorData.groupTag);
		this.statSet.add(dissipatorData.groupTag);
		this.shuntIdSet.addAll(capacitorData.idSet);
		this.shuntIdSet.addAll(dissipatorData.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		HashMap<String, StatMod> dissipatorShunts = dynamicStats.getMod(dissipatorData.groupTag).getFlatBonuses();
		if (!dissipatorShunts.isEmpty()) {
			for (String slotId : dissipatorShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);

				dynamicStats.getMod(dissipatorData.groupTag).modifyFlat(slotId, dissipatorData.dataMap.get(shuntId));
				hullSpec.activateGenericShunt(shuntId, slotId);
			}

			float dissipatorAmount = dynamicStats.getMod(dissipatorData.groupTag).computeEffective(0f);
			float dissipatorFlatMod = dissipatorAmount*dissipatorData.mods.flat;
			float dissipatorMultMod = 1f+dissipatorAmount*dissipatorData.mods.mult;

			stats.getFluxDissipation().modifyFlat(this.hullModSpecId, dissipatorFlatMod);
			stats.getFluxDissipation().modifyMult(this.hullModSpecId, dissipatorMultMod);
		}

		HashMap<String, StatMod> capacitorShunts = dynamicStats.getMod(capacitorData.groupTag).getFlatBonuses();
		if (!capacitorShunts.isEmpty()) {
			for (String slotId : capacitorShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);

				dynamicStats.getMod(capacitorData.groupTag).modifyFlat(slotId, capacitorData.dataMap.get(shuntId));
				hullSpec.activateGenericShunt(shuntId, slotId);
			}

			float capacitorAmount = dynamicStats.getMod(capacitorData.groupTag).computeEffective(0f);
			float capacitorFlatMod = capacitorAmount*capacitorData.mods.flat;
			float capacitorMultMod = 1f+capacitorAmount*capacitorData.mods.mult;

			stats.getFluxCapacity().modifyFlat(this.hullModSpecId, capacitorFlatMod);
			stats.getFluxCapacity().modifyMult(this.hullModSpecId, capacitorMultMod);
		}

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "dissipators";
			case 1: return "capacitors";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> capacitorShunts = dynamicStats.getMod(capacitorData.groupTag).getFlatBonuses();
				if (!capacitorShunts.isEmpty()) {
					int totalBonus = Math.round(ship.getMutableStats().getFluxCapacity().modified-(variant.getNumFluxCapacitors()*Misc.FLUX_PER_CAPACITOR+variant.getHullSpec().getFluxCapacity()));

					tooltip.addSectionHeading("CAPACITORS (+"+totalBonus+" CAPACITY)", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					this.printShuntCountsOnTooltip(tooltip, variant, capacitorShunts.keySet());
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CAPACITORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", 2f);
				}

				HashMap<String, StatMod> dissipatorShunts = dynamicStats.getMod(dissipatorData.groupTag).getFlatBonuses();
				if (!dissipatorShunts.isEmpty()) {
					int totalBonus = Math.round(ship.getMutableStats().getFluxDissipation().modified-(variant.getNumFluxVents()*Misc.DISSIPATION_PER_VENT+variant.getHullSpec().getFluxDissipation()));

					tooltip.addSectionHeading("DISSIPATORS (+"+totalBonus+" DISSIPATION)", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					this.printShuntCountsOnTooltip(tooltip, variant, dissipatorShunts.keySet());
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DISSIPATORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
}
