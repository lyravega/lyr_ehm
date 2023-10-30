package lyravega.utilities;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

/**
 * A class dedicated to house the helper functions for the ships, variants,
 * and whatnot. Mostly contains checkers
 * @author lyravega
 */
public class lyr_miscUtilities {
	/**
	 * @param ship to check
	 * @param hullmodIdToIgnore can be null. If not, the hullmod with this id will be ignored
	 * @return true if the ship has modular hullmods (except the ignored one if any)
	 */
	public static boolean hasModularHullmods(ShipAPI ship, String hullmodIdToIgnore, boolean ignoreHidden) {
		for (String hullModId : ship.getVariant().getNonBuiltInHullmods()) {
			if (hullmodIdToIgnore != null && hullModId.equals(hullmodIdToIgnore)) continue;
			if (!ignoreHidden && Global.getSettings().getHullModSpec(hullModId).isHiddenEverywhere()) continue;

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
	 * Checks if a hullmod is built-in on the ship
	 * @param ship to check
	 * @param ship to check
	 * @return true if ship has it, false otherwise (duh)
	 */
	public static final boolean hasBuiltInHullMod(ShipAPI ship, String hullModId) {
		return ship.getHullSpec().isBuiltInMod(hullModId);
	}

	/**
	 * Checks the ship if it has another hull modification using the passed tag
	 * @param ship to check the installed hullmods
	 * @param tag to check if the ship has one already
	 * @param ignoredHullmodId to exclude from the check, can be {@code null}
	 * @param checkAll {@code true} to check all hullmods, {@code false} to only check sMods
	 * @return true if there is another mod with the searched tag, false otherwise
	 */
	public static final boolean hasHullModWithTag(ShipAPI ship, String tag, String ignoredHullmodId, boolean checkAll) {
		for (String hullModId : checkAll ? ship.getVariant().getHullMods() : ship.getVariant().getSMods()) {
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
	 * Purges the weapon groups of weapons with the matching ids in the passed set.
	 * While this is not necessary in pretty much all cases, there are a rare few
	 * that might require this. Activated shunts is an example for this.
	 * <p> Shunts that get activated when an activator is installed stay in their
	 * weapon groups. This method ensures that the now decorative, non-functional
	 * weapons are removed from those.
	 * @param variant whose weapon groups will be purged
	 * @param weaponIdSet a set of weapon ids that will be removed from weapon groups
	 */
	public static final void cleanWeaponGroupsUp(ShipVariantAPI variant, Set<String> weaponIdSet) {
		List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
		Collection<String> groupKeepTargets = variant.getFittedWeaponSlots();	// this is to fix an (vanilla) issue where groups have incorrect entries
		Map<String, String> groupCleanupTargets = new HashMap<String, String>(variant.getHullSpec().getBuiltInWeapons());
		groupCleanupTargets.values().retainAll(weaponIdSet);

		for (Iterator<WeaponGroupSpec> iterator = weaponGroups.iterator(); iterator.hasNext();) {
			WeaponGroupSpec weaponGroup = iterator.next();

			weaponGroup.getSlots().removeAll(groupCleanupTargets.keySet());
			weaponGroup.getSlots().retainAll(groupKeepTargets);
			if (weaponGroup.getSlots().isEmpty()) iterator.remove();
		}
	}

	/**
	 * Removes a hullmod from the variant, if it has a tag that matches the
	 * given one. {@code ignoredHullmodId} is used to skip a certain hullmod,
	 * and remove the other. Used to make a tag exclusive on the variant.
	 * @param variant to check
	 * @param tag of the hullmod which will be removed
	 * @param ignoredHullmodId of the hullmod to ignore and keep
	 * @return {@code true} if a hullmod is removed, {@code false} otherwise
	 */
	public static final boolean removeHullModWithTag(ShipVariantAPI variant, String tag, String ignoredHullmodId) {
		for (String hullmodId : variant.getNonBuiltInHullmods()) {
			if (hullmodId.equals(ignoredHullmodId)) continue;
			if (!Global.getSettings().getHullModSpec(hullmodId).hasTag(tag)) continue;
			variant.removeMod(hullmodId);
			return true;
		}; return false;
	}

	/**
	 * @param ship to check
	 * @return {@code true} if ship has {@code "phasecloak"}, {@code false} otherwise
	 */
	public static final boolean hasPhaseCloak(ShipAPI ship) {
		return ship.getHullSpec().isPhase();
		// return ship.getPhaseCloak() != null && "phasecloak".equals(ship.getPhaseCloak().getId());
	}

	/**
	 * Checks a hull spec if it has any civilian hints or the civgrade hull modification.
	 * There is no standard for this, some ships utilize hints, some utilize the hullmod,
	 * hence the necessity for a blanket check to assume what is civilian and not.
	 * @param hullSpec to check
	 * @return {@code true} if it has any, {@code false} otherwise
	 */
	public static final boolean hasCivilianHintsOrMod(ShipHullSpecAPI hullSpec) {
		if (hullSpec.isBuiltInMod(HullMods.CIVGRADE)) return true;

		for (ShipTypeHints hint : hullSpec.getHints()) switch (hint) {
			case CIVILIAN: case TANKER: case TRANSPORT: case FREIGHTER: case LINER:
				return true;
			default: continue;
		}

		return false;
	}

	/**
	 * A check method to see if a ship is stripped or not. More of a convenience
	 * method that utilizes other check methods, all bundled in one. Checks wings,
	 * capacitors & vents, weapons and modular mods that are not hidden everywhere
	 * @param ship to check
	 * @param hullModSpecId to ignore, should be the caller's id
	 * @return {@code true} if ship is stripped, {@code false} otherwise
	 */
	public static final boolean isStripped(ShipAPI ship, String hullModSpecId) {
		if (hasAnyFittedWings(ship)) return false;
		if (hasCapacitorsOrVents(ship)) return false;
		if (hasWeapons(ship)) return false;
		if (hasModularHullmods(ship, hullModSpecId, false)) return false;

		return true;
	}
}
