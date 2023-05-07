package data.hullmods.ehm;

import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.playSound;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**
 * Removes the base hull modification that all other experimental ones require
 * from the ship. Strict installation requirements to avoid issues.
 * @category Misc
 * @author lyravega
 */
public class ehm_undo extends _ehm_basetracker {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (!ehm_hasRetrofitBaseBuiltIn(variant)) return;
		
		ehm_stopTracking(stats);

		variant.getHullMods().remove(this.hullModSpecId);
		variant.getHullMods().remove(lyr_internals.id.baseRetrofit);
		variant.setHullSpecAPI(ehm_hullSpecReference(variant)); commitChanges(); playSound(); 
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(lyr_tooltip.header.notApplicable, lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(lyr_tooltip.text.lacksBase, lyr_tooltip.text.padding);
			if (ehm_hasExperimentalSMod(ship.getVariant())) tooltip.addPara(lyr_tooltip.text.hasAnyExperimentalBuiltIn, lyr_tooltip.text.padding); 
			else {
				if (ehm_hasRetrofitTag(ship, lyr_internals.tag.experimental, hullModSpecId)) tooltip.addPara(lyr_tooltip.text.hasAnyExperimental, lyr_tooltip.text.padding);
				if (ehm_hasWeapons(ship)) tooltip.addPara(lyr_tooltip.text.hasWeapons, lyr_tooltip.text.padding);
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) return false;
		if (ehm_hasRetrofitTag(ship, lyr_internals.tag.experimental, hullModSpecId)) return false;
		if (ehm_hasWeapons(ship)) return false; 

		return true; 
	}
	
	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();

		return (ehm_hasRetrofitBaseBuiltIn(variant)) ? true : false;
	}
}
