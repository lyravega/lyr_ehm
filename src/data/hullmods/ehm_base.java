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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
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
	private static final boolean trackOnSync = false; // if false, scripts inherited from the parent will be used for tracking
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

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (ehm_hasRetrofitBaseBuiltIn(variant)) return;

		variant.setHullSpecAPI(ehm_hullSpecClone(variant)); commitChanges(); playSound();
	}

	// this only fires when a change is committed; for example, through commitChanges() (ui hack) or refreshRefit() (reloading refit screen)
	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (!isRefitTab()) return;
		if (trackOnSync) {
			sheep = (fleetMemberMap.containsKey(ship.getFleetMemberId())) ? ship : null;
		} else {
			shipTrackerScript(ship).setVariant(ship.getVariant());
		}
	}

	//#region EVENT TRACKING
	@Override
	public void onFleetSync(CampaignFleetAPI fleet) {
		if (!isRefitTab()) return;
		if (fleet.isPlayerFleet()) updateFleetMaps(fleet);;
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
	// END OF EVENT TRACKING

	//#region SCRIPT TRACKERS
	protected shipTrackerScript shipTracker;
	protected fleetTrackerScript fleetTracker;

	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns the 
	 * {@link shipTrackerScript} that is unique to the ship. The overloads should be used 
	 * for proper access. Scripts remain alive as long as the current tab is refit. The 
	 * reference to the script MUST be dropped otherwise it will keep living on in the memory.
	 * @param variant of the ship to track
	 * @param memberId of the ship to track
	 * @return a {@link shipTrackerScript}
	 */
	private shipTrackerScript shipTrackerScript(ShipVariantAPI variant, String memberId) {
		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof shipTrackerScript) {
				shipTrackerScript temp = (shipTrackerScript) script; 
				if (!temp.getMemberId().equals(memberId)) continue;
					
				shipTracker = (shipTrackerScript) script; break;
			}
		}

		fleetTracker = fleetTrackerScript();

		return (shipTracker == null) ? new shipTrackerScript(variant, memberId, fleetTracker) : shipTracker;
	}

	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns the 
	 * {@link shipTrackerScript} that is unique to the ship. Scripts remain alive as long as 
	 * the current tab is refit. The reference to the script MUST be dropped otherwise it 
	 * will keep living on in the memory.
	 * @param stats of the ship to track
	 * @return a {@link shipTrackerScript}
	 * @see Overload: {@link #shipTrackerScript(ShipAPI)} 
	 */
	protected shipTrackerScript shipTrackerScript(MutableShipStatsAPI stats) {
		if (stats == null) return null; shipTracker = null; 

		ShipVariantAPI variant = stats.getVariant(); 
		String memberId = (stats.getFleetMember() != null) ? stats.getFleetMember().getId() : null; // this can be null
		
		return (memberId != null) ? shipTrackerScript(variant, memberId) : null;
	}

	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns the 
	 * {@link shipTrackerScript} that is unique to the ship. Scripts remain alive as long as 
	 * the current tab is refit. The reference to the script MUST be dropped otherwise it 
	 * will keep living on in the memory.
	 * @param ship to track
	 * @return a {@link shipTrackerScript}
	 * @see Overload: {@link #shipTrackerScript(MutableShipStatsAPI)} 
	 */
	protected shipTrackerScript shipTrackerScript(ShipAPI ship) {
		if (ship == null) return null; shipTracker = null; 

		ShipVariantAPI variant = ship.getVariant();
		String memberId = ship.getFleetMemberId(); // fleet member can be null, but this never is
		
		return shipTrackerScript(variant, memberId);
	}

	/**
	 * Creates and assigns {@link #fleetTracker}. Initially developed to keep track of unique
	 * {@link shipTrackerScript} scripts, and kill them if the ships are no longer present on 
	 * the fleet, however after a few iterations, the scripts terminate theirselves as soon 
	 * as the tab is changed from refit to something else. Now serves as a resource pool for
	 * the shipTrackers, providing a common robot and logger for them to use. Also prints
	 * purty info messages from time to time. In other words, redundant. 
	 * The reference MUST be dropped otherwise it will keep living on in the memory.
	 * @return a {@link fleetTrackerScript} script
	 * @see Callers: {@link #shipTrackerScript(ShipVariantAPI, String)} 
	 */
	private fleetTrackerScript fleetTrackerScript() {
		fleetTracker = null;

		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof fleetTrackerScript) {
				fleetTracker = (fleetTrackerScript) script; break; // find the fleet script
			}
		}

		return (fleetTracker == null) ? new fleetTrackerScript() : fleetTracker;
	}
	//#endregion
	// END OF SCRIPT TRACKERS

	//#region INNER CLASS: fleetTrackerScript
	public class fleetTrackerScript implements EveryFrameScriptWithCleanup {
		private Map<String, shipTrackerScript> shipTrackers = new HashMap<String, shipTrackerScript>();
		// private Set<FleetMember> members = new HashSet<FleetMember>();
		// private CampaignFleet playerFleet = (CampaignFleet) Global.getSector().getPlayerFleet();
		private boolean isDone = false;
		private float runTime = 0f;
		
		//#region CONSTRUCTORS & ACCESSORS
		public fleetTrackerScript() {
			logger.info("EHM (Experimental Hull Modifications) - xFT: Initialized fleet tracking");
			
			Global.getSector().addScript(this);
		}
	
		public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
			shipTrackers.put(memberId, shipTracker);
			logger.info("EHM (Experimental Hull Modifications) - xFT: Keeping track of ST-"+memberId);
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {	
			if (!isRefitTab()) { logger.info("EHM (Experimental Hull Modifications) - xFT: Stopping fleet tracking"); isDone = true; return; }
	
			if (runTime > 10f) {
				runTime = 0f;
				logger.info("EHM (Experimental Hull Modifications) - xFT: Tracking "+shipTrackers.size()+" ships");
			} runTime += amount;

			// Set<FleetMember> newMembers = new HashSet<FleetMember>();
			// Set<FleetMember> oldMembers = new HashSet<FleetMember>();

			// for (FleetMember member : playerFleet.getMembers()) {
			// 	if (members.contains(member)) continue;

			// 	newMembers.add(member);
			// } members.addAll(newMembers);

			// for (FleetMember member : members) {
			// 	if (playerFleet.getMembers().contains(member)) continue;

			// 	oldMembers.add(member);
			// } members.removeAll(oldMembers);

			// for (FleetMember member : newMembers) {
			// 	spawnshipTracker(member);
			// } newMembers.clear();

			// for (FleetMember member : oldMembers) {
			// 	killshipTracker(member.getId());
			// } oldMembers.clear();

			// if (shipTrackers.isEmpty()) { logger.info("EHM (Experimental Hull Modifications) - FT: Stopping fleet tracking, no ship trackers remaining"); isDone = true; return; }
		}
	
		@Override
		public boolean runWhilePaused() {
			return true;
		}
	
		@Override
		public boolean isDone() {
			return isDone;
		}
	
		@Override
		public void cleanup() {
			shipTrackers.clear();
		}
	}
	//#endregion
	// END OF INNER CLASS: fleetTrackerScript

	//#region INNER CLASS: shipTrackerScript
	public class shipTrackerScript implements EveryFrameScriptWithCleanup {
		// private fleetTrackerScript fleetTracker = null;
		private ShipVariantAPI variant = null;
		private String memberId = null;
		protected Set<String> hullMods = null;
		private boolean isDone = false;
		
		//#region CONSTRUCTORS & ACCESSORS
		public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize
			this.variant = variant;
		}
		
		public shipTrackerScript(ShipVariantAPI variant, String memberId, fleetTrackerScript fleetTracker) {
			this.variant = variant;
			this.memberId = memberId;
	
			// this.fleetTracker = fleetTracker;
			fleetTracker.addshipTracker(memberId, this);
	
			this.hullMods = new HashSet<String>(variant.getHullMods());

			// for (String hullModId : variant.getHullMods()) { 
			// 	// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue; 
			// 	if (this.hullMods.contains(hullModId)) continue;
	
			// 	this.hullMods.add(hullModId);
			// }
			
			Global.getSector().addScript(this); 
	
			logger.info("EHM (Experimental Hull Modifications) - xST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
		}
	
		public String getMemberId() {
			return this.memberId;
		}
	
		public void kill() {
			this.isDone = true;
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {
			if (!isRefitTab()) { logger.info("EHM (Experimental Hull Modifications) - xST-"+memberId+": Stopping ship tracking"); isDone = true; return; }
	
			Set<String> newHullMods = new HashSet<String>();
			Set<String> removedHullMods = new HashSet<String>();
			
			for (String hullModId : variant.getHullMods()) {
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (hullMods.contains(hullModId)) continue;
	
				logger.info("EHM (Experimental Hull Modifications) - xST-"+memberId+": New hull modification '"+hullModId+"'");
	
				newHullMods.add(hullModId);
			}
	
			for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (variant.hasHullMod(hullModId)) continue;
	
				logger.info("EHM (Experimental Hull Modifications) - xST-"+memberId+": Removed hull modification '"+hullModId+"'");
	
				removedHullMods.add(hullModId);
			}
			
			if (!newHullMods.isEmpty()) {
				for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) { 
					/*
					String hullModId = i.next(); 
					String hullModType = hullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
					switch (hullModType) { // any weaponSlot changes require refresh
						// case lyr_internals.affix.adapterRetrofit: break; // handled through hullMod methods
						// case lyr_internals.affix.systemRetrofit: playSound = true; break;
						// case lyr_internals.affix.weaponRetrofit: playSound = true; refresh = true; break;
						// case lyr_internals.affix.shieldCosmetic: playSound = true; break;
						// case lyr_internals.affix.engineCosmetic: playSound = true; break;
						default: break;
					}
					*/

					// ShipVariantAPI refitVariant = ship.getVariant();
					// ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();
					String newHullModId = i.next(); 

					Set<String> tags = Global.getSettings().getHullModSpec(newHullModId).getTags();
					if (tags.contains(lyr_internals.tag.externalAccess)) { commitChanges(); playSound(); return; } 

					if (tags.contains(lyr_internals.tag.allRetrofit)) {
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
				} this.hullMods.addAll(newHullMods); newHullMods.clear();
			}
			
			if (!removedHullMods.isEmpty()) {
				for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) { 
					/*
					String hullModId = i.next(); 
					String hullModType = hullModId.substring(0, 7); 
					switch (hullModType) { // any weaponSlot changes and cheap removal methods require refresh
						// case lyr_internals.affix.adapterRetrofit: break; // handled through hullMod methods
						// case lyr_internals.affix.systemRetrofit: _ehm_sr_base.ehm_systemRestore(variant); playSound = true; break;
						// case lyr_internals.affix.weaponRetrofit: _ehm_wr_base.ehm_weaponSlotRestore(variant); playSound = true; refresh = true; break;
						// case lyr_internals.affix.shieldCosmetic: _ehm_sc_base.ehm_restoreShield(variant); playSound = true; break;
						// case lyr_internals.affix.engineCosmetic: _ehm_ec_base.ehm_restoreEngineSlots(variant); playSound = true;  refresh = true; break;
						default: break;
					}
					*/

					ShipVariantAPI refitVariant = variant;
					// ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();
					String removedHullModId = i.next(); 
			
					Set<String> tags = Global.getSettings().getHullModSpec(removedHullModId).getTags();
					if (tags.contains(lyr_internals.tag.externalAccess)) { refitVariant.setHullSpecAPI(ehm_hullSpecRefresh(refitVariant)); commitChanges(); playSound(); return; }
			
					if (tags.contains(lyr_internals.tag.allRetrofit)) {
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
				} this.hullMods.removeAll(removedHullMods); removedHullMods.clear();
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
	
		@Override
		public void cleanup() {
			this.isDone = true;
		}
	}
	//#endregion
	// END OF INNER CLASS: shipTrackerScript

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
