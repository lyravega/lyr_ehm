package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.listeners.lyr_lunaSettingsListener.extraInfoInHullMods;
import static lyravega.tools.lyr_uiTools.commitVariantChanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_helpers;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_internals.id.hullmods;
import lyravega.misc.lyr_internals.id.shunts.launchTubes;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public final class ehm_ar_launchtube extends _ehm_ar_base {
	static final Set<String> mutableStatBonus = new HashSet<String>();
	static final Map<String, Float> launchTubeMap = new HashMap<String, Float>();
	static {
		launchTubeMap.put(lyr_internals.id.shunts.launchTubes.large, 1.0f);
		mutableStatBonus.addAll(launchTubeMap.keySet());
	}
	
	// com.fs.starfarer.api.impl.hullmods.ConvertedHangar
	// private static final HullModEffect convertedHangarEffect = Global.getSettings().getHullModSpec("converted_hangar").getEffect();
	// com.fs.starfarer.api.impl.hullmods.VastHangar
	// private static final HullModEffect vastHangarEffect = Global.getSettings().getHullModSpec("vast_hangar").getEffect();

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		List<WeaponSlotAPI> shunts = hullSpec.getAllWeaponSlotsCopy();
		boolean commitVariantChanges = false;

		int fighterBayFlat = 0;

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
			if (slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); continue; }
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(lyr_internals.tag.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case launchTubes.large:
					fighterBayFlat += launchTubeMap.get(shuntId);
					break;
				default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case launchTubes.large:
					commitVariantChanges = ehm_deactivateSlot(hullSpec, shuntId, slotId);
					break;
				default: break;
			}
		}

		stats.getNumFighterBays().modifyFlat(this.hullModSpecId, fighterBayFlat);

		variant.setHullSpecAPI(hullSpec.retrieve());
		if (commitVariantChanges && !_ehm_helpers.ehm_isGettingRestored(variant)) { commitVariantChanges = false; commitVariantChanges(); }
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
			boolean showInfo = !extraInfoInHullMods.equals("None");
			boolean showFullInfo = extraInfoInHullMods.equals("Full");

			if (showInfo) {	
				Map<String, Integer> launchTubes = ehm_shuntCount(ship, lyr_internals.tag.tubeShunt);
	
				if (!launchTubes.isEmpty()) {
					tooltip.addSectionHeading("EXTRA HANGARS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					for (String shuntId: launchTubes.keySet()) {
						tooltip.addPara(launchTubes.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO EXTRA HANGARS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No large weapon slots are turned into hangars. Each large slot is turned into a single fighter bay with a launch tube.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);

			if (_ehm_helpers.ehm_hasExtraWings(ship, hullmods.launchtube)) tooltip.addPara(text.hasExtraWings[0], text.padding).setHighlight(text.hasExtraWings[1]);
		}
	}
	
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (ship.getVariant().hasHullMod(this.hullModSpecId) && _ehm_helpers.ehm_hasExtraWings(ship, hullmods.launchtube)) return false;

		return true;
	}
	//#endregion
}