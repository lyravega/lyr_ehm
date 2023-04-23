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

import lyr.settings.lyr_internals;
import lyr.settings.lyr_tooltip;

import org.apache.log4j.Logger;

/**
 * Serves as a requirement for all experimental hull modifications, and provides hullMod
 * tracking to the ship. 
 * </p> Depending on the {@link #trackOnSync} boolean, will either initialize hullMod 
 * tracking through {@link data.scripts.shipTrackerScript shipTrackers} or by utilizing 
 * the {@link #onFleetSync()} method. Both have their downsides, but both also do the same.
 * </p> Deprecated due to many issues that are hard to track down. Additionally, the 
 * utilized method cannot track the changes on the ship modules such as SWP Cathedral's 
 * modules. Falling back to old EveryFrameScript method instead.
 * @category Base Hull Modification 
 * @author lyravega
 */
@Deprecated
public class ehm_base_archived extends _ehm_base implements HullModFleetEffect {	
	private static final boolean log = true;
	private static final Logger logger = Logger.getLogger("lyr");
	private static ShipAPI sheep = null;

	//#region IMPLEMENTATION (HullModFleetEffect)
	@Override
	public void advanceInCampaign(CampaignFleetAPI fleet) {}

	@Override
	public boolean withAdvanceInCampaign() { return false; }

	@Override
	public boolean withOnFleetSync() { return true; }

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
		if (!isRefitTab()) return;
		if (ship != null) sheep = (fleetMemberMap.containsKey(ship.getFleetMemberId())) ? ship : null;
	}

	@Override
	public void onFleetSync(CampaignFleetAPI fleet) {
		if (!isRefitTab()) return;
		if (fleet.isPlayerFleet()) updateFleetMaps(fleet);;
		if (sheep != null) updateHullMods(sheep);
	}

	private static Map<String, Set<String>> hullModMap;
	private static Map<String, FleetMemberAPI> fleetMemberMap;

	public static void buildFleetMaps() {
		hullModMap = new HashMap<String, Set<String>>(); 
		fleetMemberMap = new HashMap<String, FleetMemberAPI>();

		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			hullModMap.put(member.getId(), new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(member.getId(), member);
		}
	}

	private static void updateFleetMaps(CampaignFleetAPI fleet) {
		Set<FleetMemberAPI> _fleetMembers = new HashSet<FleetMemberAPI>(fleet.getFleetData().getMembersListCopy());
		Set<FleetMemberAPI> _savedFleetMembers = new HashSet<FleetMemberAPI>(fleetMemberMap.values());

		if (_fleetMembers.equals(_savedFleetMembers)) return;
		String memberId;

		_savedFleetMembers.removeAll(_fleetMembers);
		for (FleetMemberAPI member : _savedFleetMembers) {
			memberId = member.getId();

			hullModMap.remove(memberId);
			fleetMemberMap.remove(memberId);
			if (log) logger.info("EHM (Experimental Hull Modifications) - FT: Unregistering ST-"+memberId);
		}

		_fleetMembers.removeAll(fleetMemberMap.values());
		for (FleetMemberAPI member : _fleetMembers) {
			memberId = member.getId();

			hullModMap.put(memberId, new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(memberId, member);
			if (log) logger.info("EHM (Experimental Hull Modifications) - FT: Registering ST-"+memberId);
		}

		if (sheep != null && !fleetMemberMap.containsKey(sheep.getFleetMemberId())) sheep = null;
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
			if (log) logger.info("EHM (Experimental Hull Modifications) - ST-"+memberId+": Removed hull modification '"+removedHullModId+"'");
		}

		_currentHullMods.removeAll(savedHullMods);
		for (String newHullModId : _currentHullMods) {
			if (savedHullMods.contains(newHullModId)) continue;

			onInstalled(newHullModId, ship);
			savedHullMods.add(newHullModId);
			if (log) logger.info("EHM (Experimental Hull Modifications) - ST-"+memberId+": New hull modification '"+newHullModId+"'");
		}
	}

	// @SuppressWarnings("unused")
	private static void onInstalled(String newHullModId, ShipAPI ship) {
		// ShipVariantAPI refitVariant = ship.getVariant();
		// ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();

		Set<String> tags = Global.getSettings().getHullModSpec(newHullModId).getTags();
		if (tags.contains(lyr_internals.tag.externalAccess)) { commitChanges(); playSound(); return; } 

		if (!tags.contains(lyr_internals.tag.allRetrofit)) return;
		String retrofitType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
		switch (retrofitType) {
			case lyr_internals.affix.adapterRetrofit: clearUndo(); playSound(); break; // 'commitChanges()' is triggered externally
			case lyr_internals.affix.systemRetrofit: commitChanges(); playSound(); break;
			case lyr_internals.affix.weaponRetrofit: commitChanges(); playSound(); break;
			case lyr_internals.affix.shieldCosmetic: commitChanges(); playSound(); break;
			case lyr_internals.affix.engineCosmetic: commitChanges(); playSound(); break;
			default: break;
		}
	}

	// @SuppressWarnings("unused")
	private static void onRemoved(String removedHullModId, ShipAPI ship) {
		ShipVariantAPI refitVariant = ship.getVariant();
		// ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();

		Set<String> tags = Global.getSettings().getHullModSpec(removedHullModId).getTags();
		if (tags.contains(lyr_internals.tag.externalAccess)) { refitVariant.setHullSpecAPI(ehm_hullSpecRefresh(refitVariant)); commitChanges(); playSound(); return; }

		if (!tags.contains(lyr_internals.tag.allRetrofit)) return;
		String retrofitType = removedHullModId.substring(0, 7); 
		switch (retrofitType) {
			case lyr_internals.affix.adapterRetrofit: refitVariant.setHullSpecAPI(ehm_adapterRemoval(refitVariant)); commitChanges(); playSound(); break;
			case lyr_internals.affix.systemRetrofit: refitVariant.setHullSpecAPI(ehm_systemRestore(refitVariant)); commitChanges(); playSound(); break;
			case lyr_internals.affix.weaponRetrofit: refitVariant.setHullSpecAPI(ehm_weaponSlotRestore(refitVariant)); commitChanges(); playSound(); break;
			case lyr_internals.affix.shieldCosmetic: refitVariant.setHullSpecAPI(ehm_restoreShield(refitVariant)); commitChanges(); playSound(); break;
			case lyr_internals.affix.engineCosmetic: refitVariant.setHullSpecAPI(ehm_restoreEngineSlots(refitVariant)); commitChanges(); playSound(); break;
			default: break;
		}
	}
	//#endregion
	// END OF TRACKING

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!ship.getVariant().hasHullMod(hullModSpecId)) {
			tooltip.addSectionHeading(lyr_tooltip.header.severeWarning, lyr_tooltip.header.severeWarning_textColour, lyr_tooltip.header.severeWarning_bgColour, Alignment.MID, lyr_tooltip.header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(lyr_tooltip.text.baseRetrofitWarning, lyr_tooltip.text.padding);

			super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		}
	}
}
