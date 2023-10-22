package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

public class lyr_shipDisplay {
	private Object shipDisplay;		// UIPanelAPI, UIComponentAPI
	static Class<?> clazz;
	private static MethodHandle setFleetMember;
	private static MethodHandle getCurrentVariant;
	private static MethodHandle getShip;
	private static MethodHandle clearFighterSlot;

	static {
		try {
			clazz = methodReflection.findMethodByName("getShipDisplay", lyr_refitPanel.clazz).getReturnType();

			setFleetMember = methodReflection.findMethodByName("setFleetMember", clazz).getMethodHandle();
			getCurrentVariant = methodReflection.findMethodByName("getCurrentVariant", clazz).getMethodHandle();
			getShip = methodReflection.findMethodByName("getShip", clazz).getMethodHandle();
			clearFighterSlot = methodReflection.findMethodByName("clearFighterSlot", clazz).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_shipDisplay'", t);
		}
	}

	public static lyr_shipDisplay proxify() {
		return new lyr_campaignUI().getCore().getCurrentTab().getRefitPanel().getShipDisplay();
	}

	public lyr_shipDisplay(Object shipDisplay) {
		this.shipDisplay = shipDisplay;
	}

	public Object retrieve() {
		return this.shipDisplay;
	}

	public void recycle(Object shipDisplay) {
		this.shipDisplay = shipDisplay;
	}

	public void setFleetMember(Object fleetMember, Object variant) {
		try {
			setFleetMember.invoke(this.shipDisplay, fleetMember, variant);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setFleetMember()' in 'lyr_shipDisplay'", t);
		}
	}

	public ShipVariantAPI getCurrentVariant() {
		try {
			return (ShipVariantAPI) getCurrentVariant.invoke(this.shipDisplay);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getCurrentVariant()' in 'lyr_shipDisplay'", t);
		}	return null;
	}

	public ShipAPI getShip() {
		try {
			return (ShipAPI) getShip.invoke(this.shipDisplay);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getShip()' in 'lyr_shipDisplay'", t);
		}	return null;
	}

	public void clearFighterSlot(int fighterSlot, ShipVariantAPI variant) {
		try {
			clearFighterSlot.invoke(this.shipDisplay, fighterSlot, variant);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'clearFighterSlot()' in 'lyr_shipDisplay'", t);
		}
	}
}
