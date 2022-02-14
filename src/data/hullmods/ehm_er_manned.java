package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.loading.specs.g;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class ehm_er_manned extends _ehm_base {
	private static final String automated = "automated";

	// TODO: Finish this
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ShipVariantAPI stockVariant = Global.getSettings().getVariant(variant.getHullVariantId());

		if (stockVariant.hasHullMod(automated)) {

		} else {

		} 
	}
	
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return "Ship does not exist"; 
		
		if (ship.getVariant().getSuppressedMods().contains(automated)) return null;
		if (!ship.getVariant().hasHullMod(automated)) return "Cannot install, ship is not automated";
		if (ship.getVariant().hasHullMod("ehm_er_automated")) return "derp";

		return null; 
	}

	@Override
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		ShipVariantAPI variant = ship.getVariant();

		if (variant.getSuppressedMods().contains(automated)) return "Cannot remove, suppressing automated";
		
		return null;
	}
}
