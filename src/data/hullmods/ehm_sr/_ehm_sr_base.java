package data.hullmods.ehm_sr;

import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import lyr.proxies.lyr_hullSpec;

/**
 * This class is used by system retrofit hullmods. They are pretty 
 * straightforward in their operation; change the system of a hullSpec.
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_sr_base extends _ehm_base {
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
	 * @see {@link data.hullmods.ehm_base#onRemoved(String, ShipAPI) onRemoved()} called externally by this method
	 */
	public static final ShipHullSpecAPI ehm_systemRestore(ShipVariantAPI variant) { 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		hullSpec.setShipSystemId(ehm_hullSpecReference(variant).getShipSystemId());
		return hullSpec.retrieve(); 
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(ehm.tooltip.header.notApplicable, ehm.tooltip.header.notApplicable_textColour, ehm.tooltip.header.notApplicable_bgColour, Alignment.MID, ehm.tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(ehm.tooltip.text.lacksBase, ehm.tooltip.text.padding);
			if (ehm_hasRetrofitTag(ship, ehm.tag.systemRetrofit, hullModSpecId)) tooltip.addPara(ehm.tooltip.text.hasSystemRetrofit, ehm.tooltip.text.padding);

			Set<String> hullModSpecTags = hullModSpec.getTags();
			if (hullModSpecTags.contains(ehm.tag.reqShields) && ship.getShield() == null) tooltip.addPara(ehm.tooltip.text.noShields, ehm.tooltip.text.padding);
			if (hullModSpecTags.contains(ehm.tag.reqNoPhase) && ship.getPhaseCloak() != null) tooltip.addPara(ehm.tooltip.text.hasPhase, ehm.tooltip.text.padding);
			if (hullModSpecTags.contains(ehm.tag.reqWings) && ship.getNumFighterBays() == 0) tooltip.addPara(ehm.tooltip.text.noWings, ehm.tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.systemRetrofit, hullModSpecId)) return false; 

		Set<String> hullModSpecTags = hullModSpec.getTags();
		if (hullModSpecTags.contains(ehm.tag.reqShields) && ship.getShield() == null) return false; 
		if (hullModSpecTags.contains(ehm.tag.reqNoPhase) && ship.getPhaseCloak() != null) return false; 
		if (hullModSpecTags.contains(ehm.tag.reqWings) && ship.getNumFighterBays() == 0) return false; 
		
		return true; 
	}
	//#endregion
}