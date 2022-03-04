package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ehm_er_automated extends _ehm_base {
	private static final String automated = "automated";

	// TODO: Finish this
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		
	}
	
	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return "Ship does not exist"; 
		
		if (ship.getVariant().hasHullMod(automated)) return "Ship is automated";
		// if (ship.getVariant().hasHullMod("ehm_er_manned")) return "herp";

		return null; 
	}

	@Override
	protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		// HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());
		
		// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

		return null;
	}
}
