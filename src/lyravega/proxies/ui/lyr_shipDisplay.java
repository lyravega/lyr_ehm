package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.tools.lyr_logger;
import lyravega.tools.lyr_reflectionTools;

public class lyr_shipDisplay implements lyr_logger {
	private Object shipDisplay;		// UIPanelAPI, UIComponentAPI
	static Class<?> clazz;
	private static MethodHandle setFleetMember;
	private static MethodHandle getCurrentVariant;
	private static MethodHandle clearFighterSlot;

	static {
		try {
			clazz = lyr_reflectionTools.findMethodByName("getShipDisplay", lyr_refitPanel.clazz).getReturnType();

			setFleetMember = lyr_reflectionTools.findMethodByName("setFleetMember", clazz).getMethodHandle();
			getCurrentVariant = lyr_reflectionTools.findMethodByName("getCurrentVariant", clazz).getMethodHandle();
			clearFighterSlot = lyr_reflectionTools.findMethodByName("clearFighterSlot", clazz).getMethodHandle();
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

	public ShipVariantAPI getCurrentVariant() {
		try {
			return (ShipVariantAPI) getCurrentVariant.invoke(shipDisplay);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getCurrentVariant()' in 'lyr_shipDisplay'", t);
		}	return null;
	}

	public void clearFighterSlot(int fighterSlot, ShipVariantAPI variant) {
		try {
			clearFighterSlot.invoke(shipDisplay, fighterSlot, variant);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'clearFighterSlot()' in 'lyr_shipDisplay'", t);
		}
	}
}
