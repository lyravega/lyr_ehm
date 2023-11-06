package experimentalHullModifications.hullmods.ehm;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.utilities.lyr_miscUtilities;

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

		variant.getHullMods().remove(ehm_internals.id.hullmods.base);
		variant.getPermaMods().remove(ehm_internals.id.hullmods.base);
		variant.getHullMods().remove(this.hullModSpecId);
		variant.setHullSpecAPI(Global.getSettings().getHullSpec(variant.getHullSpec().getHullId().replace(Misc.D_HULL_SUFFIX, ""))); commitVariantChanges(); playDrillSound();
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.experimental, null, false)) tooltip.addPara(text.hasAnyExperimentalEnhanced[0], text.padding).setHighlight(text.hasAnyExperimentalEnhanced[1]);
			else {
				if (lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.experimental, ehm_internals.id.hullmods.base, true)) tooltip.addPara(text.hasAnyExperimental[0], text.padding).setHighlight(text.hasAnyExperimental[1]);
				if (lyr_miscUtilities.hasWeapons(ship)) tooltip.addPara(text.hasWeapons[0], text.padding).setHighlight(text.hasWeapons[1]);
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) return false;
		if (lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.experimental, ehm_internals.id.hullmods.base, true)) return false;
		if (lyr_miscUtilities.hasWeapons(ship)) return false;

		return true;
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return (lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) ? true : false;
	}
}
