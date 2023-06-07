package experimentalHullModifications.hullmods;

import static lyravega.tools.lyr_uiTools.commitChanges;
import static lyravega.tools.lyr_uiTools.playSound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.loading.specs.HullVariantSpec;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import lyravega.listeners.events.normalEvents;

public class _ehm_test extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		// commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		// variant.setHullSpecAPI(ehm_hullSpecReference(variant));
		// commitChanges(); playSound();
	}

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@SuppressWarnings("unused")
	private static class hullInfo {
		private ShipHullSpecAPI hullSpec;
		private boolean isDHull_variant;
		private boolean isDHull;
		private boolean isDefaultDHull;
		private boolean isRestoreToBase;
		private String hullId;
		private String baseHullId;
		private String dParentHullId;

		private hullInfo(ShipVariantAPI variant) {
			this.hullSpec = variant.getHullSpec();
			this.isDHull_variant = variant.isDHull();
			this.isDHull = hullSpec.isDHull();
			this.isDefaultDHull = hullSpec.isDefaultDHull();
			this.isRestoreToBase = hullSpec.isRestoreToBase();
			this.hullId = hullSpec.getHullId();
			this.baseHullId = hullSpec.getBaseHullId();
			this.dParentHullId = hullSpec.getDParentHullId();
		}
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) { try {

	} catch (Throwable t ) { logger.warn("Test fail in 'applyEffectsBeforeShipCreation()'", t);	}}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) { try {

	} catch (Throwable t ) { logger.warn("Test fail in 'applyEffectsAfterShipCreation()'", t);	}}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) { try {

	} catch (Throwable t ) { logger.warn("Test fail in 'applyEffectsToFighterSpawnedByShip()'", t);	}}

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {

	}

	@Override
	public boolean affectsOPCosts() {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		// super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		tooltip.addSectionHeading(ship.getFleetMemberId(), Alignment.MID, 5f);

		List<FleetMemberAPI> membersListCopy = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
		List<FleetMemberAPI> membersListWithFightersCopy = Global.getSector().getPlayerFleet().getFleetData().getMembersListWithFightersCopy();
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		String fleetMemberId = ship.getFleetMemberId();
		FleetMemberAPI fleetMember = ship.getFleetMember();
		ShipVariantAPI variant = ship.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();
		HullVariantSpec hullVariantSpec = (HullVariantSpec) variant;

		Map<String, String> stationModules = variant.getStationModules();
		List<String> moduleSlots = variant.getModuleSlots();
		Map<String, HullVariantSpec> moduleVariants = hullVariantSpec.getModuleVariants();

		Map<String, ShipVariantAPI> test = new HashMap<String, ShipVariantAPI>();
		for (String slotId : moduleSlots) {
			test.put(slotId, variant.getModuleVariant(slotId));
		}

		Set<ShipVariantAPI> refitVariants = new HashSet<ShipVariantAPI>();
		Set<ShipVariantAPI> hullVariants = new HashSet<ShipVariantAPI>();
		Set<ShipVariantAPI> stockVariants = new HashSet<ShipVariantAPI>();
		for (ShipVariantAPI moduleVariant : test.values()) {
			if (!moduleVariant.getSource().equals(VariantSource.REFIT)) refitVariants.add(moduleVariant);
			if (!moduleVariant.getSource().equals(VariantSource.HULL)) hullVariants.add(moduleVariant);
			else stockVariants.add(moduleVariant);
		}

		boolean stationModule = ship.isStationModule();
		boolean shipWithModules = ship.isShipWithModules();
		List<ShipAPI> childModulesCopy = ship.getChildModulesCopy();

		ShipVariantAPI memberVariant = null;
		for (FleetMemberAPI member: Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (!member.getId().equals(ship.getFleetMemberId())) continue;

			memberVariant = member.getVariant(); break;
		}

		boolean ismember = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().contains(fleetMember);
		FleetMemberType type = fleetMember.getType();

		ShipAPI parent = ship.getParentStation();
		MutableShipStatsAPI stats = fleetMember.getStats();
		CombatEntityAPI entity = fleetMember.getStats().getEntity();

		ship = ship;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return super.isApplicableToShip(ship);
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
	}
}
