package data.scripts;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;

import org.apache.log4j.Logger;

public class refreshRefitScript implements EveryFrameScriptWithCleanup {
	private boolean isDone = false;
	private float runTime = 0f;
	private Robot robot = null;
	public Logger logger = null;
	
	@Override
	public boolean runWhilePaused() {
		return true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}
	
	@Override
	public void advance(float amount) {
        CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
        if (tab == null || !tab.equals(CoreUITabId.REFIT)) { isDone = true; return; }

		runTime++;
		try {
			robot = new Robot();
			if (runTime < 5) {
                robot.keyPress(KeyEvent.VK_ENTER);
            } else {
                robot.keyPress(KeyEvent.VK_R);
                robot.keyRelease(KeyEvent.VK_R);
                robot.keyRelease(KeyEvent.VK_ENTER);
				Logger logger = Logger.getLogger("FT");
				logger.info("RR: Refreshed refit tab");
				isDone = true;
				return;
            }
		}
		catch (AWTException ex) {
			return;
		}
	}

	@Override
	public void cleanup() {
		this.isDone = true;
	}
}
