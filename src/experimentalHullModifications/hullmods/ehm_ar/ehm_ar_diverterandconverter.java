package experimentalHullModifications.hullmods.ehm_ar;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.shunts;
import experimentalHullModifications.misc.ehm_internals.shunts.converters;
import experimentalHullModifications.misc.ehm_internals.shunts.diverters;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.proxies.ehm_hullSpec;
import experimentalHullModifications.shunts._ehm_shuntEffect;
import experimentalHullModifications.shunts.ehm_converterEffect;
import experimentalHullModifications.shunts.ehm_converterEffect.converterParameters;
import experimentalHullModifications.shunts.ehm_diverterEffect;
import lyravega.utilities.lyr_tooltipUtilities.colour;

/**
 * Activator retrofit that controls the listed shunts below. Their effects and such are separated to
 * keep this class relatively small.
 * @see ehm_converterEffect Converters
 * @see ehm_diverterEffect Diverters
 * @category Activator Retrofit
 * @author lyravega
 */
public final class ehm_ar_diverterandconverter extends _ehm_ar_base {
	public static _ehm_shuntEffect<converterParameters> converterEffect;
	public static _ehm_shuntEffect<Integer> diverterEffect;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		converterEffect = new ehm_converterEffect(this);
		converterEffect.addShuntData(converters.ids.mediumToLarge, new converterParameters("ML", WeaponSize.LARGE, shunts.slotValues.get(WeaponSize.LARGE) - shunts.slotValues.get(WeaponSize.MEDIUM)));
		converterEffect.addShuntData(converters.ids.smallToLarge, new converterParameters("SL", WeaponSize.LARGE, shunts.slotValues.get(WeaponSize.LARGE) - shunts.slotValues.get(WeaponSize.SMALL)));
		converterEffect.addShuntData(converters.ids.smallToMedium, new converterParameters("SM", WeaponSize.MEDIUM, shunts.slotValues.get(WeaponSize.MEDIUM) - shunts.slotValues.get(WeaponSize.SMALL)));

		diverterEffect = new ehm_diverterEffect(this);
		diverterEffect.addShuntData(diverters.ids.large, shunts.slotValues.get(WeaponSize.LARGE));
		diverterEffect.addShuntData(diverters.ids.medium, shunts.slotValues.get(WeaponSize.MEDIUM));
		diverterEffect.addShuntData(diverters.ids.small, shunts.slotValues.get(WeaponSize.SMALL));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		diverterEffect.processShunts(hullSpec, stats, dynamicStats, variant);
		converterEffect.processShunts(hullSpec, stats, dynamicStats, variant);	// must be after diverters

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
			case 4: return lyr_ehm.lunaSettings.getBaseSlotPointPenalty()+"";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			int slotPoints = Math.round(dynamicStats.getMod(ehm_internals.statIds.slotPoints).computeEffective(0f));
			int slotPointsNeeded = Math.round(dynamicStats.getMod(ehm_internals.statIds.slotPointsNeeded).computeEffective(0f));
			int slotPointsUsed = Math.round(dynamicStats.getMod(ehm_internals.statIds.slotPointsUsed).computeEffective(0f));
			int slotPointsFromMods = Math.round(dynamicStats.getMod(ehm_internals.statIds.slotPointsFromMods).computeEffective(0f));
			int slotPointsFromDiverters = Math.round(dynamicStats.getMod(ehm_internals.statIds.slotPointsFromDiverters).computeEffective(0f));
			int slotPointsToConverters = Math.round(dynamicStats.getMod(ehm_internals.statIds.slotPointsToConverters).computeEffective(0f));
			int slotPointsPenalty = lyr_ehm.lunaSettings.getBaseSlotPointPenalty()*Math.max(0, slotPointsUsed - slotPointsFromDiverters);

			tooltip.addSectionHeading(slotPointsUsed+"/"+slotPoints+(slotPointsNeeded > slotPoints ? " ("+slotPointsNeeded+") " : " ")+"SLOT POINTS", (slotPointsUsed != slotPoints) ? colour.negative : colour.highlight, header.invisible_bgColour, Alignment.MID, header.padding);
			if (slotPointsPenalty > 0) tooltip.addPara("Ship will require an additional %s", 2f, colour.negative, slotPointsPenalty + " deployment points");
			if (slotPointsFromMods > 0) tooltip.addPara("Hull modifications are providing %s", 2f, colour.positive, slotPointsFromMods + " slot points");
			if (slotPointsFromDiverters > 0) tooltip.addPara("Diverter shunts are providing %s", 2f, colour.positive, slotPointsFromDiverters + " slot points");
			if (slotPointsToConverters > 0) tooltip.addPara("Converter shunts are utilizing %s", 2f, colour.highlight, slotPointsToConverters + " slot points");
			if (slotPointsNeeded > slotPoints) tooltip.addPara("%s required for inactive converters", 2f, colour.highlight,  (slotPointsNeeded - slotPoints) + " additional slot points");
			else if (slotPointsUsed < slotPoints) tooltip.addPara("%s may be utilized", 2f, colour.highlight,  (slotPoints - slotPointsUsed) + " additional slot points");

			if (lyr_ehm.lunaSettings.getShowInfoForActivators()) {
				converterEffect.addShuntInfoToActivatorDescription(tooltip, hullSize, ship, width, isForModSpec);
				diverterEffect.addShuntInfoToActivatorDescription(tooltip, hullSize, ship, width, isForModSpec);
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
