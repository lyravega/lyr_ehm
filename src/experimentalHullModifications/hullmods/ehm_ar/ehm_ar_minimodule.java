package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.*;
import java.util.Map.Entry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberStatusAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

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
		moduleSet.add("ehm_module_shield");
	}

	// com.fs.starfarer.api.impl.hullmods.ConvertedHangar
	// private static final HullModEffect convertedHangarEffect = Global.getSettings().getHullModSpec("converted_hangar").getEffect();
	// com.fs.starfarer.api.impl.hullmods.VastHangar
	// private static final HullModEffect vastHangarEffect = Global.getSettings().getHullModSpec("vast_hangar").getEffect();

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		if (!ShipAPI.class.isInstance(stats.getEntity())) return;

		FleetMemberAPI member = stats.getFleetMember();

		ShipVariantAPI parentVariant = stats.getVariant();
		lyr_hullSpec parentHullSpec = new lyr_hullSpec(false, parentVariant.getHullSpec());
		parentHullSpec.getHints().add(ShipTypeHints.DO_NOT_SHOW_MODULES_IN_FLEET_LIST);
		parentHullSpec.getHints().add(ShipTypeHints.SHIP_WITH_MODULES);

		ShipVariantAPI moduleVariant = Global.getSettings().getVariant("ehm_module_shield_Hull").clone();
		lyr_hullSpec moduleHullSpec = new lyr_hullSpec(false, moduleVariant.getHullSpec());
		// moduleVariant.setHullVariantId("ehm_module_shield_variant");
		moduleVariant.addPermaMod("ehm_base", false);	// adding this as perma here for the removal check afterwards; if added as a normal, nonBuiltInMods will contain it
		moduleVariant.setSource(VariantSource.REFIT);

		List<WeaponSlotAPI> shunts = parentHullSpec.getAllWeaponSlotsCopy();
		Map<String, String> stationModules = parentVariant.getStationModules();

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			String slotId = iterator.next().getId();
			WeaponSpecAPI shuntSpec = parentVariant.getWeaponSpec(slotId);

			if (shuntSpec == null) { iterator.remove(); continue; }
			if (!"ehm_module_shield".equals(shuntSpec.getWeaponId())) { iterator.remove(); continue; }
			// if (stationModules.keySet().contains(slotId)) { iterator.remove(); continue; }
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isStationModule()) continue;

			String slotId = slot.getId();
			// parentVariant.clearSlot(slotId);

			lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(slotId);
			parentSlot.setWeaponType(WeaponType.STATION_MODULE);
			if (!stationModules.keySet().contains(slotId)) parentVariant.setModuleVariant(slotId, moduleVariant);
		}

		// for (String moduleSlotId : stationModules.keySet()) {
		// 	if (parentHullSpec.getWeaponSlot(moduleSlotId).getSlotType() == WeaponType.STATION_MODULE) continue;

		// 	lyr_weaponSlot parentSlot = parentHullSpec.getWeaponSlot(moduleSlotId);
		// 	parentSlot.setWeaponType(WeaponType.STATION_MODULE);
		// }

		parentVariant.setHullSpecAPI(parentHullSpec.retrieve());
		// member.setVariant(parentVariant, false, false);

		try {
			lyr_reflectionUtilities.fieldReflection.findFieldByName("status", member).set(null);
			FleetMemberStatusAPI status = member.getStatus();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		String herp = "derp";

		// FleetMemberAPI refitMember = stats.getFleetMember();
		// for (FleetMemberAPI fleetMember : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
		// 	// if (fleetMember.getId().equals(member.getId())) {
		// 	// 	fleetMember.setVariant(parentVariant, false, false);
		// 	// }

		// 	lyr_reflectionUtilities.fieldReflection.findFieldByName("status", fleetMember).set(null);
		// 	FleetMemberStatusAPI fleetMemberStatus = fleetMember.getStatus();
		// }

		// FleetMemberAPI refitMember = stats.getFleetMember();
		// for (FleetMemberAPI fleetMember : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
		// 	if (!fleetMember.getId().equals(refitMember.getId())) continue;

		// 	fleetMember.setVariant(parentVariant, false, false);
		// 	refitMember.setVariant(parentVariant, false, false);
		// 	lyr_reflectionUtilities.fieldReflection.findFieldByName("status", fleetMember).set(null);
		// 	lyr_reflectionUtilities.fieldReflection.findFieldByName("status", refitMember).set(null);
		// 	FleetMemberStatusAPI fleetMemberStatus = fleetMember.getStatus();
		// 	FleetMemberStatusAPI refitMemberStatus = refitMember.getStatus();
		// 	break;
		// }
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

		if (!this.canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut;

			tooltip.addSectionHeading(inOrOut, header.locked_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			if (!lyr_miscUtilities.areMiniModulesStripped(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, lyr_tooltipUtilities.colourizedText.negativeText("Cannot be removed")+" as the mini-modules are "+lyr_tooltipUtilities.colourizedText.highlightText("not stripped"), text.padding);
		}
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.areMiniModulesStripped(ship)) return false;

		return true;
	}
	//#endregion
}
