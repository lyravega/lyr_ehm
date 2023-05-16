package data.submarkets;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import lyr.misc.lyr_internals;

public class ehm_submarket extends BaseSubmarketPlugin {
    private static final Set<String> shunts = new HashSet<String>();    // doing this here separately as there can be disabled/unused shunts
    static {
        for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
            if (!weaponSpec.hasTag(lyr_internals.tag.experimental)) continue;

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
	}

    @Override
	public CargoAPI getCargo() {
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

		return !stack.getWeaponSpecIfWeapon().hasTag(lyr_internals.tag.experimental);
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
