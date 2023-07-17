package lyravega.proxies.ui;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;

import lyravega.tools.lyr_logger;

public class lyr_shipDisplay implements lyr_logger {
	private Object shipDisplay;		// UIPanelAPI, UIComponentAPI
	static Class<?> clazz;
	private static MethodHandle setFleetMember;
	private static MethodHandle getCurrentVariant;

	static {
		try {
			clazz = inspectMethod("getShipDisplay", lyr_refitPanel.clazz).getReturnType();

			setFleetMember = inspectMethod("setFleetMember", clazz).getMethodHandle();
			getCurrentVariant = inspectMethod("getCurrentVariant", clazz).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_shipDisplay'", t);
		}
	}

	public lyr_shipDisplay(Object shipDisplay) {
		this.shipDisplay = shipDisplay;
	}

	public Object retrieve() {
		return shipDisplay;
	}

	public void recycle(Object shipDisplay) {
		this.shipDisplay = shipDisplay;
	}

	public void setFleetMember(Object fleetMember, Object variant) {
		try {
			setFleetMember.invoke(shipDisplay, fleetMember, variant);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setFleetMember()' in 'lyr_shipDisplay'", t);
		}
	}

	public Object getCurrentVariant() {
		try {
			return getCurrentVariant.invoke(shipDisplay);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getCurrentVariant()' in 'lyr_shipDisplay'", t);
		}	return null;
	}
}
