package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

public class lyr_refitPanel {
	private Object refitPanel;	// UIPanelAPI, UIComponentAPI
	// private lyr_designDisplay designDisplay;
	// private lyr_shipDisplay shipDisplay;
	// private Object member;
	static Class<?> clazz;
	private static MethodHandle getDesignDisplay;
	private static MethodHandle getShipDisplay;
	private static MethodHandle saveCurrentVariant; // there is an overload for this, beware
	private static MethodHandle getMember;
	private static MethodHandle syncWithCurrentVariant;
	private static MethodHandle isEditedSinceLoad;
	private static MethodHandle isEditedSinceSave;
	private static MethodHandle setEditedSinceLoad;
	private static MethodHandle setEditedSinceSave;
	private static MethodHandle addAllWeaponsFromVariantToCargo;

	static {
		try {
			clazz = methodReflection.findMethodByName("notifyFleetMemberChanged", lyr_campaignUI.clazz).getParameterTypes()[0];

			getDesignDisplay = methodReflection.findMethodByName("getDesignDisplay", clazz).getMethodHandle();
			getShipDisplay = methodReflection.findMethodByName("getShipDisplay", clazz).getMethodHandle();
			saveCurrentVariant = methodReflection.findMethodByName("saveCurrentVariant", clazz, boolean.class).getMethodHandle();
			getMember = methodReflection.findMethodByName("getMember", clazz).getMethodHandle();
			syncWithCurrentVariant = methodReflection.findMethodByName("syncWithCurrentVariant", clazz, boolean.class).getMethodHandle();
			isEditedSinceLoad = methodReflection.findMethodByName("isEditedSinceLoad", clazz).getMethodHandle();
			isEditedSinceSave = methodReflection.findMethodByName("isEditedSinceSave", clazz).getMethodHandle();
			setEditedSinceLoad = methodReflection.findMethodByName("setEditedSinceLoad", clazz).getMethodHandle();
			setEditedSinceSave = methodReflection.findMethodByName("setEditedSinceSave", clazz).getMethodHandle();
			addAllWeaponsFromVariantToCargo = methodReflection.findMethodByName("addAllWeaponsFromVariantToCargo", clazz).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_refitPanel'", t);
		}
	}

	public lyr_refitPanel() {
		this.refitPanel = new lyr_campaignUI().getCore().getCurrentTab().getRefitPanel();
	}

	public lyr_refitPanel(Object refitPanel) {
		this.refitPanel = refitPanel;
	}

	public Object retrieve() {
		return this.refitPanel;
	}

	public void recycle(Object refitPanel) {
		this.refitPanel = refitPanel;
	}

	public lyr_designDisplay getDesignDisplay() {
		try {
			return new lyr_designDisplay(getDesignDisplay.invoke(this.refitPanel));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getDesignDisplay()' in 'lyr_refitPanel'", t);
		}	return null;
	}

	public lyr_shipDisplay getShipDisplay() {
		try {
			return new lyr_shipDisplay(getShipDisplay.invoke(this.refitPanel));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getShipDisplay()' in 'lyr_refitPanel'", t);
		}	return null;
	}

	public void saveCurrentVariant() {
		try {
			saveCurrentVariant.invoke(this.refitPanel, false);	// if the boolean here is true, "Financial Transaction Confirmed" message will be shown
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'saveCurrentVariant()' in 'lyr_refitPanel'", t);
		}
	}

	public FleetMemberAPI getMember() {
		try {
			return (FleetMemberAPI) getMember.invoke(this.refitPanel);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getMember()' in 'lyr_refitPanel'", t);
		}	return null;
	}

	public void syncWithCurrentVariant() {
		try {
			syncWithCurrentVariant.invoke(this.refitPanel, true);	// if the boolean here is false, isEditedSinceLoad() is set to true
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'syncWithCurrentVariant()' in 'lyr_refitPanel'", t);
		}
	}

	public boolean isEditedSinceLoad() {
		try {
			return (boolean) isEditedSinceLoad.invoke(this.refitPanel);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'isEditedSinceLoad()' in 'lyr_refitPanel'", t);
		}	return false;
	}

	public boolean isEditedSinceSave() {
		try {
			return (boolean) isEditedSinceSave.invoke(this.refitPanel);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'isEditedSinceSave()' in 'lyr_refitPanel'", t);
		}	return false;
	}

	public void setEditedSinceLoad(boolean isEditedSinceLoad) {
		try {
			setEditedSinceLoad.invoke(this.refitPanel, isEditedSinceLoad);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setEditedSinceLoad()' in 'lyr_refitPanel'", t);
		}
	}

	public void setEditedSinceSave(boolean isEditedSinceSave) {
		try {
			setEditedSinceSave.invoke(this.refitPanel, isEditedSinceSave);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setEditedSinceSave()' in 'lyr_refitPanel'", t);
		}
	}

	public void addAllWeaponsFromVariantToCargo(Object variant) {
		try {
			addAllWeaponsFromVariantToCargo.invoke(this.refitPanel, variant);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'addAllWeaponsFromVariantToCargo()' in 'lyr_refitPanel'", t);
		}
	}
}
