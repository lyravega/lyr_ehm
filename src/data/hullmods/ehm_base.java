package data.hullmods;

import static data.hullmods.ehm_ar._ehm_ar_base.ehm_adapterRemoval;
import static data.hullmods.ehm_ec._ehm_ec_base.ehm_restoreEngineSlots;
import static data.hullmods.ehm_sc._ehm_sc_base.ehm_restoreShield;
import static data.hullmods.ehm_sr._ehm_sr_base.ehm_systemRestore;
import static data.hullmods.ehm_wr._ehm_wr_base.ehm_weaponSlotRestore;
import static lyr.tools._lyr_uiTools.clearUndo;
import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.isRefitTab;
import static lyr.tools._lyr_uiTools.playSound;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import org.apache.log4j.Logger;

/**
 * Serves as a requirement for all experimental hull modifications, and provides hullMod
 * tracking to the ship. 
 * </p> Depending on the {@link #trackOnSync} boolean, will either initialize hullMod 
 * tracking through {@link data.scripts.shipTrackerScript shipTrackers} or by utilizing 
 * the {@link #onFleetSync()} method. Both have their downsides, but both also do the same.
 * @category Base Hull Modification 
 * @author lyravega
 */
public class ehm_base extends _ehm_base implements HullModFleetEffect {	
	private static final boolean trackOnSync = true; // if false, scripts inherited from the parent will be used for tracking
	private static final boolean log = true;
	private static final Logger logger = Logger.getLogger("lyr");
	private static ShipAPI sheep = null;

	//#region IMPLEMENTATION (HullModFleetEffect)
	@Override
	public void advanceInCampaign(CampaignFleetAPI fleet) {}

	@Override
	public boolean withAdvanceInCampaign() { return false; }

	@Override
	public boolean withOnFleetSync() { return trackOnSync; }

	// @Override
	// public void onFleetSync(CampaignFleetAPI fleet) {}
	//#endregion
	// END OF IMPLEMENTATION (HullModFleetEffect)

	//#region TRACKING
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (ehm_hasRetrofitBaseBuiltIn(variant)) return;

		variant.setHullSpecAPI(ehm_hullSpecClone(variant)); commitChanges(); playSound();
	}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship == null) return;
		if (!isRefitTab()) return;

		if (trackOnSync) {
			sheep = (fleetMemberMap.containsKey(ship.getFleetMemberId())) ? ship : null;
		} else {
			shipTrackerScript(ship).setVariant(ship.getVariant()); // setVariant() is necessary to reflect the changes on the "refit ship"
		}
	}

	@Override
	public void onFleetSync(CampaignFleetAPI fleet) {
		if (!fleet.isPlayerFleet()) return;
		if (sheep == null) return;
		if (!isRefitTab()) return;

		updateFleetMaps(fleet);
		if (sheep != null) updateHullMods(sheep);
	}

	private static Map<String, Set<String>> hullModMap;
	private static Map<String, FleetMemberAPI> fleetMemberMap;

	public static void buildFleetMaps() {
		if (!trackOnSync) return;

		hullModMap = new HashMap<String, Set<String>>(); 
		fleetMemberMap = new HashMap<String, FleetMemberAPI>();

		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			hullModMap.put(member.getId(), new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(member.getId(), member);
		}
	}

	private static void updateFleetMaps(CampaignFleetAPI fleet) {
		Set<FleetMemberAPI> _fleetMembers = new HashSet<FleetMemberAPI>(fleet.getFleetData().getMembersListCopy());
		Collection<FleetMemberAPI> savedFleetMembers = fleetMemberMap.values();

		if (_fleetMembers.equals(savedFleetMembers)) return;
		String memberId;
		Set<FleetMemberAPI> _savedFleetMembers = new HashSet<FleetMemberAPI>(savedFleetMembers);

		_savedFleetMembers.removeAll(_fleetMembers);
		for (FleetMemberAPI member : _savedFleetMembers) {
			memberId = member.getId();

			hullModMap.remove(memberId);
			fleetMemberMap.remove(memberId);
			if (log) logger.info("FT: Unregistering ST-"+memberId);
		}

		_fleetMembers.removeAll(savedFleetMembers);
		for (FleetMemberAPI member : _fleetMembers) {
			memberId = member.getId();

			hullModMap.put(memberId, new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(memberId, member);
			if (log) logger.info("FT: Registering ST-"+memberId);
		}

		if (!fleetMemberMap.containsKey(sheep.getFleetMemberId())) sheep = null; // probably dead code due to Ln81 and Ln90
	}

	private static void updateHullMods(ShipAPI ship) {
		Set<String> _currentHullMods = new HashSet<String>(ship.getVariant().getHullMods());
		Set<String> savedHullMods = hullModMap.get(ship.getFleetMemberId());

		if (_currentHullMods.equals(savedHullMods)) return;
		String memberId = ship.getFleetMemberId();
		Set<String> _savedHullMods = new HashSet<String>(savedHullMods);

		_savedHullMods.removeAll(_currentHullMods);
		for (String removedHullModId : _savedHullMods) {
			if (_currentHullMods.contains(removedHullModId)) continue;

			onRemoved(removedHullModId, ship);
			savedHullMods.remove(removedHullModId);
			if (log) logger.info("ST-"+memberId+": Removed hull modification '"+removedHullModId+"'");
		}

		_currentHullMods.removeAll(savedHullMods);
		for (String newHullModId : _currentHullMods) {
			if (savedHullMods.contains(newHullModId)) continue;

			onInstalled(newHullModId, ship);
			savedHullMods.add(newHullModId);
			if (log) logger.info("ST-"+memberId+": New hull modification '"+newHullModId+"'");
		}
	}

	@SuppressWarnings("unused")
	private static void onInstalled(String newHullModId, ShipAPI ship) {
		if (!newHullModId.startsWith(ehm.affix.allRetrofit)) return;

		ShipVariantAPI refitVariant = ship.getVariant();
		ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();

		String retrofitType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
		switch (retrofitType) {
			case ehm.affix.adapterRetrofit: clearUndo(); playSound(); break; // 'commitChanges()' is triggered externally
			case ehm.affix.systemRetrofit: commitChanges(); playSound(); break;
			case ehm.affix.weaponRetrofit: commitChanges(); playSound(); break;
			case ehm.affix.shieldCosmetic: commitChanges(); playSound(); break;
			case ehm.affix.engineCosmetic: commitChanges(); playSound(); break;
			default: break;
		}
	}

	@SuppressWarnings("unused")
	private static void onRemoved(String removedHullModId, ShipAPI ship) {
		if (!removedHullModId.startsWith(ehm.affix.allRetrofit)) return;

		ShipVariantAPI refitVariant = ship.getVariant();
		ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();

		String retrofitType = removedHullModId.substring(0, 7); 
		switch (retrofitType) {
			case ehm.affix.adapterRetrofit: refitVariant.setHullSpecAPI(ehm_adapterRemoval(refitVariant)); commitChanges(); playSound(); break;
			case ehm.affix.systemRetrofit: refitVariant.setHullSpecAPI(ehm_systemRestore(refitVariant)); commitChanges(); playSound(); break;
			case ehm.affix.weaponRetrofit: refitVariant.setHullSpecAPI(ehm_weaponSlotRestore(refitVariant)); commitChanges(); playSound(); break;
			case ehm.affix.shieldCosmetic: refitVariant.setHullSpecAPI(ehm_restoreShield(refitVariant)); commitChanges(); playSound(); break;
			case ehm.affix.engineCosmetic: refitVariant.setHullSpecAPI(ehm_restoreEngineSlots(refitVariant)); commitChanges(); playSound(); break;
			default: break;
		}
	}
	//#endregion
	// END OF TRACKING

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (!ship.getVariant().hasHullMod(hullModSpecId)) {
			tooltip.addSectionHeading(ehm.tooltip.header.severeWarning, ehm.tooltip.header.severeWarning_textColour, ehm.tooltip.header.severeWarning_bgColour, Alignment.MID, ehm.tooltip.header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(ehm.tooltip.text.baseRetrofitWarning, ehm.tooltip.text.padding);

			super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		}
	}
}
