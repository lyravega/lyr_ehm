package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.misc.lyr_lunaSettings.extraInfoInHullMods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public class ehm_ar_mutableshunt extends _ehm_ar_base {
	static final Set<String> mutableStatBonus = new HashSet<String>();
	static final Map<String, Float[]> capacitorMap = new HashMap<String, Float[]>();
	static final Map<String, Float[]> dissipatorMap = new HashMap<String, Float[]>();
	static final Map<String, Float> launchTubeMap = new HashMap<String, Float>();
	static {
		capacitorMap.put(lyr_internals.id.shunts.capacitors.large, new Float[] {0.04f, 6f * Misc.FLUX_PER_CAPACITOR});
		capacitorMap.put(lyr_internals.id.shunts.capacitors.medium, new Float[] {0.02f, 3f * Misc.FLUX_PER_CAPACITOR});
		capacitorMap.put(lyr_internals.id.shunts.capacitors.small, new Float[] {0.01f, 1.5f * Misc.FLUX_PER_CAPACITOR});
		mutableStatBonus.addAll(capacitorMap.keySet());

		dissipatorMap.put(lyr_internals.id.shunts.dissipators.large, new Float[] {0.04f, 6f * Misc.DISSIPATION_PER_VENT});
		dissipatorMap.put(lyr_internals.id.shunts.dissipators.medium, new Float[] {0.02f, 3f * Misc.DISSIPATION_PER_VENT});
		dissipatorMap.put(lyr_internals.id.shunts.dissipators.small, new Float[] {0.01f, 1.5f * Misc.DISSIPATION_PER_VENT});
		mutableStatBonus.addAll(dissipatorMap.keySet());

		launchTubeMap.put(lyr_internals.id.shunts.launchTubes.large, 1.0f);
		mutableStatBonus.addAll(launchTubeMap.keySet());
	}
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD / DATA CLASS, ACTIONS ARE HANDLED THROUGH BASE
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "dissipators";
			case 1: return "capacitors";
			case 2: return "launch tubes";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(hullModSpecId)) {
			boolean showInfo = !extraInfoInHullMods.equals("None");
			boolean showFullInfo = extraInfoInHullMods.equals("Full");

			if (showInfo) {
				Map<String, Integer> capacitors = ehm_shuntCount(ship, lyr_internals.tag.capacitorShunt);
	
				if (!capacitors.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CAPACITORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: capacitors.keySet()) {
						tooltip.addPara(capacitors.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO CAPACITORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", 2f);
				}

				Map<String, Integer> dissipators = ehm_shuntCount(ship, lyr_internals.tag.dissipatorShunt);
	
				if (!dissipators.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DISSIPATORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: dissipators.keySet()) {
						tooltip.addPara(dissipators.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO DISSIPATORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", 2f);
				}
	
				Map<String, Integer> launchTubes = ehm_shuntCount(ship, lyr_internals.tag.tubeShunt);
	
				if (!launchTubes.isEmpty()) {
					tooltip.addSectionHeading("EXTRA HANGARS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: launchTubes.keySet()) {
						tooltip.addPara(launchTubes.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO EXTRA HANGARS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No large weapon slots are turned into hangars. Each large slot is turned into a single fighter bay with a launch tube.", 2f);
				}
			}	
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
