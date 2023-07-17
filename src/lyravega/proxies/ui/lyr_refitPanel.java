package lyravega.proxies.ui;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

import lyravega.tools.lyr_logger;

public class lyr_refitPanel implements lyr_logger {
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
	private static MethodHandle setEditedSinceLoad;
	private static MethodHandle setEditedSinceSave;
	private static MethodHandle addAllWeaponsFromVariantToCargo;

	static {
		try {
			clazz = inspectMethod("notifyFleetMemberChanged", lyr_campaignUI.clazz).getParameterTypes()[0];

			getDesignDisplay = inspectMethod("getDesignDisplay", clazz).getMethodHandle();
			getShipDisplay = inspectMethod("getShipDisplay", clazz).getMethodHandle();
			saveCurrentVariant = inspectMethod("saveCurrentVariant", clazz, boolean.class).getMethodHandle();
			getMember = inspectMethod("getMember", clazz).getMethodHandle();
			syncWithCurrentVariant = inspectMethod("syncWithCurrentVariant", clazz, boolean.class).getMethodHandle();
			setEditedSinceLoad = inspectMethod("setEditedSinceLoad", clazz).getMethodHandle();
			setEditedSinceSave = inspectMethod("setEditedSinceSave", clazz).getMethodHandle();
			addAllWeaponsFromVariantToCargo = inspectMethod("addAllWeaponsFromVariantToCargo", clazz).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_refitPanel'", t);
		}
	}

	public lyr_refitPanel(Object refitPanel) {
		this.refitPanel = refitPanel;
	}

	public Object retrieve() {
		return refitPanel;
	}

	public void recycle(Object refitPanel) {
		this.refitPanel = refitPanel;
	}

	public lyr_designDisplay getDesignDisplay() {
		try {
			return new lyr_designDisplay(getDesignDisplay.invoke(refitPanel));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getDesignDisplay()' in 'lyr_refitPanel'", t);
		}	return null;
	}

	public lyr_shipDisplay getShipDisplay() {
		try {
			return new lyr_shipDisplay(getShipDisplay.invoke(refitPanel));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getShipDisplay()' in 'lyr_refitPanel'", t);
		}	return null;
	}

	public void saveCurrentVariant() {
		try {
			saveCurrentVariant.invoke(refitPanel, false);	// if the boolean here is true, "Financial Transaction Confirmed" message will be shown
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'saveCurrentVariant()' in 'lyr_refitPanel'", t);
		}
	}

	public FleetMemberAPI getMember() {
		try {
			return (FleetMemberAPI) getMember.invoke(refitPanel);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getMember()' in 'lyr_refitPanel'", t);
		}	return null;
	}

	public void syncWithCurrentVariant() {
		try {
			syncWithCurrentVariant.invoke(refitPanel, true);	// if the boolean here is false, isEditedSinceLoad() is set to true
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'syncWithCurrentVariant()' in 'lyr_refitPanel'", t);
		}
	}

	public void setEditedSinceLoad(boolean isEditedSinceLoad) {
		try {
			setEditedSinceLoad.invoke(refitPanel, isEditedSinceLoad);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setEditedSinceLoad()' in 'lyr_refitPanel'", t);
		}
	}

	public void setEditedSinceSave(boolean isEditedSinceSave) {
		try {
			setEditedSinceSave.invoke(refitPanel, isEditedSinceSave);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setEditedSinceSave()' in 'lyr_refitPanel'", t);
		}
	}

	public void addAllWeaponsFromVariantToCargo(Object variant) {
		try {
			addAllWeaponsFromVariantToCargo.invoke(refitPanel, variant);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'addAllWeaponsFromVariantToCargo()' in 'lyr_refitPanel'", t);
		}
	}
}
