package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.utilities.lyr_reflectionUtilities.fieldReflection;
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

	public lyr_parentData getParentData() {
		try {
			Object parentData = getParentData.invoke(this.refitTab);
			if (parentData != null)	return new lyr_parentData(getParentData.invoke(this.refitTab));
			else return null;
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getParentData()' in 'lyr_refitTab'", t);
		}	return null;
	}

	public static class lyr_parentData {
		private final FleetMemberAPI member;

		private final WeaponSlotAPI weaponSlot;

		private lyr_parentData(final Object parentData) throws Throwable {
			this.member = (FleetMemberAPI) fieldReflection.findFieldByClass(FleetMemberAPI.class, parentData).get();
			this.weaponSlot = (WeaponSlotAPI) fieldReflection.findFieldByClass(WeaponSlotAPI.class, parentData).get();
		}

		public FleetMemberAPI getMember() { return this.member; }

		public WeaponSlotAPI getWeaponSlot() { return this.weaponSlot; }
	}
}
