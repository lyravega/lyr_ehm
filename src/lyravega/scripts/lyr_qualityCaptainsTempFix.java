package lyravega.scripts;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

import lyravega.tools.lyr_logger;

public class lyr_qualityCaptainsTempFix implements EveryFrameScript, lyr_logger {
	private transient boolean isDone = false;
	
	@Override
	public void advance(float amount) {
		List<EveryFrameScript> scripts = new ArrayList<EveryFrameScript>(Global.getSector().getScripts());

		for (EveryFrameScript script : scripts) {
			if (!script.getClass().getSimpleName().equals("CaptainsFieldRepairsScript")) continue;
			
			logger.warn(logPrefix + "Suppressed 'FieldRepairScript' replacement from 'QualityCaptains' mod");
			Global.getSector().removeScript(script);
			isDone = true;
		}
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public boolean runWhilePaused() {
		return true;
	}
}


