package experimentalHullModifications.hullmods.ehm;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.lyr_internals;
import experimentalHullModifications.misc.lyr_tooltip.header;
import experimentalHullModifications.misc.lyr_tooltip.text;
import experimentalHullModifications.plugin.lyr_settings;
import lyravega.proxies.lyr_hullSpec;

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
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_base implements HullModEffect {
	protected HullModSpecAPI hullModSpec;
	protected String hullModSpecId;
	protected Set<String> hullModSpecTags;

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		this.hullModSpec = hullModSpec;
		this.hullModSpecId = hullModSpec.getId();
		this.hullModSpecTags = hullModSpec.getTags();
	}

	//#region IMPLEMENTATION
	@Override public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {}

	@Override public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {}

	@Override public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String hullModSpecId) {}
	
	@Override public void advanceInCampaign(FleetMemberAPI member, float amount) {}
	
	@Override public void advanceInCombat(ShipAPI ship, float amount) {}

	@Override public boolean affectsOPCosts() { return false; }

	@Override public int getDisplaySortOrder() { return 100; }

	@Override public int getDisplayCategoryIndex() { return -1; }

	@Override public boolean hasSModEffect() { return false; }

	@Override public boolean isSModEffectAPenalty() { return false; }

	@Override public boolean showInRefitScreenModPickerFor(ShipAPI ship) { return Global.getCurrentState() != GameState.TITLE; }	// hide stuff in missions

	//#region TOOLTIP
	@Override public Color getBorderColor() { return null; }

	@Override public Color getNameColor() { return null; }

	@Override public float getTooltipWidth() { return 369f; }

	@Override public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return true; }

	@Override 
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (ship.getVariant().getSMods().contains(this.hullModSpecId)) return;

		if (isApplicableToShip(ship) && canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(header.warning, header.warning_textColour, header.warning_bgColour, Alignment.MID, header.padding);
			tooltip.addPara(text.warning[0], text.padding).setHighlight(text.warning[1]);
		}
	}

	@Override public String getDescriptionParam(int index, HullSize hullSize) { return null; }

	@Override public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return getDescriptionParam(index, hullSize); }

	@Override public boolean hasSModEffectSection(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return false; }

	@Override public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {}

	@Override public void addSModEffectSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {}

	@Override public String getSModDescriptionParam(int index, HullSize hullSize) { return null; }
	
	@Override public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return getSModDescriptionParam(index, hullSize); }
	//#endregion
	// END OF TOOLTIP

	//#region CHECKS
	@Override public boolean isApplicableToShip(ShipAPI ship) { return true; }
	
	@Override public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return true; }
	
	@Override public String getUnapplicableReason(ShipAPI ship) { return null; }	// handled with description instead

	@Override public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; }	// handled with description instead
	//#endregion
	// END OF CHECKS
	//#endregion
	// END OF IMPLEMENTATION

	/**
	 * Called from the {@link ehm_base retrofit base} only. If the hull does not have the base built-in, clones
	 * the hullSpec, alters it, and returns it so that it can be applied on a variant.
	 * <p> There are two problems with replacing the hull specs. From what I can tell, game replaces the hull
	 * specs of the ships after a combat, and after repairs. The latter is handled through the vanilla script
	 * {@link com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript#restoreToNonDHull FieldRepairsScript}
	 * which gets active if the player has the hull restoration skill. The former happens when a ship suffers
	 * damage, through {@link com.fs.starfarer.api.impl.campaign.DModManager#setDHull DModManager}.
	 * <p> The first problem is suppressed by replacing the script. The second problem is avoided by using
	 * d-hulls instead of normal ones at all times. In addition, any ship that can be restored to another
	 * hull spec is visually restored immediately as it creates another issue.
	 * @param variant to be used as a template
	 * @return a cloned hullSpec
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecClone(ShipVariantAPI variant) {
		final SettingsAPI settings = Global.getSettings();

		ShipHullSpecAPI hullSpecToClone = variant.getHullSpec();
		lyr_hullSpec hullSpec;
		lyr_hullSpec originalHullSpec;

		if (hullSpecToClone.isRestoreToBase() && hullSpecToClone.getBaseHullId() != null ) {	// these extras are necessary for ships that may be restored to a different hull spec
			for (String hullModId : hullSpecToClone.getBuiltInMods()) {	// transfer the dmods on the hullspec to the variant instead
				if (!settings.getHullModSpec(hullModId).hasTag(Tags.HULLMOD_DMOD)) continue;

				if (!variant.getSuppressedMods().contains(hullModId)) {	// if a dmod is suppressed (fixed), do not transfer it
					variant.removeSuppressedMod(hullModId);
					variant.addPermaMod(hullModId, false);
				}
			}
			hullSpecToClone = hullSpecToClone.getBaseHull();	// target the base hull spec instead, to perform a soft restoration
		}

		hullSpec = new lyr_hullSpec(settings.getHullSpec(Misc.getDHullId(hullSpecToClone)), true);
		originalHullSpec = new lyr_hullSpec(settings.getHullSpec(hullSpecToClone.getHullId().replace(Misc.D_HULL_SUFFIX, "")), false);

		ehm_hullSpecAlteration(hullSpec, originalHullSpec);

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
		final SettingsAPI settings = Global.getSettings();

		lyr_hullSpec hullSpec = new lyr_hullSpec(settings.getHullSpec(variant.getHullSpec().getHullId()), true);
		lyr_hullSpec originalHullSpec = new lyr_hullSpec(settings.getHullSpec(variant.getHullSpec().getHullId().replace(Misc.D_HULL_SUFFIX, "")), false);

		ehm_hullSpecAlteration(hullSpec, originalHullSpec);
		
		return hullSpec.retrieve();
	}

	/**
	 * As the hull specs use (D) versions to avoid a couple of issues, a method is necessary
	 * to make some alterations and restore some fields to their original, non (D) versions.
	 * @param hullSpec proxy with a cloned hull spec in it
	 * @param originalHullSpec to be used as a reference; not a (D) version
	 */
	private static final void ehm_hullSpecAlteration(lyr_hullSpec hullSpec, lyr_hullSpec originalHullSpec) {
		for (String hullSpecTag : originalHullSpec.getTags()) // this is a set, so there cannot be any duplicates, but still
		if (!hullSpec.getTags().contains(hullSpecTag))
		hullSpec.addTag(hullSpecTag);

		for (String builtInHullModSpecId : originalHullSpec.getBuiltInMods()) // this is a list, there can be duplicates so check first
		if (!hullSpec.getBuiltInMods().contains(builtInHullModSpecId))
		hullSpec.addBuiltInMod(builtInHullModSpecId);

		// hullSpec.setDParentHullId(null);
		// hullSpec.setBaseHullId(null);
		// hullSpec.setRestoreToBase(false);
		hullSpec.setBaseValue(originalHullSpec.getBaseValue());	// because d-hulls lose 25% in value immediately
		hullSpec.setSpriteSpec(originalHullSpec.getSpriteSpec());	// to reduce memory imprint, letting garbage collector dispose same sprite specs
		if (lyr_settings.getShowExperimentalFlavour()) {
			hullSpec.setManufacturer(text.flavourManufacturer);
			hullSpec.setDescriptionPrefix(text.flavourDescription);
			hullSpec.setHullName(originalHullSpec.getHullName() + " (E)");	// restore to base hull name, replacing "(D)" with "(E)"
		} else {
			hullSpec.setDescriptionPrefix(hullSpec.getDescriptionPrefix());	// restore with base prefix, if any
			hullSpec.setHullName(originalHullSpec.getHullName());	// restore to base hull name, removing "(D)"
		}
		hullSpec.addBuiltInMod(lyr_internals.id.hullmods.base);
	}

	/**
	 * Returns an unmodified hull spec from the spec store.
	 * @param variant to be used as a template
	 * @return a stock hullSpec from the SpecStore
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecReference(ShipVariantAPI variant) {
		return Global.getSettings().getHullSpec(variant.getHullSpec().getHullId());
	}

	/**
	 * Returns the original hull spec, or the d-hull version if there are d-mods.
	 * @param variant to be used as a template
	 * @return a stock hullSpec from the SpecStore
	 */
	protected static final ShipHullSpecAPI ehm_hullSpecOriginal(ShipVariantAPI variant) {
		if (variant.hasDMods()) return Global.getSettings().getHullSpec(variant.getHullSpec().getHullId());
		return Global.getSettings().getHullSpec(variant.getHullSpec().getHullId().replaceAll(Misc.D_HULL_SUFFIX, ""));
	}
}