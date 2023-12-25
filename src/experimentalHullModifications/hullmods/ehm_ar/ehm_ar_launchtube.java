package experimentalHullModifications.hullmods.ehm_ar;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.hangars;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.proxies.ehm_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_launchtube extends _ehm_ar_base {
	public static final class hangarData {
		public static final class ids {
			public static final String
				large = hangars.ids.large;	// must match weapon id in .csv and .wpn
		}
		public static final String activatorId = hangars.activatorId;
		public static final String tag = hangars.groupTag;
		public static final String groupTag = hangars.groupTag;
		public static final Map<String, float[][]> dataMap = new HashMap<String, float[][]>();
		public static final Set<String> idSet = dataMap.keySet();
		private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.convertedSlot});

		public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
			return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
		}

		static {
			dataMap.put(ids.large, new float[][]{{0f,0f}, {4f,4f}, {4f,-4f}, {-4f,4f}, {-4f,-4f}});
		}
	}

	public ehm_ar_launchtube() {
		super();

		this.statSet.add(hangarData.groupTag);
		this.shuntIdSet.addAll(hangarData.idSet);
	}

	// com.fs.starfarer.api.impl.hullmods.ConvertedHangar
	// private static final HullModEffect convertedHangarEffect = Global.getSettings().getHullModSpec("converted_hangar").getEffect();
	// com.fs.starfarer.api.impl.hullmods.VastHangar
	// private static final HullModEffect vastHangarEffect = Global.getSettings().getHullModSpec("vast_hangar").getEffect();

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		HashMap<String, StatMod> hangarShunts = dynamicStats.getMod(hangarData.groupTag).getFlatBonuses();
		if (!hangarShunts.isEmpty()) {
			for (String slotId : hangarShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;	// parent slot turns into decorative, spawns a child launch bay
				String shuntId = variant.getWeaponId(slotId);

				stats.getDynamic().getMod(hangars.groupTag).modifyFlat(slotId, 1);
				hullSpec.turnSlotIntoBay(shuntId, slotId);
			}

			float hangarMod = hangarShunts.size();	// hangars always give 1 bonus since there is only one large type, so use size

			stats.getNumFighterBays().modifyFlat(this.hullModSpecId, hangarMod);
		}

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "launch tubes";
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
				HashMap<String, StatMod> hangarShunts = dynamicStats.getMod(hangarData.groupTag).getFlatBonuses();
				if (!hangarShunts.isEmpty()) {
					tooltip.addSectionHeading("EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					this.printShuntCountsOnTooltip(tooltip, variant, hangarShunts.keySet());
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No large weapon slots are turned into hangars. Each large slot is turned into a single fighter bay with a launch tube.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
