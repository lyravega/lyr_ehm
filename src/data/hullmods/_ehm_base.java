package data.hullmods;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.proxies.lyr_hullSpec;

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
	
	@Override 
	public void advanceInCampaign(FleetMemberAPI member, float amount) {}
	
	@Override 
	public void advanceInCombat(ShipAPI ship, float amount) {}

	@Override 
	public boolean affectsOPCosts() { return false; }

	@Override 
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return true; }

	@Override 
	public Color getBorderColor() { return null; }

	@Override 
	public Color getNameColor() { return null; }

	@Override 
	public int getDisplaySortOrder() { return 100; }

	@Override 
	public int getDisplayCategoryIndex() { return -1; }

	//#region INSTALLATION CHECKS
	@Override 
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (isApplicableToShip(ship) && canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(ehm.tooltip.header.warning, ehm.tooltip.header.warning_textColour, ehm.tooltip.header.warning_bgColour, Alignment.MID, ehm.tooltip.header.padding);
			tooltip.addPara(ehm.tooltip.text.warning, ehm.tooltip.text.padding);
		}
	}

	@Override public boolean isApplicableToShip(ShipAPI ship) { return true; }
	
	@Override public String getUnapplicableReason(ShipAPI ship) { return null; }
	
	@Override public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return true; }

	@Override public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; }
	//#endregion
	// END OF INSTALLATION CHECKS
	//#endregion
	// END OF IMPLEMENTATION

	//#region DICTIONARY
	// isn't there an easier way to do this in java? like Lua tables?
	public static enum ehm { ; 
		public static enum id { ;
			public static final String drillSound = "drill"; // must match .json
			public static final String baseRetrofit = "ehm_base"; // must match hullmod id in .csv
			public static enum adapter { ;
				public static final String mediumDual = "ehm_adapter_meduimDual"; // must match weapon id in .csv and .wpn
				public static final String largeDual = "ehm_adapter_largeDual"; // must match weapon id in .csv and .wpn
				public static final String largeTriple = "ehm_adapter_largeTriple"; // must match weapon id in .csv and .wpn
				public static final String largeQuad = "ehm_adapter_largeQuad"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(mediumDual);
					set.add(largeDual);
					set.add(largeTriple);
					set.add(largeQuad);
				}
			}
		}
		public static enum tag { ;
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
		public static enum tooltip { ;
			public static enum text { ;
				public static final String flavourManufacturer = "Experimental";  // for color, must match .json TODO: make flavour optional
				public static final String flavourDescription = "This design utilizes experimental hull modifications created by a spacer who has been living in "+
																" a junkyard for most of his life. His 'treasure hoard' is full of franken-ships that somehow fly "+
																"by using cannibalized parts from other ships that would be deemed incompatible. Benefits of such "+
																"modifications are unclear as they do not provide a certain advantage over the stock designs. "+
																"However the level of customization and flexibility they offer is certainly unparalleled.";
				public static final String warning = "Installing or removing Experimental Hull Modifications will commit the changes immediately; "+
														"the variant will be saved and any market and/or cargo transactions will be finalized";
				public static final String baseRetrofitWarning = "Will become a built-in hull modification as soon as it is installed";
				public static final float padding = 5.0f;
				public static final String noShip = "Ship does not exist";
				public static final String lacksBase = "Requires experimental hull modifications base to be installed first";
				public static final String hasSystemRetrofit = "Another system retrofit is already installed";
				public static final String hasWeaponRetrofit = "Another weapon retrofit is already installed";
				public static final String hasAdapterRetrofit = "Another slot adapter retrofit is already installed";
				public static final String hasShieldCosmetic = "Another shield cosmetic modification is already installed";
				public static final String hasEngineCosmetic = "Another engine cosmetic modification is already installed";
				public static final String noShields = "Cannot function without shields";
				public static final String hasPhase = "Cannot function with a phase cloak";
				public static final String noWings = "Cannot function without wings";
				// public static final String adapterActivated = "An adapter has been activated. Can only be removed with the adapter removal hull mod";
				public static final String noAdapterRetrofit = "There are no adapters to remove";
				public static final String hasWeapons = "Cannot be installed or uninstalled as long as there are weapons present on the ship";
				public static final String hasWeaponsOnAdaptedSlots = "Cannot be uninstalled as long as adapted slots have weapons on them";
			}
			public static enum header { ;
				public static final float padding = 15.0f;
				public static final String warning = "WARNING";
				public static final Color warning_bgColour = Color.BLACK;
				public static final Color warning_textColour = Color.YELLOW;
				public static final String severeWarning = "WARNING"; // use flash for severity
				public static final Color severeWarning_bgColour = Color.BLACK;
				public static final Color severeWarning_textColour = Color.RED;
				public static final String notApplicable = "NOT APPLICABLE";
				public static final Color notApplicable_bgColour = Color.BLACK;
				public static final Color notApplicable_textColour = Color.RED;
				public static final String locked = "LOCKED";
				public static final Color locked_bgColour = Color.BLACK;
				public static final Color locked_textColour = Color.ORANGE;
			}
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
	protected static final boolean ehm_hasWeapons(ShipAPI ship) {
		return !ship.getVariant().getNonBuiltInWeaponSlots().isEmpty();
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
	protected static final boolean ehm_hasWeapons(ShipAPI ship, String slotAffix) {
		for (String slotId : ship.getVariant().getNonBuiltInWeaponSlots()) {
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
	protected static final boolean ehm_hasWeapons(ShipAPI ship, Set<String> weaponIdsToIgnore) {
		ShipVariantAPI variant = ship.getVariant();

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
	 * <p> Re-adds itself if the hullSpec is replaced with something else.
	 * @param variant to be used as a template
	 * @return a cloned hullSpec
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecClone(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), true);

		hullSpec.addBuiltInMod(ehm.id.baseRetrofit);
		hullSpec.setManufacturer(ehm.tooltip.text.flavourManufacturer);
		hullSpec.setDescriptionPrefix(ehm.tooltip.text.flavourDescription);

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
	protected static final ShipHullSpecAPI ehm_hullSpecRefresh(ShipVariantAPI variant) {
		lyr_hullSpec stockHullSpec = new lyr_hullSpec(Global.getSettings().getHullSpec(variant.getHullSpec().getHullId()), true);

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
		stockHullSpec.setManufacturer(ehm.tooltip.text.flavourManufacturer);
		stockHullSpec.setDescriptionPrefix(ehm.tooltip.text.flavourDescription);
		
		return stockHullSpec.retrieve();
	}

	/**
	 * Simply returns a stock hullSpec, with no changes or whatsoever. Should ONLY
	 * be used as a reference.
	 * @param variant to be used as a template
	 * @return a stock hullSpec from the SpecStore
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecReference(ShipVariantAPI variant) {
		return Global.getSettings().getHullSpec(variant.getHullSpec().getHullId());
	}
}