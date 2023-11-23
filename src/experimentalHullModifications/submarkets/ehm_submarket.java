package experimentalHullModifications.submarkets;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import experimentalHullModifications.misc.ehm_internals;

/**
 * A submarket for the experimental slot shunts. The submarket is attached/detached
 * if the relevant setting is selected, and whenever the player interacts with a
 * valid market. The submarket is transient, will be removed after interaction.
 * <p> Each time it is attached to a market, it will get re-initialized, at which
 * point its contens will be refreshed.
 * @see {@link experimentalHullModifications.abilities.listeners.ehm_submarketInjector Submarket Injector} listener to detect when the player opens/closes a market
 * @author lyravega
 */
public final class ehm_submarket extends BaseSubmarketPlugin {
	public static final Set<String> shunts = new HashSet<String>();	// doing this here separately as there can be disabled/unused shunts
	static {
		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {	// doing this here might be problematic, OK so far
			if (!weaponSpec.hasTag(ehm_internals.tags.experimental)) continue;
		// for (String weaponId : ehm_internals.id.shunts.set) {	this is dangerous during game launch, older above one works though
		// 	WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(weaponId);
			// if (weaponSpec == null) continue;

			shunts.add(weaponSpec.getWeaponId());
		}
	}

	@Override
	public void init(SubmarketAPI submarket) {
		this.submarket = submarket;
		this.market = submarket.getMarket();
		this.cargo = Global.getFactory().createCargo(true);

		for (String shuntId : shunts) {
			this.cargo.addWeapons(shuntId, 1000);
		}
		this.cargo.addMothballedShip(FleetMemberType.SHIP, "crig_Standard", "EHM Lab");
		this.cargo.getMothballedShips().getMembersListCopy().iterator().next().getVariant().addMod(ehm_internals.ids.hullmods.base);
	}

	@Override
	public CargoAPI getCargo() {
		if (this.cargo == null) {
			this.cargo = Global.getFactory().createCargo(true);
		}

		return this.cargo;
	}

	@Override
	public CargoAPI getCargoNullOk() {
		return this.cargo;
	}

	@Override
	public void updateCargoPrePlayerInteraction() {

	}

	@Override
	public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
		return true;
	}

	@Override
	public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
		if (!stack.isWeaponStack()) return true;

		return !stack.getWeaponSpecIfWeapon().hasTag(ehm_internals.tags.experimental);
	}

	@Override
	public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
		return "Only accessible for slot shunts";
	}

	@Override
	public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
		return true;
	}

	@Override
	public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
		return "Only accessible for slot shunts";
	}

	@Override
	public boolean isParticipatesInEconomy() {
		return false;
	}

	@Override
	public float getTariff() {
		return 0f;
	}

	@Override
	public boolean isFreeTransfer() {
		return true;
	}

	@Override
	public String getBuyVerb() {
		return "Assemble";
	}

	@Override
	public String getSellVerb() {
		return "Salvage";
	}

	@Override
	public boolean isEnabled(CoreUIAPI ui) {
		return true;
	}
}
