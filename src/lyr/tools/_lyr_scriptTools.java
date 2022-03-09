package lyr.tools;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;

public class _lyr_scriptTools {
	private static refreshRefitScript refreshRefitScript;

	protected static void refreshRefit() {
		for(EveryFrameScript script : Global.getSector().getTransientScripts()) {
			if(script instanceof refreshRefitScript) {
				refreshRefitScript = (refreshRefitScript) script; 
			}
		}

		if (refreshRefitScript == null) { 
			refreshRefitScript = new refreshRefitScript();
		}
	}

	private static class refreshRefitScript implements EveryFrameScript {
		private boolean isDone = false;
		private float frameCount = 0f;
		private static Robot robot;

		static {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}

		private refreshRefitScript() {
			Global.getSector().addTransientScript(this);
		}
		
		@Override
		public void advance(float amount) {
			CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
			if (tab == null || !tab.equals(CoreUITabId.REFIT)) { isDone = true; return; }
	
			frameCount++;
			if (frameCount < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				refreshRefitScript = null; // clean the parent
				isDone = true;
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
	}
}
