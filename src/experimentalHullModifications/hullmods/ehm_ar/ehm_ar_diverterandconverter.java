package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.shunts.converters;
import experimentalHullModifications.misc.ehm_internals.shunts.converters.converterParameters;
import experimentalHullModifications.misc.ehm_internals.shunts.diverters;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_diverterandconverter extends _ehm_ar_base {
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

	static final Map<String, converterParameters> converterMap = converters.dataMap;
	static final Map<String, Integer> diverterMap = diverters.dataMap;
	static final Set<String> comboSet = new HashSet<String>();
	static {
		comboSet.addAll(converters.idSet);
		comboSet.addAll(diverters.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		List<WeaponSlotAPI> shunts = lyr_hullSpec.getAllWeaponSlotsCopy();

		int slotPointsFromMods = ehm_slotPointsFromHullMods(variant);
		int slotPoints = slotPointsFromMods;	// as hullMod methods are called several times, slotPoints accumulate correctly on subsequent call(s)

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }
			// if (slotId.startsWith(ehm_internals.affix.convertedSlot)) { iterator.remove(); continue; }	// the fuck were you thinking you dimwit

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(ehm_internals.hullmods.tags.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case converters.ids.mediumToLarge: case converters.ids.smallToLarge: case converters.ids.smallToMedium: {
					if (!slotId.startsWith(ehm_internals.affixes.normalSlot)) { iterator.remove(); break; }
					if (!slot.isDecorative()) break;
					int mod = converterMap.get(shuntId).getChildCost();
					slotPoints -= mod;
					stats.getDynamic().getMod(ehm_internals.stats.slotPointsToConverters).modifyFlat(slotId, -mod);	// used in tooltips
					break;
				} case diverters.ids.large: case diverters.ids.medium: case diverters.ids.small: {
					if (slotId.startsWith(ehm_internals.affixes.convertedSlot)) { iterator.remove(); break; }
					if (!slot.isDecorative()) break;
					int mod = diverterMap.get(shuntId);
					slotPoints += mod;
					stats.getDynamic().getMod(ehm_internals.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);	// used in tooltips
					break;
				} default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case converters.ids.mediumToLarge: case converters.ids.smallToLarge: case converters.ids.smallToMedium: {
					int cost = converterMap.get(shuntId).getChildCost();
					if (slotPoints - cost < 0) break;
					slotPoints -= cost;
					ehm_convertSlot(lyr_hullSpec, shuntId, slotId);
					break;
				} case diverters.ids.large: case diverters.ids.medium: case diverters.ids.small: {
					slotPoints += diverterMap.get(shuntId);
					ehm_deactivateSlot(lyr_hullSpec, shuntId, slotId);
					break;
				} default: break;
			}
		}

		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(this.hullModSpecId, Math.max(0, ehm_settings.getBaseSlotPointPenalty()*Math.min(slotPointsFromMods, slotPointsFromMods - slotPoints)));

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
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

		if (ship.getVariant().hasHullMod(this.hullModSpecId)) {
			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			int fromMods = (int) dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).computeEffective(0f);
			int fromDiverters = (int) dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).computeEffective(0f);
			int toConverters = (int) dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).computeEffective(0f);
			int total = fromMods + fromDiverters + toConverters;
			int deploymentPenalty = ehm_settings.getBaseSlotPointPenalty() > 0 ? Math.max(0, ehm_settings.getBaseSlotPointPenalty()*Math.min(fromMods, fromMods - total)) : 0;

			if (total > 0) tooltip.addSectionHeading(total + " UNUSED SLOT POINTS", header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			else if (total == 0) tooltip.addSectionHeading("NO SLOT POINTS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			else tooltip.addSectionHeading("SLOT POINT DEFICIT", header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (fromMods > 0) tooltip.addPara("Hull modifications are providing " + fromMods + " slot points", 2f, header.sEffect_textColour, fromMods + " slot points");
			if (fromDiverters > 0) tooltip.addPara("Diverter shunts are providing " + fromDiverters + " slot points", 2f, header.sEffect_textColour, fromDiverters + " slot points");
			if (toConverters < 0) tooltip.addPara("Converter shunts are utilizing " + toConverters + " slot points", 2f, header.notApplicable_textColour, toConverters + " slot points");
			if (deploymentPenalty > 0) tooltip.addPara("Ship will require an additional " + deploymentPenalty + " deployment points", 2f, header.notApplicable_textColour, deploymentPenalty + " deployment points");

			if (ehm_settings.getShowInfoForActivators()) {
				Map<String, Integer> converterCount = ehm_shuntCount(ship, converters.tag);

				if (!converterCount.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					for (String shuntId: converterCount.keySet()) {
						tooltip.addPara(converterCount.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO CONVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No converters are installed. Converters are used to make a smaller slot a bigger one, if there are enough slot points.", 2f);
				}

				Map<String, Integer> diverterCount = ehm_shuntCount(ship, diverters.tag);

				if (!diverterCount.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					for (String shuntId: diverterCount.keySet()) {
						tooltip.addPara(diverterCount.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO DIVERTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No diverters are installed. Diverters disable a slot and provide slot points that are used by converters in turn.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
