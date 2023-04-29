package data.hullmods;

import static data.hullmods.ehm_ar._ehm_ar_base.ehm_adapterRemoval;
import static data.hullmods.ehm_ec._ehm_ec_base.ehm_restoreEngineSlots;
import static data.hullmods.ehm_sc._ehm_sc_base.ehm_restoreShield;
import static data.hullmods.ehm_sr._ehm_sr_base.ehm_systemRestore;
import static data.hullmods.ehm_wr._ehm_wr_base.ehm_weaponSlotRestore;
import static lyr.tools._lyr_scriptTools.shipTrackerScript;
import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.isRefitTab;
import static lyr.tools._lyr_uiTools.playSound;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

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

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (!isRefitTab()) return;

		shipTrackerScript(ship).setVariant(ship.getVariant());	// through this the changes are detected
	}

	/**
	 * Compares the cached hullmods stored in the ship's own {@link shipTrackerScript}
	 * with current hullmods, and executes installation actions if there are any while
	 * updating the cache.
	 * @param variant of the ship
	 * @param cachedHullMods set that is cached in the script for comparison
	 * @param memberId of the ship for logging purposes
	 */
	public static void onInstalled(ShipVariantAPI variant, Set<String> cachedHullMods, String memberId) {
		Set<String> newHullMods = new HashSet<String>();
			
		for (String hullModId : variant.getHullMods()) {
			// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
			if (cachedHullMods.contains(hullModId)) continue;

			if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": New hull modification '"+hullModId+"'");

			newHullMods.add(hullModId);
		}
		
		if (!newHullMods.isEmpty()) {
			for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) {
				// ShipVariantAPI refitVariant = ship.getVariant();
				String newHullModId = i.next(); 

				Set<String> tags = Global.getSettings().getHullModSpec(newHullModId).getTags();
				if (tags.contains(lyr_internals.tag.externalAccess)) { commitChanges(); playSound(); break; } 
				if (!tags.contains(lyr_internals.tag.experimental)) continue;

				String retrofitType = newHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
				switch (retrofitType) {
					case lyr_internals.affix.adapterRetrofit: commitChanges(); playSound(); break; // no longer uses 'clearUndo()' as it fails in some cases and as such, is deprecated.
					case lyr_internals.affix.systemRetrofit: commitChanges(); playSound(); break;
					case lyr_internals.affix.weaponRetrofit: commitChanges(); playSound(); break;
					case lyr_internals.affix.shieldCosmetic: commitChanges(); playSound(); break;
					case lyr_internals.affix.engineCosmetic: commitChanges(); playSound(); break;
					default: break;
				}
			} cachedHullMods.addAll(newHullMods); newHullMods.clear();
		}
	}

	/**
	 * Compares the cached hullmods stored in the ship's own {@link shipTrackerScript}
	 * with current hullmods, and executes removal actions if there are any while
	 * updating the cache.
	 * @param variant of the ship
	 * @param cachedHullMods set that is cached in the script for comparison
	 * @param memberId of the ship for logging purposes
	 */
	public static void onRemoved(ShipVariantAPI variant, Set<String> cachedHullMods, String memberId) {
		Set<String> removedHullMods = new HashSet<String>();

		for (Iterator<String> i = cachedHullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
			// if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue;
			if (variant.hasHullMod(hullModId)) continue;

			if (log) logger.info(lyr_internals.logPrefix+"xST-"+memberId+": Removed hull modification '"+hullModId+"'");

			removedHullMods.add(hullModId);
		}
		
		if (!removedHullMods.isEmpty()) {
			for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) {
				ShipVariantAPI refitVariant = variant;
				String removedHullModId = i.next(); 
		
				Set<String> tags = Global.getSettings().getHullModSpec(removedHullModId).getTags();
				if (tags.contains(lyr_internals.tag.externalAccess)) { refitVariant.setHullSpecAPI(ehm_hullSpecRefresh(refitVariant)); commitChanges(); playSound(); break; }
				if (!tags.contains(lyr_internals.tag.experimental)) continue;

				String retrofitType = removedHullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
				switch (retrofitType) {
					case lyr_internals.affix.adapterRetrofit: refitVariant.setHullSpecAPI(ehm_adapterRemoval(refitVariant)); commitChanges(); playSound(); break;
					case lyr_internals.affix.systemRetrofit: refitVariant.setHullSpecAPI(ehm_systemRestore(refitVariant)); commitChanges(); playSound(); break;
					case lyr_internals.affix.weaponRetrofit: refitVariant.setHullSpecAPI(ehm_weaponSlotRestore(refitVariant)); commitChanges(); playSound(); break;
					case lyr_internals.affix.shieldCosmetic: refitVariant.setHullSpecAPI(ehm_restoreShield(refitVariant)); commitChanges(); playSound(); break;
					case lyr_internals.affix.engineCosmetic: refitVariant.setHullSpecAPI(ehm_restoreEngineSlots(refitVariant)); commitChanges(); playSound(); break;
					default: break;
				}
			} cachedHullMods.removeAll(removedHullMods); removedHullMods.clear();
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
