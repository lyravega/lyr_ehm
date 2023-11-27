package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.shunts.hangars;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_launchtube extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (hangarMap.keySet().contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (hangarMap.keySet().contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	static final Map<String, Integer> hangarMap = ehm_internals.shunts.hangars.dataMap;

	// com.fs.starfarer.api.impl.hullmods.ConvertedHangar
	// private static final HullModEffect convertedHangarEffect = Global.getSettings().getHullModSpec("converted_hangar").getEffect();
	// com.fs.starfarer.api.impl.hullmods.VastHangar
	// private static final HullModEffect vastHangarEffect = Global.getSettings().getHullModSpec("vast_hangar").getEffect();

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		HashMap<String, StatMod> hangarShunts = stats.getDynamic().getMod(hangars.groupTag).getFlatBonuses();
		if (hangarShunts != null && !hangarShunts.isEmpty()) {
			float hangarAmount =  stats.getDynamic().getMod(hangars.groupTag).computeEffective(0f);

			for (String slotId : hangarShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				ehm_turnSlotIntoBay(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}

			stats.getNumFighterBays().modifyFlat(this.hullModSpecId, hangarAmount);
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
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

		if (ship.getVariant().hasHullMod(this.hullModSpecId)) {
			final DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> hangarShunts = dynamicStats.getMod(hangars.groupTag).getFlatBonuses();
				if (hangarShunts != null && !hangarShunts.isEmpty()) {
					tooltip.addSectionHeading("EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, dynamicStats, hangars.idSet);
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
