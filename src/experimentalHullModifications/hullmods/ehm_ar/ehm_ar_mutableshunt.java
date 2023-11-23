package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals;
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
		List<WeaponSlotAPI> shunts = lyr_hullSpec.getAllWeaponSlotsCopy();

		StatBonus dissipatorStat = stats.getDynamic().getMod(ehm_internals.stats.dissipators);
		StatBonus capacitorStat = stats.getDynamic().getMod(ehm_internals.stats.capacitors);

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }
			if (slotId.startsWith(ehm_internals.affixes.convertedSlot)) { iterator.remove(); continue; }

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(ehm_internals.hullmods.tags.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case capacitors.ids.large: case capacitors.ids.medium: case capacitors.ids.small: {
					capacitorStat.modifyFlat(slotId, capacitorMap.get(shuntId));
					break;
				} case dissipators.ids.large: case dissipators.ids.medium: case dissipators.ids.small: {
					dissipatorStat.modifyFlat(slotId, dissipatorMap.get(shuntId));
					break;
				} default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case capacitors.ids.large: case capacitors.ids.medium: case capacitors.ids.small:
				case dissipators.ids.large: case dissipators.ids.medium: case dissipators.ids.small: {
					ehm_deactivateSlot(lyr_hullSpec, shuntId, slotId);
					break;
				} default: break;
			}
		}

		float effectiveCapacitorStat = capacitorStat.computeEffective(0f);
		float effectiveDissipatorStat = dissipatorStat.computeEffective(0f);

		stats.getFluxCapacity().modifyMult(this.hullModSpecId, 1f+effectiveCapacitorStat*fluxMultMod);
		stats.getFluxCapacity().modifyFlat(this.hullModSpecId, effectiveCapacitorStat*capacitorFlatMod);
		stats.getFluxDissipation().modifyMult(this.hullModSpecId, 1f+effectiveDissipatorStat*fluxMultMod);
		stats.getFluxDissipation().modifyFlat(this.hullModSpecId, effectiveDissipatorStat*dissipatorFlatMod);

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
			if (ehm_settings.getShowInfoForActivators()) {
				Map<String, Integer> capacitorCount = ehm_shuntCount(ship, capacitors.tag);

				if (!capacitorCount.isEmpty()) {
					float totalBonus = ship.getMutableStats().getFluxCapacity().modified-(variant.getNumFluxCapacitors()*Misc.FLUX_PER_CAPACITOR+variant.getHullSpec().getFluxCapacity());

					tooltip.addSectionHeading("ACTIVE CAPACITORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("Total capacity bonus: "+(int) totalBonus, 2f);
					for (String shuntId: capacitorCount.keySet()) {
						tooltip.addPara(capacitorCount.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CAPACITORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", 2f);
				}

				Map<String, Integer> dissipatorCount = ehm_shuntCount(ship, dissipators.tag);

				if (!dissipatorCount.isEmpty()) {
					float totalBonus = ship.getMutableStats().getFluxDissipation().modified-(variant.getNumFluxVents()*Misc.DISSIPATION_PER_VENT+variant.getHullSpec().getFluxDissipation());

					tooltip.addSectionHeading("ACTIVE DISSIPATORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("Total dissipation bonus: "+(int) totalBonus, 2f);
					for (String shuntId: dissipatorCount.keySet()) {
						tooltip.addPara(dissipatorCount.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DISSIPATORS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
}
