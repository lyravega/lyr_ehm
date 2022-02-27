package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class _ehm_test extends _ehm_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {

	}

	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return "Ship does not exist"; 

		// if (!Global.getSector().getPlayerFleet().getFlagship().equals(ship.getFleetMember())) return "This ain't the flagship";

		return null; 
	}

	@Override
	protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		// HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());

		// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

		return null;
	}
}
