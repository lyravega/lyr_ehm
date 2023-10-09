package experimentalHullModifications.hullmods.ehm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import lyravega.misc.lyr_internals;

/**
 * A class dedicated to house the helper functions for the ships
 * @author lyravega
 */
public class _ehm_helpers {
	/**
	 * @param ship to check
	 * @param hullmodIdToIgnore can be null. If not, the hullmod with this id will be ignored
	 * @return true if the ship has modular hullmods (except the ignored one if any)
	 */
	public static boolean hasModularHullmods(ShipAPI ship, String hullmodIdToIgnore) {
		if (hullmodIdToIgnore == null) return !ship.getVariant().getNonBuiltInHullmods().isEmpty();
		for (String hullModId : ship.getVariant().getNonBuiltInHullmods()) {
			if (hullModId.equals(hullmodIdToIgnore)) continue;

			return true;
		}; return false;
	}

	/**
	 * @param ship to check
	 * @return true if the ship has any fitted (non-built-in) wings
	 */
	public static boolean hasAnyFittedWings(ShipAPI ship) {
		return !ship.getVariant().getNonBuiltInWings().isEmpty();
	}

	/**
	 * @param ship to check
	 * @param hullModSpecId of the hull modification that grant extra fighter slots
	 * @return true if the extra slots (newly & last added ones) have fighters in them
	 */
	public static boolean hasExtraWings(ShipAPI ship, String hullModSpecId) {
		int wingsSize = ship.getVariant().getWings().size();
		StatMod flatStatMod = ship.getMutableStats().getNumFighterBays().getFlatStatMod(hullModSpecId);
	
		if (flatStatMod != null) {
			float fighterBayFlat = flatStatMod.getValue();
	
			for (int i = (int) (ship.getMutableStats().getNumFighterBays().getModifiedValue() - fighterBayFlat); i < wingsSize; i++) {
				if (ship.getVariant().getWingId(i) != null) return true;
			}
		}; return false;
	}

	/**
	 * @param ship to check
	 * @return true if the ship is a module
	 */
	public static boolean isModule(ShipAPI ship) {
		return !Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().contains(ship.getFleetMember());
	}

	/**
	 * @param ship to check
	 * @return true if the ship is a parent; a ship with modules
	 */
	public static boolean isParent(ShipAPI ship) {
		return !ship.getVariant().getStationModules().isEmpty();
	}

	/**
	 * @param ship to check
	 * @return true if there are any capacitors or vents installed on the variant
	 */
	public static boolean hasCapacitorsOrVents(ShipAPI ship) {
		return ship.getVariant().getNumFluxVents() > 0 || ship.getVariant().getNumFluxCapacitors() > 0;
	}	

	/**
	 * @param ship to check
	 * @return true if the ship has weapons on weapon slots; will ignore shunts
	 */
	public static final boolean hasWeapons(ShipAPI ship) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();
	
			if (!slot.isWeaponSlot()) continue;
			return true;
		}; return false;
	}

	/**
	 * @param ship to check
	 * @param slotPrefix only slots with this prefix will be cared about
	 * @return true if the ship has weapons on specific slots
	 */
	public static final boolean hasWeapons(ShipAPI ship, String slotPrefix) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();
	
			if (!slot.getId().startsWith(slotPrefix)) continue;
			else if (slot.isDecorative()) return true;	// in this case, it is an activated shunt on a spawned slot
			if (!slot.isWeaponSlot()) continue;
			return true;
		}; return false;
	}

	/**
	 * @param ship to check
	 * @param ignoredWeaponIds set that contains weapon ids to be ignored
	 * @return true if the ship has weapons with non-matching weapon ids
	 */
	@Deprecated
	public static final boolean hasWeapons(ShipAPI ship, Set<String> ignoredWeaponIds) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();
	
			if (ignoredWeaponIds.contains(weapon.getId())) continue;
			if (!slot.isWeaponSlot()) continue;
			return true;
		}; return false;
	}

	/**
	 * @param ship to check
	 * @param weaponType of the weapons that will be ignored
	 * @return true if the ship has weapons with type other than the specified type
	 */
	public static final boolean hasWeapons(ShipAPI ship, WeaponType weaponType) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();
	
			if (weapon.getType() == weaponType) continue;
			if (!slot.isWeaponSlot()) continue;
			return true;
		}; return false;
	}

	/**
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
	 * @param ship to check 
	 * @return true if ship has it, false otherwise (duh)
	 */
	public static final boolean hasExperimentalSMod(ShipAPI ship) {
		for (String hullModId: ship.getVariant().getSMods()) {
			if (Global.getSettings().getHullModSpec(hullModId).hasTag(lyr_internals.tag.experimental)) return true;
		}; return false;
	}

	/**
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
	 * @param ship to check 
	 * @return true if ship has it, false otherwise (duh)
	 */
	public static final boolean hasRetrofitBaseBuiltIn(ShipAPI ship) {
		return ship.getVariant().getHullSpec().isBuiltInMod(lyr_internals.id.hullmods.base);
	}

	/**
	 * Checks the ship if it has another hull modification using the passed tag
	 * @param ship to check the installed hullmods
	 * @param tag to check if the ship has one already
	 * @param ignoredHullmodId to exclude from the check, can be {@code null}
	 * @return true if there is another mod with the searched tag, false otherwise
	 */
	public static final boolean hasHullModWithTag(ShipAPI ship, String tag, String ignoredHullmodId) {
		for (String hullModId : ship.getVariant().getHullMods()) {
			if (hullModId.equals(ignoredHullmodId)) continue;
			if (Global.getSettings().getHullModSpec(hullModId).hasTag(tag)) return true;
		}; return false;
	}

	/**
	 * Checks the variant if it has another hull modification using the passed tag
	 * @param variant to check the installed hullmods
	 * @param tag to check if the ship has one already
	 * @param ignoredHullmodId to exclude from the check, can be {@code null}
	 * @return true if there is another mod with the searched tag, false otherwise
	 */
	public static final boolean hasHullModWithTag(ShipVariantAPI variant, String tag, String ignoredHullmodId) {
		for (String hullModId : variant.getHullMods()) {
			if (hullModId.equals(ignoredHullmodId)) continue;
			if (Global.getSettings().getHullModSpec(hullModId).hasTag(tag)) return true;
		}; return false;
	}

	/** 
	 * Activated shunts (decorative, built-in ones) are added to the weapon
	 * groups by the game in some cases, like when the hullSpec is replaced.
	 * <p>This method goes over the groups and removes them. Not sure when
	 * / why / how this happens. This is a sufficient workaround till the
	 * root cause can be found, however.
	 * @param variant whose weapon groups will be purged of activated shunts
	 */
	public static final void cleanWeaponGroupsUp(ShipVariantAPI variant) {
		List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
		Collection<String> groupKeepTargets = variant.getFittedWeaponSlots();	// this is to fix an (vanilla) issue where groups have incorrect entries
		Map<String, String> groupCleanupTargets = new HashMap<String, String>(variant.getHullSpec().getBuiltInWeapons());
		groupCleanupTargets.values().retainAll(lyr_internals.id.shunts.set);
	
		for (Iterator<WeaponGroupSpec> iterator = weaponGroups.iterator(); iterator.hasNext();) {
			WeaponGroupSpec weaponGroup = iterator.next();
	
			weaponGroup.getSlots().removeAll(groupCleanupTargets.keySet());
			weaponGroup.getSlots().retainAll(groupKeepTargets);
			if (weaponGroup.getSlots().isEmpty()) iterator.remove();
		}
	}

	public static final boolean removeHullModsWithSameTag(ShipVariantAPI variant, String tag, String ignoredHullmodId) {
		for (String hullmodId : variant.getNonBuiltInHullmods()) {
			if (hullmodId.equals(ignoredHullmodId)) continue;
			if (!Global.getSettings().getHullModSpec(hullmodId).hasTag(tag)) continue;
			variant.removeMod(hullmodId);
			return true;
		}; return false;
	}

	public static boolean isExperimentalMod(HullModSpecAPI spec, boolean excludeRestricted) {
		if (!spec.getManufacturer().equals(lyr_internals.id.manufacturer)) return false;
		if (!spec.hasTag(lyr_internals.tag.experimental)) return false;
		if (excludeRestricted && spec.hasTag(lyr_internals.tag.restricted)) return false;
		return true;
	}

	public static boolean isExperimentalShunt(WeaponSpecAPI spec, boolean excludeRestricted) {
		if (!spec.getManufacturer().equals(lyr_internals.id.manufacturer)) return false;
		if (!spec.hasTag(lyr_internals.tag.experimental)) return false;
		if (excludeRestricted && spec.hasTag(lyr_internals.tag.restricted)) return false;
		return true;
	}
}
