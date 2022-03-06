package lyr.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;

/**
 * Provides specialized MethodHandles and a few methods that 
 * are designed to be used on the Starsector UI, which is 
 * why these are splitted from the parent class.
 * @author lyravega
 */
@SuppressWarnings("unused")
public class _lyr_uiTools extends _lyr_reflectionTools {
	private static Class<?> campaignUIClass;
	private static Class<?> screenPanelClass;
	private static Class<?> encounterDialogueClass;
	private static Class<?> coreClass;
	private static Class<?> wrapperClass;
	private static Class<?> refitTabClass;
	private static Class<?> refitPanelClass;
	private static Class<?> designDisplayClass;

	private static methodMap campaignUI_getScreenPanel;
	private static methodMap campaignUI_getEncounterDialog;
	private static methodMap campaignUI_getCore;
	private static methodMap encounterDialog_getCoreUI;
	private static methodMap refitTab_getRefitPanel;
	private static methodMap refitPanel_getDesignDisplay;
	
	private static methodMap designDisplay_undo;
	private static methodMap refitPanel_saveCurrentVariant;

	public static class _lyr_delayedFinder implements EveryFrameScript {
		private boolean isDone = false;

		public _lyr_delayedFinder() {
			logger.info("Waiting to find the UI classes");
		}

		@Override
		public void advance(float amount) {
			try {
				CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
				if (tab == null || !tab.equals(CoreUITabId.REFIT)) return; 

				campaignUIClass = Global.getSector().getCampaignUI().getClass();
				screenPanelClass = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getScreenPanel").getReturnType();
				encounterDialogueClass = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getEncounterDialog").getReturnType();
				coreClass = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getCore").getReturnType();
				refitPanelClass = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "notifyFleetMemberChanged").getParameterTypes()[0]; 
				refitTabClass = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "getRefitTab").getReturnType();
				designDisplayClass = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "getDesignDisplay").getReturnType();

				// same shit used above, defined here separately to keep them tidy because 'MUH GAEMUR OCD'
				campaignUI_getScreenPanel = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getScreenPanel");
				campaignUI_getEncounterDialog = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getEncounterDialog");
				campaignUI_getCore = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getCore");
				encounterDialog_getCoreUI = _lyr_reflectionTools.findTypesForMethod(encounterDialogueClass, "getCoreUI");
				refitTab_getRefitPanel = _lyr_reflectionTools.findTypesForMethod(refitTabClass, "getRefitPanel");
				refitPanel_getDesignDisplay = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "getDesignDisplay");
				
				designDisplay_undo = _lyr_reflectionTools.findTypesForMethod(designDisplayClass, "undo");
				refitPanel_saveCurrentVariant = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "saveCurrentVariant"); // there is an overload for this, beware
				
				// redoing stuff just to find the wrapper, or whatever the fuck it is
				Object campaignUI = Global.getSector().getCampaignUI();
				Object screenPanel = campaignUI_getScreenPanel.getMethodHandle().invoke(campaignUI);
				Object encounterDialogue = campaignUI_getEncounterDialog.getMethodHandle().invoke(campaignUI);
				Object core = (encounterDialogue != null) ? encounterDialog_getCoreUI.getMethodHandle().invoke(encounterDialogue) : campaignUI_getCore.getMethodHandle().invoke(campaignUI);
				Object wrapper = adaptiveSearch_findObjectWithChildClass(core, refitTabClass, false, 1);
				Object refitTab = adaptiveSearch_findObjectWithChildClass(wrapper, refitPanelClass, false, 1);
				Object refitPanel = refitTab_getRefitPanel.getMethodHandle().invoke(refitTab);
				Object designDisplay = refitPanel_getDesignDisplay.getMethodHandle().invoke(refitPanel);
				
				// campaignUIClass = campaignUI.getClass();					// com.fs.starfarer.campaign.CampaignState;
				// screenPanelClass = screenPanel.getClass();				// com.fs.starfarer.ui.v;
				// encounterDialogueClass = encounterDialogue.getClass();	// com.fs.starfarer.ui.newui.o0Oo;
				// coreClass = core.getClass();								// com.fs.starfarer.ui.newui.OO0O;
				wrapperClass = wrapper.getClass();							// com.fs.starfarer.ui.newui.o0OO;
				// refitTabClass = refitTab.getClass();						// com.fs.starfarer.coreui.refit.F;
				// refitPanelClass = refitPanel.getClass();					// com.fs.starfarer.coreui.refit.V;
				// designDisplayClass = designDisplay.getClass();			// com.fs.starfarer.coreui.refit.oOOo;

				logger.info("Found the classes"); 
				isDone = true; return;
			} catch (Throwable t) {
				logger.error("Failed to find the classes"); t.printStackTrace(); 
				isDone = true; return;
			}
		}
	
		@Override
		public boolean runWhilePaused() {
			return true;
		}
	
		@Override
		public boolean isDone() {
			return isDone;
		}
	}

	private static Object recursiveSearch_findObjectWithMethod(Object object, String methodName) { // doesn't handle methods with arguments
		try { // search the current object for the methodName
			if (getDeclaredMethod.invoke(object.getClass(), methodName) != null) return object;
		} catch (Throwable t) {
			// no catch on purpose
		}

		try { // if not found, search children of the object recursively
			MethodHandle getChildrenNonCopy = lookup.findVirtual(object.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenNonCopy.invoke(object));

			for (Object child : children) 
				if (recursiveSearch_findObjectWithMethod(child, methodName) != null) return child; 
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	/**
	 * Does a recursive search on a UI object, trying to find a child object with 
	 * the given declared methodName. Useful for finding the class of a child, or
	 * for finding a unique one to utilize. Useless if the goal is neither finding
	 * the class, nor utilizing the child object.
	 * <p> maxDepth parameter controls the search depth; if it is 0, then only the
	 * children for the given parent will be searched, as if this is an iterative
	 * method. For values above 0, recursive searching till the given maxDepth 
	 * will be performed. 
	 * @param parent the root ui object whose children will be searched
	 * @param methodName to search for, methods with arguments are not handled
	 * @param maxDepth to limit the recursive search to a certain depth, minimum 0
	 * @return the child object having a declared method with methodName
	 */
	private static Object adaptiveSearch_findChildObjectWithDeclaredMethod(Object parent, String methodName, int maxDepth) {
		maxDepth = (maxDepth < 0) ? 0 : maxDepth;
		return adaptiveSearch_findChildObjectWithDeclaredMethod(parent, methodName, 0, maxDepth);
	}
	private static Object adaptiveSearch_findChildObjectWithDeclaredMethod(Object parent, String methodName, int depth, int maxDepth) {
		try {
			MethodHandle getChildrenNonCopy = lookup.findVirtual(parent.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenNonCopy.invoke(parent));

			for (Object child : children) {
				try {
					if (getDeclaredMethod.invoke(child.getClass(), methodName) != null) return child;
				} catch (Exception e) {
					// no catch on purpose
				}
			}

			if (depth < maxDepth) for (Object child : children) {
				Object targetObject = adaptiveSearch_findChildObjectWithDeclaredMethod(child, methodName, depth+1, maxDepth); 
				
				if (targetObject != null) return targetObject; 
			}
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	/**
	 * Does a recursive search on a UI object, trying to find a child object with 
	 * the given childClass. If such an object is found, depending on the getChild
	 * boolean, either the child object itself, or its parent will be retrieved.
	 * <p> maxDepth parameter controls the search depth; if it is 0, then only the
	 * children for the given parent will be searched, as if this is an iterative
	 * method. For values above 0, recursive searching till the given maxDepth 
	 * will be performed. 
	 * @param object the root ui object whose children will be searched
	 * @param childClass to search for
	 * @param getChild to determine whether the child or its parent object to be retrieved
	 * @param maxDepth to limit the recursive search to a certain depth, minimum 0
	 * @return the child object with the given childClass, or its parent object
	 */
	private static Object adaptiveSearch_findObjectWithChildClass(Object object, Class<?> childClass, boolean getChild, int maxDepth) {
		maxDepth = (maxDepth < 0) ? 0 : maxDepth;
		return adaptiveSearch_findObjectWithChildClass(object, childClass, getChild, 0, maxDepth);
	}
	private static Object adaptiveSearch_findObjectWithChildClass(Object object, Class<?> childClass, boolean getChild, int depth, int maxDepth) {
		try {
			MethodHandle getChildrenNonCopy = lookup.findVirtual(object.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenNonCopy.invoke(object));

			for (Object child : children) {
				if (child.getClass().equals(childClass)) return (getChild) ? child : object;
			}

			if (depth < maxDepth) for (Object child : children) {
				Object targetObject = adaptiveSearch_findObjectWithChildClass(child, childClass, getChild, depth+1, maxDepth); 
				
				if (targetObject != null) return targetObject; 
			}
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}
	
	public static void refreshRefitShip() {
		// Object campaignUI = null;
		// Object screenPanel = null;
		// Object encounterDialogue = null;
		// Object core = null;
		// Object wrapper = null;
		// Object refitTab = null;
		Object refitPanel = null;
		Object designDisplay = null;
		try {
			Object campaignUI = Global.getSector().getCampaignUI();
			// Object screenPanel = campaignUI_getScreenPanel.getMethodHandle().invoke(campaignUI); 
			Object encounterDialogue = campaignUI_getEncounterDialog.getMethodHandle().invoke(campaignUI); 
			Object core = (encounterDialogue != null) ? encounterDialog_getCoreUI.getMethodHandle().invoke(encounterDialogue) : campaignUI_getCore.getMethodHandle().invoke(campaignUI);
			Object wrapper = adaptiveSearch_findObjectWithChildClass(core, wrapperClass, true, 0);
			Object refitTab = adaptiveSearch_findObjectWithChildClass(wrapper, refitTabClass, true, 0);
			refitPanel = refitTab_getRefitPanel.getMethodHandle().invoke(refitTab);
			designDisplay = refitPanel_getDesignDisplay.getMethodHandle().invoke(refitPanel);
		} catch (Throwable t) { // hardcoded fallback to do a brute search on ALL the UI
			logger.warn("Fallback used in 'refreshRefitShip()'");
			// TODO: set up a proper fallback
		} finally {
			try {
				refitPanel_saveCurrentVariant.getMethodHandle().invoke(refitPanel);
				designDisplay_undo.getMethodHandle().invoke(designDisplay);
			} catch (Throwable t) {
				logger.error("Total failure in 'refreshRefitShip()'"); t.printStackTrace();
			}
		}
	}
}
