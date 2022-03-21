package lyr;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class lyr_plugin extends BaseModPlugin {
	private static void updateBlueprints() {
		FactionAPI playerFaction = Global.getSector().getPlayerFaction();
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			String hullModSpecId = hullModSpec.getId();
			if (hullModSpec.hasTag("ehm") && !playerFaction.knowsHullMod(hullModSpecId)) playerFaction.addKnownHullMod(hullModSpecId);
			else if (hullModSpec.hasTag("ehm_restricted") && playerFaction.knowsHullMod(hullModSpecId)) playerFaction.removeKnownHullMod(hullModSpecId);
		}
		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (weaponSpec.hasTag("ehm_adapters") && !playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.addKnownWeapon(weaponSpec.getWeaponId(), false);
		}
	}

	@Override
	public void onGameLoad(boolean newGame) {
		data.hullmods.ehm_base.buildFleetMaps();
		new lyr.tools._lyr_uiTools._lyr_delayedFinder();
		updateBlueprints();
	}

	@Override
	public void beforeGameSave() {

	}
}
