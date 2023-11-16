package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import lyravega.utilities.lyr_reflectionUtilities.fieldReflection;
import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class which offers a few proxy methods for the obfuscated refit tab class.
 * It is a part of the refit UI, responsible for the whole refit tab.
 * <p> The stored object may be retrieved with {@code retrieve()}, and it implements the
 * {@link UIPanelAPI} and {@link UIComponentAPI} interfaces from the API which may be
 * utilized for additional access.
 * <p> Reconstructed when the refit tab is opened.
 * @author lyravega
 */
public class lyr_refitTab {
	private Object refitTab;	// UIPanelAPI, UIComponentAPI
	// private lyr_refitPanel refitPanel;
	static Class<?> clazz;
	private static MethodHandle getRefitPanel;
	private static MethodHandle getParentData;
	private static MethodHandle goBackToParentIfNeeded;

	static {
		try {
			clazz = methodReflection.findMethodByName("getRefitTab", lyr_refitPanel.clazz).getReturnType();

			getRefitPanel = methodReflection.findMethodByName("getRefitPanel", clazz).getMethodHandle();
			getParentData = methodReflection.findMethodByName("getParentData", clazz).getMethodHandle();
			goBackToParentIfNeeded = methodReflection.findMethodByName("goBackToParentIfNeeded", clazz).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_refitTab'", t);
		}
	}

	public static lyr_refitTab proxify() {
		return new lyr_campaignUI().getCore().getCurrentTab();
	}

	public lyr_refitTab(Object refitTab) {
		this.refitTab = refitTab;
	}

	/** @return an object which may be cast on {@link UIPanelAPI} or {@link UIComponentAPI} for partial API access */
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
			return new lyr_parentData(getParentData.invoke(this.refitTab));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getParentData()' in 'lyr_refitTab'", t);
		}	return null;
	}

	public void goBackToParentIfNeeded() {
		try {
			goBackToParentIfNeeded.invoke(this.refitTab);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'goBackToParentIfNeeded()' in 'lyr_refitTab'", t);
		}
	}

	public static class lyr_parentData {
		private final FleetMemberAPI member; public FleetMemberAPI getMember() { return this.member; }
		private final WeaponSlotAPI weaponSlot; public WeaponSlotAPI getWeaponSlot() { return this.weaponSlot; }

		private lyr_parentData(final Object parentData) throws Throwable {
			this.member = (FleetMemberAPI) fieldReflection.findFieldByClass(FleetMemberAPI.class, parentData).get();
			this.weaponSlot = (WeaponSlotAPI) fieldReflection.findFieldByClass(WeaponSlotAPI.class, parentData).get();
		}
	}
}
