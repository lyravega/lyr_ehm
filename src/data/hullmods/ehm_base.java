package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * Serves as a requirement for all experimental hull modifications. Clones the hullSpec, adds
 * flavour text and manufacturer, and adds a hull tag to avoid re-cloning.
 * 
 * With a cloned hullSpec, all changes will apply to only one ship, and will not be shared with 
 * other ships using the same hull. This base hullMod ensures that, but also does even more.  
 * 
 * Triggers a script to keep track of the installed, new and removed hullMods, implementing 
 * jury-rigged 'onAdd()' and 'onRemove()' functions in essence, which are used to refresh the 
 * refit screen in order to show the changes correctly. For further details, check the ship 
 * script {@link shipTrackerScript}, and the TRACKERS part of {@link _ehm_base_master}. 
 * 
 * If the hullSpec is restored through {@link #ehm_getStockHullSpec(ShipVariantAPI, boolean)},
 * the persistence of this base hullMod will ensure that it will run through the same cloning
 * process again.
 * @category Base Hull Modification 
 * @author lyravega
 * @version 0.5
 * @since 0.4
 */
public class ehm_base extends _ehm_base_master {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant(); 
		variant.setHullSpecAPI(ehm_hullSpecClone(variant)); 
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (ship == null) return;

        CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
        if (tab == null || !tab.equals(CoreUITabId.REFIT)) return;

		shipTrackerScript(ship).setVariant(ship.getVariant()); // setVariant() is necessary to reflect the changes on the "refit ship"
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		
	}

	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		return null; 
	}

	@Override
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ehm_hasRetrofitTag(ship, ehm.tag.allRetrofit, hullModSpecId)) return ehm.excuses.hasAnyRetrofit;

		return null;
	}
}
