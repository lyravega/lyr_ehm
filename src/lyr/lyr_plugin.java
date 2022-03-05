package lyr;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import lyr.tools._lyr_uiTools;

public class lyr_plugin extends BaseModPlugin {
	@Override
	public void onGameLoad(boolean newGame) {
		data.hullmods.ehm_base.buildFleetMaps();
		Global.getSector().addTransientScript(new _lyr_uiTools._lyr_delayedFinder());
	}

	@Override
	public void beforeGameSave() {

	}
}
