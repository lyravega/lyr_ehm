package lyravega.utilities;

import static lyravega.utilities.lyr_interfaceUtilities.isRefitTab;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

public class lyr_scriptUtilities extends lyr_reflectionUtilities {
	public static <T extends EveryFrameScript> List<T> getScriptsOfClass(Class<T> clazz) {
		List<T> scripts = new ArrayList<T>();

		for(EveryFrameScript script : Global.getSector().getScripts()) {
			if (clazz.isInstance(script)) scripts.add(clazz.cast(script));
		}

		return scripts;
	}

	public static <T extends EveryFrameScript> List<T> getTransientScriptsOfClass(Class<T> clazz) {
		List<T> transientScripts = new ArrayList<T>();

		for(EveryFrameScript transientScript : Global.getSector().getTransientScripts()) {
			if (clazz.isInstance(transientScript)) transientScripts.add(clazz.cast(transientScript));
		}

		return transientScripts;
	}

	//#region INNER CLASS: refreshRefitScript
	private static refreshRefitScript refreshRefitScript;

	public static void refreshRefit() {
		if (!isRefitTab()) return;

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
			if (!isRefitTab()) { isDone = true; return; }

			frameCount++;
			if (frameCount < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				isDone = true;
				refreshRefitScript = null; // clean the parent
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
	//#endregion
	// END OF INNER CLASS: refreshRefitScript
}
