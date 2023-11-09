package experimentalHullModifications.hullmods.ehm_wr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

/**
 * This class is used by weapon retrofit hullmods. They are pretty
 * straightforward in their operation; change all of the weapon slots
 * on a ship to a different type.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_wr_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		if (lyr_miscUtilities.removeHullModWithTag(variant, ehm_internals.tag.weaponRetrofit, this.hullModSpecId)) return;	// if installing this removes another, skip
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		// if (!_ehm_helpers.ehm_hasHullModWithTag(variant, lyr_internals.tag.weaponRetrofit, this.hullModSpecId))	// unlike other exclusive mods, this one needs to run to restore the slots to original first
			variant.setHullSpecAPI(ehm_weaponSlotRestore_lazy(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	/**
	 * Alters the weapon slots on the passed variant's hullSpec, and returns it.
	 * @param variant whose hullSpec will be altered
	 * @param conversions is a map that pairs slot types
	 * @param slotSize of the applicable slots, all sizes if {@code null}
	 * @return an altered hullSpec with different weaponSlots
	 * @see {@link #ehm_weaponSlotRestore()} reverses this process one slot at a time
	 */
	protected static final ShipHullSpecAPI ehm_weaponSlotRetrofit(ShipVariantAPI variant, Map<WeaponType, WeaponType> conversions, WeaponSize slotSize) {
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		for (WeaponSlotAPI slot: lyr_hullSpec.getAllWeaponSlotsCopy()) {
			if (slotSize != null && slot.getSlotSize() != slotSize) continue;

			String slotId = slot.getId();
			WeaponType convertFrom = slot.getWeaponType();

			if (conversions.containsKey(convertFrom)) {
				WeaponType convertTo = conversions.get(convertFrom);
				lyr_hullSpec.getWeaponSlot(slotId).setWeaponType(convertTo);
			}
		}

		return lyr_hullSpec.retrieve();
	}

	/**
	 * Refers to a stock hullSpec, and restores the slots on the passed variant's
	 * hullSpec one by one. Ignores activated adapters, and affects adapted slots.
	 * @param variant whose hullSpec will have its weaponSlots restored
	 * @return an altered hullSpec with restored weaponSlots
	 */
	@Deprecated
	public static final ShipHullSpecAPI ehm_weaponSlotRestore(ShipVariantAPI variant) {
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		for (WeaponSlotAPI stockSlot: lyr_hullSpec.referenceNonDamaged().getAllWeaponSlotsCopy()) {
			String slotId = stockSlot.getId();
			String weaponId = variant.getWeaponId(slotId);
			lyr_weaponSlot slot = lyr_hullSpec.getWeaponSlot(slotId);
			WeaponType stockSlotWeaponType = stockSlot.getWeaponType();

			// doesn't support new additions
			if (slot.retrieve().isDecorative() && ehm_internals.id.shunts.adapters.set.contains(weaponId)) {
				lyr_hullSpec.getWeaponSlot(ehm_internals.affix.adaptedSlot+slotId+"L").setWeaponType(stockSlotWeaponType);
				lyr_hullSpec.getWeaponSlot(ehm_internals.affix.adaptedSlot+slotId+"R").setWeaponType(stockSlotWeaponType);
				if (weaponId.endsWith("Triple"))
					lyr_hullSpec.getWeaponSlot(ehm_internals.affix.adaptedSlot+slotId+"C").setWeaponType(stockSlotWeaponType);
				else if (weaponId.endsWith("Quad")) {
					lyr_hullSpec.getWeaponSlot(ehm_internals.affix.adaptedSlot+slotId+"FL").setWeaponType(stockSlotWeaponType);
					lyr_hullSpec.getWeaponSlot(ehm_internals.affix.adaptedSlot+slotId+"FR").setWeaponType(stockSlotWeaponType);
				}
			} else {
				slot.setWeaponType(stockSlotWeaponType);
			}
		}

		return lyr_hullSpec.retrieve();
	}

	public static final ShipHullSpecAPI ehm_weaponSlotRestore_lazy(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.lacksBase, text.colourized.padding);
			// if (_ehm_helpers.ehm_hasHullmodWithTag(ship, lyr_internals.tag.weaponRetrofit, this.hullModSpecId)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasWeaponRetrofit, text.padding);
			if (ship.getVariant().hasHullMod(ehm_internals.id.hullmods.logisticsoverhaul)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasLogisticsOverhaul, text.colourized.padding);
		}

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (lyr_miscUtilities.hasWeapons(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeapons, text.colourized.padding);
			if (lyr_miscUtilities.hasAnyFittedWings(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWings, text.colourized.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) return false;
		// if (_ehm_helpers.ehm_hasHullmodWithTag(ship, lyr_internals.tag.weaponRetrofit, this.hullModSpecId)) return false;
		if (ship.getVariant().hasHullMod(ehm_internals.id.hullmods.logisticsoverhaul)) return false;

		return true;
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (lyr_miscUtilities.hasWeapons(ship)) return false;
		if (lyr_miscUtilities.hasAnyFittedWings(ship)) return false;

		return true;
	}
	//#endregion
}