package data.hullmods.ehm_ar;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public class ehm_ar_mutableshunt extends _ehm_ar_base {
	static final Map<String,Float> fluxCapacityBonus = new HashMap<String,Float>();
	static final Map<String,Float> fluxDissipationBonus = new HashMap<String,Float>();
	static final Map<String,Float> fighterBayBonus = new HashMap<String,Float>();
	static final Map<String,Float> mutableStatBonus = new HashMap<String,Float>();
	static {
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.large, 0.16f);
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.medium, 0.08f);
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.small, 0.04f);
		mutableStatBonus.putAll(fluxCapacityBonus);

		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.large, 0.16f);
		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.medium, 0.08f);
		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.small, 0.04f);
		mutableStatBonus.putAll(fluxDissipationBonus);

		fighterBayBonus.put(lyr_internals.id.shunts.launchTube.large, 1.00f);
		mutableStatBonus.putAll(fighterBayBonus);
	}
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD, HANDLED THROUGH BASE
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(hullModSpecId)) {
			Map<String, Integer> adapters = ehm_shuntCount(variant, lyr_internals.tag.adapterShunt);

			if (!adapters.isEmpty()) {
				tooltip.addSectionHeading("ACTIVE ADAPTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
				for (String shuntId: adapters.keySet()) {
					tooltip.addPara(adapters.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
				}
			} else {
				tooltip.addSectionHeading("NO ADAPTERS", lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);
				tooltip.addPara("No installed adapters", 2f);
			}
		}
		
		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
}
