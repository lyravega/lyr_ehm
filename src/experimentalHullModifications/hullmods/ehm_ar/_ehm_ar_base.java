package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_auxilarygenerators;
import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_overengineered;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.shunts.*;
import experimentalHullModifications.misc.ehm_internals.shunts.adapters.adapterParameters;
import experimentalHullModifications.misc.ehm_internals.shunts.converters.converterParameters;
import experimentalHullModifications.misc.ehm_lostAndFound;
import experimentalHullModifications.misc.ehm_settings;
import lyravega.listeners.events.normalEvents;
import lyravega.listeners.events.weaponEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_vectorUtilities;
import lyravega.utilities.logger.lyr_logger;


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
public abstract class _ehm_ar_base extends _ehm_base implements normalEvents, weaponEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		lyr_miscUtilities.cleanWeaponGroupsUp(variant, ehm_internals.shunts.idSet);
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_activatorRemoval_lazy(variant));
		commitVariantChanges(); playDrillSound();
	}

	@Override public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {}

	@Override public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {}
	//#endregion
	// END OF CUSTOM EVENTS

	private static final Map<String, adapterParameters> adapterMap = adapters.dataMap;
	private static final Map<String, converterParameters> converterMap = converters.dataMap;
	private static final Map<String, Integer> diverterMap = diverters.dataMap;

	private static final Pattern pattern = Pattern.compile("WS[ 0-9]{4}");
	private static Matcher matcher;

	public static final void ehm_preProcessShunts(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		// primarily to deal with stuff on load
		if (!ehm_settings.getClearUnknownSlots()) for (String slotId : variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) != null) continue;
			matcher = pattern.matcher(slotId);
			if (matcher.find()) slotId = matcher.group();
			else continue;	// this should never happen

			if (!slotId.startsWith(ehm_internals.affixes.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != variant.getSlot(slotId).getSlotSize()) continue;

			String shuntId = shuntSpec.getWeaponId();
			if (adapters.idSet.contains(shuntId)) ehm_adaptSlot(lyr_hullSpec, shuntId, slotId);
			else if (converters.idSet.contains(shuntId)) ehm_convertSlot(lyr_hullSpec, shuntId, slotId);
		} else for (String slotId : variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) != null) continue;

			String weaponId = variant.getWeaponId(slotId);
			lyr_logger.warn("Slot with the ID '"+slotId+"' not found, stashing the weapon '"+weaponId+"'");
			ehm_lostAndFound.addLostItem(weaponId);	// to recover the weapons 'onGameLoad()'

			variant.clearSlot(slotId);	// this is an emergency option to allow loading because I fucked up
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	public static final void ehm_preProcessDynamicStats(MutableShipStatsAPI stats) {
		final DynamicStatsAPI dynamicStats = stats.getDynamic();
		final ShipVariantAPI variant = stats.getVariant();

		if (variant.getSMods().contains(ehm_internals.hullmods.misc.overengineered)) {
			String source = ehm_mr_overengineered.class.getSimpleName();
			int mod = ehm_mr_overengineered.slotPointBonus.get(variant.getHullSize());

			dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(source, mod);
			dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).modifyFlat(source, mod);
			// TODO: add a dynamic stat for ordnancePoints from here
		}

		if (variant.hasHullMod(ehm_internals.hullmods.misc.auxilarygenerators)) {
			String source = ehm_mr_auxilarygenerators.class.getSimpleName();
			int mod = ehm_mr_auxilarygenerators.slotPointBonus.get(variant.getHullSize());

			dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(source, mod);
			dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).modifyFlat(source, mod);
		}

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);

			if (shuntSpec == null) continue;
			if (shuntSpec.getSize() != slot.getSlotSize()) continue;
			if (!shuntSpec.hasTag(ehm_internals.hullmods.tags.experimental)) continue;

			String shuntId = shuntSpec.getWeaponId();
			String shuntGroupTag = shuntSpec.getWeaponGroupTag();
			switch (shuntGroupTag) {
				case adapters.groupTag: {
					if (!variant.hasHullMod(adapters.activatorId)) continue;
					if (!adapters.isValidSlot(slot, shuntSpec)) continue;

					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);

					// switch (shuntId) {
					// 	case adapters.ids.largeDual: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 	}; continue;
					// 	case adapters.ids.largeQuad: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 	}; continue;
					// 	case adapters.ids.largeTriple: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 	}; continue;
					// 	case adapters.ids.mediumDual: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 	}; continue;
					// }
				}; continue;
				case converters.groupTag: {
					if (!variant.hasHullMod(converters.activatorId)) continue;
					if (!converters.isValidSlot(slot, shuntSpec)) continue;

					final int mod = converters.dataMap.get(shuntId).getChildCost();
					if (!slot.isDecorative()) {
						dynamicStats.getMod(shuntId+"_inactive").modifyFlat(slotId, 1);
						dynamicStats.getMod(shuntGroupTag+"_inactive").modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, mod);
					} else {
						dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
						dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).modifyFlat(slotId, mod);
					}
					// TODO: converters should have an active and inactive stat group separately

					// switch (shuntId) {
					// 	case converters.ids.mediumToLarge: {
					// 		if (slot.isDecorative()) dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		if (slot.isDecorative()) dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 2);
					// 		dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, 2);
					// 	}; continue;
					// 	case converters.ids.smallToLarge: {
					// 		if (slot.isDecorative()) dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		if (slot.isDecorative()) dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 3);
					// 		dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, 3);
					// 	}; continue;
					// 	case converters.ids.smallToMedium: {
					// 		if (slot.isDecorative()) dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		if (slot.isDecorative()) dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, 1);
					// 	}; continue;
					// }
				}; continue;
				case diverters.groupTag: {
					if (!variant.hasHullMod(diverters.activatorId)) continue;
					if (!diverters.isValidSlot(slot, shuntSpec)) continue;

					final int mod = diverters.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);
					dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, mod);
					dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);

					// switch (shuntId) {
					// 	case diverters.ids.large: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 4);
					// 		dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, 4);
					// 	}; continue;
					// 	case diverters.ids.medium: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 2);
					// 		dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, 2);
					// 	}; continue;
					// 	case diverters.ids.small: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, 1);
					// 	}; continue;
					// }
				}; continue;
				case capacitors.groupTag: {
					if (!variant.hasHullMod(capacitors.activatorId)) continue;
					if (!capacitors.isValidSlot(slot, shuntSpec)) continue;

					final int mod = capacitors.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);

					// switch (shuntId) {
					// 	case capacitors.ids.large: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 4);
					// 	}; continue;
					// 	case capacitors.ids.medium: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 2);
					// 	}; continue;
					// 	case capacitors.ids.small: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
					// 	}; continue;
					// }
				}; continue;
				case dissipators.groupTag: {
					if (!variant.hasHullMod(dissipators.activatorId)) continue;
					if (!dissipators.isValidSlot(slot, shuntSpec)) continue;

					final int mod = dissipators.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);

					// switch (shuntId) {
					// 	case dissipators.ids.large: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 4);
					// 	}; continue;
					// 	case dissipators.ids.medium: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 2);
					// 	}; continue;
					// 	case dissipators.ids.small: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
					// 	}; continue;
					// }
				}; continue;
				case hangars.groupTag: {
					if (!variant.hasHullMod(hangars.activatorId)) continue;
					if (!hangars.isValidSlot(slot, shuntSpec)) continue;

					final int mod = hangars.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);

					// switch (shuntId) {
					// 	case hangars.ids.large: {
					// 		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					// 		dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
					// 	}; continue;
					// }
				}; continue;
				default: continue;
			}
		}
	}

	protected static final void ehm_adaptSlot(lyr_hullSpec lyr_hullSpec, String shuntId, String slotId) {
		adapterParameters childrenParameters = adapterMap.get(shuntId);
		lyr_weaponSlot parentSlot = lyr_hullSpec.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = ehm_internals.affixes.adaptedSlot + slotId + childId; // also used as nodeId because nodeId isn't visible
			Vector2f childSlotLocation = lyr_vectorUtilities.generateChildLocation(parentSlot.getLocation(), parentSlot.getAngle(), childrenParameters.getChildOffset(childId));
			WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

		 	lyr_hullSpec.addWeaponSlot(childSlot);
		}

		lyr_hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (ehm_settings.getHideAdapters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);	// sometimes the activated shunts (decoratives) on these new slots (especially hardpoint ones) are rendered below the adapter, hence the change
	}

	protected static final void ehm_convertSlot(lyr_hullSpec lyr_hullSpec, String shuntId, String slotId) {
		// childParameters childParameters = converters.get(shuntId);
		// int childCost = childParameters.getChildCost();
		// if (slotPoints != null && slotPoints - childCost < 0) return slotPoints - childCost;

		converterParameters childParameters = converterMap.get(shuntId);
		lyr_weaponSlot parentSlot = lyr_hullSpec.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = ehm_internals.affixes.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, parentSlot.getLocation());
		childSlot.setSlotSize(childParameters.getChildSize());

		lyr_hullSpec.addWeaponSlot(childSlot);

		// if (slotPoints != null) slotPoints -= converters.get(shuntId).getChildCost();	// needs to be subtracted from here on initial install to avoid infinite installs
		lyr_hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (ehm_settings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);	// sometimes the activated shunts (decoratives) on these new slots (especially hardpoint ones) are rendered below the adapter, hence the change
	}

	protected static final void ehm_deactivateSlot(lyr_hullSpec lyr_hullSpec, String shuntId, String slotId) {
		if (shuntId != null) lyr_hullSpec.addBuiltInWeapon(slotId, shuntId);
		lyr_hullSpec.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
	}

	protected static final int ehm_slotPointsFromHullMods(ShipVariantAPI variant) {
		int slotPoints = 0;

		if (variant.getSMods().contains(ehm_internals.hullmods.misc.overengineered))
			slotPoints += ehm_mr_overengineered.slotPointBonus.get(variant.getHullSize());
		if (variant.hasHullMod(ehm_internals.hullmods.misc.auxilarygenerators))
			slotPoints += ehm_mr_auxilarygenerators.slotPointBonus.get(variant.getHullSize());

		return slotPoints;
	}

	/**
	 * Calculates slot point relevant stats, only to be used in the tooltips.
	 * @param variant of the ship
	 * @param initialBonus if the ship has any initial bonus slot points
	 * @return int array: 0=total, 1=fromHullMods, 2=fromDiverters, 3=forConverters, 4=deploymentPenalty
	 * @deprecated as dynamic stats are utilized for this information
	 */
	@Deprecated
	protected static final int[] ehm_slotPointCalculation(ShipAPI ship) {
		int fromDiverters = 0;
		int forConverters = 0;
		int fromHullMods = ehm_slotPointsFromHullMods(ship.getVariant());

		for (WeaponAPI weapon: ship.getAllWeapons()) {
			if (!weapon.getSlot().isDecorative()) continue;

			String weaponId = weapon.getId();

			if (diverters.idSet.contains(weaponId)) fromDiverters += diverterMap.get(weaponId);
			else if (converters.idSet.contains(weaponId)) forConverters += converterMap.get(weaponId).getChildCost();
		}

		int slotPointsTotal = fromHullMods+fromDiverters-forConverters;
		int deploymentPenalty = Math.max(0, ehm_settings.getBaseSlotPointPenalty()*Math.min(fromHullMods, fromHullMods - slotPointsTotal));
		int[] slotPointArray = {slotPointsTotal, fromHullMods, fromDiverters, forConverters, deploymentPenalty};
		return slotPointArray;
	}

	/**
	 * Checks and reports any shunt and shunt counts, only to be used in the tooltips.
	 * @param ship
	 * @param groupTag of the shunts to be counted
	 * @return a map with shuntId:count entries
	 */
	@Deprecated
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
}