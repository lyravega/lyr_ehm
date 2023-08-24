package lyravega.proxies.ui;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;

import lyravega.tools.lyr_logger;

public class lyr_designDisplay implements lyr_logger {
	private Object designDisplay;	// UIPanelAPI, UIComponentAPI
	static Class<?> clazz;
	private static MethodHandle undo;

	static {
		try {
			clazz = inspectMethod("getDesignDisplay", lyr_designDisplay.clazz).getReturnType();

			undo = inspectMethod("undo", clazz).getMethodHandle(); // not used anymore because fucks up for ships with officers
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_designDisplay'", t);
		}
	}

	public lyr_designDisplay(Object designDisplay) {
		this.designDisplay = designDisplay;
	}

	public Object retrieve() {
		return designDisplay;
	}

	public void recycle(Object designDisplay) {
		this.designDisplay = designDisplay;
	}

	public void undo() {
		try {
			undo.invoke(designDisplay);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'undo()' in 'lyr_designDisplay'", t);
		}
	}
}