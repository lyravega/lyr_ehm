package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class which offers a few proxy methods for the obfuscated core UI class.
 * It is a part of the refit UI, acts as a container mostly for the bottom left buttons.
 * <p> The stored object may be retrieved with {@code retrieve()}, and it implements the
 * {@link UIPanelAPI} and {@link UIComponentAPI} interfaces which may be utilized for
 * additional access.
 * <p> Reconstructed when the refit tab is opened.
 * @author lyravega
 */
public class lyr_designDisplay {
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
			lyr_logger.fatal("Failed to find a method in 'lyr_designDisplay'", t);
		}
	}

	public static lyr_designDisplay proxify() {
		return new lyr_campaignUI().getCore().getCurrentTab().getRefitPanel().getDesignDisplay();
	}

	public lyr_designDisplay(Object designDisplay) {
		this.designDisplay = designDisplay;
	}

	/** @return an object which may be cast on {@link UIPanelAPI} or {@link UIComponentAPI} for partial API access */
	public Object retrieve() {
		return this.designDisplay;
	}

	public void recycle(Object designDisplay) {
		this.designDisplay = designDisplay;
	}

	public void undo() {
		try {
			undo.invoke(this.designDisplay);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'undo()' in 'lyr_designDisplay'", t);
		}
	}

	public ButtonAPI getSaveButton() {
		try {
			return (ButtonAPI) getSaveButton.invoke(this.designDisplay);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getSaveButton()' in 'lyr_designDisplay'", t);
		}	return null;
	}

	public ButtonAPI getUndoButton() {
		try {
			return (ButtonAPI) getUndoButton.invoke(this.designDisplay);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getUndoButton()' in 'lyr_designDisplay'", t);
		}	return null;
	}
}
