package data.hullmods;

import static data.hullmods.ehm_ar._ehm_ar_base.ehm_adapterRemoval;
import static data.hullmods.ehm_ec._ehm_ec_base.ehm_restoreEngineSlots;
import static data.hullmods.ehm_sc._ehm_sc_base.ehm_restoreShield;
import static data.hullmods.ehm_sr._ehm_sr_base.ehm_systemRestore;
import static data.hullmods.ehm_wr._ehm_wr_base.ehm_weaponSlotRestore;
import static lyr.tools._lyr_uiTools.clearUndo;
import static lyr.tools._lyr_uiTools.commitChanges;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

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

		variant.setHullSpecAPI(ehm_hullSpecClone(variant)); 
	}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship == null) return;
		
		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		if (tab == null || !tab.equals(CoreUITabId.REFIT)) return;

		if (trackOnSync) {
			sheep = ship;
		} else {
			shipTrackerScript(ship).setVariant(ship.getVariant()); // setVariant() is necessary to reflect the changes on the "refit ship"
		}
	}

	@Override
	public void onFleetSync(CampaignFleetAPI fleet) {
		if (!fleet.isPlayerFleet()) return;
		if (sheep == null) return;

		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		if (tab == null || !tab.equals(CoreUITabId.REFIT)) return;

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
		Set<FleetMemberAPI> fleetMembers = new HashSet<FleetMemberAPI>(fleet.getFleetData().getMembersListCopy());
		Set<FleetMemberAPI> savedFleetMembers = new HashSet<FleetMemberAPI>(fleetMemberMap.values());

		if (fleetMembers.equals(savedFleetMembers)) return;
		String memberId;

		savedFleetMembers.removeAll(fleetMembers);
		for (FleetMemberAPI member : savedFleetMembers) {
			memberId = member.getId();

			hullModMap.remove(memberId);
			fleetMemberMap.remove(memberId);
			if (log) logger.info("FT: Unregistering ST-"+memberId);
		}

		fleetMembers.removeAll(fleetMemberMap.values());
		for (FleetMemberAPI member : fleetMembers) {
			memberId = member.getId();

			hullModMap.put(memberId, new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(memberId, member);
			if (log) logger.info("FT: Registering ST-"+memberId);
		}

		if (!fleetMemberMap.containsKey(sheep.getFleetMemberId())) sheep = null;
	}

	private static void updateHullMods(ShipAPI ship) {
		Set<String> savedHullMods = hullModMap.get(ship.getFleetMemberId());
		Collection<String> currentHullMods = ship.getVariant().getHullMods();

		if (savedHullMods.size() == currentHullMods.size()) return;
		
		String memberId = ship.getFleetMemberId();
		Set<String> _savedHullMods = new HashSet<String>(savedHullMods);

		if (savedHullMods.size() < currentHullMods.size()) {
			for (String newHullModId : currentHullMods) {
				if (savedHullMods.contains(newHullModId)) continue;

				onInstalled(newHullModId, ship);
				savedHullMods.add(newHullModId);
				if (log) logger.info("ST-"+memberId+": New hull modification '"+newHullModId+"'");
			}
		} else /*if (savedHullMods.size() > currentHullMods.size())*/ {
			for (String removedHullModId : _savedHullMods) {
				if (currentHullMods.contains(removedHullModId)) continue;

				onRemoved(removedHullModId, ship);
				savedHullMods.remove(removedHullModId);
				if (log) logger.info("ST-"+memberId+": Removed hull modification '"+removedHullModId+"'");
			}
		}
	}

	@SuppressWarnings("unused")
	private static void onInstalled(String newHullModId, ShipAPI ship) {
		ShipVariantAPI refitVariant = ship.getVariant();
		ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();
		boolean playSound = false;
		boolean commitChanges = false; // due to the changes under the hood, refresh is necessary for pretty much everything to avoid weird issues
		boolean clearUndo = false; // either this, or 'refreshShip' should be used. 'refreshShip' has precedence if both are set to 'true'

		String hullModType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
		switch (hullModType) {
			case ehm.affix.adapterRetrofit: playSound = true; clearUndo = true; break;
			case ehm.affix.systemRetrofit: playSound = true; commitChanges = true; break;
			case ehm.affix.weaponRetrofit: playSound = true; commitChanges = true; break;
			case ehm.affix.shieldCosmetic: playSound = true; commitChanges = true; break;
			case ehm.affix.engineCosmetic: playSound = true; commitChanges = true; break;
			default: switch (newHullModId) {
				case ehm.tag.baseRetrofit: playSound = true; commitChanges = true; break;
			} break;
		}

		if (commitChanges) commitChanges();
		else if (clearUndo) clearUndo();
		if (playSound) Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
	}

	@SuppressWarnings("unused")
	private static void onRemoved(String removedHullModId, ShipAPI ship) {
		ShipVariantAPI refitVariant = ship.getVariant();
		ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();
		boolean playSound = false;
		boolean commitChanges = false;
		boolean clearUndo = false;

		String hullModType = removedHullModId.substring(0, 7); 
		switch (hullModType) {
			case ehm.affix.adapterRetrofit: refitVariant.setHullSpecAPI(ehm_adapterRemoval(refitVariant)); playSound = true; commitChanges = true; break;
			case ehm.affix.systemRetrofit: refitVariant.setHullSpecAPI(ehm_systemRestore(refitVariant)); playSound = true; commitChanges = true; break;
			case ehm.affix.weaponRetrofit: refitVariant.setHullSpecAPI(ehm_weaponSlotRestore(refitVariant)); playSound = true; commitChanges = true; break;
			case ehm.affix.shieldCosmetic: refitVariant.setHullSpecAPI(ehm_restoreShield(refitVariant)); playSound = true; commitChanges = true; break;
			case ehm.affix.engineCosmetic: refitVariant.setHullSpecAPI(ehm_restoreEngineSlots(refitVariant)); playSound = true; commitChanges = true; break;
			default: break;
		}

		if (commitChanges) commitChanges();
		else if (clearUndo) clearUndo();
		if (playSound) Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
	}
	//#endregion
	// END OF TRACKING

	//#region SCRIPTS
	private static class refreshRefitScript implements EveryFrameScript {
		private boolean isDone = false;
		private boolean playSound = false;
		private float frameCount = 0f;
		private static Robot robot;

		static {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	
		public refreshRefitScript(boolean playSound) {
			this.playSound = playSound;
			Global.getSector().addTransientScript(this);
		}
		
		@Override
		public void advance(float amount) {
			CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
			if (tab == null || !tab.equals(CoreUITabId.REFIT)) { isDone = true; return; }
	
			frameCount++;
			if (frameCount < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				if (log) logger.info("RR: Refreshed refit tab");
				if (playSound) Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
				isDone = true;
				return;
			}
		}
	
		@Override
		public boolean runWhilePaused() {
			return true;
		}
	
		@Override
		public boolean isDone() {
			return isDone;
		}
	}

	private static refreshRefitScript refreshRefitScript;
	
	protected static void refreshRefit(boolean playSound) {
		refreshRefitScript = null;
		
		for(EveryFrameScript script : Global.getSector().getTransientScripts()) {
			if(script instanceof refreshRefitScript) {
				refreshRefitScript = (refreshRefitScript) script; 
			}
		}

		if (refreshRefitScript == null) { 
			refreshRefitScript = new refreshRefitScript(playSound);
		}
	}
	//#endregion
	// END OF SCRIPTS

	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		return null; 
	}

	@Override
	protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ehm_hasRetrofitTag(ship, ehm.tag.allRetrofit, hullModSpecId)) return ehm.excuses.hasAnyRetrofit;

		return null;
	}
}
