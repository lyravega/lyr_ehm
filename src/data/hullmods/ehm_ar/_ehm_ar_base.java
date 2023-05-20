package data.hullmods.ehm_ar;

import static data.hullmods.ehm_ar.ehm_ar_diverterandconverter.converters;
import static data.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverters;
import static data.hullmods.ehm_ar.ehm_ar_diverterandconverter.divertersAndConverters;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.fighterBayBonus;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.fluxCapacityBonus;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.fluxDissipationBonus;
import static data.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapters;
import static lyr.misc.lyr_utilities.generateChildLocation;
import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.playSound;

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
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods.ehm.events.normalEvents;
import data.hullmods.ehm_ar.ehm_ar_diverterandconverter.childParameters;
import data.hullmods.ehm_ar.ehm_ar_stepdownadapter.childrenParameters;
import lyr.misc.lyr_externals;
import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;
import lyr.proxies.lyr_hullSpec;
import lyr.proxies.lyr_weaponSlot;


/**
 * This class is used by slot adapter hullmods. Slot adapters are designed 
 * to search the ship for specific weapons, and perform operations on the 
 * hullSpec to yield interesting results, such as creating a new weapon slot. 
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_ar_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_adapterRemoval_lazy(variant));
		commitChanges(); playSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	protected static final boolean extraActiveInfoInHullMods = lyr_externals.extraActiveInfoInHullMods;
	protected static final boolean extraInactiveInfoInHullMods = lyr_externals.extraInactiveInfoInHullMods;

	private static final Pattern pattern = Pattern.compile("WS [0-9]{3}");
	private static Matcher matcher;

	public static final void ehm_test(MutableShipStatsAPI stats, int slotPoints) {
		ShipVariantAPI variant = stats.getVariant(); 
		boolean hasAdapterActivator = variant.hasHullMod(lyr_internals.id.hullmods.stepdownadapter);
		boolean hasMutableActivator = variant.hasHullMod(lyr_internals.id.hullmods.mutableshunt);
		boolean hasConverterActivator = variant.hasHullMod(lyr_internals.id.hullmods.diverterandconverter);
		if (!hasAdapterActivator && !hasMutableActivator && !hasConverterActivator) return;

		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		float[] totalFluxCapacityBonus = {1.0f, 0.0f};	// 0 mult, 1 flat
		float[] totalFluxDissipationBonus = {1.0f, 0.0f};	// 0 mult, 1 flat
		int fighterBayFlat = 0;

		// primarily to deal with stuff on load
		for (Iterator<String> iterator = variant.getFittedWeaponSlots().iterator(); iterator.hasNext();) {
			String slotId = iterator.next();
			if (variant.getSlot(slotId) != null) continue;
			matcher = pattern.matcher(slotId); matcher.find(); slotId = matcher.group();

			if (!slotId.startsWith(lyr_internals.affix.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			// if (!shuntSpec.hasTag(lyr_internals.tag.experimental)) { iterator.remove(); continue; }
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue;

			String shuntId = shuntSpec.getWeaponId();
			if (adapters.containsKey(shuntId)) refreshRefit = ehm_adaptSlot(hullSpec, shuntId, slotId);
			else if (converters.containsKey(shuntId)) refreshRefit = ehm_convertSlot(hullSpec, shuntId, slotId);
		}

		List<WeaponSlotAPI> shunts = hullSpec.retrieve().getAllWeaponSlotsCopy();
		
		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;
			
			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }

			// if (!slotId.startsWith(lyr_internals.affix.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(lyr_internals.tag.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case "ehm_adapter_largeDual": case "ehm_adapter_largeQuad":	case "ehm_adapter_largeTriple":	case "ehm_adapter_mediumDual":
					if (!hasAdapterActivator || !slotId.startsWith(lyr_internals.affix.normalSlot)) { iterator.remove(); break; }
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case "ehm_converter_mediumToLarge":	case "ehm_converter_smallToLarge": case "ehm_converter_smallToMedium":
					if (!hasConverterActivator || !slotId.startsWith(lyr_internals.affix.normalSlot)) { iterator.remove(); break; }
					if (slot.isDecorative()) slotPoints -= converters.get(shuntId).getChildCost();
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case "ehm_diverter_large": case "ehm_diverter_medium": case "ehm_diverter_small":
					if (!hasConverterActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					if (slot.isDecorative()) slotPoints += diverters.get(shuntId);
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case "ehm_capacitor_large": case "ehm_capacitor_medium": case "ehm_capacitor_small":
					if (!hasMutableActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					totalFluxCapacityBonus[0] += fluxCapacityBonus.get(shuntId)[0];
					totalFluxCapacityBonus[1] += fluxCapacityBonus.get(shuntId)[1];
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case "ehm_dissipator_large": case "ehm_dissipator_medium": case "ehm_dissipator_small":
					if (!hasMutableActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					totalFluxDissipationBonus[0] += fluxDissipationBonus.get(shuntId)[0];
					totalFluxDissipationBonus[1] += fluxDissipationBonus.get(shuntId)[1];
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				case "ehm_tube_large":
					if (!hasMutableActivator || slotId.startsWith(lyr_internals.affix.convertedSlot)) { iterator.remove(); break; }
					fighterBayFlat += fighterBayBonus.get(shuntId);
					// hullSpec.addBuiltInWeapon(slotId, shuntId);
					break;
				default: break;
			}
		}

		if (hasMutableActivator) {
			stats.getFluxCapacity().modifyMult(lyr_internals.id.hullmods.mutableshunt, totalFluxCapacityBonus[0]);
			stats.getFluxCapacity().modifyFlat(lyr_internals.id.hullmods.mutableshunt, totalFluxCapacityBonus[1]);
			stats.getFluxDissipation().modifyMult(lyr_internals.id.hullmods.mutableshunt, totalFluxDissipationBonus[0]);
			stats.getFluxDissipation().modifyFlat(lyr_internals.id.hullmods.mutableshunt, totalFluxDissipationBonus[1]);
			stats.getNumFighterBays().modifyFlat(lyr_internals.id.hullmods.mutableshunt, fighterBayFlat);
		}

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			if (slot.isDecorative()) continue;
			
			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case "ehm_adapter_largeDual": case "ehm_adapter_largeQuad":	case "ehm_adapter_largeTriple":	case "ehm_adapter_mediumDual":
					refreshRefit = ehm_adaptSlot(hullSpec, shuntId, slotId);
					break;
				case "ehm_converter_mediumToLarge":	case "ehm_converter_smallToLarge": case "ehm_converter_smallToMedium":
					int cost = converters.get(shuntId).getChildCost();
					if (slotPoints - cost < 0) break;
					slotPoints -= cost;
					refreshRefit = ehm_convertSlot(hullSpec, shuntId, slotId);
					break;
				case "ehm_diverter_large": case "ehm_diverter_medium": case "ehm_diverter_small":
				case "ehm_capacitor_large": case "ehm_capacitor_medium": case "ehm_capacitor_small":
				case "ehm_dissipator_large": case "ehm_dissipator_medium": case "ehm_dissipator_small":
				case "ehm_tube_large":
					refreshRefit = ehm_deactivateSlot(hullSpec, shuntId, slotId);
					break;
				default: break;
			}
		}

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (refreshRefit) { refreshRefit = false; commitChanges(); }
	}

	private static final boolean ehm_adaptSlot(lyr_hullSpec hullSpec, String shuntId, String slotId) {
		childrenParameters childrenParameters = adapters.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = lyr_internals.affix.adaptedSlot + slotId + childId; // also used as nodeId because nodeId isn't visible
			Vector2f childSlotLocation = generateChildLocation(parentSlot.retrieve().getLocation(), parentSlot.retrieve().getAngle(), childrenParameters.getChildOffset(childId));
			WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

		 	hullSpec.addWeaponSlot(childSlot.retrieve());
		}

		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		return true;
	}

	private static final boolean ehm_convertSlot(lyr_hullSpec hullSpec, String shuntId, String slotId) {
		// childParameters childParameters = converters.get(shuntId);
		// int childCost = childParameters.getChildCost();
		// if (slotPoints != null && slotPoints - childCost < 0) return slotPoints - childCost;

		childParameters childParameters = converters.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = lyr_internals.affix.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, parentSlot.retrieve().getLocation());
		childSlot.setSlotSize(childParameters.getChildSize());

		hullSpec.addWeaponSlot(childSlot.retrieve());

		// if (slotPoints != null) slotPoints -= converters.get(shuntId).getChildCost();	// needs to be subtracted from here on initial install to avoid infinite installs
		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		return true;
	}

	private static final boolean ehm_deactivateSlot(lyr_hullSpec hullSpec, String shuntId, String slotId) {
		hullSpec.addBuiltInWeapon(slotId, shuntId);
		hullSpec.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
		return true;
	}

	/**
	 * Calculates slot point relevant stats, only to be used in the tooltips.
	 * @param variant of the ship
	 * @param initialBonus if the ship has any initial bonus slot points
	 * @return int array, 0=total, 1=misc, 2=diverters, 3=converters
	 */
	protected static final int[] ehm_slotPointCalculation(ShipVariantAPI variant, int initialBonus) {
		int diverterBonus = 0;
		int converterMalus = 0;

		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.isDecorative()) continue;	// since activated shunts become decorative, only need to check decorative
			String slotId = slot.getId();
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId); if (shuntSpec == null) continue;	// skip empty slots
			String shuntId = shuntSpec.getWeaponId();
			if (!divertersAndConverters.contains(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size

			if (diverters.containsKey(shuntId)) diverterBonus += diverters.get(shuntId);
			else if (converters.containsKey(shuntId)) converterMalus -= converters.get(shuntId).getChildCost();
		}

		int[] pointArray = {initialBonus+converterMalus+diverterBonus, initialBonus, diverterBonus, converterMalus};
		return pointArray;
	}

	/**
	 * Checks and reports any shunt and shunt counts, only to be used in the tooltips.
	 * @param variant of the ship
	 * @param tag of the shunts to be counted
	 * @return a map with shuntId:count entries
	 */
	protected static final Map<String, Integer> ehm_shuntCount(ShipVariantAPI variant, String tag) {
		Map<String, Integer> shuntMap = new HashMap<String, Integer>();

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.isDecorative()) continue;
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slot.getId()); if (weaponSpec == null) continue;
			if (!weaponSpec.hasTag(tag)) continue;
			String weaponId = weaponSpec.getWeaponId();

			shuntMap.put(weaponId, shuntMap.containsKey(weaponId) ? shuntMap.get(weaponId) + 1 : 1);
		}

		return shuntMap;
	}

	/** 
	 * Refreshes the hullSpec and returns it.
	 * @param variant whose hullSpec will be restored
	 * @return a restored hullSpec
	 */
	public static final ShipHullSpecAPI ehm_adapterRemoval_lazy(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(lyr_tooltip.header.notApplicable, lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(lyr_tooltip.text.lacksBase, lyr_tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) return false; 
		// if (ehm_hasRetrofitTag(ship, tag.adapterRetrofit, hullModSpecId)) return false; 
		
		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		return true;
	}
	//#endregion
}