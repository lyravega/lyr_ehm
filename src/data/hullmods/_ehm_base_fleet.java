package data.hullmods;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import org.apache.log4j.Logger;

import data.hullmods.ehm_ec._ehm_ec_base;
import data.hullmods.ehm_sc._ehm_sc_base;
import data.hullmods.ehm_sr._ehm_sr_base;
import data.hullmods.ehm_wr._ehm_wr_base;
import data.scripts.refreshRefitScript;
import lyr.lyr_hullSpec;

/**
 * This is the master base class for all experimental hullmods. Stores the most 
 * common methods and strings for global access to some degree, which are used by
 * other bases, and hullMods. 
 * <p> The other bases implement their own, more specific methods for the hullMods 
 * that use them as their parent, and it usually is the place where the hullSpec
 * changes occur.
 * <p> The usable hullMods themselves only contain extremely specific things like 
 * the values to be passed, and custom {@code ehm_cannotBeInstalledNowReason(...)} 
 * and/or {@code ehm_unapplicableReason(...))} if necessary. It is here that the 
 * variants swap their hullSpecs.
 * <p> Primary reason for doing it this way is to provide better maintenance for 
 * different categories at the cost of a few extra calls to get to where the 
 * action is.
 * <p> Do NOT alter the string values, and avoid using this directly if possible. 
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 * @version 0.7
 * @since 0.3
 */
public class _ehm_base_fleet implements HullModEffect, HullModFleetEffect {
	protected HullModSpecAPI hullModSpec;
	protected String hullModSpecId;

	@Override 
	public void init(HullModSpecAPI hullModSpec) {	
		this.hullModSpec = hullModSpec; 
		this.hullModSpecId = hullModSpec.getId();
	}

	//#region IMPLEMENTATION (HullModEffect)
	@Override 
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {}

	// @Override 
	// public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {}

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

	//#region INSTALLATION CHECKS
	@Override public boolean isApplicableToShip(ShipAPI ship) { return ehm_unapplicableReason(ship) == null; }
	
	@Override public String getUnapplicableReason(ShipAPI ship) {	return ehm_unapplicableReason(ship); }

	/**
	 * Combined {@link #isApplicableToShip()} and {@link #getUnapplicableReason()}.
	 * @return a string or null, which is also used for the if-check. 
	 */
	protected String ehm_unapplicableReason(ShipAPI ship) {	return null; } 
	
	@Override public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return ehm_cannotBeInstalledNowReason(ship, marketOrNull, mode) == null; }

	@Override public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return ehm_cannotBeInstalledNowReason(ship, marketOrNull, mode); }

	/**
	 * Combined {@link #canBeAddedOrRemovedNow()} and {@link #getCanNotBeInstalledNowReason()}.
	 * @return a string or null, which is also used for the if-check.  
	 */
	protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; } 
	//#endregion
	// END OF INSTALLATION CHECKS
	//#endregion
	// END OF IMPLEMENTATION (HullModEffect)

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

	//#region TRACKERS
	private static refreshRefitScript refreshRefitScript;
	
	/**
	 * Creates and runs a script that refreshes the refit tab by simulating key presses. 
	 * Must be used with EXTREME CAUTION as if this is called from somewhere that is NOT
	 * going to be executed once per frame, will cause the screen to flicker.
	 * <p>
	 * The other similar scripts run continuously, and will trigger a refresh on certain
	 * conditions; in contrast, this one is designed to be called externally, and from
	 * a piece of code that will execute ONCE. Examples are, a hullmod removing itself, 
	 * hullmod becoming a built-in one, etc... which should already have proper checks 
	 * that prevents further executions. 
	 * 
	 * @see Trigger: {@link ehm_base} (through {@link #ehm_hullSpecClone()})
	 * @see Trigger: {@link data.hullmods.ehm_ar.ehm_ar_adapterremoval Adapter Removal} (through {@link #ehm_hullSpecRefresh()})
	 * @see Trigger: {@link data.hullmods.ehm_ar.ehm_ar_stepdownadapter Step-Down Adapter} (everytime adapter functions)
	 */
	protected static void refreshRefit() {
		refreshRefitScript = null;
		
		for(EveryFrameScript script : Global.getSector().getTransientScripts()) {
			if(script instanceof refreshRefitScript) {
				refreshRefitScript = (refreshRefitScript) script; 
			}
		}

		if (refreshRefitScript == null) { 
			refreshRefitScript = new refreshRefitScript();
		}
	}

	private static ShipAPI sheep = null;

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship == null) return;
		sheep = ship;
	}

	@Override
	public void onFleetSync(CampaignFleetAPI fleet) {
		if (!fleet.isPlayerFleet()) return;
		if (sheep == null) return;

		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		if (tab == null || !tab.equals(CoreUITabId.REFIT)) return;

		updateFleetMaps();
		if (sheep != null) updateHullMods(sheep);
	}

	private static Map<String, Set<String>> hullModMap;
	private static Map<String, FleetMemberAPI> fleetMemberMap;

	public static void buildHullModMap() {	
		hullModMap = new HashMap<String, Set<String>>(); 
		fleetMemberMap = new HashMap<String, FleetMemberAPI>();

		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			hullModMap.put(member.getId(), new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(member.getId(), member);
		}
	}

	private static void updateFleetMaps() {
		List<FleetMemberAPI> fleetMembers = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
		String memberId;

		for (FleetMemberAPI member : fleetMemberMap.values()) {
			if (fleetMembers.contains(member)) continue;

			memberId = member.getId();
			hullModMap.remove(memberId);
			Global.getLogger(_ehm_test.class).info(memberId+" removed");
		} fleetMemberMap.keySet().retainAll(hullModMap.keySet());

		for (FleetMemberAPI member : fleetMembers) { 
			if (fleetMemberMap.values().contains(member)) continue;

			memberId = member.getId();
			hullModMap.put(memberId, new HashSet<String>(member.getVariant().getHullMods()));
			fleetMemberMap.put(memberId, member);
			Global.getLogger(_ehm_test.class).info(memberId+" added");
		}
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
			}
		} else /*if (savedHullMods.size() > currentHullMods.size())*/ {
			for (String removedHullModId : _savedHullMods) {
				if (currentHullMods.contains(removedHullModId)) continue;

				onRemoved(removedHullModId, ship);
				savedHullMods.remove(removedHullModId);
			}
		}
	}

	private static void onInstalled(String newHullModId, ShipAPI ship) {
		// ShipVariantAPI refitVariant = ship.getVariant();
		// ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();
		boolean playSound = false;
		boolean refresh = false;
		boolean log = true;

		String hullModType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
		switch (hullModType) { // any weaponSlot changes require refresh
			case ehm.affix.adapterRetrofit: break; // handled through hullMod methods
			case ehm.affix.systemRetrofit: playSound = true; break;
			case ehm.affix.weaponRetrofit: playSound = true; refresh = true; break;
			case ehm.affix.shieldCosmetic: playSound = true; break;
			case ehm.affix.engineCosmetic: playSound = true; break;
			default: playSound = true; break;
		}
		
		if (log) Logger.getLogger("lyr").info("ST-"+ship.getFleetMemberId()+": New hull modification '"+newHullModId+"'");
		if (playSound) Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
		if (refresh) refreshRefit();
	}

	private static void onRemoved(String removedHullModId, ShipAPI ship) {
		ShipVariantAPI refitVariant = ship.getVariant();
		// ShipVariantAPI realVariant = fleetMemberMap.get(ship.getFleetMemberId()).getVariant();
		boolean playSound = false;
		boolean refresh = false;
		boolean log = true;

		String hullModType = removedHullModId.substring(0, 7); 
		switch (hullModType) { // any weaponSlot changes and cheap removal methods require refresh
			case ehm.affix.adapterRetrofit: break; // handled through hullMod methods
			case ehm.affix.systemRetrofit: _ehm_sr_base.ehm_systemRestore(refitVariant); playSound = true; break;
			case ehm.affix.weaponRetrofit: _ehm_wr_base.ehm_weaponSlotRestore(refitVariant); playSound = true; refresh = true; break;
			case ehm.affix.shieldCosmetic: _ehm_sc_base.ehm_restoreShield(refitVariant); playSound = true; break;
			case ehm.affix.engineCosmetic: _ehm_ec_base.ehm_restoreEngineSlots(refitVariant); playSound = true; refresh = true; break;
			default: playSound = true; break;
		}

		if (log) Logger.getLogger("lyr").info("ST-"+ship.getFleetMemberId()+": Removed hull modification '"+removedHullModId+"'");
		if (playSound) Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
		if (refresh) refreshRefit();
	}
	//#endregion
	// END OF TRACKERS
	
	//#region DICTIONARY
	// isn't there an easier way to do this in java? like Lua tables?
	public static enum ehm { ; 
		public static enum id { ;
			public static final String baseRetrofit = "ehm_base"; // must match hullmod id in .csv
			public static final String adapterRemoveTool = "ehm_ar_adapterremoval"; // must match hullmod id in .csv
			public static final Map<String, String> adapters = new HashMap<String, String>();
			static {
				adapters.put("ehm_ar_adaptermedium", "line"); // must match weapon id in .csv and .wpn
				adapters.put("ehm_ar_adapterlarge", "line"); // must match weapon id in .csv and .wpn
			}
		}
		public static enum tag { ;
			public static final String hullSpec = "ehm_experimental"; 
			public static final String baseRetrofit = "ehm_base"; // must match hullmod tag in .csv
			public static final String allRetrofit = "ehm"; // must match hullmod tag in .csv
			public static final String systemRetrofit = "ehm_sr"; // must match hullmod tag in .csv
			public static final String weaponRetrofit = "ehm_wr"; // must match hullmod tag in .csv
			public static final String adapterRetrofit = "ehm_ar"; // must match hullmod tag in .csv
			public static final String shieldCosmetic = "ehm_sc"; // must match hullmod tag in .csv
			public static final String engineCosmetic = "ehm_ec"; // must match hullmod tag in .csv
			public static final String adapterWeapon = "ehm_adapter"; // must match weapon tag in .csv
			public static final String reqShields = "ehm_sr_require_shields"; // must match hullmod tag in .csv
			public static final String reqNoPhase = "ehm_sr_require_no_phase"; // must match hullmod tag in .csv
			public static final String reqWings = "ehm_sr_require_wings"; // must match hullmod tag in .csv
		}
		public static enum affix { ;
			public static final String adaptedSlot = "AS_"; // should NOT be altered in any update
			public static final String allRetrofit = "ehm_"; // must match hullmod id in .csv
			public static final String systemRetrofit = "ehm_sr_"; // must match hullmod id in .csv
			public static final String weaponRetrofit = "ehm_wr_"; // must match hullmod id in .csv
			public static final String adapterRetrofit = "ehm_ar_"; // must match hullmod id in .csv
			public static final String shieldCosmetic = "ehm_sc_"; // must match hullmod id in .csv
			public static final String engineCosmetic = "ehm_ec_"; // must match hullmod id in .csv
		}
		public static enum excuses { ;
			public static final String hasAnyRetrofit = "An experimental hull modification is installed. This cannot be removed as long as they are present on the hull"; // never shown as it becomes built-in
			public static final String noShip = "Ship does not exist";
			public static final String lacksBase = "Requires experimental hull modifications base to be installed first";
			public static final String hasSystemRetrofit = "Another system retrofit is already installed";
			public static final String hasWeaponRetrofit = "Another weapon retrofit is already installed";
			public static final String hasAdapterRetrofit = "Another slot adapter is already installed";
			public static final String hasShieldCosmetic = "Another shield cosmetic modification is already installed";
			public static final String hasEngineCosmetic = "Another engine cosmetic modification is already installed";
			public static final String noShields = "Cannot function without shields";
			public static final String hasPhase = "Cannot function with a phase cloak";
			public static final String noWings = "Cannot function without wings";
			public static final String adapterActivated = "An adapter has been activated. Can only be removed with the adapter removal hull mod";
			public static final String noAdapterRetrofit = "There are no adapters to remove";
			public static final String hasWeapons = "Cannot be installed or uninstalled as long as there are weapons present on the ship";
			public static final String hasWeaponsOnAdaptedSlots = "Cannot be used as long as adapted slots have weapons on them";
			
		}
	}
	//#endregion
	// END OF DICTIONARY
	
	//#region CHECK HELPERS
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
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
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
	 * Checks if the ship has any weapons installed. Decorative slots are ignored 
	 * through {@code getNonBuiltInWeaponSlots()}, and as such the activated adapters
	 * are ignored as the adapters turn the slots into decorative ones after 
	 * activation. Evolved into its overloads for more specific checks.
	 * @param variant to check
	 * @return true if the ship has weapons on weapon slots
	 */
	protected static final boolean ehm_hasWeapons(ShipVariantAPI variant) {
		return !variant.getNonBuiltInWeaponSlots().isEmpty();
	}

	/**
	 * Checks if the ship has any weapons installed on slots with specific slot ids
	 * that start with the passed slotAffix.
	 * <p> Example: If the slotAffix is "AS", then only the slots with the slot ids
	 * starting with "AS" are considered, and the rest are ignored.
	 * @param variant to check
	 * @param slotAffix for checking the slotId
	 * @return true if the ship has weapons on specific slots
	 */
	protected static final boolean ehm_hasWeapons(ShipVariantAPI variant, String slotAffix) {
		for (String slotId : variant.getNonBuiltInWeaponSlots()) {
			if (slotId.startsWith(slotAffix)) return true; break;
		}
		
		return false;
	}

	/**
	 * Checks if the ship has any weapons with ids that do not match the weapon ids
	 * contained in the passed ignore set.
	 * <p> Example: If the passed set contains a weapon id like "adapter", then
	 * any slot having this weapon installed on them are ignored.
	 * @param variant to check
	 * @param weaponIdsToIgnore while checking the weapon slots
	 * @return true if the ship has weapons with non-matching weapon ids
	 */
	protected static final boolean ehm_hasWeapons(ShipVariantAPI variant, Set<String> weaponIdsToIgnore) {
		for (String slotId : variant.getNonBuiltInWeaponSlots()) {
			if (weaponIdsToIgnore.contains(variant.getWeaponId(slotId))) continue; return true;
		}
		
		return false;
	}
	//#endregion
	// END OF CHECK HELPERS

	/**
	 * Called from the {@link ehm_base retrofit base} only. If the hull does not
	 * have the base built-in, clones the hullSpec, adds flavour, builds the retrofit 
	 * base in the hull, and refreshes the screen. Otherwise, just returns the same 
	 * hullSpec.  
	 * @param variant to be used as a template
	 * @return a cloned hullSpec
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecClone(ShipVariantAPI variant) {
		if (ehm_hasRetrofitBaseBuiltIn(variant)) return variant.getHullSpec();

		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), true);

		hullSpec.addBuiltInMod(ehm.id.baseRetrofit);
		hullSpec.setManufacturer("Experimental"); // for color, must match .json TODO: make flavour optional
		hullSpec.setDescriptionPrefix("This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.");

		refreshRefit();
		return hullSpec.retrieve();
	}

	/**
	 * Similar to clone in how it does things internally. Grabs a stock hullSpec from
	 * the SpecStore, which is used for comparison / restoration purposes.
	 * <p> The returned hullSpec will have any tags and built-in stuff that the current 
	 * hullSpec has, however they should be standard. The reason for trying to retain
	 * such additions is, in some restoration cases, the returned hullSpec is simply
	 * applied to the variant, whereas a step-by-step restoration should be preferred.
	 * <p> As no other mods does things this way as far as I know, at the very least 
	 * the aforementioned things will be preserved. But to be honest, I should expand 
	 * the restoration methods instead of simply applying the returned hullSpec.
	 * @param variant to be used as a template
	 * @return a 'fresh' hullSpec from the SpecStore
	 */
	// TODO expand restoration methods to avoid applying the hullSpec
	protected static final ShipHullSpecAPI ehm_hullSpecRefresh(ShipVariantAPI variant) {
		lyr_hullSpec stockHullSpec = new lyr_hullSpec(Global.getSettings().getVariant(variant.getHullVariantId()).getHullSpec(), true);

		ShipHullSpecAPI hullSpec = variant.getHullSpec();
		ShipHullSpecAPI stockHullSpecAPI = stockHullSpec.retrieve();

		for (String hullSpecTag : hullSpec.getTags()) // this is a set, so there cannot be any duplicates, but still
		if (!stockHullSpecAPI.getTags().contains(hullSpecTag))
		stockHullSpecAPI.addTag(hullSpecTag);

		for (String builtInHullModSpecId : hullSpec.getBuiltInMods()) // this is a list, there can be duplicates so check first
		if (!stockHullSpecAPI.getBuiltInMods().contains(builtInHullModSpecId))
		stockHullSpecAPI.addBuiltInMod(builtInHullModSpecId);

		// for (String builtInWeaponSlot : hullSpec.getBuiltInWeapons().keySet()) // this is a map; slotId, weaponSpecId
		// if (!stockHullSpecAPI.getBuiltInWeapons().keySet().contains(builtInWeaponSlot))
		// stockHullSpecAPI.addBuiltInWeapon(builtInWeaponSlot, hullSpec.getBuiltInWeapons().get(builtInWeaponSlot));

		// for (String builtInWing : hullSpec.getBuiltInWings()) // this is a list, there can be duplicates so check first
		// if (!stockHullSpecAPI.getBuiltInWings().contains(builtInWing))
		// stockHullSpec.addBuiltInWing(builtInWing);

		// hullSpec.addBuiltInMod(ehm.id.baseRetrofit);
		stockHullSpec.setManufacturer("Experimental"); 
		stockHullSpec.setDescriptionPrefix("This design utilizes experimental hull modifications created by a spacer who has been living in a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such modifications are unclear as they do not provide a certain advantage over the stock designs. However the level of customization and flexibility they offer is certainly unparalleled.");
		
		return stockHullSpec.retrieve();
	}

	/**
	 * Simply returns a stock hullSpec, with no changes or whatsoever. Should ONLY
	 * be used as a reference.
	 * @param variant to be used as a template
	 * @return a stock hullSpec from the SpecStore
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecReference(ShipVariantAPI variant) {
		// return Global.getSettings().getHullSpec(variant.getHullSpec().getHullId());
		return Global.getSettings().getVariant(variant.getHullVariantId()).getHullSpec();
	}
}