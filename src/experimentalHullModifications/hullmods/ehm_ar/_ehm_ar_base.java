package experimentalHullModifications.hullmods.ehm_ar;

import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverterConverterSet;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverterMap;
// import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_launchtube.launchTubeMap;
// import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt.capacitorMap;
// import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt.dissipatorMap;
import static experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterMap;
import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.childParameters;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.childrenParameters;
import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_auxilarygenerators;
import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_overengineered;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.regexText;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.normalEvents;
import lyravega.listeners.events.weaponEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;
import lyravega.utilities.lyr_vectorUtilities;


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
		lyr_miscUtilities.cleanWeaponGroupsUp(variant, ehm_internals.id.shunts.set);
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

	private static final Pattern pattern = Pattern.compile("WS[ 0-9]{4}");
	private static Matcher matcher;

	public static final void ehm_preProcessShunts(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		// primarily to deal with stuff on load
		for (String slotId : variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) != null) continue;
			matcher = pattern.matcher(slotId);
			if (matcher.find()) slotId = matcher.group();
			else continue;	// this should never happen

			if (!slotId.startsWith(ehm_internals.affix.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != variant.getSlot(slotId).getSlotSize()) continue;

			String shuntId = shuntSpec.getWeaponId();
			if (adapterMap.containsKey(shuntId)) ehm_adaptSlot(lyr_hullSpec, shuntId, slotId);
			else if (converterMap.containsKey(shuntId)) ehm_convertSlot(lyr_hullSpec, shuntId, slotId);
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	protected static final void ehm_adaptSlot(lyr_hullSpec lyr_hullSpec, String shuntId, String slotId) {
		childrenParameters childrenParameters = adapterMap.get(shuntId);
		lyr_weaponSlot parentSlot = lyr_hullSpec.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = ehm_internals.affix.adaptedSlot + slotId + childId; // also used as nodeId because nodeId isn't visible
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
	}

	protected static final void ehm_convertSlot(lyr_hullSpec lyr_hullSpec, String shuntId, String slotId) {
		// childParameters childParameters = converters.get(shuntId);
		// int childCost = childParameters.getChildCost();
		// if (slotPoints != null && slotPoints - childCost < 0) return slotPoints - childCost;

		childParameters childParameters = converterMap.get(shuntId);
		lyr_weaponSlot parentSlot = lyr_hullSpec.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = ehm_internals.affix.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, parentSlot.getLocation());
		childSlot.setSlotSize(childParameters.getChildSize());

		lyr_hullSpec.addWeaponSlot(childSlot);

		// if (slotPoints != null) slotPoints -= converters.get(shuntId).getChildCost();	// needs to be subtracted from here on initial install to avoid infinite installs
		lyr_hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (ehm_settings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
	}

	protected static final void ehm_deactivateSlot(lyr_hullSpec lyr_hullSpec, String shuntId, String slotId) {
		if (shuntId != null) lyr_hullSpec.addBuiltInWeapon(slotId, shuntId);
		lyr_hullSpec.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
	}

	protected static final int ehm_slotPointsFromHullMods(ShipVariantAPI variant) {
		int slotPoints = 0;

		if (variant.getSMods().contains(ehm_internals.id.hullmods.overengineered))
			slotPoints += ehm_mr_overengineered.slotPointBonus.get(variant.getHullSize());
		if (variant.hasHullMod(ehm_internals.id.hullmods.auxilarygenerators))
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
			if (!diverterConverterSet.contains(weapon.getId())) continue;

			String weaponId = weapon.getId();

			if (diverterMap.containsKey(weaponId)) fromDiverters += diverterMap.get(weaponId);
			else if (converterMap.containsKey(weaponId)) forConverters += converterMap.get(weaponId).getChildCost();
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

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.lacksBase, text.padding);
			if (this.hullModSpecTags.contains(ehm_internals.tag.reqNoPhase) && lyr_miscUtilities.hasPhaseCloak(ship)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasPhase, text.padding);
			if (ship.getVariant().hasHullMod(ehm_internals.id.hullmods.logisticsoverhaul)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasLogisticsOverhaul, text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) return false;
		if (this.hullModSpecTags.contains(ehm_internals.tag.reqNoPhase) && lyr_miscUtilities.hasPhaseCloak(ship)) return false;
		if (ship.getVariant().hasHullMod(ehm_internals.id.hullmods.logisticsoverhaul)) return false;

		return true;
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		return true;
	}
	//#endregion
}