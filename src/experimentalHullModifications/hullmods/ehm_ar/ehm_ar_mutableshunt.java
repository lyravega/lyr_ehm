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
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.id.shunts.capacitors;
import experimentalHullModifications.misc.ehm_internals.id.shunts.dissipators;
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
		if (fluxShuntSet.contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (fluxShuntSet.contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	static final Set<String> fluxShuntSet = new HashSet<String>();
	static final Map<String, Integer> capacitorMap = new HashMap<String, Integer>();
	static final Map<String, Integer> dissipatorMap = new HashMap<String, Integer>();
	static final float capacitorFlatMod = 1.5f * Misc.FLUX_PER_CAPACITOR;
	static final float dissipatorFlatMod = 1.5f * Misc.DISSIPATION_PER_VENT;
	static final float fluxMultMod = 0.01f;

	static {
		capacitorMap.put(ehm_internals.id.shunts.capacitors.large, 4);
		capacitorMap.put(ehm_internals.id.shunts.capacitors.medium, 2);
		capacitorMap.put(ehm_internals.id.shunts.capacitors.small, 1);
		fluxShuntSet.addAll(capacitorMap.keySet());

		dissipatorMap.put(ehm_internals.id.shunts.dissipators.large, 4);
		dissipatorMap.put(ehm_internals.id.shunts.dissipators.medium, 2);
		dissipatorMap.put(ehm_internals.id.shunts.dissipators.small, 1);
		fluxShuntSet.addAll(dissipatorMap.keySet());
	}

	@Override
	public void applyEffectsBeforeShipCreation(final HullSize hullSize, final MutableShipStatsAPI stats, final String hullModSpecId) {
		final ShipVariantAPI variant = stats.getVariant();
		final lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec());
		final List<WeaponSlotAPI> shunts = hullSpec.getAllWeaponSlotsCopy();

		final StatBonus dissipatorStat = stats.getDynamic().getMod(ehm_internals.id.stats.dissipators);
		final StatBonus capacitorStat = stats.getDynamic().getMod(ehm_internals.id.stats.capacitors);

		for (final Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			final WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			final String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }
			if (slotId.startsWith(ehm_internals.affix.convertedSlot)) { iterator.remove(); continue; }

			final WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(ehm_internals.tag.experimental)) { iterator.remove(); continue; }

			final String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case capacitors.large: case capacitors.medium: case capacitors.small: {
					capacitorStat.modifyFlat(slotId, capacitorMap.get(shuntId));
					break;
				} case dissipators.large: case dissipators.medium: case dissipators.small: {
					dissipatorStat.modifyFlat(slotId, dissipatorMap.get(shuntId));
					break;
				} default: { iterator.remove(); break; }
			}
		}

		for (final WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			final String slotId = slot.getId();
			final String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case capacitors.large: case capacitors.medium: case capacitors.small:
				case dissipators.large: case dissipators.medium: case dissipators.small: {
					ehm_deactivateSlot(hullSpec, shuntId, slotId);
					break;
				} default: break;
			}
		}

		final float effectiveCapacitorStat = capacitorStat.computeEffective(0f);
		final float effectiveDissipatorStat = dissipatorStat.computeEffective(0f);

		stats.getFluxCapacity().modifyMult(this.hullModSpecId, 1f+effectiveCapacitorStat*fluxMultMod);
		stats.getFluxCapacity().modifyFlat(this.hullModSpecId, effectiveCapacitorStat*capacitorFlatMod);
		stats.getFluxDissipation().modifyMult(this.hullModSpecId, 1f+effectiveDissipatorStat*fluxMultMod);
		stats.getFluxDissipation().modifyFlat(this.hullModSpecId, effectiveDissipatorStat*dissipatorFlatMod);

		variant.setHullSpecAPI(hullSpec.retrieve());
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
				Map<String, Integer> capacitors = ehm_shuntCount(ship, ehm_internals.tag.capacitorShunt);

				if (!capacitors.isEmpty()) {
					float totalBonus = ship.getMutableStats().getFluxCapacity().modified-(variant.getNumFluxCapacitors()*Misc.FLUX_PER_CAPACITOR+variant.getHullSpec().getFluxCapacity());

					tooltip.addSectionHeading("ACTIVE CAPACITORS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("Total capacity bonus: "+(int) totalBonus, 2f);
					for (String shuntId: capacitors.keySet()) {
						tooltip.addPara(capacitors.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CAPACITORS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No capacitors are installed. Capacitors increase the total flux capacity of the ship, and affect built-in capacitors.", 2f);
				}

				Map<String, Integer> dissipators = ehm_shuntCount(ship, ehm_internals.tag.dissipatorShunt);

				if (!dissipators.isEmpty()) {
					float totalBonus = ship.getMutableStats().getFluxDissipation().modified-(variant.getNumFluxVents()*Misc.DISSIPATION_PER_VENT+variant.getHullSpec().getFluxDissipation());

					tooltip.addSectionHeading("ACTIVE DISSIPATORS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("Total dissipation bonus: "+(int) totalBonus, 2f);
					for (String shuntId: dissipators.keySet()) {
						tooltip.addPara(dissipators.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DISSIPATORS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No dissipators are installed. Dissipators increase the total flux dissipation of the ship, and affect built-in vents.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);
		}
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		return true;
	}
	//#endregion
}
