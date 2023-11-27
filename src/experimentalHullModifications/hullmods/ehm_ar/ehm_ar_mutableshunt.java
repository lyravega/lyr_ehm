package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals.shunts.capacitors;
import experimentalHullModifications.misc.ehm_internals.shunts.dissipators;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_mutableshunt extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (comboSet.contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (comboSet.contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	static final Map<String, Integer> capacitorMap = capacitors.dataMap;
	static final Map<String, Integer> dissipatorMap = dissipators.dataMap;
	static final Set<String> comboSet = new HashSet<String>();
	static final float capacitorFlatMod = 1.5f * Misc.FLUX_PER_CAPACITOR;
	static final float dissipatorFlatMod = 1.5f * Misc.DISSIPATION_PER_VENT;
	static final float fluxMultMod = 0.01f;

	static {
		comboSet.addAll(capacitors.idSet);
		comboSet.addAll(dissipators.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		HashMap<String, StatMod> dissipatorShunts = stats.getDynamic().getMod(dissipators.groupTag).getFlatBonuses();
		if (dissipatorShunts != null && !dissipatorShunts.isEmpty()) {
			float dissipatorAmount =  stats.getDynamic().getMod(dissipators.groupTag).computeEffective(0f);

			for (String slotId : dissipatorShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				ehm_deactivateSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}

			stats.getFluxDissipation().modifyMult(this.hullModSpecId, 1f+dissipatorAmount*fluxMultMod);
			stats.getFluxDissipation().modifyFlat(this.hullModSpecId, dissipatorAmount*dissipatorFlatMod);
		}

		HashMap<String, StatMod> capacitorShunts = stats.getDynamic().getMod(capacitors.groupTag).getFlatBonuses();
		if (capacitorShunts != null && !capacitorShunts.isEmpty()) {
			float capacitorAmount =  stats.getDynamic().getMod(capacitors.groupTag).computeEffective(0f);

			for (String slotId : capacitorShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				ehm_deactivateSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
			}

			stats.getFluxCapacity().modifyMult(this.hullModSpecId, 1f+capacitorAmount*fluxMultMod);
			stats.getFluxCapacity().modifyFlat(this.hullModSpecId, capacitorAmount*capacitorFlatMod);
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
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
			final DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> capacitorShunts = dynamicStats.getMod(capacitors.groupTag).getFlatBonuses();
				if (capacitorShunts != null && !capacitorShunts.isEmpty()) {
					int totalBonus = Math.round(ship.getMutableStats().getFluxCapacity().modified-(variant.getNumFluxCapacitors()*Misc.FLUX_PER_CAPACITOR+variant.getHullSpec().getFluxCapacity()));

					tooltip.addSectionHeading("ACTIVE CAPACITORS (+"+totalBonus+")", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, dynamicStats, capacitors.idSet);
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CAPACITORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", 2f);
				}

				HashMap<String, StatMod> dissipatorShunts = dynamicStats.getMod(dissipators.groupTag).getFlatBonuses();
				if (dissipatorShunts != null && !dissipatorShunts.isEmpty()) {
					float totalBonus = ship.getMutableStats().getFluxDissipation().modified-(variant.getNumFluxVents()*Misc.DISSIPATION_PER_VENT+variant.getHullSpec().getFluxDissipation());

					tooltip.addSectionHeading("ACTIVE DISSIPATORS (+"+totalBonus+")", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, dynamicStats, dissipators.idSet);
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DISSIPATORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
}
