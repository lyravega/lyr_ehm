package experimentalHullModifications.hullmods.ehm_ar;

import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverterConverterSet;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverterMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_launchtube.launchTubeMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt.capacitorMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt.dissipatorMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterMap;
import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm._ehm_helpers;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.childParameters;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.childrenParameters;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_internals.id.hullmods;
import lyravega.misc.lyr_internals.id.shunts.adapters;
import lyravega.misc.lyr_internals.id.shunts.capacitors;
import lyravega.misc.lyr_internals.id.shunts.converters;
import lyravega.misc.lyr_internals.id.shunts.dissipators;
import lyravega.misc.lyr_internals.id.shunts.diverters;
import lyravega.misc.lyr_internals.id.shunts.launchTubes;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.misc.lyr_vectorUtility;
import lyravega.plugin.lyr_ehm;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;


/**
 * This class is used by slot activator hullmods. Slot activators are designed 
 * to search the ship for specific weapons, and perform operations on the 
 * hullSpec to yield interesting results, such as creating a new weapon slot. 
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_ar_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_activatorRemoval_lazy(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	private static final Pattern pattern = Pattern.compile("WS[ 0-9]{4}");
	private static Matcher matcher;

	@Deprecated
	public static final void ehm_processShunts(MutableShipStatsAPI stats, boolean isGettingRestored) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);

		boolean commitVariantChanges = false;

		// primarily to deal with stuff on load
		for (Iterator<String> iterator = variant.getFittedWeaponSlots().iterator(); iterator.hasNext();) {
			String slotId = iterator.next();
			if (variant.getSlot(slotId) != null) continue;
			matcher = pattern.matcher(slotId);
			if (matcher.find()) slotId = matcher.group();
			else continue;	// this should never happen

			if (!slotId.startsWith(lyr_internals.affix.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			// if (!shuntSpec.hasTag(lyr_internals.tag.experimental)) { iterator.remove(); continue; }
			if (shuntSpec.getSize() != variant.getSlot(slotId).getSlotSize()) continue;

			String shuntId = shuntSpec.getWeaponId();
			if (adapterMap.containsKey(shuntId)) commitVariantChanges = ehm_adaptSlot(hullSpec, shuntId, slotId);
			else if (converterMap.containsKey(shuntId)) commitVariantChanges = ehm_convertSlot(hullSpec, shuntId, slotId);
		}

		boolean hasAdapterActivator = variant.hasHullMod(hullmods.stepdownadapter);
		boolean hasMutableActivator = variant.hasHullMod(hullmods.mutableshunt);
		boolean hasConverterActivator = variant.hasHullMod(hullmods.diverterandconverter);
		if (!hasAdapterActivator && !hasMutableActivator && !hasConverterActivator) return;

		List<WeaponSlotAPI> shunts = hullSpec.getAllWeaponSlotsCopy();

		float[] totalFluxCapacityBonus = {1.0f, 0.0f};	// [0] mult, [1] flat
		float[] totalFluxDissipationBonus = {1.0f, 0.0f};	// [0] mult, [1] flat
		int fighterBayFlat = 0;
		int slotPointsFromMods = hasConverterActivator ? ehm_slotPointsFromHullMods(variant) : 0;
		int slotPoints = slotPointsFromMods;	// as hullMod methods are called several times, slotPoints accumulate correctly on subsequent call(s)
		
		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }

			// if (!slotId.startsWith(lyr_internals.affix.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(lyr_internals.tag.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case adapters.largeDual: case adapters.largeQuad: case adapters.largeTriple: case adapters.mediumDual:
					if (!hasAdapterActivator || !slotId.startsWith(lyr_internals.affix.normalSlot)) { iterator.remove(); break; }
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case converters.mediumToLarge: case converters.smallToLarge: case converters.smallToMedium:
					if (!hasConverterActivator || !slotId.startsWith(lyr_internals.affix.normalSlot)) { iterator.remove(); break; }
					if (slot.isDecorative()) slotPoints -= converterMap.get(shuntId).getChildCost();
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case diverters.large: case diverters.medium: case diverters.small:
					if (!hasConverterActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					if (slot.isDecorative()) slotPoints += diverterMap.get(shuntId);
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case capacitors.large: case capacitors.medium: case capacitors.small:
					if (!hasMutableActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					totalFluxCapacityBonus[0] += capacitorMap.get(shuntId)[0];
					totalFluxCapacityBonus[1] += capacitorMap.get(shuntId)[1];
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case dissipators.large: case dissipators.medium: case dissipators.small:
					if (!hasMutableActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					totalFluxDissipationBonus[0] += dissipatorMap.get(shuntId)[0];
					totalFluxDissipationBonus[1] += dissipatorMap.get(shuntId)[1];
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case launchTubes.large:
					if (!hasMutableActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					fighterBayFlat += launchTubeMap.get(shuntId);
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				default: break;
			}
		}

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			if (slot.isDecorative()) continue;
			
			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case adapters.largeDual: case adapters.largeQuad: case adapters.largeTriple: case adapters.mediumDual:
					commitVariantChanges = ehm_adaptSlot(hullSpec, shuntId, slotId);
					break;
				case converters.mediumToLarge: case converters.smallToLarge: case converters.smallToMedium:
					int cost = converterMap.get(shuntId).getChildCost();
					if (slotPoints - cost < 0) break;
					slotPoints -= cost;
					commitVariantChanges = ehm_convertSlot(hullSpec, shuntId, slotId);
					break;
				case diverters.large: case diverters.medium: case diverters.small:
					slotPoints += diverterMap.get(shuntId);
				case capacitors.large: case capacitors.medium: case capacitors.small:
				case dissipators.large: case dissipators.medium: case dissipators.small:
				case launchTubes.large:
					commitVariantChanges = ehm_deactivateSlot(hullSpec, shuntId, slotId);
					break;
				default: break;
			}
		}

		// if (hasMutableActivator) {
			stats.getFluxCapacity().modifyMult(hullmods.mutableshunt, totalFluxCapacityBonus[0]);
			stats.getFluxCapacity().modifyFlat(hullmods.mutableshunt, totalFluxCapacityBonus[1]);
			stats.getFluxDissipation().modifyMult(hullmods.mutableshunt, totalFluxDissipationBonus[0]);
			stats.getFluxDissipation().modifyFlat(hullmods.mutableshunt, totalFluxDissipationBonus[1]);
			stats.getNumFighterBays().modifyFlat(hullmods.mutableshunt, fighterBayFlat);
		// }
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(hullmods.diverterandconverter, Math.max(0, lyr_ehm.settings.getBaseSlotPointPenalty()*Math.min(slotPointsFromMods, slotPointsFromMods - slotPoints)));

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (commitVariantChanges && !_ehm_helpers.ehm_isGettingRestored(variant)) { commitVariantChanges = false; commitVariantChanges(); }
	}

	public static final void ehm_preProcessShunts(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean commitVariantChanges = false;

		// primarily to deal with stuff on load
		for (String slotId : variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) != null) continue;
			matcher = pattern.matcher(slotId);
			if (matcher.find()) slotId = matcher.group();
			else continue;	// this should never happen

			if (!slotId.startsWith(lyr_internals.affix.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != variant.getSlot(slotId).getSlotSize()) continue;

			String shuntId = shuntSpec.getWeaponId();
			if (adapterMap.containsKey(shuntId)) commitVariantChanges = ehm_adaptSlot(hullSpec, shuntId, slotId);
			else if (converterMap.containsKey(shuntId)) commitVariantChanges = ehm_convertSlot(hullSpec, shuntId, slotId);
		}

		variant.setHullSpecAPI(hullSpec.retrieve());
		if (commitVariantChanges && !_ehm_helpers.ehm_isGettingRestored(variant)) { commitVariantChanges = false; commitVariantChanges(); }
	}

	protected static final boolean ehm_adaptSlot(lyr_hullSpec hullSpec, String shuntId, String slotId) {
		childrenParameters childrenParameters = adapterMap.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = lyr_internals.affix.adaptedSlot + slotId + childId; // also used as nodeId because nodeId isn't visible
			Vector2f childSlotLocation = lyr_vectorUtility.generateChildLocation(parentSlot.getLocation(), parentSlot.getAngle(), childrenParameters.getChildOffset(childId));
			WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

		 	hullSpec.addWeaponSlot(childSlot);
		}

		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.settings.getHideAdapters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		return true;
	}

	protected static final boolean ehm_convertSlot(lyr_hullSpec hullSpec, String shuntId, String slotId) {
		// childParameters childParameters = converters.get(shuntId);
		// int childCost = childParameters.getChildCost();
		// if (slotPoints != null && slotPoints - childCost < 0) return slotPoints - childCost;

		childParameters childParameters = converterMap.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = lyr_internals.affix.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, parentSlot.getLocation());
		childSlot.setSlotSize(childParameters.getChildSize());

		hullSpec.addWeaponSlot(childSlot);

		// if (slotPoints != null) slotPoints -= converters.get(shuntId).getChildCost();	// needs to be subtracted from here on initial install to avoid infinite installs
		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.settings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		return true;
	}

	protected static final boolean ehm_deactivateSlot(lyr_hullSpec hullSpec, String shuntId, String slotId) {
		if (shuntId != null) hullSpec.addBuiltInWeapon(slotId, shuntId);
		hullSpec.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
		return true;
	}

	protected static final int ehm_slotPointsFromHullMods(ShipVariantAPI variant) {
		int slotPoints = 0;

		if (variant.getSMods().contains(lyr_internals.id.hullmods.overengineered))
			slotPoints += experimentalHullModifications.hullmods.ehm_mr.ehm_mr_overengineered.slotPointBonus.get(variant.getHullSize());
		if (variant.hasHullMod(lyr_internals.id.hullmods.auxilarygenerators))
			slotPoints += experimentalHullModifications.hullmods.ehm_mr.ehm_mr_auxilarygenerators.slotPointBonus.get(variant.getHullSize());

		return slotPoints;
	}

	/**
	 * Calculates slot point relevant stats, only to be used in the tooltips.
	 * @param variant of the ship
	 * @param initialBonus if the ship has any initial bonus slot points
	 * @return int array: 0=total, 1=fromHullMods, 2=fromDiverters, 3=forConverters, 4=deploymentPenalty
	 */
	protected static final int[] ehm_slotPointCalculation(ShipAPI ship) {
		int fromDiverters = 0;
		int forConverters = 0;
		int fromHullMods = ehm_slotPointsFromHullMods(ship.getVariant());

		for (WeaponAPI weapon: ship.getAllWeapons()) {
			if (!weapon.getSlot().isDecorative()) continue;
			if (!diverterConverterSet.contains(weapon.getId())) continue;

			String weaponId = weapon.getId();

			if (diverterMap.containsKey(weaponId)) fromDiverters += diverterMap.get(weaponId);
			else if (converterMap.containsKey(weaponId)) forConverters += converterMap.get(weaponId).getChildCost();
		}

		int slotPointsTotal = fromHullMods+fromDiverters-forConverters;
		int deploymentPenalty = Math.max(0, lyr_ehm.settings.getBaseSlotPointPenalty()*Math.min(fromHullMods, fromHullMods - slotPointsTotal));
		int[] slotPointArray = {slotPointsTotal, fromHullMods, fromDiverters, forConverters, deploymentPenalty};
		return slotPointArray;
	}

	/**
	 * Checks and reports any shunt and shunt counts, only to be used in the tooltips.
	 * @param ship
	 * @param groupTag of the shunts to be counted
	 * @return a map with shuntId:count entries
	 */
	protected static final Map<String, Integer> ehm_shuntCount(ShipAPI ship, String groupTag) {
		Map<String, Integer> shuntMap = new HashMap<String, Integer>();

		for (WeaponAPI weapon : ship.getAllWeapons()) {
			if (!weapon.getSlot().isDecorative()) continue;
			if (!weapon.getSpec().getWeaponGroupTag().equals(groupTag)) continue;

			String weaponId = weapon.getId();

			shuntMap.put(weaponId, shuntMap.containsKey(weaponId) ? shuntMap.get(weaponId) + 1 : 1);
		}

		return shuntMap;
	}

	/** 
	 * Refreshes the hullSpec and returns it.
	 * @param variant whose hullSpec will be restored
	 * @return a restored hullSpec
	 */
	public static final ShipHullSpecAPI ehm_activatorRemoval_lazy(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!_ehm_helpers.ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (this.hullModSpecTags.contains(lyr_internals.tag.reqNoPhase) && ship.getPhaseCloak() != null) tooltip.addPara(text.hasPhase[0], text.padding).setHighlight(text.hasPhase[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!_ehm_helpers.ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		if (this.hullModSpecTags.contains(lyr_internals.tag.reqNoPhase) && ship.getPhaseCloak() != null) return false; 
		
		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		return true;
	}
	//#endregion
}