package experimentalHullModifications.hullmods.ehm_sr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.logger.lyr_logger;

/**
 * This class is used by system retrofit hullmods. They are pretty
 * straightforward in their operation; change the system of a hullSpec.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_sr_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		if (lyr_miscUtilities.removeHullModsWithSameTag(variant, ehm_internals.tag.systemRetrofit, this.hullModSpecId)) return;
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		if (!lyr_miscUtilities.hasHullModWithTag(variant, ehm_internals.tag.systemRetrofit, this.hullModSpecId))
			variant.setHullSpecAPI(ehm_systemRestore(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		String shipSystemId = this.getClass().getSimpleName().replace(ehm_internals.affix.systemRetrofit, "");

		if (Global.getSettings().getShipSystemSpec(shipSystemId) == null) {
			hullModSpec.setHidden(true);
			hullModSpec.setHiddenEverywhere(true);

			lyr_logger.warn("Ship system with systemId '"+shipSystemId+"' not found, hiding '"+hullModSpec.getId()+"'"); return;
		}

		Description shipSystemDescription = Global.getSettings().getDescription(shipSystemId, Description.Type.SHIP_SYSTEM);

		hullModSpec.setDescriptionFormat(shipSystemDescription.getText1());
		// hullModSpec.setShortDesc(shipSystemDescription.getText3());	// this is not on the API
		try {
			lyr_reflectionUtilities.methodReflection.invokeDirect(hullModSpec, "setShortDesc", shipSystemDescription.getText3());
		} catch (Throwable t) {
			lyr_logger.error("Could not set the short description of hull modification spec", t);
		}

		super.init(hullModSpec);
	}

	/**
	 * Alters the system on a hullSpec, and returns it. The returned hullSpec needs
	 * to be installed on the variant.
	 * @param variant of the ship that will have its system replaced
	 * @param systemId of the system to be installed on the passed variant
	 * @return a new hullSpec to be installed on the variant
	 * @see {@link #ehm_systemRestore()} reverses this process
	 */
	protected static final ShipHullSpecAPI ehm_systemRetrofit(ShipVariantAPI variant, String systemId) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		hullSpec.setShipSystemId(systemId);
		return hullSpec.retrieve();
	}

	/**
	 * Restores a system of a hullSpec to its stock one, and returns it. Returned hullSpec
	 * needs to be installed on the variant.
	 * @param variant that will have its system reset to factory defaults
	 * @return a hullspec to be installed on the variant
	 */
	public static final ShipHullSpecAPI ehm_systemRestore(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		hullSpec.setShipSystemId(ehm_hullSpecReference(variant).getShipSystemId());
		return hullSpec.retrieve();
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!this.isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!lyr_miscUtilities.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			// if (_ehm_helpers.ehm_hasHullmodWithTag(ship, lyr_internals.tag.systemRetrofit, this.hullModSpecId)) tooltip.addPara(text.hasSystemRetrofit[0], text.padding).setHighlight(text.hasSystemRetrofit[1]);
			if (ship.getVariant().hasHullMod(ehm_internals.id.hullmods.logisticsoverhaul)) tooltip.addPara(text.hasLogisticsOverhaul[0], text.padding).setHighlight(text.hasLogisticsOverhaul[1]);

			if (this.hullModSpecTags.contains(ehm_internals.tag.reqShields) && ship.getShield() == null) tooltip.addPara(text.noShields[0], text.padding).setHighlight(text.noShields[1]);
			if (this.hullModSpecTags.contains(ehm_internals.tag.reqNoPhase) && ship.getPhaseCloak() != null) tooltip.addPara(text.hasPhase[0], text.padding).setHighlight(text.hasPhase[1]);
			if (this.hullModSpecTags.contains(ehm_internals.tag.reqWings) && ship.getNumFighterBays() == 0) tooltip.addPara(text.noWings[0], text.padding).setHighlight(text.noWings[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!lyr_miscUtilities.hasRetrofitBaseBuiltIn(ship)) return false;
		// if (_ehm_helpers.ehm_hasHullmodWithTag(ship, lyr_internals.tag.systemRetrofit, this.hullModSpecId)) return false;
		if (ship.getVariant().hasHullMod(ehm_internals.id.hullmods.logisticsoverhaul)) return false;

		if (this.hullModSpecTags.contains(ehm_internals.tag.reqShields) && ship.getShield() == null) return false;
		if (this.hullModSpecTags.contains(ehm_internals.tag.reqNoPhase) && ship.getPhaseCloak() != null) return false;
		if (this.hullModSpecTags.contains(ehm_internals.tag.reqWings) && ship.getNumFighterBays() == 0) return false;

		return true;
	}
	//#endregion
}