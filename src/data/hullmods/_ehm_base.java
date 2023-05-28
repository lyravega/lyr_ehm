package data.hullmods;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.ehm.ehm_base;
import lyr.misc.lyr_externals;
import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;
import lyr.proxies.lyr_hullSpec;

/**
 * This is the master base class for all experimental hullmods. Stores the most 
 * common methods and strings for global access, which are used by other bases, 
 * and hullMods. 
 * <p> The other bases implement their own, more specific methods for the hullMods 
 * that use them as their parent, and it usually is the place where the hullSpec
 * changes occur. 
 * <p> The usable hullMods themselves only contain extremely specific things like 
 * the values to be passed, and custom {@code ehm_cannotBeInstalledNowReason(...)} 
 * and/or {@code ehm_unapplicableReason(...))} if necessary. 
 * <p> Primary reason for doing it this way is to provide better maintenance for 
 * different categories at the cost of a few extra calls to get to where the 
 * action is.
 * <p> Do NOT alter the string values (if there are any), and avoid using this 
 * directly if possible. 
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_base extends BaseHullMod {
	protected static final Logger logger = Logger.getLogger(lyr_internals.logName);
	protected static final boolean log = true;
	public static final SettingsAPI settings = Global.getSettings();

	protected HullModSpecAPI hullModSpec;
	protected String hullModSpecId;

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		this.hullModSpec = hullModSpec;
		this.hullModSpecId = hullModSpec.getId();
	}

	//#region IMPLEMENTATION
	@Override 
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {}

	@Override 
	public String getDescriptionParam(int index, HullSize hullSize) { return null; }

	@Override 
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return getDescriptionParam(index, hullSize); }

	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize) { return null; }
	
	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return getSModDescriptionParam(index, hullSize); }

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

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override 
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (ship.getVariant().getSMods().contains(this.hullModSpecId)) return;

		if (isApplicableToShip(ship) && canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(lyr_tooltip.header.warning, lyr_tooltip.header.warning_textColour, lyr_tooltip.header.warning_bgColour, Alignment.MID, lyr_tooltip.header.padding);
			tooltip.addPara(lyr_tooltip.text.warning, lyr_tooltip.text.padding);
		}
	}

	@Override public boolean isApplicableToShip(ShipAPI ship) { return true; }
	
	@Override public String getUnapplicableReason(ShipAPI ship) { return null; }
	
	@Override public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return true; }

	@Override public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; }
	//#endregion
	// END OF INSTALLATION CHECKS / DESCRIPTION
	//#endregion
	// END OF IMPLEMENTATION

	//#region CHECK HELPERS
	/**
	 * Checks the ship if it has another hull modification using the passed tag
	 * @param ship to check the installed hullmods
	 * @param retrofitTag to check if the ship has one already
	 * @return true if there is another mod with the searched tag, false otherwise
	 */
	protected static final boolean ehm_hasRetrofitTag(ShipAPI ship, String retrofitTag, String thisHullModId) {
		for (String hullModId : ship.getVariant().getHullMods()) {
			if (hullModId.equals(thisHullModId)) continue;
			if (settings.getHullModSpec(hullModId).hasTag(retrofitTag)) return true;
		}

		return false;
	}

	/**
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
	 * @param variant to check 
	 * @return true if ship has it, false otherwise (duh)
	 */
	protected static final boolean ehm_hasRetrofitBaseBuiltIn(ShipVariantAPI variant) {
		return variant.getHullSpec().isBuiltInMod(lyr_internals.id.baseModification);
	}

	/**
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
	 * @param variant to check 
	 * @return true if ship has it, false otherwise (duh)
	 */
	protected static final boolean ehm_hasExperimentalSMod(ShipVariantAPI variant) {
		for (String hullModId: variant.getSMods()) {
			if (settings.getHullModSpec(hullModId).hasTag(lyr_internals.tag.experimental)) return true;
		}
		return false;
	}

	/**
	 * Checks the ship if it has retrofit base ({@link ehm_base}) installed
	 * @param variant to check 
	 * @return true if ship has it, false otherwise (duh)
	 */
	protected static final boolean ehm_hasExperimentalModWithTag(ShipVariantAPI variant, String tag) {
		for (String hullModId: variant.getHullMods()) {
			if (settings.getHullModSpec(hullModId).hasTag(tag)) return true;
		}
		return false;
	}

	/**
	 * Checks if the ship has any weapons installed. Decorative slots are ignored 
	 * through {@code getNonBuiltInWeaponSlots()}, and as such the activated adapters
	 * are ignored as the adapters turn the slots into decorative ones after 
	 * activation. Evolved into its overloads for more specific checks.
	 * @param ship to check
	 * @return true if the ship has weapons on weapon slots
	 */
	protected static final boolean ehm_hasWeapons(ShipAPI ship) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();

			if (slot.isBuiltIn() || slot.isSystemSlot()) continue;
			return true;
		}

		return false;
	}

	/**
	 * Checks if the ship has any weapons installed on slots with specific slot ids
	 * that start with the passed slotAffix.
	 * <p> Example: If the slotAffix is "AS", then only the slots with the slot ids
	 * starting with "AS" are considered, and the rest are ignored.
	 * @param ship to check
	 * @param slotAffix for checking the slotId
	 * @return true if the ship has weapons on specific slots
	 */
	protected static final boolean ehm_hasWeapons(ShipAPI ship, String slotAffix) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();

			if (slot.isBuiltIn() || slot.isSystemSlot()) continue;
			if (!slot.getId().startsWith(slotAffix)) continue;
			return true;
		}

		return false;
	}

	/**
	 * Checks if the ship has any weapons with ids that do not match the weapon ids
	 * contained in the passed ignore set.
	 * <p> Example: If the passed set contains a weapon id like "adapter", then
	 * any slot having this weapon installed on them are ignored.
	 * @param ship to check
	 * @param weaponIdsToIgnore while checking the weapon slots
	 * @return true if the ship has weapons with non-matching weapon ids
	 */
	protected static final boolean ehm_hasWeapons(ShipAPI ship, Set<String> weaponIdsToIgnore) {
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			WeaponSlotAPI slot = weapon.getSlot();

			if (slot.isBuiltIn() || slot.isSystemSlot()) continue;
			if (weaponIdsToIgnore.contains(weapon.getId())) continue;
			return true;
		}

		return false;
	}
	//#endregion
	// END OF CHECK HELPERS

	/** 
	 * Activated shunts (decorative, built-in ones) are added to the weapon
	 * groups by the game in some cases, like when the hullSpec is replaced.
	 * <p>This method goes over the groups and removes them. Not sure when
	 * / why / how this happens. This is a sufficient workaround till the
	 * root cause can be found, however.
	 * @param variant whose weapon groups will be purged of activated stuff
	 */
	protected static final void ehm_cleanWeaponGroupsUp(ShipVariantAPI variant) {
		List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
		Map<String, String> groupCleanupTargets = new HashMap<String, String>(variant.getHullSpec().getBuiltInWeapons());
		groupCleanupTargets.values().retainAll(lyr_internals.id.shunts.set);
		for (WeaponGroupSpec weaponGroup: weaponGroups) {
			weaponGroup.getSlots().removeAll(groupCleanupTargets.keySet());
		}
	}

	/**
	 * Called from the {@link ehm_base retrofit base} only. If the hull does not have the base built-in, clones
	 * the hullSpec, adds flavour, builds the retrofit base in the hull, and refreshes the screen. Otherwise,
	 * just returns the same hullSpec. Re-adds itself if the hullSpec is replaced with something else.
	 * <p> There are two problems with replacing the hull specs. From what I can tell, game replaces the hull
	 * specs of the ships after a combat, and after repairs. The latter is handled through the vanilla script
	 * {@link com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript#restoreToNonDHull FieldRepairsScript}
	 * which gets active if the player has the hull restoration skill. The former happens when a ship suffers
	 * damage, through {@link com.fs.starfarer.api.impl.campaign.DModManager#setDHull DModManager}.
	 * <p> Both of these linked methods have certain conditions, and check for things like if the hull spec in
	 * question is a d-hull, or a default d-hull, or has a base hull, etc... there are no easy ways to avoid
	 * those checks. The main problem is, these checked hull specs aren't fields; they're grabbed from the
	 * script store directly, and only the hull spec id's are stored.
	 * <p> As a workaround, the script is replaced with a custom one that ignores any hull spec that has the
	 * base variant installed on them. However, avoiding the other one requires an ugly hack; applying a
	 * damaged hull spec instead of anything else so that the check returns false and the hull spec stays
	 * the same.
	 * <p> Also, as a necessary evil, some variant hull specs that can be restored to a base hull spec needs
	 * to be restored immediately or they'll stay as they are with double (D) markers. Replacing them with
	 * their base skin right away fucks their d-mods up so they're added to the variant first. So this specific
	 * swap only affects them visually, and nothing more.
	 * <p> This is certainly not ideal. If hull specs for the damaged & base versions were stored in fields
	 * instead of just their hull ids, the solution would've been easier; clone and adjust up to three hull
	 * specs. But as long as they refer to the spec store, this workaround has to be in place. This has been
	 * the case forever. Maybe Alex will add them as a field someday, after the update winds are calmed down.
	 * @param variant to be used as a template
	 * @return a cloned hullSpec
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecClone(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpecToClone = variant.getHullSpec();
		lyr_hullSpec hullSpec;

		if (hullSpecToClone.isRestoreToBase() && hullSpecToClone.getBaseHullId() != null ) {
			for (String hullModId : hullSpecToClone.getBuiltInMods()) {
				if (!settings.getHullModSpec(hullModId).hasTag(Tags.HULLMOD_DMOD)) continue;

				variant.removeSuppressedMod(hullModId);
				variant.addPermaMod(hullModId, false);
			}
			hullSpec = new lyr_hullSpec(settings.getHullSpec(Misc.getDHullId(hullSpecToClone.getBaseHull())), true);
		} else {
			hullSpec = new lyr_hullSpec(settings.getHullSpec(Misc.getDHullId(hullSpecToClone)), true);
		}

		// if ((variant.isDHull() || hullSpecToClone.isDHull()) && hullSpecToClone.getDParentHullId() != null) {
		// 	hullSpecToClone = hullSpecToClone.getDParentHull();
		// }

		// lyr_hullSpec hullSpec = new lyr_hullSpec(hullSpecToClone, true);

		hullSpec.addBuiltInMod(lyr_internals.id.baseModification);
		// hullSpec.setDParentHullId(null);
		// hullSpec.setBaseHullId(null);
		// hullSpec.setRestoreToBase(false);
		if (lyr_externals.showExperimentalFlavour) {
			hullSpec.setManufacturer(lyr_tooltip.text.flavourManufacturer);
			hullSpec.setDescriptionPrefix(lyr_tooltip.text.flavourDescription);
		}

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
		lyr_hullSpec stockHullSpec = new lyr_hullSpec(settings.getHullSpec(variant.getHullSpec().getHullId()), true);

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
		if (lyr_externals.showExperimentalFlavour) {
			stockHullSpec.setManufacturer(lyr_tooltip.text.flavourManufacturer);
			stockHullSpec.setDescriptionPrefix(lyr_tooltip.text.flavourDescription);
		}
		
		return stockHullSpec.retrieve();
	}

	/**
	 * Simply returns a stock hullSpec, with no changes or whatsoever. Should ONLY
	 * be used as a reference, or when the base is removed.
	 * @param variant to be used as a template
	 * @return a stock hullSpec from the SpecStore
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecReference(ShipVariantAPI variant) {
		return settings.getHullSpec(variant.getHullSpec().getHullId());
	}
}