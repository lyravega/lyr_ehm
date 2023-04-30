package data.hullmods;

import static data.hullmods.ehm_ar._ehm_ar_base.ehm_adapterRemoval;
import static data.hullmods.ehm_ec._ehm_ec_base.ehm_restoreEngineSlots;
import static data.hullmods.ehm_sc._ehm_sc_base.ehm_restoreShield;
import static data.hullmods.ehm_sr._ehm_sr_base.ehm_systemRestore;
import static data.hullmods.ehm_wr._ehm_wr_base.ehm_weaponSlotRestore;
import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.playSound;

import java.util.Iterator;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;
import lyr.tools._lyr_scriptTools.shipTrackerScript;

/**
 * Serves as a requirement for all experimental hull modifications, and provides hullMod
 * tracking to the ship.
 * @category Base Hull Modification 
 * @author lyravega
 */
public class ehm_base extends _ehm_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (ehm_hasRetrofitBaseBuiltIn(variant)) return;

		variant.setHullSpecAPI(ehm_hullSpecClone(variant)); commitChanges(); playSound();
	}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		ehm_trackShip(ship);
	}

	/**
	 * If a change is detected in the ship's {@link shipTrackerScript}, this method is
	 * called. Executes installation actions if there are any for the new hullmods.
	 * @param variant of the ship
	 * @param newHullMods set of new hull mods
	 */
	public static void onInstalled(ShipVariantAPI variant, Set<String> newHullMods) {
		String newHullModId;
		String retrofitType;
		Set<String> tags;

		for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) {
			newHullModId = i.next(); 

			tags = Global.getSettings().getHullModSpec(newHullModId).getTags();
			if (tags.contains(lyr_internals.tag.externalAccess)) { commitChanges(); playSound(); break; } 
			if (!tags.contains(lyr_internals.tag.experimental)) continue;

			retrofitType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
			switch (retrofitType) {
				case lyr_internals.affix.adapterRetrofit: commitChanges(); playSound(); break; // no longer uses 'clearUndo()' as it fails in some cases and as such, is deprecated.
				case lyr_internals.affix.systemRetrofit: commitChanges(); playSound(); break;
				case lyr_internals.affix.weaponRetrofit: commitChanges(); playSound(); break;
				case lyr_internals.affix.shieldCosmetic: commitChanges(); playSound(); break;
				case lyr_internals.affix.engineCosmetic: commitChanges(); playSound(); break;
				default: break;
			}
		}
	}

	/**
	 * If a change is detected in the ship's {@link shipTrackerScript}, this method is
	 * called. Executes removal actions if there are any for the old hullmods.
	 * @param variant of the ship
	 * @param removedHullMods set of removed hull mods
	 */
	public static void onRemoved(ShipVariantAPI variant, Set<String> removedHullMods) {	
		String removedHullModId;
		String retrofitType;
		Set<String> tags;

		for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) {
			removedHullModId = i.next(); 
	
			tags = Global.getSettings().getHullModSpec(removedHullModId).getTags();
			if (tags.contains(lyr_internals.tag.externalAccess)) { variant.setHullSpecAPI(ehm_hullSpecRefresh(variant)); commitChanges(); playSound(); break; }
			if (!tags.contains(lyr_internals.tag.experimental)) continue;

			retrofitType = removedHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
			switch (retrofitType) {
				case lyr_internals.affix.adapterRetrofit: variant.setHullSpecAPI(ehm_adapterRemoval(variant)); commitChanges(); playSound(); break;
				case lyr_internals.affix.systemRetrofit: variant.setHullSpecAPI(ehm_systemRestore(variant)); commitChanges(); playSound(); break;
				case lyr_internals.affix.weaponRetrofit: variant.setHullSpecAPI(ehm_weaponSlotRestore(variant)); commitChanges(); playSound(); break;
				case lyr_internals.affix.shieldCosmetic: variant.setHullSpecAPI(ehm_restoreShield(variant)); commitChanges(); playSound(); break;
				case lyr_internals.affix.engineCosmetic: variant.setHullSpecAPI(ehm_restoreEngineSlots(variant)); commitChanges(); playSound(); break;
				default: break;
			}
		}
	}

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
