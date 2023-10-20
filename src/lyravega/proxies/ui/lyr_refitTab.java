package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

public class lyr_refitTab {
	private Object refitTab;	// UIPanelAPI, UIComponentAPI
	// private lyr_refitPanel refitPanel;
	static Class<?> clazz;
	private static MethodHandle getRefitPanel;
	private static MethodHandle getParentData;

	static {
		try {
			clazz = methodReflection.findMethodByName("getRefitTab", lyr_refitPanel.clazz).getReturnType();

			getRefitPanel = methodReflection.findMethodByName("getRefitPanel", clazz).getMethodHandle();
			getParentData = methodReflection.findMethodByName("getParentData", clazz).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_refitTab'", t);
		}
	}

	public lyr_refitTab() {
		this.refitTab = new lyr_campaignUI().getCore().getCurrentTab().retrieve();
	}

	public lyr_refitTab(Object refitTab) {
		this.refitTab = refitTab;
	}

	public Object retrieve() {
		return this.refitTab;
	}

	public void recycle(Object refitTab) {
		this.refitTab = refitTab;
	}

	public lyr_refitPanel getRefitPanel() {
		try {
			return new lyr_refitPanel(getRefitPanel.invoke(this.refitTab));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getRefitPanel()' in 'lyr_refitTab'", t);
		}	return null;
	}

	public Object getParentData() {
		try {
			return getParentData.invoke(this.refitTab);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getParentData()' in 'lyr_refitTab'", t);
		}	return null;
	}
}
