package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;

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

	private static List<Object> objectPath = new LinkedList<Object>();

	private static Object recursiveChildrenSearch(Object object, String methodName) {
		try { // search the current object for the methodName
			Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
			MethodHandle getDeclaredMethod = MethodHandles.lookup().findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, Class[].class));
			if (getDeclaredMethod.invoke(object.getClass(), methodName) != null) return object;
		} catch (Throwable t) {
			// no catch, fuck you
		}

		try { // if not found, search children of the object recursively
			MethodHandle getChildrenCopy = MethodHandles.lookup().findVirtual(object.getClass(), "getChildrenCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) 
				if (recursiveChildrenSearch(child, methodName) != null) return child; 
		} catch (Throwable t) {
			// no catch, fuck you twice
		}

		return null;
	}

	public static void refreshRefitShip() {
		try {
			findCoreClass();
			findRefitPanelClass();

			Map<String, Object> getDialogParentMap = _lyr_finder.findTypesForMethod(Global.getSector().getCampaignUI().getClass(), "getDialogParent");
			Object screenPanel = MethodHandle.class.cast(getDialogParentMap.get("methodHandle")).invoke(Global.getSector().getCampaignUI());

			Object refitPanel = recursiveChildrenSearch(screenPanel, refitPanelClass); // TODO: find proper path

			MethodHandle saveCurrentVariant = MethodHandles.lookup().findVirtual(refitPanelClass, "saveCurrentVariant", MethodType.methodType(void.class, boolean.class));
			saveCurrentVariant.invoke(refitPanel, false);

			Object button = recursiveChildrenSearch(refitPanel, "undo"); // TODO: find proper path

			Map<String, Object> undoMap = _lyr_finder.findTypesForMethod(button.getClass(), "undo");
			MethodHandle.class.cast(undoMap.get("methodHandle")).invoke(button);
		} catch (Throwable t) {

		}
	}
}
