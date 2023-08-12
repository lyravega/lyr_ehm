package experimentalHullModifications.hullmods.ehm_ar;

import static experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base.ehm_adaptSlot;
import static experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base.ehm_convertSlot;
import static experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base.ehm_deactivateSlot;
import static lyravega.listeners.lyr_lunaSettingsListener.baseSlotPointPenalty;
import static lyravega.listeners.lyr_lunaSettingsListener.extraInfoInHullMods;
import static lyravega.tools.lyr_uiTools.commitVariantChanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_hullSpec;
import lyravega.misc.lyr_internals.id.hullmods;
import lyravega.misc.lyr_internals.id.shunts.adapters;
import lyravega.misc.lyr_internals.id.shunts.capacitors;
import lyravega.misc.lyr_internals.id.shunts.converters;
import lyravega.misc.lyr_internals.id.shunts.dissipators;
import lyravega.misc.lyr_internals.id.shunts.diverters;
import lyravega.misc.lyr_internals.id.shunts.launchTubes;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public final class ehm_ar_diverterandconverter extends _ehm_ar_base {
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
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		List<WeaponSlotAPI> shunts = hullSpec.getAllWeaponSlotsCopy();
		boolean commitVariantChanges = false;

		int slotPointsFromMods = ehm_slotPointsFromHullMods(variant);
		int slotPoints = slotPointsFromMods;	// as hullMod methods are called several times, slotPoints accumulate correctly on subsequent call(s)

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (!slotId.startsWith(lyr_internals.affix.normalSlot)) { iterator.remove(); continue; }
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(lyr_internals.tag.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case converters.mediumToLarge: case converters.smallToLarge: case converters.smallToMedium:
					if (slot.isDecorative()) slotPoints -= converterMap.get(shuntId).getChildCost();
					break;
				case diverters.large: case diverters.medium: case diverters.small:
					if (slot.isDecorative()) slotPoints += diverterMap.get(shuntId);
					break;
				default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case converters.mediumToLarge: case converters.smallToLarge: case converters.smallToMedium:
					int cost = converterMap.get(shuntId).getChildCost();
					if (slotPoints - cost < 0) break;
					slotPoints -= cost;
					commitVariantChanges = ehm_convertSlot(hullSpec, shuntId, slotId);
					break;
				case diverters.large: case diverters.medium: case diverters.small:
					slotPoints += diverterMap.get(shuntId);
					commitVariantChanges = ehm_deactivateSlot(hullSpec, shuntId, slotId);
					break;
				default: break;
			}
		}

		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(hullmods.diverterandconverter, Math.max(0, baseSlotPointPenalty*Math.min(slotPointsFromMods, slotPointsFromMods - slotPoints)));

		variant.setHullSpecAPI(hullSpec.retrieve());
		if (commitVariantChanges && !isGettingRestored(variant)) { commitVariantChanges = false; commitVariantChanges(); }
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "converters";
			case 1: return "diverters";
			case 2: return "gained and utilized";
			case 3: return "deployment point";
			case 4: return baseSlotPointPenalty+"";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(hullModSpecId)) {
			boolean showInfo = !extraInfoInHullMods.equals("None");
			boolean showFullInfo = extraInfoInHullMods.equals("Full");

			int[] pointArray = ehm_slotPointCalculation(ship);

			if (pointArray[0] > 0) tooltip.addSectionHeading(pointArray[0] + " UNUSED SLOT POINTS", header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			else if (pointArray[0] == 0) tooltip.addSectionHeading("NO SLOT POINTS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
			else tooltip.addSectionHeading("SLOT POINT DEFICIT", header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (pointArray[1] > 0) tooltip.addPara("Hull modifications are providing " + pointArray[1] + " slot points", 2f, header.sEffect_textColour, pointArray[1] + " slot points");
			if (pointArray[2] > 0) tooltip.addPara("Diverter shunts are providing " + pointArray[2] + " slot points", 2f, header.sEffect_textColour, pointArray[2] + " slot points");
			if (pointArray[3] > 0) tooltip.addPara("Converter shunts are utilizing " + pointArray[3] + " slot points", 2f, header.notApplicable_textColour, pointArray[3] + " slot points");
			if (baseSlotPointPenalty > 0 && pointArray[4] > 0) tooltip.addPara("Ship will require an additional " + pointArray[4] + " deployment points", 2f, header.notApplicable_textColour, pointArray[4] + " deployment points");

			if (showInfo) {
				Map<String, Integer> converters = ehm_shuntCount(ship, lyr_internals.tag.converterShunt);

				if (!converters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CONVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					for (String shuntId: converters.keySet()) {
						tooltip.addPara(converters.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO CONVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
				}

				Map<String, Integer> diverters = ehm_shuntCount(ship, lyr_internals.tag.diverterShunt);

				if (!diverters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DIVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					for (String shuntId: diverters.keySet()) {
						tooltip.addPara(diverters.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (showFullInfo) {
					tooltip.addSectionHeading("NO DIVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No diverters are installed. Diverters disable a slot and provide slot points that are used by converters in turn.", 2f);
				}
			}
		}
		
		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);

			if (ehm_hasWeapons(ship, lyr_internals.affix.convertedSlot)) tooltip.addPara(text.hasWeaponsOnConvertedSlots[0], text.padding).setHighlight(text.hasWeaponsOnConvertedSlots[1]);
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
