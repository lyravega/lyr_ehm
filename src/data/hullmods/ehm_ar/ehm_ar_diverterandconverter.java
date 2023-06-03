package data.hullmods.ehm_ar;

import static data.hullmods.ehm_mr.ehm_mr_overengineered.slotPointBonus;
import static lyravega.plugin.lyr_lunaSettings.extraInfoInHullMods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip;

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
	
	static final Map<String, childParameters> converterMap = new HashMap<String, childParameters>();
	private static childParameters mediumToLarge = new childParameters("ML", WeaponSize.LARGE, 2);
	private static childParameters smallToLarge = new childParameters("SL", WeaponSize.LARGE, 3);
	private static childParameters smallToMedium = new childParameters("SM", WeaponSize.MEDIUM, 1);
	static {
		converterMap.put(lyr_internals.id.shunts.converters.mediumToLarge, mediumToLarge);
		converterMap.put(lyr_internals.id.shunts.converters.smallToLarge, smallToLarge);
		converterMap.put(lyr_internals.id.shunts.converters.smallToMedium, smallToMedium);
	}

	static final Map<String, Integer> diverterMap = new HashMap<String, Integer>();	// slotPoint reward
	static {
		diverterMap.put(lyr_internals.id.shunts.diverters.large, 4);
		diverterMap.put(lyr_internals.id.shunts.diverters.medium, 2);
		diverterMap.put(lyr_internals.id.shunts.diverters.small, 1);
	}

	static final Set<String> diverterConverterSet = new HashSet<String>();
	static {
		diverterConverterSet.addAll(converterMap.keySet());
		diverterConverterSet.addAll(diverterMap.keySet());
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD / DATA CLASS, ACTIONS ARE HANDLED THROUGH BASE
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(hullModSpecId)) {
			boolean showInfo = !extraInfoInHullMods.equals("None");
			boolean showFullInfo = extraInfoInHullMods.equals("Full");

			int pointBonus = variant.getSMods().contains(lyr_internals.id.hullmods.overengineered) ? slotPointBonus.get(hullSize) : 0;
			int[] pointArray = ehm_slotPointCalculation(variant, pointBonus);

			if (pointArray[0] > 0) {
				tooltip.addSectionHeading(pointArray[0] + " UNUSED SLOT POINTS", lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding).flash(1.0f, 1.0f);
			} else {
				tooltip.addSectionHeading("NO SLOT POINTS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
			}
			if (pointArray[1] > 0) tooltip.addPara("Hull modifications are providing " + pointArray[1] + " slot points", 2f, lyr_tooltip.header.sEffect_textColour, pointArray[1] + " slot points");
			if (pointArray[2] > 0)tooltip.addPara("Diverter shunts are providing " + pointArray[2] + " slot points in total", 2f, lyr_tooltip.header.sEffect_textColour, pointArray[2] + " slot points");
			if (pointArray[3] < 0)tooltip.addPara("Converter shunts are consuming " + pointArray[3] + " slot points in total", 2f, lyr_tooltip.header.notApplicable_textColour, pointArray[3] + " slot points");

			if (showInfo) {
				Map<String, Integer> converters = ehm_shuntCount(variant, lyr_internals.tag.converterShunt);

				if (!converters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CONVERTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: converters.keySet()) {
						tooltip.addPara(converters.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO CONVERTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
				}

				Map<String, Integer> diverters = ehm_shuntCount(variant, lyr_internals.tag.diverterShunt);

				if (!diverters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DIVERTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: diverters.keySet()) {
						tooltip.addPara(diverters.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO DIVERTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No diverters are installed. Diverters disable a slot and provide slot points that are used by converters in turn.", 2f);
				}
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
