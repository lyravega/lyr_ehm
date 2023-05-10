package data.hullmods.ehm_ar;

import static data.hullmods.ehm_ar.ehm_ar_diverterandconverter.converters;
import static data.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverters;
import static data.hullmods.ehm_ar.ehm_ar_diverterandconverter.divertersAndConverters;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.fighterBayBonus;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.fluxCapacityBonus;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.fluxDissipationBonus;
import static data.hullmods.ehm_ar.ehm_ar_mutableshunt.mutableStatBonus;
import static data.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapters;
import static lyr.misc.lyr_utilities.generateChildLocation;
import static lyr.tools._lyr_uiTools.commitChanges;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods.ehm._ehm_eventhandler;
import data.hullmods.ehm._ehm_eventmethod;
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
public class _ehm_ar_base extends _ehm_base implements _ehm_eventmethod {
	//#region LISTENER & EVENT REGISTRATION
	protected _ehm_eventhandler hullModEventHandler = null;

	@Override
	public void onInstall(ShipVariantAPI variant) {}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_adapterRemoval_lazy(variant));
	}

	@Override
	public void sModCleanUp(ShipVariantAPI variant) {}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		if (this.hullModEventHandler == null) {
			this.hullModEventHandler = new _ehm_eventhandler(this.hullModSpecId, this);
			hullModEventHandler.registerOnInstall(true, true, false);
			hullModEventHandler.registerOnRemove(true, true, true);
		}
	}
	//#endregion
	// END OF LISTENER & EVENT REGISTRATION

	protected static final boolean extraActiveInfoInHullMods = lyr_externals.extraActiveInfoInHullMods;
	protected static final boolean extraInactiveInfoInHullMods = lyr_externals.extraInactiveInfoInHullMods;

	//#region ADAPTERS	
	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them.
	 * Adapters are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 */
	public static final void ehm_adapterActivator(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		
		for (String shuntSlotId: variant.getFittedWeaponSlots()) {
			if (!shuntSlotId.startsWith(lyr_internals.affix.normalSlot)) continue;	// only works on normal slots
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(shuntSlotId);
			String shuntId = shuntSpec.getWeaponId();
			if (!adapters.containsKey(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(shuntSlotId).getSlotSize())) continue;	// requires size match

			childrenParameters childrenParameters = adapters.get(shuntId);
			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(shuntSlotId); 
			Vector2f parentSlotLocation = parentSlot.retrieve().getLocation();
			float parentSlotAngle = parentSlot.retrieve().getAngle();

			for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
				lyr_weaponSlot childSlot = parentSlot.clone();
				String childSlotId = lyr_internals.affix.adaptedSlot + shuntSlotId + childId; // also used as nodeId because nodeId isn't visible
				Vector2f childSlotLocation = generateChildLocation(parentSlotLocation, parentSlotAngle, childrenParameters.getChildOffset(childId));
				WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

				childSlot.setId(childSlotId);
				childSlot.setNode(childSlotId, childSlotLocation);
				childSlot.setSlotSize(childSlotSize);

			 	hullSpec.addWeaponSlot(childSlot.retrieve());
			}

			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(shuntSlotId, shuntId);
			refreshRefit = true;
		}

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}
	//#endregion
	// END OF ADAPTERS

	//#region MUTABLES
	/** 
	 * Provides bonuses based on the number of shunts installed in slots.
	 * Shunts are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 * @param hullModSpecId to properly accumulate the bonuses under the same id
	 */
	public static final void ehm_mutableShuntActivator(MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		float fluxCapacityMult = 1.0f;
		float fluxDissipationMult = 1.0f;
		int fighterBayFlat = 0;

		// slot conversion
		for (String shuntSlotId: variant.getFittedWeaponSlots()) {
			if (shuntSlotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// only works on normal and adapted slots
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(shuntSlotId);
			String shuntId = shuntSpec.getWeaponId();
			if (!mutableStatBonus.containsKey(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(shuntSlotId).getSlotSize())) continue;	// requires size match

			// slotPoints += slotValue.get(weaponSize);	// needs to be calculated afterwards like mutableStat bonus as this block will execute only on install
			hullSpec.getWeaponSlot(shuntSlotId).setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(shuntSlotId, shuntId);
			refreshRefit = true;
		}

		// bonus calculation
		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check them
			String slotId = slot.getId();
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId); if (shuntSpec == null) continue;	// skip empty slots
			String shuntId = shuntSpec.getWeaponId();
			if (!mutableStatBonus.containsKey(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size

			if (fluxCapacityBonus.containsKey(shuntId)) fluxCapacityMult += fluxCapacityBonus.get(shuntId);
			else if (fluxDissipationBonus.containsKey(shuntId)) fluxDissipationMult += fluxDissipationBonus.get(shuntId);
			else if (fighterBayBonus.containsKey(shuntId)) fighterBayFlat += fighterBayBonus.get(shuntId);
		}

		stats.getFluxCapacity().modifyMult(hullModSpecId, fluxCapacityMult);
		stats.getFluxDissipation().modifyMult(hullModSpecId, fluxDissipationMult);
		stats.getNumFighterBays().modifyFlat(hullModSpecId, fighterBayFlat);

		variant.setHullSpecAPI(hullSpec.retrieve());
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}
	//#endregion
	// END OF MUTABLES

	//#region CONVERTERS & DIVERTERS
	/** 
	 * Spawns additional weapon slots, if the slots have converters or diverters
	 * on them. They are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 */
	public static final void ehm_diverterAndConverterActivator(MutableShipStatsAPI stats, int slotPoints) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		SortedSet<String> sortedFittedWeaponSlots = new TreeSet<String>(variant.getFittedWeaponSlots());

		// slot conversion for diverters
		for (String shuntSlotId: sortedFittedWeaponSlots) {
			if (shuntSlotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// only works on normal and adapted slots
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(shuntSlotId);
			String shuntId = shuntSpec.getWeaponId();
			if (!diverters.containsKey(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(shuntSlotId).getSlotSize())) continue;	// requires size match

			// slotPoints += slotValue.get(weaponSize);	// needs to be calculated afterwards like mutableStat bonus as this block will execute only on install
			hullSpec.getWeaponSlot(shuntSlotId).setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(shuntSlotId, shuntId);
			refreshRefit = true;
		}

		// slotPoints calculation
		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check decorative
			String slotId = slot.getId();
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId); if (shuntSpec == null) continue;	// skip empty slots
			String shuntId = shuntSpec.getWeaponId();
			if (!divertersAndConverters.contains(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size

			if (diverters.containsKey(shuntId)) slotPoints += diverters.get(shuntId);
			else if (converters.containsKey(shuntId)) slotPoints -= converters.get(shuntId).getChildCost();
		}

		final Pattern pattern = Pattern.compile("WS [0-9]{3}");
		Matcher matcher;

		// slot conversion for converters
		for (String shuntSlotId: sortedFittedWeaponSlots) {
			if (variant.getSlot(shuntSlotId) == null) {
				matcher = pattern.matcher(shuntSlotId);
				shuntSlotId = matcher.find() ? matcher.group() : null;
				if (shuntSlotId == null) continue;
			}
			if (!shuntSlotId.startsWith(lyr_internals.affix.normalSlot)) continue;	// only works on normal slots
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(shuntSlotId);
			String shuntId = shuntSpec.getWeaponId();
			if (!converters.containsKey(shuntId)) continue;	// only care about these shunts
			if (!shuntSpec.getSize().equals(variant.getSlot(shuntSlotId).getSlotSize())) continue;	// requires size match

			childParameters childParameters = converters.get(shuntId);
			int childCost = childParameters.getChildCost();
			if (slotPoints - childCost < 0) continue;

			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(shuntSlotId);
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = lyr_internals.affix.convertedSlot + shuntSlotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, parentSlot.retrieve().getLocation());
			childSlot.setSlotSize(childParameters.getChildSize());

			hullSpec.addWeaponSlot(childSlot.retrieve());

			slotPoints -= converters.get(shuntId).getChildCost();	// needs to be subtracted from here on initial install to avoid infinite installs
			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(shuntSlotId, shuntId);
			refreshRefit = true; 
		}

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}
	//#endregion
	// END OF CONVERTERS & DIVERTERS

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
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check decorative
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
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;
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