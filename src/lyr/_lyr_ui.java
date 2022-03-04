package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.combat.ShipSystemSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import data.hullmods.ehm_base;

public class _lyr_ui {
	private static Class<?> coreClass = null;
	private static Class<?> refitPanelClass = null;
	private static Object core = null;
	private static Object refitPanel = null;

	private static Lookup lookup = MethodHandles.lookup();

	public static void findRefitPanelClass() throws ClassNotFoundException {
		String refitPanelClassName = null;
		for (Object method : Global.getSector().getCampaignUI().getClass().getDeclaredMethods()) {
			if (!method.toString().contains("notifyFleetMemberChanged")) continue;
			
			refitPanelClassName = method.toString().split(" ")[2]; break;
		}
		refitPanelClassName = refitPanelClassName.substring(refitPanelClassName.indexOf("(")+1, refitPanelClassName.length()-1).split(",")[0];
		refitPanelClass = Class.forName(refitPanelClassName, false, ehm_base.class.getClassLoader());
	}

	public static void findCoreClass() throws ClassNotFoundException {
		String coreClassName = null;
		for (Object method : Global.getSector().getCampaignUI().getClass().getDeclaredMethods()) {
			if (!method.toString().contains("getCore")) continue;
			
			coreClassName = method.toString().split(" ")[1]; break;
		}
		coreClass = Class.forName(coreClassName, false, ehm_base.class.getClassLoader());
	}

	private static Object recursiveChildrenSearch(Object object, Class<?> targetClass) {
		Object targetObject = null;

		try {
			MethodHandle getChildrenCopy = MethodHandles.lookup().findVirtual(object.getClass(), "getChildrenCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) {
				if (child.getClass().equals(targetClass)) return child;
			}
	
			for (Object child : children) {
				targetObject = recursiveChildrenSearch(child, targetClass);
				if (targetObject != null) return targetObject; 
			}
		} catch (Throwable t) {
			//
		}

		return targetObject;
	}

	private static Object recursiveChildrenSearch(Object object, String methodName) {
		Object targetObject = null;

		try {
			MethodHandle getChildrenCopy = MethodHandles.lookup().findVirtual(object.getClass(), "getChildrenCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) {
				for (Object method : child.getClass().getDeclaredMethods())
					if (method.toString().contains(methodName)) return child;
			}
	
			for (Object child : children) {
				targetObject = recursiveChildrenSearch(child, methodName);
				if (targetObject != null) return targetObject; 
			}
		} catch (Throwable t) {
			//
		}

		return targetObject;
	}

	public static void refreshRefitShip() {
		try {
			findCoreClass();
			findRefitPanelClass();

			Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
			Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());

			Map<String, Object> getDialogParentMap = _lyr_finder.findTypesForMethod(Global.getSector().getCampaignUI().getClass(), "getDialogParent");
			Object screenPanel = MethodHandle.class.cast(getDialogParentMap.get("methodHandle")).invoke(Global.getSector().getCampaignUI());

			Object refitPanel = recursiveChildrenSearch(screenPanel, refitPanelClass);

			MethodHandle saveCurrentVariant = MethodHandles.lookup().findVirtual(refitPanelClass, "saveCurrentVariant", MethodType.methodType(void.class, boolean.class));
			saveCurrentVariant.invoke(refitPanel, true);

			Object button = recursiveChildrenSearch(refitPanel, "undo");

			Map<String, Object> undoMap = _lyr_finder.findTypesForMethod(button.getClass(), "undo");
			MethodHandle.class.cast(undoMap.get("methodHandle")).invoke(button);
		} catch (Throwable t) {

		}
	}
}
