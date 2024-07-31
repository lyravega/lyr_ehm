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
import experimentalHullModifications.misc.ehm_internals.shunts.capacitors;
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
public final class ehm_capacitorEffect extends _ehm_shuntEffect<Integer> {
	private static final float flatMod = 1.5f * Misc.FLUX_PER_CAPACITOR;
	private static final float multMod = 0.01f;

	public ehm_capacitorEffect(_ehm_ar_base activatorEffect) {
		super(activatorEffect, capacitors.groupTag, null, affixes.convertedSlot);
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
		HashMap<String, StatMod> capacitorShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!capacitorShunts.isEmpty()) {
			for (String slotId : capacitorShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;
				String shuntId = variant.getWeaponId(slotId);

				dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, this.shuntDataMap.get(shuntId));
				this.activateShunt(hullSpec, slotId, shuntId);
			}

			float capacitorAmount = dynamicStats.getMod(this.shuntGroupTag).computeEffective(0f);
			float capacitorFlatMod = capacitorAmount*flatMod;
			float capacitorMultMod = 1f+capacitorAmount*multMod;

			stats.getFluxCapacity().modifyFlat(this.shuntActivatorId, capacitorFlatMod);
			stats.getFluxCapacity().modifyMult(this.shuntActivatorId, capacitorMultMod);
		}
	}

	@Override
	public void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		ShipVariantAPI variant = ship.getVariant();
		DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
		HashMap<String, StatMod> capacitorShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!capacitorShunts.isEmpty()) {
			int totalBonus = Math.round(ship.getMutableStats().getFluxCapacity().modified-(variant.getNumFluxCapacitors()*Misc.FLUX_PER_CAPACITOR+variant.getHullSpec().getFluxCapacity()));

			tooltip.addSectionHeading("CAPACITORS (+"+totalBonus+" CAPACITY)", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			this.printShuntCountsOnTooltip(tooltip, variant, capacitorShunts.keySet());
		} else if (lyr_ehm.lunaSettings.getShowFullInfoForActivators()) {
			tooltip.addSectionHeading("NO CAPACITORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", text.padding);
		}
	}
}