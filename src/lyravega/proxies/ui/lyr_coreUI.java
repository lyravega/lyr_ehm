package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class which offers a few proxy methods for the obfuscated core UI class.
 * <p> The stored object may be retrieved with {@code retrieve()}, and it implements the
 * {@link CoreUIAPI}, {@link UIPanelAPI} and {@link UIComponentAPI} interfaces which may
 * be utilized for additional access.
 * <p> If there is no encounter dialogue, then campaign UI's core UI is utilized.
 * Otherwise, each new encounter dialogue constructs its own core UI.
 * @author lyravega
 */
public class lyr_coreUI {
	private Object coreUI;	// CoreUIAPI, UIPanelAPI, UIComponentAPI
	// private lyr_refitTab refitTab;
	static Class<?> clazz;
	private static MethodHandle getCurrentTab;

	static {
		try {
			clazz = methodReflection.findMethodByName("getCore", lyr_campaignUI.clazz).getReturnType();

			getCurrentTab = methodReflection.findMethodByName("getCurrentTab", clazz).getMethodHandle();	// aimed at refit tab
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_coreUI'", t);
		}
	}

	public static lyr_coreUI proxify() {
		return new lyr_campaignUI().getCore();
	}

	public lyr_coreUI(Object coreUI) {
		this.coreUI = coreUI;
	}

	/** @return an object which may be cast on {@link CoreUIAPI}, {@link UIPanelAPI} or {@link UIComponentAPI} for partial API access */
	public Object retrieve() {
		return this.coreUI;
	}

	public void recycle(Object coreUI) {
		this.coreUI = coreUI;
	}

	// this is currently only utilized for the refit tab
	public lyr_refitTab getCurrentTab() {
		try {
			return new lyr_refitTab(getCurrentTab.invoke(this.coreUI));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getCurrentTab()' in 'lyr_coreUI'", t);
		}	return null;
	}
}
