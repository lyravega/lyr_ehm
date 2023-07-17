package lyravega.proxies.ui;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;

import lyravega.tools.lyr_logger;

public class lyr_refitTab implements lyr_logger {
	private Object refitTab;	// UIPanelAPI, UIComponentAPI
	// private lyr_refitTab refitPanel;
	static Class<?> clazz;
	private static MethodHandle getRefitPanel;

	static {
		try {
			clazz = inspectMethod("getRefitTab", lyr_refitPanel.clazz).getReturnType();

			getRefitPanel = inspectMethod("getRefitPanel", clazz).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_refitTab'", t);
		}
	}

	public lyr_refitTab(Object refitTab) {
		this.refitTab = refitTab;
	}

	public Object retrieve() {
		return refitTab;
	}

	public void recycle(Object refitTab) {
		this.refitTab = refitTab;
	}

	public lyr_refitPanel getRefitPanel() {
		try {
			return new lyr_refitPanel(getRefitPanel.invoke(refitTab));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getRefitPanel()' in 'lyr_refitTab'", t);
		}	return null;
	}
}
