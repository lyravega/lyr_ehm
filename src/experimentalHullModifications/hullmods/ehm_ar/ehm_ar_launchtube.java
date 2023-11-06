package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.id.hullmods;
import experimentalHullModifications.misc.ehm_internals.id.shunts.launchTubes;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.regexText;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_launchtube extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (launchTubeMap.keySet().contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (launchTubeMap.keySet().contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	static final Map<String, Float> launchTubeMap = new HashMap<String, Float>();
	static {
		launchTubeMap.put(ehm_internals.id.shunts.launchTubes.large, 1.0f);
	}

	// com.fs.starfarer.api.impl.hullmods.ConvertedHangar
	// private static final HullModEffect convertedHangarEffect = Global.getSettings().getHullModSpec("converted_hangar").getEffect();
	// com.fs.starfarer.api.impl.hullmods.VastHangar
	// private static final HullModEffect vastHangarEffect = Global.getSettings().getHullModSpec("vast_hangar").getEffect();

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(true, variant.getHullSpec());
		List<WeaponSlotAPI> shunts = lyr_hullSpec.getAllWeaponSlotsCopy();

		StatBonus launchTubeStat = stats.getDynamic().getMod(ehm_internals.id.stats.launchTubes);

		// if (stats.getNumFighterBays().getBaseValue() <= 0) {
		// 	convertedHangarEffect.applyEffectsBeforeShipCreation(hullSize, stats, "converted_hangar");
		// } else {
		// 	vastHangarEffect.applyEffectsBeforeShipCreation(hullSize, stats, "vast_hangar");
		// 	convertedHangarEffect.applyEffectsBeforeShipCreation(hullSize, stats, "converted_hangar");
		// }

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }
			if (slotId.startsWith(ehm_internals.affix.convertedSlot)) { iterator.remove(); continue; }

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(ehm_internals.tag.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case launchTubes.large: {
					launchTubeStat.modifyFlat(slotId, launchTubeMap.get(shuntId));
					break;
				} default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case launchTubes.large: {
					ehm_deactivateSlot(lyr_hullSpec, shuntId, slotId);
					break;
				} default: break;
			}
		}

		stats.getNumFighterBays().modifyFlat(this.hullModSpecId, launchTubeStat.computeEffective(0f));

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
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			if (ehm_settings.getShowInfoForActivators()) {
				Map<String, Integer> launchTubes = ehm_shuntCount(ship, ehm_internals.tag.tubeShunt);

				if (!launchTubes.isEmpty()) {
					tooltip.addSectionHeading("EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					for (String shuntId: launchTubes.keySet()) {
						tooltip.addPara(launchTubes.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No large weapon slots are turned into hangars. Each large slot is turned into a single fighter bay with a launch tube.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (lyr_miscUtilities.hasExtraWings(ship, hullmods.launchtube)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasExtraWings, text.padding);
		}
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (ship.getVariant().hasHullMod(this.hullModSpecId) && lyr_miscUtilities.hasExtraWings(ship, hullmods.launchtube)) return false;

		return true;
	}
	//#endregion
}
