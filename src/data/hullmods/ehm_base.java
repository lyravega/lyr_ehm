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
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**
 * Serves as a requirement for all experimental hull modifications, and provides hullMod
 * tracking to the ship. 
 * </p> Depending on the {@link #trackOnSync} boolean, will either initialize hullMod 
 * tracking through {@link data.scripts.shipTrackerScript shipTrackers} or by utilizing 
 * the {@link #onFleetSync()} method. Both have their downsides, but both also do the same.
 * @category Base Hull Modification 
 * @author lyravega
 */
public class ehm_base extends _ehm_base {
	private static final Logger logger = Logger.getLogger(lyr_internals.logName);
	private static final boolean log = true;

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

		shipTrackerScript(ship).setVariant(ship.getVariant());
	}

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
			if (log) logger.info(lyr_internals.logPrefix+"xFT: Initialized fleet tracking");
			
			Global.getSector().addScript(this);
		}
	
		public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
			shipTrackers.put(memberId, shipTracker);
			if (log) logger.info(lyr_internals.logPrefix+"xFT: Keeping track of ST-"+memberId);
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {	
			if (!isRefitTab()) { if (log) logger.info(lyr_internals.logPrefix+"xFT: Stopping fleet tracking"); isDone = true; return; }
	
			if (runTime > 30f) {
				runTime = 0f;
				if (log) logger.info(lyr_internals.logPrefix+"xFT: Tracking "+shipTrackers.size()+" ships");
			} runTime += amount;
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
		public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize / a year later, I have no idea what I mean by this
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
	
			if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
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
			if (!isRefitTab()) { if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Stopping ship tracking"); isDone = true; return; }
	
			Set<String> newHullMods = new HashSet<String>();
			Set<String> removedHullMods = new HashSet<String>();
			
			for (String hullModId : variant.getHullMods()) {
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (hullMods.contains(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": New hull modification '"+hullModId+"'");
	
				newHullMods.add(hullModId);
			}
	
			for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
				// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
				if (variant.hasHullMod(hullModId)) continue;
	
				if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Removed hull modification '"+hullModId+"'");
	
				removedHullMods.add(hullModId);
			}
			
			if (!newHullMods.isEmpty()) {
				for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) {
					// ShipVariantAPI refitVariant = ship.getVariant();
					String newHullModId = i.next(); 

					Set<String> tags = Global.getSettings().getHullModSpec(newHullModId).getTags();
					if (tags.contains(lyr_internals.tag.externalAccess)) { commitChanges(); playSound(); break; } 
					if (!tags.contains(lyr_internals.tag.anyExperimental)) continue;

					String retrofitType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
					switch (retrofitType) {
						case lyr_internals.affix.adapterRetrofit: clearUndo(); playSound(); break; // 'commitChanges()' is triggered externally
						case lyr_internals.affix.systemRetrofit: commitChanges(); playSound(); break;
						case lyr_internals.affix.weaponRetrofit: commitChanges(); playSound(); break;
						case lyr_internals.affix.shieldCosmetic: commitChanges(); playSound(); break;
						case lyr_internals.affix.engineCosmetic: commitChanges(); playSound(); break;
						default: break;
					}
				} this.hullMods.addAll(newHullMods); newHullMods.clear();
			}
			
			if (!removedHullMods.isEmpty()) {
				for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) {
					ShipVariantAPI refitVariant = variant;
					String removedHullModId = i.next(); 
			
					Set<String> tags = Global.getSettings().getHullModSpec(removedHullModId).getTags();
					if (tags.contains(lyr_internals.tag.externalAccess)) { refitVariant.setHullSpecAPI(ehm_hullSpecRefresh(refitVariant)); commitChanges(); playSound(); break; }
					if (!tags.contains(lyr_internals.tag.anyExperimental)) continue;

					String retrofitType = removedHullModId.substring(0, 7); 
					switch (retrofitType) {
						case lyr_internals.affix.adapterRetrofit: refitVariant.setHullSpecAPI(ehm_adapterRemoval(refitVariant)); commitChanges(); playSound(); break;
						case lyr_internals.affix.systemRetrofit: refitVariant.setHullSpecAPI(ehm_systemRestore(refitVariant)); commitChanges(); playSound(); break;
						case lyr_internals.affix.weaponRetrofit: refitVariant.setHullSpecAPI(ehm_weaponSlotRestore(refitVariant)); commitChanges(); playSound(); break;
						case lyr_internals.affix.shieldCosmetic: refitVariant.setHullSpecAPI(ehm_restoreShield(refitVariant)); commitChanges(); playSound(); break;
						case lyr_internals.affix.engineCosmetic: refitVariant.setHullSpecAPI(ehm_restoreEngineSlots(refitVariant)); commitChanges(); playSound(); break;
						default: break;
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
