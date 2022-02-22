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
	private float frameCount = 0f;
	private Robot robot = null;
	public Logger logger = null;

	public refreshRefitScript() {
		Global.getSector().addTransientScript(this);
	}
	
	@Override
	public void advance(float amount) {
		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		if (tab == null || !tab.equals(CoreUITabId.REFIT)) { isDone = true; return; }

		try { frameCount++;
			robot = new Robot();
			if (frameCount < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				Logger logger = Logger.getLogger("lyr");
				logger.info("RR: Refreshed refit tab");
				Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
				isDone = true;
				return;
			}
		}
		catch (AWTException ex) {
			return;
		}
	}

	@Override
	public boolean runWhilePaused() {
		return true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void cleanup() {
		this.isDone = true;
	}
}
