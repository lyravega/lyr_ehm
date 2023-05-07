package data.hullmods.ehm_ar;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public class ehm_ar_diverterandconverter extends _ehm_ar_base {
	/**
	 * An inner class to supply the converters with relevant child data
	 */
	static class childParameters {
		private String childSuffix; // childIds are used as position identifier, and used as a suffix
		private int childCost;
		private WeaponSize childSize;

		private childParameters(String childSuffix, WeaponSize childSize, int childCost) {
			this.childSuffix = childSuffix;
			this.childCost = childCost;
			this.childSize = childSize;
		}

		String getChildSuffix() {
			return this.childSuffix;
		}

		int getChildCost() {
			return this.childCost;
		}

		WeaponSize getChildSize() {
			return this.childSize;
		}
	}
	
	static final Map<String, childParameters> converters = new HashMap<String, childParameters>();
	private static childParameters mediumToLarge = new childParameters("ML", WeaponSize.LARGE, 2);
	private static childParameters smallToLarge = new childParameters("SL", WeaponSize.LARGE, 3);
	private static childParameters smallToMedium = new childParameters("SM", WeaponSize.MEDIUM, 1);
	static {
		converters.put(lyr_internals.id.shunts.converters.mediumToLarge, mediumToLarge);
		converters.put(lyr_internals.id.shunts.converters.smallToLarge, smallToLarge);
		converters.put(lyr_internals.id.shunts.converters.smallToMedium, smallToMedium);
	}

	static final Map<String, Integer> diverters = new HashMap<String, Integer>();
	static {
		diverters.put(lyr_internals.id.shunts.diverters.large, 4);
		diverters.put(lyr_internals.id.shunts.diverters.medium, 2);
		diverters.put(lyr_internals.id.shunts.diverters.small, 1);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD, HANDLED THROUGH BASE
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
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

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(hullModSpecId) ? lyr_tooltip.header.lockedIn : lyr_tooltip.header.lockedOut;

			tooltip.addSectionHeading(inOrOut, lyr_tooltip.header.locked_textColour, lyr_tooltip.header.locked_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (ehm_hasWeapons(ship, lyr_internals.affix.convertedSlot)) tooltip.addPara(lyr_tooltip.text.hasWeaponsOnConvertedSlots, lyr_tooltip.text.padding);
		}
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		if (ehm_hasWeapons(ship, lyr_internals.affix.convertedSlot)) return false;

		return true;
	}
	//#endregion
}
