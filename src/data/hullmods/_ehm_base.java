package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.loading.specs.HullVariantSpec;
import com.fs.starfarer.loading.specs.g;

import data.hullmods.ehm_ar._ehm_ar_base;
import data.hullmods.ehm_sr._ehm_sr_base;
import data.hullmods.ehm_wr._ehm_wr_base;
import data.scripts.fleetTrackerScript;
import data.scripts.refreshRefitScript;
import data.scripts.shipTrackerScript;

/**
 * This is the master base class for all experimental hullmods. 
 * Stores common methods and most tag related strings. Other 
 * bases inherit from this one, and implement their own, more 
 * specific methods. Purpose is to minimize mistakes, and 
 * further organize the different hullmod types. Do not alter 
 * the string values, and avoid using this directly if possible. 
 * @see {@link _ehm_ar_base} for slot adapter base
 * @see {@link _ehm_sr_base} for system retrofit base
 * @see {@link _ehm_wr_base} for weapon retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_base implements HullModEffect {
	//#region IMPLEMENTATION
	protected HullModSpecAPI hullModSpec;
	protected String hullModSpecId;

	@Override 
	public void init(HullModSpecAPI hullModSpec) {	
		this.hullModSpec = hullModSpec; 
		this.hullModSpecId = hullModSpec.getId();
	}

	@Override 
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {}

	@Override 
	public String getDescriptionParam(int index, HullSize hullSize) { return null; }

	@Override 
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return getDescriptionParam(index, hullSize); }

	@Override 
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {}

	// @Override 
	// public boolean isApplicableToShip(ShipAPI ship) { return true; }

	// @Override 
	// public String getUnapplicableReason(ShipAPI ship) { return null; }

	// @Override 
	// public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return true; }

	// @Override 
	// public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; }
	
	@Override 
	public void advanceInCampaign(FleetMemberAPI member, float amount) {}
	
	@Override 
	public void advanceInCombat(ShipAPI ship, float amount) {}

	@Override 
	public boolean affectsOPCosts() { return false; }

	@Override 
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return true; }
	
	@Override 
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {}

	@Override 
	public Color getBorderColor() { return null; }

	@Override 
	public Color getNameColor() { return null; }

	@Override 
	public int getDisplaySortOrder() { return 100; }

	@Override 
	public int getDisplayCategoryIndex() { return -1; }
	//#endregion
	// END OF IMPLEMENTATION

	//#region INSTALLATION CHECKS
	@Override public boolean isApplicableToShip(ShipAPI ship) { return unapplicableReason(ship) == null; }
	
	@Override public String getUnapplicableReason(ShipAPI ship) {	return unapplicableReason(ship); }

	/**
	 * Combined {@link #isApplicableToShip()} and {@link #getUnapplicableReason()}.
	 * @return a string or null, which is also used for the if-check. 
	 */
	protected String unapplicableReason(ShipAPI ship) {	return null; } 
	
	@Override public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return cannotBeInstalledNowReason(ship, marketOrNull, mode) == null; }

	@Override public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return cannotBeInstalledNowReason(ship, marketOrNull, mode); }

	/**
	 * Combined {@link #canBeAddedOrRemovedNow()} and {@link #getCanNotBeInstalledNowReason()}.
	 * @return a string or null, which is also used for the if-check.  
	 */
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; } 
	//#endregion
	// END OF INSTALLATION CHECKS

	//#region TRACKERS
	protected shipTrackerScript shipTracker;
	protected fleetTrackerScript fleetTracker;
	private static refreshRefitScript refreshRefitScript;

	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns
	 * the {@link shipTrackerScript} that is unique to the ship. The overloads should be used for proper
	 * access. Scripts remain alive as long as the current tab is refit. The reference to the script 
	 * MUST be dropped otherwise it will keep living on in the memory.
	 * @param variant of the ship to track
	 * @param memberId of the ship to track
	 * @return a {@link shipTrackerScript} script
	 * @see Overloads: {@link #shipTrackerScript()} and {@link #shipTrackerScript()} 
	 */
	private shipTrackerScript shipTrackerScript(HullVariantSpec variant, String memberId) {
		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof shipTrackerScript) {
				shipTrackerScript temp = (shipTrackerScript) script; 
				if (!temp.getMemberId().equals(memberId)) continue;
					
				shipTracker = (shipTrackerScript) script; break; // find the correct ship script
			}
		}

		fleetTracker = fleetTrackerScript();

		if (shipTracker == null) { // if there is no script for the ship, create a new one
			shipTracker = new shipTrackerScript();
			Global.getSector().addScript(shipTracker); 
			fleetTracker.addshipTracker(memberId, shipTracker); // add the ship tracker to fleet tracker
			shipTracker.initialize(variant, memberId, fleetTracker); // set the member id 
		}

		return shipTracker;
	}

	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns
	 * the {@link shipTrackerScript} that is unique to the ship. Scripts remain alive as long as the current
	 * tab is refit. The reference to the script MUST be dropped otherwise it will keep living on in 
	 * the memory.
	 * @param stats of the ship to track
	 * @return a {@link shipTrackerScript} script
	 * @see Overload: {@link #shipTrackerScript()} 
	 */
	protected shipTrackerScript shipTrackerScript(MutableShipStatsAPI stats) {
		if (stats == null) return null; shipTracker = null; 

		HullVariantSpec variant = HullVariantSpec.class.cast(stats.getVariant()); 
		String memberId = (stats.getFleetMember() != null) ? stats.getFleetMember().getId() : null; // this can be null
		
		return (memberId != null) ? shipTrackerScript(variant, memberId) : null;
	}

	/**
	 * Creates and assigns {@link #shipTracker} and {@link #fleetTracker}, then returns
	 * the {@link shipTrackerScript} that is unique to the ship. Scripts remain alive as long as the current
	 * tab is refit. The reference to the script MUST be dropped otherwise it will keep living on in 
	 * the memory.
	 * @param ship to track
	 * @return a {@link shipTrackerScript} script
	 * @see Overload: {@link #shipTrackerScript()} 
	 */
	protected shipTrackerScript shipTrackerScript(ShipAPI ship) {
		if (ship == null) return null; shipTracker = null; 

		HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());
		String memberId = ship.getFleetMemberId(); // fleet member can be null, but this never is
		
		return shipTrackerScript(variant, memberId);
	}

	/**
	 * Creates and assigns {@link #fleetTracker}. Initially developed to keep track of 
	 * unique {@link shipTrackerScript} scripts, and kill them if the ships are no longer present on the 
	 * fleet, however after a few iterations, the scripts terminate theirselves as soon as the tab 
	 * is changed from refit to something else. In short, currently completely redundant, and used
	 * to output useless info logs on the terminal, and does nothing else. The reference MUST be
	 * dropped otherwise it will keep living on in the memory.
	 * @return a {@link fleetTrackerScript} script
	 * @see Callers: {@link #shipTrackerScript(ShipAPI)} and {@link #shipTrackerScript()} 
	 */
	private fleetTrackerScript fleetTrackerScript() {
		fleetTracker = null;

		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof fleetTrackerScript) {
				fleetTracker = (fleetTrackerScript) script; break; // find the fleet script
			}
		}

		if (fleetTracker == null) { // if there is no fleet script, create a new one
			fleetTracker = new fleetTrackerScript();
			Global.getSector().addScript(fleetTracker);
			fleetTracker.initialize(); // custom method; just some logger shit
		}

		return fleetTracker;
	}
	
	/**
	 * Creates and runs a script that refreshes the refit tab by simulating key presses. 
	 * Must be used with EXTREME CAUTION as if this is called from somewhere that is NOT
	 * going to be executed once per frame, will cause the screen to flicker.
	 * 
	 * The other similar scripts run continuously, and will trigger a refresh on certain
	 * conditions; in contrast, this one is designed to be called externally, and from
	 * a piece of code that will execute ONCE. Examples are, a hullmod removing itself, 
	 * hullmod becoming a built-in one, etc... which should already have proper checks 
	 * that prevents further executions. 
	 * 
	 * @see Trigger: {@link ehm_base} (through {@link #ehm_hullSpecClone()})
	 * @see Trigger: {@link data.hullmods.ehm_ar.ehm_ar_adapterremoval} (through {@link #ehm_hullSpecClone()})
	 * @see Trigger: {@link data.hullmods.ehm_ar.ehm_ar_stepdownadapter} (everytime adapter functions)
	 */
	protected static void refreshRefit() {
		refreshRefitScript = null;

		Set<refreshRefitScript> test = new HashSet<refreshRefitScript>();
		
		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if(script instanceof refreshRefitScript) {
				refreshRefitScript = (refreshRefitScript) script; test.add(refreshRefitScript); 
			}
		}

		if (refreshRefitScript == null) { 
			refreshRefitScript = new refreshRefitScript();
			Global.getSector().addTransientScript(refreshRefitScript);
		}
	}

	//#endregion
	// END OF TRACKERS
	
	//#region DICTIONARY
	// isn't there an easier way to do this in java? like Lua tables?
	public static enum ehm { ; 
		public static enum id { ;
			public static final String baseRetrofit = "ehm_base"; // must match hullmod id in .csv
			public static final String adapterRemoveTool = "ehm_ar_adapterremoval"; // must match hullmod id in .csv
			public static final Map<String, String> weapons = new HashMap<String, String>();
			static {
				weapons.put("ehm_ar_adaptermedium", "line"); // must match weapon id in .csv and .wpn
				weapons.put("ehm_ar_adapterlarge", "line"); // must match weapon id in .csv and .wpn
			}
		}
		public static enum tag { ;
			public static final String hullSpec = "ehm_experimental"; 
			public static final String baseRetrofit = "ehm_base"; // must match hullmod tag in .csv
			public static final String allRetrofit = "ehm"; // must match hullmod tag in .csv
			public static final String systemRetrofit = "ehm_sr"; // must match hullmod tag in .csv
			public static final String weaponRetrofit = "ehm_wr"; // must match hullmod tag in .csv
			public static final String adapterRetrofit = "ehm_ar"; // must match hullmod tag in .csv
			public static final String adapterWeapon = "ehm_adapter"; // must match weapon tag in .csv
			public static final String reqShields = "ehm_sr_require_shields"; // must match hullmod tag in .csv
			public static final String reqNoPhase = "ehm_sr_require_no_phase"; // must match hullmod tag in .csv
			public static final String reqWings = "ehm_sr_require_wings"; // must match hullmod tag in .csv
		}
		public static enum affix { ;
			public static final String adaptedSlot = "AS_"; 
			public static final String allRetrofit = "ehm_"; // must match hullmod id in .csv
			public static final String systemRetrofit = "ehm_sr_"; // must match hullmod id in .csv
			public static final String weaponRetrofit = "ehm_wr_"; // must match hullmod id in .csv
		}
		public static enum excuses { ;
			public static final String hasAnyRetrofit = "An experimental hull modification is installed. This cannot be removed as long as they are present on the hull";
			public static final String noShip = "Ship does not exist";
			public static final String lacksBase = "Requires base hull modification to allow retrofits";
			public static final String hasSystemRetrofit = "Another system retrofit is already installed";
			public static final String hasWeaponRetrofit = "Another weapon retrofit is already installed";
			public static final String hasAdapterRetrofit = "Another slot adapter is already installed";
			public static final String noShields = "Cannot function without shields";
			public static final String hasPhase = "Cannot function with a phase cloak";
			public static final String noWings = "Cannot function without wings";
			public static final String adapterActivated = "Adapter has been activated. Can be removed with the adapter removal hull mod. If this has just been activated, refit panel must be refreshed (R) to see the changes";
			public static final String noAdapterRetrofit = "There are no adapters to remove. If one has just been removed, refit panel must be refreshed (R) to see the changes";
			public static final String hasWeapons = "Cannot be installed or uninstalled as long as there are weapons present on the ship";
			
		}
	}
	//#endregion
	// END OF DICTIONARY
	
	/**
	 * Checks the ship if it has another hull modification using the passed tag
	 * @param ship to check the installed hullmods
	 * @param retrofitTag to check if the ship has one already
	 * @return true if there is another mod with the searched tag, false otherwise
	 */
	protected static final boolean ehm_hasRetrofitTag(ShipAPI ship, String retrofitTag, String ownHullModId) {
		for (String hullModId : ship.getVariant().getHullMods()) {
			if (hullModId.equals(ownHullModId)) continue;
			if (Global.getSettings().getHullModSpec(hullModId).hasTag(retrofitTag)) return true;
		}

		return false;
	}

	/**
	 * Checks the ship if it has retrofit base installed ({@link ehm_base}) installed
	 * @param ship to check 
	 * @return true if ship has it, false otherwise (duh)
	 * @see Overload: {@link #ehm_hasRetrofitBaseBuiltIn()}
	 */
	protected static final boolean ehm_hasRetrofitBaseBuiltIn(ShipAPI ship) {
		return ship.getVariant().getHullSpec().isBuiltInMod(ehm.id.baseRetrofit);
	}

	/**
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
	 * @param variant to check 
	 * @return true if ship has it, false otherwise (duh)
	 * @see Overload: {@link #ehm_hasRetrofitBaseBuiltIn()}
	 */
	protected static final boolean ehm_hasRetrofitBaseBuiltIn(ShipVariantAPI variant) {
		return variant.getHullSpec().isBuiltInMod(ehm.id.baseRetrofit);
	}

	/**
	 * Called from the retrofit base ({@link ehm_base}). If the hull does not
	 * have the mod built-in, clones the hullSpec, adds flavour, builds the 
	 * retrofit base in the hull, and refreshes the screen. Otherwise, just 
	 * returns the same hullSpec. 
	 * @param variant to be used as a template
	 * @return the same or a new hullSpec
	 */
	protected static final g ehm_hullSpecClone(HullVariantSpec variant, boolean getFresh) {
		if (!getFresh && ehm_hasRetrofitBaseBuiltIn(variant)) return variant.getHullSpec();

		g hullSpec = getFresh // TL;DR: if 'getFresh' is true, grab a stock variant hullSpec, otherwise grab current hullSpec
		? g.class.cast(Global.getSettings().getHullSpec(variant.getHullSpec().getHullId())).clone()
		: variant.getHullSpec().clone();

		hullSpec.addBuiltInMod(ehm.id.baseRetrofit);
		hullSpec.setManufacturer("Experimental"); // for color, must match .json TODO: make flavour optional
		hullSpec.setDescriptionPrefix("This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.");

		if (!getFresh) refreshRefit();
		return hullSpec;
	}
	@Deprecated // without obfuscated stuff
	protected static final ShipHullSpecAPI ehm_hullSpecClone(ShipVariantAPI variantAPI, boolean getFresh) {
		if (!getFresh && ehm_hasRetrofitBaseBuiltIn(variantAPI)) return variantAPI.getHullSpec();

		HullVariantSpec tempVariant = getFresh // TL;DR: if 'getFresh' is true, grab a stock variant hullSpec, otherwise grab current hullSpec
		? new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(Global.getSettings().getVariant(variantAPI.getHullVariantId())).getHullSpec().clone())
		: new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(variantAPI).getHullSpec().clone());

		tempVariant.getHullSpec().addBuiltInMod(ehm.id.baseRetrofit);
		tempVariant.getHullSpec().setManufacturer("Experimental"); // for color, must match .json TODO: make flavour optional
		tempVariant.getHullSpec().setDescriptionPrefix("This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.");

		if (!getFresh) refreshRefit();
		return tempVariant.getHullSpec();
	}
}