package experimentalHullModifications.shunts;

import java.util.HashMap;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.dissipators;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.proxies.lyr_hullSpec;

/**
 * This shunt effect class serves as a companion object for its own activator retrofit.
 * See {@link ehm_ar_mutableshunt} hullmod effect class where these will be utilized.
 * @category Shunt Effect
 * @author lyravega
 */
public final class ehm_dissipatorEffect extends _ehm_shuntEffect<Integer> {
	private static final float flatMod = 1.5f * Misc.DISSIPATION_PER_VENT;
	private static final float multMod = 0.01f;

	public ehm_dissipatorEffect(_ehm_ar_base activatorEffect) {
		super(activatorEffect, dissipators.groupTag, null, affixes.convertedSlot);
	}

	@Override
	public void registerShunt(MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant, WeaponSlotAPI slot, String slotId, String shuntId) {
		if (!variant.hasHullMod(this.shuntActivatorId)) return;
		if (!this.isValidSlot(slotId)) return;

		int mod = this.shuntDataMap.get(shuntId);
		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
		dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, mod);
	}

	@Override
	public void processShunts(lyr_hullSpec hullSpec, MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant) {
		HashMap<String, StatMod> dissipatorShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!dissipatorShunts.isEmpty()) {
			for (String slotId : dissipatorShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);
				int mod = this.shuntDataMap.get(shuntId);

				dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, mod);
				this.activateShunt(hullSpec, slotId, shuntId);
			}

			float dissipatorAmount = dynamicStats.getMod(this.shuntGroupTag).computeEffective(0f);
			float dissipatorFlatMod = dissipatorAmount*flatMod;
			float dissipatorMultMod = 1f+dissipatorAmount*multMod;

			stats.getFluxDissipation().modifyFlat(this.shuntActivatorId, dissipatorFlatMod);
			stats.getFluxDissipation().modifyMult(this.shuntActivatorId, dissipatorMultMod);
		}
	}

	@Override
	public void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		ShipVariantAPI variant = ship.getVariant();
		DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
		HashMap<String, StatMod> dissipatorShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!dissipatorShunts.isEmpty()) {
			int totalBonus = Math.round(ship.getMutableStats().getFluxDissipation().modified-(variant.getNumFluxVents()*Misc.DISSIPATION_PER_VENT+variant.getHullSpec().getFluxDissipation()));

			tooltip.addSectionHeading("DISSIPATORS (+"+totalBonus+" DISSIPATION)", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			this.printShuntCountsOnTooltip(tooltip, variant, dissipatorShunts.keySet());
		} else if (lyr_ehm.lunaSettings.getShowFullInfoForActivators()) {
			tooltip.addSectionHeading("NO DISSIPATORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", text.padding);
		}
	}
}