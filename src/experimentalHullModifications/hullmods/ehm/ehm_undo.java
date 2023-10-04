package experimentalHullModifications.hullmods.ehm;

import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyravega.listeners.lyr_fleetTracker;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;

/**
 * Removes the base hull modification that all other experimental ones require
 * from the ship. Strict installation requirements to avoid issues.
 * @category Base Hull Un-Modification
 * @author lyravega
 */
public final class ehm_undo extends _ehm_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		lyr_fleetTracker.terminateShipTracker(stats);

		variant.getHullMods().remove(lyr_internals.id.hullmods.base);
		variant.getPermaMods().remove(lyr_internals.id.hullmods.base);
		variant.getHullMods().remove(this.hullModSpecId);
		variant.setHullSpecAPI(ehm_hullSpecOriginal(variant)); commitVariantChanges(); playDrillSound(); 
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (_ehm_helpers.hasExperimentalSMod(ship)) tooltip.addPara(text.hasAnyExperimentalBuiltIn[0], text.padding).setHighlight(text.hasAnyExperimentalBuiltIn[1]); 
			else {
				if (_ehm_helpers.hasHullModWithTag(ship, lyr_internals.tag.experimental, lyr_internals.id.hullmods.base)) tooltip.addPara(text.hasAnyExperimental[0], text.padding).setHighlight(text.hasAnyExperimental[1]);
				if (_ehm_helpers.hasWeapons(ship)) tooltip.addPara(text.hasWeapons[0], text.padding).setHighlight(text.hasWeapons[1]);
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) return false;
		if (_ehm_helpers.hasHullModWithTag(ship, lyr_internals.tag.experimental, lyr_internals.id.hullmods.base)) return false;
		if (_ehm_helpers.hasWeapons(ship)) return false; 

		return true; 
	}
	
	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		if (!super.showInRefitScreenModPickerFor(ship)) return false;
		return (_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) ? true : false;
	}
}
