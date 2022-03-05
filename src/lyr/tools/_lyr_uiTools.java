package lyr.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;

import data.hullmods.ehm_base;

/**
 * Provides specialized MethodHandles and a few methods that 
 * are designed to be used on the Starsector UI, which is 
 * why these are splitted from the parent class.
 * @author lyravega
 */
public class _lyr_uiTools extends _lyr_reflectionTools {
	private static Class<?> coreClass = null;
	private static Class<?> refitPanelClass = null;
	private static Object core = null;
	private static Object refitPanel = null;

	private static void findRefitPanelClass() throws ClassNotFoundException { // TODO: use methodhandles to get that shit
		String refitPanelClassName = null;
		for (Object method : Global.getSector().getCampaignUI().getClass().getDeclaredMethods()) {
			if (!method.toString().contains("notifyFleetMemberChanged")) continue;
			
			refitPanelClassName = method.toString().split(" ")[2]; break;
		}
		refitPanelClassName = refitPanelClassName.substring(refitPanelClassName.indexOf("(")+1, refitPanelClassName.length()-1).split(",")[0];
		refitPanelClass = Class.forName(refitPanelClassName, false, ehm_base.class.getClassLoader());
	}

	private static void findCoreClass() throws ClassNotFoundException { // TODO: fuck core, not used. delete after sleep
		String coreClassName = null;
		for (Object method : Global.getSector().getCampaignUI().getClass().getDeclaredMethods()) {
			if (!method.toString().contains("getCore")) continue;
			
			coreClassName = method.toString().split(" ")[1]; break;
		}
		coreClass = Class.forName(coreClassName, false, ehm_base.class.getClassLoader());
	}

	private static Object recursiveSearch_findObjectWithClass(Object object, Class<?> targetClass) { // TODO: mimic the other overload
		if (object.getClass().equals(targetClass)) return object;

		try {
			MethodHandle getChildrenCopy = lookup.findVirtual(object.getClass(), "getChildrenCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) 
				if (recursiveSearch_findObjectWithClass(child, targetClass) != null) return child; 
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	private static Object recursiveSearch_findObjectWithMethod(Object object, String methodName) {
		try { // search the current object for the methodName
			if (getDeclaredMethod.invoke(object.getClass(), methodName) != null) return object;
		} catch (Throwable t) {
			// no catch on purpose
		}

		try { // if not found, search children of the object recursively
			MethodHandle getChildrenCopy = lookup.findVirtual(object.getClass(), "getChildrenCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) 
				if (recursiveSearch_findObjectWithMethod(child, methodName) != null) return child; 
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	public static void refreshRefitShip() {
		try {
			findCoreClass();
			findRefitPanelClass();

			Map<String, Object> getDialogParentMap = _lyr_reflectionTools.findTypesForMethod(Global.getSector().getCampaignUI().getClass(), "getDialogParent");
			Object screenPanel = MethodHandle.class.cast(getDialogParentMap.get("methodHandle")).invoke(Global.getSector().getCampaignUI());

			Object refitPanel = recursiveSearch_findObjectWithClass(screenPanel, refitPanelClass); // TODO: find proper path

			MethodHandle saveCurrentVariant = lookup.findVirtual(refitPanelClass, "saveCurrentVariant", MethodType.methodType(void.class, boolean.class));
			saveCurrentVariant.invoke(refitPanel, false);

			Object button = recursiveSearch_findObjectWithMethod(refitPanel, "undo"); // TODO: find proper path

			Map<String, Object> undoMap = _lyr_reflectionTools.findTypesForMethod(button.getClass(), "undo");
			MethodHandle.class.cast(undoMap.get("methodHandle")).invoke(button);
		} catch (Throwable t) {

		}
	}
}
