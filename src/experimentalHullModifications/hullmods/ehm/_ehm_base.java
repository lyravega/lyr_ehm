package experimentalHullModifications.hullmods.ehm;

import java.awt.Color;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.diverterData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_launchtube.hangarData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_minimodule.moduleData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt.capacitorData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_mutableshunt.dissipatorData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterData;
import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_auxilarygenerators;
import experimentalHullModifications.hullmods.ehm_mr.ehm_mr_overengineered;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.hullmods;
import experimentalHullModifications.misc.ehm_internals.hullmods.tags;
import experimentalHullModifications.misc.ehm_lostAndFound;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm.friend;
import experimentalHullModifications.proxies.ehm_hullSpec;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;
import lyravega.utilities.logger.lyr_logger;

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
	protected final extendedData extendedData = new extendedData();

	public class extendedData {	// extended comma separated values
		public boolean isCosmetic = false;
		public boolean isRestricted = false;
		public boolean isCustomizable = false;
		public String groupTag = null;
		public Set<String> applicableChecks = null;
		public Set<String> lockedInChecks = null;
		public Set<String> lockedOutChecks = null;

		public Set<String> getApplicableChecks() { return this.applicableChecks; }

		public Set<String> getLockedChecks(ShipAPI ship) { return ship.getVariant().hasHullMod(_ehm_base.this.hullModSpecId) ? this.lockedInChecks : this.lockedOutChecks; }
	}

	public extendedData getExtendedData(friend friend) {
		return friend != null ? this.extendedData : null;
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		this.hullModSpec = hullModSpec;
		this.hullModSpecId = hullModSpec.getId();
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

	@Override public boolean showInRefitScreenModPickerFor(ShipAPI ship) { return true; }	// "restricted" tag is used to hide stuff in missions

	//#region TOOLTIP
	@Override public Color getBorderColor() { return null; }

	@Override public Color getNameColor() { return null; }

	@Override public float getTooltipWidth() { return 369f; }

	@Override public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return true; }

	@Override public String getDescriptionParam(int index, HullSize hullSize) { return null; }

	@Override public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return this.getDescriptionParam(index, hullSize); }

	@Override public boolean hasSModEffectSection(HullSize hullSize, ShipAPI ship, boolean isForModSpec) { return false; }

	@Override public void addSModSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {}

	@Override public void addSModEffectSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec, boolean isForBuildInList) {}

	@Override public String getSModDescriptionParam(int index, HullSize hullSize) { return null; }

	@Override public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) { return this.getSModDescriptionParam(index, hullSize); }

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (this.extendedData.isCustomizable) {
			tooltip.addSectionHeading(header.customizable, header.customizable_textColour, header.invisible_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.customizable, text.padding);
		}

		if (!this.isApplicableToShip(ship) && this.extendedData.getApplicableChecks() != null) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			for (String check : this.extendedData.getApplicableChecks()) switch (check) {
				case tags.reqBase: if (!lyr_miscUtilities.hasBuiltInHullMod(ship, hullmods.main.base)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.lacksBase, text.colourized.padding); continue;
				case tags.reqNoLogistics: if (ship.getVariant().hasHullMod(hullmods.misc.logisticsoverhaul)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasLogisticsOverhaul, text.colourized.padding); continue;
				case tags.reqShield: if (ship.getShield() == null) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.noShields, text.padding); continue;
				case tags.reqEngine: if (!lyr_miscUtilities.hasEngines(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.noEngines, text.padding); continue;
				case tags.reqNoPhase: if (lyr_miscUtilities.hasPhaseCloak(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasPhase, text.padding); continue;
				case tags.reqWingBays: if (ship.getNumFighterBays() == 0) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.noWings, text.padding); continue;
				case tags.reqNotChild: if (lyr_miscUtilities.isModule(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.isModule, text.padding); continue;
				case tags.reqDiverterAndConverter: if (!ship.getVariant().hasHullMod(hullmods.activatorRetrofits.diverterConverterActivator)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.lacksActivator, text.padding); continue;
				default: continue;
			}
		}

		if (!this.canBeAddedOrRemovedNow(ship, null, null) && this.extendedData.getLockedChecks(ship) != null) {
			tooltip.addSectionHeading(ship.getVariant().hasHullMod(this.hullModSpecId) ? header.lockedIn : header.lockedOut, header.locked_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			for (String check : this.extendedData.getLockedChecks(ship)) switch (check) {
				case tags.hasWeaponsOnConvertedSlots: if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affixes.convertedSlot)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeaponsOnConvertedSlots, text.padding); continue;
				case tags.hasWeaponsOnAdaptedSlots: if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affixes.adaptedSlot)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeaponsOnAdaptedSlots, text.padding); continue;
				case tags.hasExtraWings: if (lyr_miscUtilities.hasExtraWings(ship, this.hullModSpecId)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasExtraWings, text.padding); continue;
				case tags.hasWeapons: if (lyr_miscUtilities.hasWeapons(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeapons, text.colourized.padding); continue;
				case tags.hasMiniModules: if (lyr_miscUtilities.hasModulesWithPrefix(ship, "ehm_module")) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasMiniModules, text.padding); continue;
				case tags.hasAnyFittedWings: if (lyr_miscUtilities.hasAnyFittedWings(ship)) lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWings, text.colourized.padding); continue;
				default: continue;
			}
		}

		if (ship.getVariant().getSMods().contains(this.hullModSpecId)) return;

		if (this.isApplicableToShip(ship) && this.canBeAddedOrRemovedNow(ship, null, null)) {
			tooltip.addSectionHeading(header.warning, header.warning_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.warning, text.padding);
		}
	}
	//#endregion
	// END OF TOOLTIP

	//#region CHECKS
	@Override public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (this.extendedData.getApplicableChecks() != null) for (String check : this.extendedData.getApplicableChecks()) switch (check) {
			case tags.reqBase: if (!lyr_miscUtilities.hasBuiltInHullMod(ship, hullmods.main.base)) return false; else continue;
			case tags.reqNoLogistics: if (ship.getVariant().hasHullMod(hullmods.misc.logisticsoverhaul)) return false; else continue;
			case tags.reqShield: if (ship.getShield() == null) return false; else continue;
			case tags.reqEngine: if (!lyr_miscUtilities.hasEngines(ship)) return false; else continue;
			case tags.reqNoPhase: if (lyr_miscUtilities.hasPhaseCloak(ship)) return false; else continue;
			case tags.reqWingBays: if (ship.getNumFighterBays() == 0) return false; else continue;
			case tags.reqNotChild: if (lyr_miscUtilities.isModule(ship)) return false; else continue;
			case tags.reqDiverterAndConverter: if (!ship.getVariant().hasHullMod(hullmods.activatorRetrofits.diverterConverterActivator)) return false; else continue;
			default: continue;
		}

		return true;
	}

	@Override public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false;

		if (this.extendedData.getLockedChecks(ship) != null) for (String check : this.extendedData.getLockedChecks(ship)) switch (check) {
			case tags.hasWeaponsOnConvertedSlots: if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affixes.convertedSlot)) return false; else continue;
			case tags.hasWeaponsOnAdaptedSlots: if (lyr_miscUtilities.hasWeapons(ship, ehm_internals.affixes.adaptedSlot)) return false; else continue;
			case tags.hasExtraWings: if (lyr_miscUtilities.hasExtraWings(ship, this.hullModSpecId)) return false; else continue;
			case tags.hasWeapons: if (lyr_miscUtilities.hasWeapons(ship)) return false; else continue;
			case tags.hasMiniModules: if (lyr_miscUtilities.hasModulesWithPrefix(ship, "ehm_module")) return false; else continue;	// TODO: prefix needs to be moved to a constant
			case tags.hasAnyFittedWings: if (lyr_miscUtilities.hasAnyFittedWings(ship)) return false; else continue;
			default: continue;
		}

		return true;
	}

	@Override public String getUnapplicableReason(ShipAPI ship) { return null; }	// handled with description instead

	@Override public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) { return null; }	// handled with description instead
	//#endregion
	// END OF CHECKS
	//#endregion
	// END OF IMPLEMENTATION

	/**
	 * Clones and alters the hull spec if necessary, and applies it on the variant.
	 * <p> Actual cloning and alteration is done on the proxy's constructor. Other methods that also
	 * utilize that constructor guarantees that the hull spec will be unique to prevent any leakage.
	 * This method should be used if no further use of the proxy is required.
	 * @param stats of the ship/member whose hull spec may be swapped
	 * @see {@link ehm_hullSpec#ehm_hullSpec(ShipHullSpecAPI, boolean) Hull Spec Proxy Constructor}
	 */
	final void swapHullSpec(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);

		if (!hullSpec.isBuiltInMod(this.hullModSpecId)) hullSpec.addBuiltInMod(this.hullModSpecId);

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	// TODO: javadoc
	protected final void refreshHullSpec(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), true);

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	protected final void registerModInGroup(MutableShipStatsAPI stats) {
		stats.getDynamic().getMod(this.extendedData.groupTag).modifyFlat(this.hullModSpecId, 1);
	}

	protected final Set<String> getModsFromSameGroup(MutableShipStatsAPI stats) {
		return stats.getDynamic().getMod(this.extendedData.groupTag).getFlatBonuses().keySet();
	}

	protected final void preProcessShunts(MutableShipStatsAPI stats) {
		Pattern pattern = Pattern.compile("WS[ 0-9]{4}");
		Matcher matcher;

		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);

		// primarily to deal with stuff on load
		if (!ehm_settings.getClearUnknownSlots()) for (String slotId : variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) != null) continue;
			matcher = pattern.matcher(slotId);
			if (matcher.find()) slotId = matcher.group();
			else continue;	// this should never happen

			if (!slotId.startsWith(ehm_internals.affixes.normalSlot)) continue;
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != variant.getSlot(slotId).getSlotSize()) continue;

			String shuntId = shuntSpec.getWeaponId();
			if (adapterData.idSet.contains(shuntId)) hullSpec.adaptSlot(shuntId, slotId);
			else if (converterData.idSet.contains(shuntId)) hullSpec.convertSlot(shuntId, slotId);
		} else for (String slotId : variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) != null) continue;

			String weaponId = variant.getWeaponId(slotId);
			lyr_logger.warn("Slot with the ID '"+slotId+"' not found, stashing the weapon '"+weaponId+"'");
			ehm_lostAndFound.addLostItem(weaponId);	// to recover the weapons 'onGameLoad()'

			variant.clearSlot(slotId);	// this is an emergency option to allow loading because I fucked up
		}

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	protected final void preProcessDynamicStats(MutableShipStatsAPI stats) {
		DynamicStatsAPI dynamicStats = stats.getDynamic();
		ShipVariantAPI variant = stats.getVariant();

		if (variant.getSMods().contains(ehm_internals.hullmods.misc.overengineered)) {
			String source = ehm_mr_overengineered.class.getSimpleName();
			int mod = ehm_mr_overengineered.slotPointBonus.get(variant.getHullSize());

			dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(source, mod);
			dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).modifyFlat(source, mod);
		}

		if (variant.hasHullMod(ehm_internals.hullmods.misc.auxilarygenerators)) {
			String source = ehm_mr_auxilarygenerators.class.getSimpleName();
			int mod = ehm_mr_auxilarygenerators.slotPointBonus.get(variant.getHullSize());

			dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(source, mod);
			dynamicStats.getMod(ehm_internals.stats.slotPointsFromMods).modifyFlat(source, mod);
		}

		for (WeaponSlotAPI slot : variant.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slot.getId();
			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);

			if (shuntSpec == null) continue;
			if (shuntSpec.getSize() != slot.getSlotSize()) continue;
			if (!shuntSpec.hasTag(ehm_internals.hullmods.tags.experimental)) continue;

			String shuntId = shuntSpec.getWeaponId();
			String shuntGroupTag = shuntSpec.getWeaponGroupTag();
			switch (shuntGroupTag) {
				case adapterData.groupTag: {
					if (!variant.hasHullMod(adapterData.activatorId)) continue;
					if (!adapterData.isValidSlot(slot, shuntSpec)) continue;

					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
				}; continue;
				case converterData.groupTag: {
					if (!variant.hasHullMod(converterData.activatorId)) continue;
					if (!converterData.isValidSlot(slot, shuntSpec)) continue;

					int mod = converterData.dataMap.get(shuntId).getChildCost();
					if (!slot.isDecorative()) {
						dynamicStats.getMod(shuntId+"_inactive").modifyFlat(slotId, 1);
						dynamicStats.getMod(shuntGroupTag+"_inactive").modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, mod);
					} else {
						dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
						dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsNeeded).modifyFlat(slotId, mod);
						dynamicStats.getMod(ehm_internals.stats.slotPointsUsed).modifyFlat(slotId, mod);
						// dynamicStats.getMod(ehm_internals.stats.slotPointsToConverters).modifyFlat(slotId, mod);	// redundant since stat ids point at the group tag
					}
				}; continue;
				case diverterData.groupTag: {
					if (!variant.hasHullMod(diverterData.activatorId)) continue;
					if (!diverterData.isValidSlot(slot, shuntSpec)) continue;

					int mod = diverterData.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);
					dynamicStats.getMod(ehm_internals.stats.slotPoints).modifyFlat(slotId, mod);
					// dynamicStats.getMod(ehm_internals.stats.slotPointsFromDiverters).modifyFlat(slotId, mod);	// redundant since stat ids point at the group tag
				}; continue;
				case capacitorData.groupTag: {
					if (!variant.hasHullMod(capacitorData.activatorId)) continue;
					if (!capacitorData.isValidSlot(slot, shuntSpec)) continue;

					int mod = capacitorData.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);
				}; continue;
				case dissipatorData.groupTag: {
					if (!variant.hasHullMod(dissipatorData.activatorId)) continue;
					if (!dissipatorData.isValidSlot(slot, shuntSpec)) continue;

					int mod = dissipatorData.dataMap.get(shuntId);
					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, mod);
				}; continue;
				case hangarData.groupTag: {
					if (!variant.hasHullMod(hangarData.activatorId)) continue;
					if (!hangarData.isValidSlot(slot, shuntSpec)) continue;

					dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag).modifyFlat(slotId, 1);
				}; continue;
				case moduleData.groupTag: {
					if (!variant.hasHullMod(moduleData.activatorId)) continue;
					if (!moduleData.isValidSlot(slot, shuntSpec)) continue;
					if (variant.getModuleSlots().contains(slotId)) { variant.clearSlot(slotId);	continue; }

					dynamicStats.getMod(shuntId+"_inactive").modifyFlat(slotId, 1);
					dynamicStats.getMod(shuntGroupTag+"_inactive").modifyFlat(slotId, 1);
				}; continue;
				default: continue;
			}
		}
	}
}