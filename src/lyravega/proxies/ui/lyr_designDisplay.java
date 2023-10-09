package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.ui.ButtonAPI;

import lyravega.tools.lyr_logger;
import lyravega.tools.lyr_reflectionTools.methodReflection;

public class lyr_designDisplay implements lyr_logger {
	private Object designDisplay;	// UIPanelAPI, UIComponentAPI
	static Class<?> clazz;
	private static MethodHandle undo;
	private static MethodHandle getSaveButton;
	private static MethodHandle getUndoButton;

	static {
		try {
			clazz = methodReflection.findMethodByName("getDesignDisplay", lyr_refitPanel.clazz).getReturnType();

			undo = methodReflection.findMethodByName("undo", clazz).getMethodHandle(); // not used anymore because fucks up for ships with officers
			getSaveButton = methodReflection.findMethodByName("getSaveButton", clazz).getMethodHandle();
			getUndoButton = methodReflection.findMethodByName("getUndoButton", clazz).getMethodHandle();
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

	public ButtonAPI getSaveButton() {
		try {
			return (ButtonAPI) getSaveButton.invoke(designDisplay);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getSaveButton()' in 'lyr_designDisplay'", t);
		}	return null;
	}

	public ButtonAPI getUndoButton() {
		try {
			return (ButtonAPI) getUndoButton.invoke(designDisplay);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getUndoButton()' in 'lyr_designDisplay'", t);
		}	return null;
	}
}
