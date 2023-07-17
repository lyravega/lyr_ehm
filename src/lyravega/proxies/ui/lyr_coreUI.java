package lyravega.proxies.ui;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;

import lyravega.tools.lyr_logger;

public class lyr_coreUI implements lyr_logger {
	private Object coreUI;	// CoreUIAPI, UIPanelAPI, UIComponentAPI
	// private lyr_refitTab refitTab;
	static Class<?> clazz;
	private static MethodHandle getCurrentTab;

	static {
		try {
			clazz = inspectMethod("getCore", lyr_campaignUI.clazz).getReturnType();

			getCurrentTab = inspectMethod("getCurrentTab", clazz).getMethodHandle();	// aimed at refit tab
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_coreUI'", t);
		}
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

	@Deprecated
	public Object getCurrentTab(boolean isDeprecated) {
		try {
			return (Object) getCurrentTab.invoke(coreUI);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getCurrentTab()' in 'lyr_coreUI'", t);
		}	return null;
	}

	// this is currently only utilized for the refit tab
	public lyr_refitTab getCurrentTab() {
		try {
			return new lyr_refitTab(getCurrentTab.invoke(coreUI));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getCurrentTab()' in 'lyr_coreUI'", t);
		}	return null;
	}
}
