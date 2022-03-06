package lyr;

import com.fs.starfarer.api.BaseModPlugin;

public class lyr_plugin extends BaseModPlugin {
	@Override
	public void onGameLoad(boolean newGame) {
		data.hullmods.ehm_base.buildFleetMaps();
		new lyr.tools._lyr_uiTools._lyr_delayedFinder();
	}

	@Override
	public void beforeGameSave() {

	}
}
