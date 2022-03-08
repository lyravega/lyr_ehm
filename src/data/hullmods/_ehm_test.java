package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class _ehm_test extends _ehm_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {

	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

	}

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {

	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {

	}

	@Override
	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {

	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
			boolean isForModSpec) {
		// TODO Auto-generated method stub
		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		// TODO Auto-generated method stub
		return super.isApplicableToShip(ship);
	}

	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		// TODO Auto-generated method stub
		return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
	}

	// @Override
	// protected String ehm_unapplicableReason(ShipAPI ship) {
	// 	if (ship == null) return "Ship does not exist";

	// 	// if (!Global.getSector().getPlayerFleet().getFlagship().equals(ship.getFleetMember())) return "This ain't the flagship";

	// 	return null; 
	// }

	// @Override
	// protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
	// 	// HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());

	// 	// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

	// 	return null;
	// }
}
