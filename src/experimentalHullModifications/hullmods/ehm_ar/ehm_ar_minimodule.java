package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
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
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.modules;
import experimentalHullModifications.proxies.ehm_hullSpec;
import lyravega.listeners.events.moduleEvents;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.logger.lyr_logger;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_minimodule extends _ehm_ar_base implements moduleEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		// lyr_fleetTracker.instance().addTracking(stats.getVariant(), null, null);	// order of this method matters; needs to be done before commit

		super.onInstalled(stats);
	}

	// uses super's onRemoved(), weapon events are never called as module events overtakes those

	@Override
	public void onModuleInstalled(MutableShipStatsAPI stats, ShipVariantAPI moduleVariant, String moduleSlotId) {
		if (!this.shuntIdSet.contains(moduleVariant.getHullVariantId().replaceFirst("_Hull", ""))) return;	// TODO: make a new variant instead of using hull?

		// lyr_fleetTracker.instance().addTracking(stats.getVariant(), null, null);	// order of this method matters; needs to be done before commit

		commitVariantChanges();
	}

	@Override
	public void onModuleRemoved(MutableShipStatsAPI stats, ShipVariantAPI moduleVariant, String moduleSlotId) {}
	//#endregion
	// END OF CUSTOM EVENTS

	public static final class moduleData {
		public static final class ids {
			public static final String
				prototype = modules.ids.prototype;	// must match weapon id in .csv and .wpn
		}
		public static final String activatorId = modules.activatorId;
		public static final String tag = modules.groupTag;
		public static final String groupTag = modules.groupTag;
		public static final Map<String, Object[]> dataMap = new HashMap<String, Object[]>();
		public static final Set<String> idSet = dataMap.keySet();
		private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.adaptedSlot, affixes.convertedSlot});

		public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
			return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
		}

		static {
			ShipVariantAPI prototypeVariant = Global.getSettings().getVariant(ids.prototype+"_Hull").clone();	// TODO: make a new variant instead of using hull?
			prototypeVariant.setSource(VariantSource.REFIT);

			dataMap.put(ids.prototype, new Object[]{prototypeVariant.getHullSpec().getOrdnancePoints(null), prototypeVariant});
		}
	}

	public ehm_ar_minimodule() {
		super();

		this.statSet.add(moduleData.groupTag);
		this.shuntIdSet.addAll(moduleData.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI parentVariant = stats.getVariant();
		ehm_hullSpec parentHullSpec = new ehm_hullSpec(parentVariant.getHullSpec(), false);
		DynamicStatsAPI parentDynamicStats = stats.getDynamic();
		Map<String, String> parentModules = parentVariant.getStationModules();

		// hullSpec.getHints().add(ShipTypeHints.DO_NOT_SHOW_MODULES_IN_FLEET_LIST);	// with this some status related shit is avoided but best not to use it
		parentHullSpec.getHints().add(ShipTypeHints.SHIP_WITH_MODULES);

		HashMap<String, StatMod> moduleShunts = parentDynamicStats.getMod(moduleData.groupTag+"_inactive").getFlatBonuses();
		if (!moduleShunts.isEmpty()) {
			for (String slotId : moduleShunts.keySet()) {
				if (parentHullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.STATION_MODULE) continue;

				lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(slotId);
				ShipVariantAPI moduleVariant = ShipVariantAPI.class.cast(moduleData.dataMap.get(moduleData.ids.prototype)[1]).clone();
				ehm_hullSpec moduleHullSpec = new ehm_hullSpec(moduleVariant.getHullSpec(), false);

				moduleVariant.setHullSpecAPI(moduleHullSpec.retrieve());
				parentSlot.setWeaponType(WeaponType.STATION_MODULE);
				parentSlot.setSlotType(slotTypeConstants.hidden);

				if (!parentModules.keySet().contains(slotId)) parentVariant.setModuleVariant(slotId, moduleVariant);
			}
		}

		for (String moduleSlotId : parentModules.keySet()) { String moduleVariantId = parentModules.get(moduleSlotId);
			if (!this.shuntIdSet.contains(moduleVariantId.replaceFirst("_Hull", ""))) continue;	// TODO: make a new variant instead of using hull?

			int ordnancePointMod = -(int) moduleData.dataMap.get(moduleData.ids.prototype)[0];

			parentDynamicStats.getMod(moduleVariantId).modifyFlat(moduleSlotId, 1);
			parentDynamicStats.getMod(moduleData.groupTag).modifyFlat(moduleSlotId, ordnancePointMod);
			parentDynamicStats.getMod(ehm_internals.stats.ordnancePoints).modifyFlat(moduleSlotId, ordnancePointMod);

			if (parentHullSpec.getWeaponSlot(moduleSlotId).getWeaponType() == WeaponType.STATION_MODULE) continue;

			lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(moduleSlotId);

			parentSlot.setWeaponType(WeaponType.STATION_MODULE);
			parentSlot.setSlotType(slotTypeConstants.hidden);
		}

		parentHullSpec.modOrdnancePoints(stats);
		parentVariant.setHullSpecAPI(parentHullSpec.retrieve());

		// the remaining part of this block is specifically for the refit tab to refresh the refit member's status
		// if not done so, the game will crash as the new modules will lack an entry in the member's status array
		if (stats.getFleetMember() == null) return;
		ehm_updateMemberStatus(stats.getFleetMember());
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		// MutableShipStatsAPI stats = ship.getMutableStats();
		// ShipVariantAPI parentVariant = stats.getVariant();
		// ehm_hullSpec parentHullSpec = new ehm_hullSpec(false, parentVariant.getHullSpec());
		// lyr_shieldSpec parentShieldSpec = parentHullSpec.getShieldSpec();
		// // parentHullSpec.getHints().add(ShipTypeHints.DO_NOT_SHOW_MODULES_IN_FLEET_LIST);	// with this some status related shit is avoided but best not to use it
		// parentHullSpec.getHints().add(ShipTypeHints.SHIP_WITH_MODULES);

		// ShipVariantAPI moduleVariant = Global.getSettings().getVariant("ehm_module_prototype_Hull").clone();
		// ehm_hullSpec moduleHullSpec = new ehm_hullSpec(false, moduleVariant.getHullSpec());
		// // moduleVariant.setHullVariantId("ehm_module_shield_variant");
		// moduleVariant.addPermaMod("ehm_module_prototype", false);
		// moduleVariant.setSource(VariantSource.REFIT);

		// // if (!moduleHullSpec.isBuiltInMod("shield_always_on")) moduleHullSpec.addBuiltInMod("shield_always_on");
		// // if (!parentHullSpec.isBuiltInMod("shield_always_on")) parentHullSpec.addBuiltInMod("shield_always_on");
		// // moduleVariant.setHullSpecAPI(moduleHullSpec.retrieve());
		// // Object parentSpriteSpec = parentHullSpec.getSpriteSpec();
		// // Object clonedSpec = null;
		// // try {
		// // 	clonedSpec = lyr_reflectionUtilities.methodReflection.invokeDirect(parentSpriteSpec, "clone");
		// // 	// MethodHandle methodHandle = lyr_reflectionUtilities.methodReflection.findMethodByClass(clonedSpec, null, String.class).getMethodHandle();

		// // } catch (Throwable e) {
		// // 	e.printStackTrace();
		// // }
		// // moduleHullSpec.setSpriteSpec(clonedSpec);

		// List<WeaponSlotAPI> shunts = parentHullSpec.getAllWeaponSlotsCopy();
		// Map<String, String> stationModules = parentVariant.getStationModules();

		// for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
		// 	String slotId = iterator.next().getId();
		// 	WeaponSpecAPI shuntSpec = parentVariant.getWeaponSpec(slotId);

		// 	if (shuntSpec == null) { iterator.remove(); continue; }
		// 	if (!"ehm_module_prototype".equals(shuntSpec.getWeaponId())) { iterator.remove(); continue; }
		// }

		// int i = 1;
		// for (WeaponSlotAPI slot : shunts) {
		// 	if (slot.isStationModule()) continue;

		// 	String slotId = slot.getId();
		// 	// parentVariant.clearSlot(slotId);	// modules clear the slot when removed from their refit button

		// 	// TODO: prototypes (modules as weapons) needs to get cleared or turned into a decorative one because otherwise mass slot retrofits will crash the game
		// 	// they need to affect the OP of the ship

		// 	lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(slotId);
		// 	parentSlot.setWeaponType(WeaponType.STATION_MODULE);
		// 	parentSlot.setSlotType(slotTypeConstants.hidden);

		// 	Vector2f shieldCenterForModule = lyr_vectorUtilities.calculateParentShieldCenterForModule(parentShieldSpec.retrieve(), parentSlot.retrieve());
		// 	ShipVariantAPI clone = moduleVariant.clone();
		// 	clone.addTag("ehm_module_parentShield:"+(shieldCenterForModule.x)+"/"+(shieldCenterForModule.y)+"/"+(parentShieldSpec.getRadius()+i*15)); i++;

		// 	if (!stationModules.keySet().contains(slotId)) parentVariant.setModuleVariant(slotId, clone);	// module variant insertion on the parent variant is done here
		// }

		// // the block below is necessary if shunts are to be removed after module insertion, but as the shunts stay on the variant the function is executed above
		// // for (String moduleSlotId : stationModules.keySet()) {
		// // 	if (parentHullSpec.getWeaponSlot(moduleSlotId).getWeaponType() == WeaponType.STATION_MODULE) continue;

		// // 	lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(moduleSlotId);
		// // 	parentSlot.setWeaponType(WeaponType.STATION_MODULE);
		// // 	parentSlot.setSlotType(slotTypeConstants.hidden);
		// // }

		// for (Entry<String, String> moduleEntry : stationModules.entrySet()) {
		// 	if (moduleEntry.getValue().startsWith("ehm_module") && parentVariant.getWeaponId(moduleEntry.getKey()) == null) parentVariant.addWeapon(moduleEntry.getKey(), "ehm_module_prototype");
		// }

		// parentVariant.setHullSpecAPI(parentHullSpec.retrieve());

		// // the remaining part of this block is specifically for the refit tab to refresh the refit member's status
		// // if not done so, the game will crash as the new modules will lack an entry in the member's status array
		// if (stats.getFleetMember() == null) return;
		// ehm_updateMemberStatus(stats.getFleetMember());
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
				ehm_hullSpec hullSpec = new ehm_hullSpec(member.getHullSpec(), false);

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
