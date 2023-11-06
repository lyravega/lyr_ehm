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
import experimentalHullModifications.misc.ehm_tooltip.regexText;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

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
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.invisible_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasBuiltInHullMod(ship, ehm_internals.id.hullmods.base)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.lacksBase, text.padding);
			if (lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.experimental, null, false)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasAnyExperimentalEnhanced, text.padding);
			else {
				if (lyr_miscUtilities.hasHullModWithTag(ship, ehm_internals.tag.experimental, ehm_internals.id.hullmods.base, true)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasAnyExperimental, text.padding);
				if (lyr_miscUtilities.hasWeapons(ship)) lyr_tooltipUtilities.addColorizedPara(tooltip, regexText.hasWeapons, text.padding);
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
