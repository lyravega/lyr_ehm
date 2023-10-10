package experimentalHullModifications.hullmods.ehm_sr;

import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm._ehm_helpers;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_hullSpec;

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
	public void onInstall(ShipVariantAPI variant) {
		if (_ehm_helpers.removeHullModsWithSameTag(variant, lyr_internals.tag.systemRetrofit, this.hullModSpecId)) return;
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		if (!_ehm_helpers.hasHullModWithTag(variant, lyr_internals.tag.systemRetrofit, this.hullModSpecId))
			variant.setHullSpecAPI(ehm_systemRestore(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

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

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			// if (_ehm_helpers.ehm_hasHullmodWithTag(ship, lyr_internals.tag.systemRetrofit, this.hullModSpecId)) tooltip.addPara(text.hasSystemRetrofit[0], text.padding).setHighlight(text.hasSystemRetrofit[1]);
			if (ship.getVariant().hasHullMod(lyr_internals.id.hullmods.logisticsoverhaul)) tooltip.addPara(text.hasLogisticsOverhaul[0], text.padding).setHighlight(text.hasLogisticsOverhaul[1]);

			if (this.hullModSpecTags.contains(lyr_internals.tag.reqShields) && ship.getShield() == null) tooltip.addPara(text.noShields[0], text.padding).setHighlight(text.noShields[1]);
			if (this.hullModSpecTags.contains(lyr_internals.tag.reqNoPhase) && ship.getPhaseCloak() != null) tooltip.addPara(text.hasPhase[0], text.padding).setHighlight(text.hasPhase[1]);
			if (this.hullModSpecTags.contains(lyr_internals.tag.reqWings) && ship.getNumFighterBays() == 0) tooltip.addPara(text.noWings[0], text.padding).setHighlight(text.noWings[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) return false; 
		// if (_ehm_helpers.ehm_hasHullmodWithTag(ship, lyr_internals.tag.systemRetrofit, this.hullModSpecId)) return false;
		if (ship.getVariant().hasHullMod(lyr_internals.id.hullmods.logisticsoverhaul)) return false;

		if (this.hullModSpecTags.contains(lyr_internals.tag.reqShields) && ship.getShield() == null) return false; 
		if (this.hullModSpecTags.contains(lyr_internals.tag.reqNoPhase) && ship.getPhaseCloak() != null) return false; 
		if (this.hullModSpecTags.contains(lyr_internals.tag.reqWings) && ship.getNumFighterBays() == 0) return false; 
		
		return true; 
	}
	//#endregion
}