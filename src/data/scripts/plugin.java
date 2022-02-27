package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;

public class plugin extends BaseModPlugin {
	@Override
	public void onGameLoad(boolean newGame) {
		data.hullmods.ehm_base.buildFleetMaps();
	}

	@Override
	public void beforeGameSave() {

	}
}
