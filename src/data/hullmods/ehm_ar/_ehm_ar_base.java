package data.hullmods.ehm_ar;

import static lyr.misc.lyr_utilities.generateChildLocation;
import static lyr.tools._lyr_uiTools.commitChanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods._ehm_basetracker.hullModEventListener;
import data.hullmods._ehm_hullmodeventmethods;
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
public class _ehm_ar_base extends _ehm_base implements _ehm_hullmodeventmethods {
	//#region LISTENER & EVENT REGISTRATION
	protected hullModEventListener hullModEventListener;

	@Override	// not used
	public void onInstall(ShipVariantAPI variant) {}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_adapterRemoval(variant));
	}

	@Override
	public void sModCleanUp(ShipVariantAPI variant) {}

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);
		this.hullModEventListener = new hullModEventListener(this.hullModSpecId, this);
		hullModEventListener.registerEvent(lyr_internals.event.onInstall, true, true, null);
		hullModEventListener.registerEvent(lyr_internals.event.onRemove, true, true, lyr_internals.eventMethod.onRemove);
	}
	//#endregion
	// END OF LISTENER & EVENT REGISTRATION

	/**
	 * An inner class to supply the adapters with relevant child data
	 */
	private static class childrenParameters {
		private Set<String> children; // childIds are used as position identifier, and used as a suffix
		private Map<String, Vector2f> childrenOffsets;
		private Map<String, WeaponSize> childrenSizes;

		protected childrenParameters() {
			children = new HashSet<String>();
			childrenOffsets = new HashMap<String, Vector2f>();
			childrenSizes = new HashMap<String, WeaponSize>();
		}

		private void addChild(String childId, WeaponSize childSize, Vector2f childOffset) {
			this.children.add(childId);
			this.childrenOffsets.put(childId, childOffset);
			this.childrenSizes.put(childId, childSize);
		}

		public Set<String> getChildren() {
			return this.children;
		}

		private Vector2f getChildOffset(String childPrefix) {
			return this.childrenOffsets.get(childPrefix);
		}

		private WeaponSize getChildSize(String childPrefix) {
			return this.childrenSizes.get(childPrefix);
		}
	}

	private static final Map<String, childrenParameters> adapters = new HashMap<String, childrenParameters>();
	private static final childrenParameters mediumDual = new childrenParameters();
	private static final childrenParameters largeDual = new childrenParameters();
	private static final childrenParameters largeTriple = new childrenParameters();
	private static final childrenParameters largeQuad = new childrenParameters();
	static {
		mediumDual.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		mediumDual.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		adapters.put(lyr_internals.id.shunts.adapters.mediumDual, mediumDual);

		largeDual.addChild("L", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
		largeDual.addChild("R", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
		adapters.put(lyr_internals.id.shunts.adapters.largeDual, largeDual);

		largeTriple.addChild("L", WeaponSize.SMALL, new Vector2f(-4.0f, 17.0f)); // left
		largeTriple.addChild("R", WeaponSize.SMALL, new Vector2f(-4.0f, -17.0f)); // right
		largeTriple.addChild("C", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
		adapters.put(lyr_internals.id.shunts.adapters.largeTriple, largeTriple);

		largeQuad.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		largeQuad.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 17.0f)); // far left
		largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -17.0f)); // far right
		adapters.put(lyr_internals.id.shunts.adapters.largeQuad, largeQuad);
	}
	
	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them.
	 * Adapters are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 */
	protected static final void ehm_adapterActivator(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;

		for (String slotId: variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) == null) continue;	// short-circuit to avoid a potential null pointer (may happen when vanilla hullSpec is (re)loaded)
			if (slotId.startsWith(lyr_internals.affix.adaptedSlot)) continue;	// short-circuit to prevent adapters working on adapted slots
			if (slotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// short-circuit to prevent adapters working on converted slots

			// WeaponType slotType = variant.getSlot(slotId).getWeaponType();
			// WeaponSize slotSize = variant.getSlot(slotId).getSlotSize();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			// WeaponType weaponType = weaponSpec.getType();
			// WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue;	// to avoid plugging medium universal to large universal
			if (!lyr_internals.id.shunts.adapters.set.contains(weaponId)) continue;	// to short-circuit the function if it isn't an adapter

			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId); 
			Vector2f parentSlotLocation = parentSlot.retrieve().getLocation();
			float parentSlotAngle = parentSlot.retrieve().getAngle();
			String parentSlotId = slotId; /*parentSlot.retrieve().getId();*/

			childrenParameters childrenParameters = adapters.get(weaponId);
			for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
				lyr_weaponSlot childSlot = parentSlot.clone();
				String childSlotId = lyr_internals.affix.adaptedSlot + parentSlotId + childId; // also used as nodeId because nodeId isn't visible
				Vector2f childSlotLocation = generateChildLocation(parentSlotLocation, parentSlotAngle, childrenParameters.getChildOffset(childId));
				WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

				childSlot.setId(childSlotId);
				childSlot.setNode(childSlotId, childSlotLocation);
				childSlot.setSlotSize(childSlotSize);

			 	hullSpec.addWeaponSlot(childSlot.retrieve());
			}

			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(parentSlotId, weaponId);
			refreshRefit = true;
		}

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}

	private static final Map<String,Float> fluxCapacityBonus = new HashMap<String,Float>();
	private static final Map<String,Float> fluxDissipationBonus = new HashMap<String,Float>();
	private static final Map<String,Float> fighterBayBonus = new HashMap<String,Float>();
	private static final Map<String,Float> mutableStatBonus = new HashMap<String,Float>();
	static {
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.large, 0.16f);
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.medium, 0.08f);
		fluxCapacityBonus.put(lyr_internals.id.shunts.capacitors.small, 0.04f);
		mutableStatBonus.putAll(fluxCapacityBonus);

		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.large, 0.16f);
		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.medium, 0.08f);
		fluxDissipationBonus.put(lyr_internals.id.shunts.dissipators.small, 0.04f);
		mutableStatBonus.putAll(fluxDissipationBonus);

		fighterBayBonus.put(lyr_internals.id.shunts.launchTube.large, 1.00f);
		mutableStatBonus.putAll(fighterBayBonus);
	}

	/** 
	 * Provides bonuses based on the number of shunts installed in slots.
	 * Shunts are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 * @param hullModSpecId to properly accumulate the bonuses under the same id
	 */
	protected static final void ehm_mutableShuntActivator(MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		float fluxCapacityMult = 1.0f;
		float fluxDissipationMult = 1.0f;
		int fighterBayFlat = 0;

		// slot conversion
		for (String slotId: variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) == null) continue;	// short-circuit to avoid a potential null pointer (may/will happen when vanilla hullSpec is (re)loaded)
			if (slotId.startsWith(lyr_internals.affix.adaptedSlot)) continue;	// short-circuit to prevent diverters working on adapted slots
			if (slotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// short-circuit to prevent diverters working on converted slots

			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (!mutableStatBonus.containsKey(weaponId)) continue; // to short-circuit the function if it isn't a shunt

			lyr_weaponSlot slot = hullSpec.getWeaponSlot(slotId); 

			slot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(slotId, weaponId);
			refreshRefit = true; 
		}

		// bonus calculation
		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check them

			String slotId = slot.getId();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId); if (weaponSpec == null) continue;	// skip empty slots
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();
	
			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (fluxCapacityBonus.containsKey(weaponId)) fluxCapacityMult += fluxCapacityBonus.get(weaponId);
			else if (fluxDissipationBonus.containsKey(weaponId)) fluxDissipationMult += fluxDissipationBonus.get(weaponId);
			else if (fighterBayBonus.containsKey(weaponId)) fighterBayFlat += fighterBayBonus.get(weaponId);
		}

		stats.getFluxCapacity().modifyMult(hullModSpecId, fluxCapacityMult);
		stats.getFluxDissipation().modifyMult(hullModSpecId, fluxDissipationMult);
		stats.getNumFighterBays().modifyFlat(hullModSpecId, fighterBayFlat);

		variant.setHullSpecAPI(hullSpec.retrieve());
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}

	/**
	 * An inner class to supply the converters with relevant child data
	 */
	private static class childParameters {
		private String childSuffix; // childIds are used as position identifier, and used as a suffix
		private int childCost;
		private WeaponSize childSize;

		protected childParameters(String childSuffix, WeaponSize childSize, int childCost) {
			this.childSuffix = childSuffix;
			this.childCost = childCost;
			this.childSize = childSize;
		}

		public String getChildSuffix() {
			return this.childSuffix;
		}

		private int getChildCost() {
			return this.childCost;
		}

		private WeaponSize getChildSize() {
			return this.childSize;
		}
	}
	
	private static Map<String, childParameters> converters = new HashMap<String, childParameters>();
	private static childParameters mediumToLarge = new childParameters("ML", WeaponSize.LARGE, 2);
	private static childParameters smallToLarge = new childParameters("SL", WeaponSize.LARGE, 3);
	private static childParameters smallToMedium = new childParameters("SM", WeaponSize.MEDIUM, 1);
	static {
		converters.put(lyr_internals.id.shunts.converters.mediumToLarge, mediumToLarge);
		converters.put(lyr_internals.id.shunts.converters.smallToLarge, smallToLarge);
		converters.put(lyr_internals.id.shunts.converters.smallToMedium, smallToMedium);
	}

	private static Map<WeaponSize, Integer> slotValue = new HashMap<WeaponSize, Integer>();
	static {
		slotValue.put(WeaponSize.LARGE, 4);
		slotValue.put(WeaponSize.MEDIUM, 2);
		slotValue.put(WeaponSize.SMALL, 1);
	}

	private static Map<String, Integer> converterCost = new HashMap<String, Integer>();
	static {
		converterCost.put(lyr_internals.id.shunts.converters.mediumToLarge, 2);
		converterCost.put(lyr_internals.id.shunts.converters.smallToLarge, 3);
		converterCost.put(lyr_internals.id.shunts.converters.smallToMedium, 1);
	}

	/** 
	 * Spawns additional weapon slots, if the slots have adapters on them.
	 * Adapters are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 */
	protected static final void ehm_diverterAndConverterActivator(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		int slotPoints = variant.getSMods().contains("ehm_test") ? 8 : 0;
		SortedSet<String> sortedFittedWeaponSlots = new TreeSet<String>(variant.getFittedWeaponSlots());

		LinkedHashSet<String> sMods = variant.getSMods();
		sMods = sMods;
		String testId = "CS_WS 001ML";
		String derp = testId.substring(3, testId.length()-2);
		derp = derp;

		// slot conversion for diverters
		for (String slotId: sortedFittedWeaponSlots) {	// need to use a sorted set to keep diversion/conversion in order
			if (variant.getSlot(slotId) == null) {
				slotId = slotId.substring(3, slotId.length()-2);
			} else {
				if (slotId.startsWith(lyr_internals.affix.adaptedSlot)) continue;	// short-circuit to prevent diverters working on adapted slots
				if (slotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// short-circuit to prevent diverters working on converted slots
			}

			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (!lyr_internals.id.shunts.diverters.set.contains(weaponId)) continue; // to short-circuit the function if it isn't a shunt

			lyr_weaponSlot slot = hullSpec.getWeaponSlot(slotId); 

			// slotPoints += slotValue.get(weaponSize);	// needs to be calculated afterwards like mutableStat bonus as this block will execute only on install
			slot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(slotId, weaponId);
			refreshRefit = true;
		}

		// slotPoints calculation
		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check decorative

			String slotId = slot.getId();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId); if (weaponSpec == null) continue;	// skip empty slots
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (lyr_internals.id.shunts.diverters.set.contains(weaponId)) slotPoints += slotValue.get(weaponSize); // to short-circuit the function if it isn't a shunt
			else if (lyr_internals.id.shunts.converters.set.contains(weaponId)) slotPoints -= converterCost.get(weaponId); // to short-circuit the function if it isn't a shunt
		}

		// slot conversion for converters
		for (String slotId: sortedFittedWeaponSlots) {	// need to use a sorted set to keep diversion/conversion in order
			if (variant.getSlot(slotId) == null) {
				slotId = slotId.substring(3, slotId.length()-2);
			} else {
				if (slotId.startsWith(lyr_internals.affix.adaptedSlot)) continue;	// short-circuit to prevent diverters working on adapted slots
				if (slotId.startsWith(lyr_internals.affix.convertedSlot)) continue;	// short-circuit to prevent diverters working on converted slots
			}

			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (!lyr_internals.id.shunts.converters.set.contains(weaponId)) continue; // to short-circuit the function if it isn't a shunt

			childParameters childParameters = converters.get(weaponId);
			int childCost = childParameters.getChildCost(); if (slotPoints - childCost < 0) continue;

			lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = lyr_internals.affix.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId because nodeId isn't visible
			Vector2f childSlotLocation = parentSlot.retrieve().getLocation();
			WeaponSize childSlotSize = childParameters.getChildSize();

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

			hullSpec.addWeaponSlot(childSlot.retrieve());

			slotPoints -= converterCost.get(weaponId);	// needs to be subtracted from here on initial install to avoid infinite installs
			parentSlot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(slotId, weaponId);
			refreshRefit = true; 
		}

		variant.setHullSpecAPI(hullSpec.retrieve()); 
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}

	/** 
	 * Refreshes the hullSpec and returns it.
	 * @param variant whose hullSpec will be restored
	 * @return a restored hullSpec
	 */
	public static final ShipHullSpecAPI ehm_adapterRemoval(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}

	/** 
	 * Activated shunts and adapters (decorative, built-in ones) are added
	 * to the weapon groups by the game in some cases such as installing
	 * them first then their activators immediately. When this happens,
	 * even though they are unusable, they appear as ghosts under weapon
	 * groups and as a selectable group in combat.
	 * <p>This method goes over the groups and removes them. Not sure when
	 * / why / how this happens. This is a sufficient workaround till the
	 * root cause can be found, however.
	 * @param variant whose weapon groups will be purged of activated stuff
	 */
	protected static final void ehm_cleanWeaponGroupsUp(ShipVariantAPI variant) {
		List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
		Map<String, String> groupCleanupTargets = new HashMap<String, String>(variant.getHullSpec().getBuiltInWeapons());
		groupCleanupTargets.values().retainAll(lyr_internals.id.shunts.set);
		for (WeaponGroupSpec weaponGroup: weaponGroups) {
			weaponGroup.getSlots().removeAll(groupCleanupTargets.keySet());
		}
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(lyr_tooltip.header.notApplicable, lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(lyr_tooltip.text.lacksBase, lyr_tooltip.text.padding);
		}

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(hullModSpecId) ? lyr_tooltip.header.lockedIn : lyr_tooltip.header.lockedOut;

			tooltip.addSectionHeading(inOrOut, lyr_tooltip.header.locked_textColour, lyr_tooltip.header.locked_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (ehm_hasWeapons(ship, lyr_internals.affix.adaptedSlot)) tooltip.addPara(lyr_tooltip.text.hasWeaponsOnAdaptedSlots, lyr_tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		// if (ehm_hasRetrofitTag(ship, lyr_internals.tag.adapterRetrofit, hullModSpecId)) return false; 
		
		return true; 
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		return true;
	}
	//#endregion
}