package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import lyravega.utilities.logger.lyr_logger;
import lyravega.utilities.lyr_reflectionUtilities.methodReflection;

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

	public lyr_coreUI() {
		this.coreUI = new lyr_campaignUI().getCore();
	}

	public lyr_coreUI(Object coreUI) {
		this.coreUI = coreUI;
	}

	public Object retrieve() {
		return coreUI;
	}

	public void recycle(Object coreUI) {
		this.coreUI = coreUI;
	}

	// this is currently only utilized for the refit tab
	public lyr_refitTab getCurrentTab() {
		try {
			return new lyr_refitTab(getCurrentTab.invoke(coreUI));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getCurrentTab()' in 'lyr_coreUI'", t);
		}	return null;
	}
}
