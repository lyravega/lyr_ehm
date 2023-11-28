package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.*;
import java.util.Map.Entry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyravega.listeners.lyr_fleetTracker;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.logger.lyr_logger;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_minimodule extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		Map<String, String> stationModules = variant.getStationModules();

		for (Iterator<Entry<String, String>> iterator = stationModules.entrySet().iterator(); iterator.hasNext(); ) {
			Entry<String, String> moduleEntry = iterator.next();

			if (!moduleEntry.getValue().startsWith("ehm_module")) continue;

			iterator.remove();
		}

		variant.setHullSpecAPI(new lyr_hullSpec(false, variant.getHullSpec()).reference());
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (!moduleSet.contains(weaponId)) return;
		lyr_fleetTracker.instance().addTracking(variant, null, null);	// order of this method matters; needs to be done before commit
		commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (!moduleSet.contains(weaponId)) return;
		commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	static final Set<String> moduleSet = new HashSet<String>();
	static {
		moduleSet.add("ehm_module_base");
	}

	// com.fs.starfarer.title.Object.M

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI parentVariant = stats.getVariant();
		lyr_hullSpec parentHullSpec = new lyr_hullSpec(false, parentVariant.getHullSpec());
		// parentHullSpec.getHints().add(ShipTypeHints.DO_NOT_SHOW_MODULES_IN_FLEET_LIST);	// with this some status related shit is avoided but best not to use it
		parentHullSpec.getHints().add(ShipTypeHints.SHIP_WITH_MODULES);

		ShipVariantAPI moduleVariant = Global.getSettings().getVariant("ehm_module_base_Hull").clone();
		lyr_hullSpec moduleHullSpec = new lyr_hullSpec(false, moduleVariant.getHullSpec());
		// moduleVariant.setHullVariantId("ehm_module_shield_variant");
		moduleVariant.addPermaMod("ehm_module_base", false);
		moduleVariant.setSource(VariantSource.REFIT);

		List<WeaponSlotAPI> shunts = parentHullSpec.getAllWeaponSlotsCopy();
		Map<String, String> stationModules = parentVariant.getStationModules();

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			String slotId = iterator.next().getId();
			WeaponSpecAPI shuntSpec = parentVariant.getWeaponSpec(slotId);

			if (shuntSpec == null) { iterator.remove(); continue; }
			if (!"ehm_module_base".equals(shuntSpec.getWeaponId())) { iterator.remove(); continue; }
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isStationModule()) continue;

			String slotId = slot.getId();
			// parentVariant.clearSlot(slotId);	// modules clear the slot when removed from their refit button

			// TODO: prototypes (modules as weapons) needs to get cleared or turned into a decorative one because otherwise mass slot retrofits will crash the game
			// they need to affect the OP of the ship

			lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(slotId);
			parentSlot.setWeaponType(WeaponType.STATION_MODULE);
			parentSlot.setSlotType(slotTypeConstants.hidden);
			if (!stationModules.keySet().contains(slotId)) parentVariant.setModuleVariant(slotId, moduleVariant);	// module variant insertion on the parent variant is done here
		}

		// the block below is necessary if shunts are to be removed after module insertion, but as the shunts stay on the variant the function is executed above
		// for (String moduleSlotId : stationModules.keySet()) {
		// 	if (parentHullSpec.getWeaponSlot(moduleSlotId).getWeaponType() == WeaponType.STATION_MODULE) continue;

		// 	lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(moduleSlotId);
		// 	parentSlot.setWeaponType(WeaponType.STATION_MODULE);
		// 	parentSlot.setSlotType(slotTypeConstants.hidden);
		// }

		// this is to prevent strip from removing the base weapons as they're not built-in, to make modules cost OP in a way
		for (Entry<String, String> moduleEntry : stationModules.entrySet()) {
			if (moduleEntry.getValue().startsWith("ehm_module") && parentVariant.getWeaponId(moduleEntry.getKey()) == null) parentVariant.addWeapon(moduleEntry.getKey(), "ehm_module_base");
		}

		parentVariant.setHullSpecAPI(parentHullSpec.retrieve());

		// the remaining part of this block is specifically for the refit tab to refresh the refit member's status
		// if not done so, the game will crash as the new modules will lack an entry in the member's status array
		if (stats.getFleetMember() == null) return;
		ehm_updateMemberStatus(stats.getFleetMember());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			// case 0: return "launch tubes";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		// TODO: this may need more info?
	}
	//#endregion

	@Deprecated
	public static void ehm_updateAllMemberStatuses() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getStatus().getNumStatuses() != member.getVariant().getStationModules().size() + 1) {
				lyr_hullSpec hullSpec = new lyr_hullSpec(false, member.getHullSpec());

				for (String moduleSlotId : member.getVariant().getStationModules().keySet()) {
					if (hullSpec.getWeaponSlot(moduleSlotId).getWeaponType() == WeaponType.STATION_MODULE) continue;

					lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(moduleSlotId);
					parentSlot.setWeaponType(WeaponType.STATION_MODULE);
				}

				try {
					lyr_reflectionUtilities.fieldReflection.findFieldByName("status", member).set(null);
					member.getStatus();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method refreshes the status of a fleet member by nullifying the status field of it using
	 * reflection, which causes the getter to reinitialize the field with all of the child modules
	 * included.
	 * <p> This is necessary for the refit ship/member, as dynamic module additions to the ship will
	 * require an update on its member as well. Otherwise the game will crash as it will attempt to
	 * seek a non-existent index in the status array.
	 * <p> This is not necessary on load, since the game builds the status information after the
	 * hull modification effects are processed and at that time any additional dynamic modules will
	 * be present on the actual member's variant, and its status will be included in the array.
	 * @param member of the refit ship; actual members already have updated statuses but dynamic changes require updates in refit tab
	 */
	public static void ehm_updateMemberStatus(FleetMemberAPI member) {
		if (member.getStatus().getNumStatuses() != member.getVariant().getStationModules().size() + 1) {	// check if status needs to be refreshed; status array includes parent's, so check with module amount + 1
			try {
				lyr_logger.debug("Rebuilding the member status for "+member.getShipName());
				lyr_reflectionUtilities.fieldReflection.findFieldByName("status", member).set(null);	// setting this field to null will cause the getter to repopulate
				member.getStatus();	// as the status field is null, this getter will repopulate the status array
			} catch (Throwable t) {
				lyr_logger.error("Rebuilding the member status failed for "+member.getShipName(), t);
			}
		}
	}
}
