package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.Global;
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
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.id.hullmods;
import experimentalHullModifications.misc.ehm_internals.id.shunts.converters;
import experimentalHullModifications.misc.ehm_internals.id.shunts.diverters;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_diverterandconverter extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (diverterConverterSet.contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (diverterConverterSet.contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

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
		converterMap.put(ehm_internals.id.shunts.converters.mediumToLarge, mediumToLarge);
		converterMap.put(ehm_internals.id.shunts.converters.smallToLarge, smallToLarge);
		converterMap.put(ehm_internals.id.shunts.converters.smallToMedium, smallToMedium);
	}

	static final Map<String, Integer> diverterMap = new HashMap<String, Integer>();	// slotPoint reward
	static {
		diverterMap.put(ehm_internals.id.shunts.diverters.large, 4);
		diverterMap.put(ehm_internals.id.shunts.diverters.medium, 2);
		diverterMap.put(ehm_internals.id.shunts.diverters.small, 1);
	}

	static final Set<String> diverterConverterSet = new HashSet<String>();
	static {
		diverterConverterSet.addAll(converterMap.keySet());
		diverterConverterSet.addAll(diverterMap.keySet());
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec());
		List<WeaponSlotAPI> shunts = hullSpec.getAllWeaponSlotsCopy();

		int slotPointsFromMods = ehm_slotPointsFromHullMods(variant);
		int slotPoints = slotPointsFromMods;	// as hullMod methods are called several times, slotPoints accumulate correctly on subsequent call(s)

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }
			if (slotId.startsWith(ehm_internals.affix.convertedSlot)) { iterator.remove(); continue; }

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(ehm_internals.tag.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case converters.mediumToLarge: case converters.smallToLarge: case converters.smallToMedium: {
					if (!slot.isDecorative()) break;
					int mod = converterMap.get(shuntId).getChildCost();
					slotPoints -= mod;
					stats.getDynamic().getMod(ehm_internals.id.stats.slotPointsToConverters).modifyFlat(slotId, -mod);	// used in tooltips
					break;
				} case diverters.large: case diverters.medium: case diverters.small: {
					if (!slot.isDecorative()) break;
					int mod = diverterMap.get(shuntId);
					slotPoints += mod;
					stats.getDynamic().getMod(ehm_internals.id.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);	// used in tooltips
					break;
				} default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case converters.mediumToLarge: case converters.smallToLarge: case converters.smallToMedium: {
					int cost = converterMap.get(shuntId).getChildCost();
					if (slotPoints - cost < 0) break;
					slotPoints -= cost;
					ehm_convertSlot(hullSpec, shuntId, slotId);
					break;
				} case diverters.large: case diverters.medium: case diverters.small: {
					slotPoints += diverterMap.get(shuntId);
					ehm_deactivateSlot(hullSpec, shuntId, slotId);
					break;
				} default: break;
			}
		}

		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(hullmods.diverterandconverter, Math.max(0, ehm_settings.getBaseSlotPointPenalty()*Math.min(slotPointsFromMods, slotPointsFromMods - slotPoints)));

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "converters";
			case 1: return "diverters";
			case 2: return "gained and utilized";
			case 3: return "deployment point";
			case 4: return ehm_settings.getBaseSlotPointPenalty()+"";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			int fromMods = (int) dynamicStats.getMod(ehm_internals.id.stats.slotPointsFromMods).computeEffective(0f);
			int fromDiverters = (int) dynamicStats.getMod(ehm_internals.id.stats.slotPointsFromDiverters).computeEffective(0f);
			int toConverters = (int) dynamicStats.getMod(ehm_internals.id.stats.slotPointsToConverters).computeEffective(0f);
			int total = fromMods + fromDiverters + toConverters;
			int deploymentPenalty = ehm_settings.getBaseSlotPointPenalty() > 0 ? Math.max(0, ehm_settings.getBaseSlotPointPenalty()*Math.min(fromMods, fromMods - total)) : 0;

			if (total > 0) tooltip.addSectionHeading(total + " UNUSED SLOT POINTS", header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			else if (total == 0) tooltip.addSectionHeading("NO SLOT POINTS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
			else tooltip.addSectionHeading("SLOT POINT DEFICIT", header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (fromMods > 0) tooltip.addPara("Hull modifications are providing " + fromMods + " slot points", 2f, header.sEffect_textColour, fromMods + " slot points");
			if (fromDiverters > 0) tooltip.addPara("Diverter shunts are providing " + fromDiverters + " slot points", 2f, header.sEffect_textColour, fromDiverters + " slot points");
			if (toConverters < 0) tooltip.addPara("Converter shunts are utilizing " + toConverters + " slot points", 2f, header.notApplicable_textColour, toConverters + " slot points");
			if (deploymentPenalty > 0) tooltip.addPara("Ship will require an additional " + deploymentPenalty + " deployment points", 2f, header.notApplicable_textColour, deploymentPenalty + " deployment points");

			if (ehm_settings.getShowInfoForActivators()) {
				Map<String, Integer> converters = ehm_shuntCount(ship, ehm_internals.tag.converterShunt);

				if (!converters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CONVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					for (String shuntId: converters.keySet()) {
						tooltip.addPara(converters.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CONVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
				}

				Map<String, Integer> diverters = ehm_shuntCount(ship, ehm_internals.tag.diverterShunt);

				if (!diverters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DIVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					for (String shuntId: diverters.keySet()) {
						tooltip.addPara(diverters.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DIVERTERS", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No diverters are installed. Diverters disable a slot and provide slot points that are used by converters in turn.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.locked_bgColour, Alignment.MID, header.padding);

			if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affix.convertedSlot)) tooltip.addPara(text.hasWeaponsOnConvertedSlots[0], text.padding).setHighlight(text.hasWeaponsOnConvertedSlots[1]);
		}
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affix.convertedSlot)) return false;

		return true;
	}
	//#endregion
}
