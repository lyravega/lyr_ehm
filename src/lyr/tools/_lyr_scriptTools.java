package lyr.tools;

import static lyr.tools._lyr_uiTools.isRefitTab;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

public class _lyr_scriptTools extends _lyr_reflectionTools {
	//#region INNER CLASS DECLARATION: refreshRefitScript
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
	//#endregion
	// END OF INNER CLASS DECLARATION: refreshRefitScript

	//#region EXPERIMENTS
	/*
	private static remoteInvokerScript remoteInvokerScript;
	
	protected static void remoteInvoker(boolean isStaticMethod, Class<?> clazz, String methodName, Class<?> returnType, List<Object> parameters) {
		for(EveryFrameScript script : Global.getSector().getTransientScripts()) {
			if(script instanceof remoteInvokerScript) {
				remoteInvokerScript = (remoteInvokerScript) script; 
			}
		}

		if (remoteInvokerScript == null) { 
			remoteInvokerScript = new remoteInvokerScript(isStaticMethod, clazz, methodName, returnType, parameters);
		}
	}

	private static class remoteInvokerScript implements EveryFrameScript {
		private boolean isDone = false;
		private MethodHandle methodHandle = null;
		private List<Object> parameters;

		private remoteInvokerScript(boolean isStaticMethod, Class<?> clazz, String methodName, Class<?> returnType, List<Object> parameters) {
			List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

			for (Iterator<Object> i = parameters.iterator(); i.hasNext(); )
				parameterTypes.add(i.next().getClass());

			this.methodHandle = findMethodHandle(isStaticMethod, clazz, methodName, returnType, parameterTypes);

			Global.getSector().addTransientScript(this);
		}

		@Override
		public void advance(float amount) {
			methodHandle.invoke(null);

			remoteInvokerScript = null; // clean the parent
			isDone = true;
			return;
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
	*/
	//#endregion
	// END OF EXPERIMENTS
}
