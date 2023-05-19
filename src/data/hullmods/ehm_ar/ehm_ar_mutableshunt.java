package data.hullmods.ehm_ar;

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

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public class ehm_ar_mutableshunt extends _ehm_ar_base {
	static final Set<String> mutableStatBonus = new HashSet<String>();
	static final Map<String, Float[]> fluxCapacityBonus = new HashMap<String, Float[]>();
	static final Map<String, Float[]> fluxDissipationBonus = new HashMap<String, Float[]>();
	static final Map<String, Float> fighterBayBonus = new HashMap<String, Float>();
	static {
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.large, new Float[] {0.04f, 8f * Misc.FLUX_PER_CAPACITOR});
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.medium, new Float[] {0.02f, 4f * Misc.FLUX_PER_CAPACITOR});
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.small, new Float[] {0.01f, 2f * Misc.FLUX_PER_CAPACITOR});
		mutableStatBonus.addAll(fluxCapacityBonus.keySet());

		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.large, new Float[] {0.04f, 8f * Misc.DISSIPATION_PER_VENT});
		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.medium, new Float[] {0.02f, 4f * Misc.DISSIPATION_PER_VENT});
		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.small, new Float[] {0.01f, 2f * Misc.DISSIPATION_PER_VENT});
		mutableStatBonus.addAll(fluxDissipationBonus.keySet());

		fighterBayBonus.put(lyr_internals.id.shunts.launchTube.large, 1.0f);
		mutableStatBonus.addAll(fighterBayBonus.keySet());
	}
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD / DATA CLASS, ACTIONS ARE HANDLED THROUGH BASE
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(hullModSpecId)) {
			if (extraActiveInfoInHullMods) {
				Map<String, Integer> capacitors = ehm_shuntCount(variant, lyr_internals.tag.capacitorShunt);
	
				if (!capacitors.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CAPACITORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: capacitors.keySet()) {
						tooltip.addPara(capacitors.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (extraInactiveInfoInHullMods) {
					tooltip.addSectionHeading("NO CAPACITORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", 2f);
				}

				Map<String, Integer> dissipators = ehm_shuntCount(variant, lyr_internals.tag.dissipatorShunt);
	
				if (!dissipators.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DISSIPATORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: dissipators.keySet()) {
						tooltip.addPara(dissipators.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (extraInactiveInfoInHullMods) {
					tooltip.addSectionHeading("NO DISSIPATORS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", 2f);
				}
	
				Map<String, Integer> launchTubes = ehm_shuntCount(variant, lyr_internals.tag.tubeShunt);
	
				if (!launchTubes.isEmpty()) {
					tooltip.addSectionHeading("EXTRA HANGARS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: launchTubes.keySet()) {
						tooltip.addPara(launchTubes.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (extraInactiveInfoInHullMods) {
					tooltip.addSectionHeading("NO EXTRA HANGARS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No large weapon slots are turned into hangars. Each large slot is turned into a single fighter bay with a launch tube.", 2f);
				}
			}	
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
}
