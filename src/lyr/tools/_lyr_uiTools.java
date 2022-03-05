package lyr.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;

/**
 * Provides specialized MethodHandles and a few methods that 
 * are designed to be used on the Starsector UI, which is 
 * why these are splitted from the parent class.
 * @author lyravega
 */
public class _lyr_uiTools extends _lyr_reflectionTools {
	private static Class<?> campaignUIClass;
	private static Class<?> coreClass;
	private static Class<?> screenPanelClass;
	private static Class<?> refitPanelClass;
	private static Class<?> refitTabClass;
	private static Class<?> designDisplayClass;
	private static methodMap campaignUI_getCore;
	// private static methodMap campaignUI_notifyFleetMemberChanged;
	private static methodMap campaignUI_getScreenPanel;
	private static methodMap refitPanel_getRefitTab;
	private static methodMap refitTab_getRefitPanel;
	private static methodMap refitPanel_getDesignDisplay;
	private static methodMap refitPanel_saveCurrentVariant;
	private static methodMap designDisplay_undo;

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

				campaignUI_getCore = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getCore");
				coreClass = campaignUI_getCore.getReturnType();

				methodMap campaignUI_notifyFleetMemberChanged = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "notifyFleetMemberChanged");
				refitPanelClass = campaignUI_notifyFleetMemberChanged.getParameterTypes()[0]; 

				campaignUI_getScreenPanel = _lyr_reflectionTools.findTypesForMethod(campaignUIClass, "getScreenPanel");
				screenPanelClass = campaignUI_getScreenPanel.getReturnType();

				refitPanel_getRefitTab = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "getRefitTab");
				refitTabClass = refitPanel_getRefitTab.getReturnType();

				refitTab_getRefitPanel = _lyr_reflectionTools.findTypesForMethod(refitTabClass, "getRefitPanel");
				// refitPanelClass = refitTab_getRefitPanel.getReturnType();

				refitPanel_getDesignDisplay = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "getDesignDisplay");
				designDisplayClass = refitPanel_getDesignDisplay.getReturnType();

				refitPanel_saveCurrentVariant = _lyr_reflectionTools.findTypesForMethod(refitPanelClass, "saveCurrentVariant"); // there is an overload for this, beware
				designDisplay_undo = _lyr_reflectionTools.findTypesForMethod(designDisplayClass, "undo");

				isDone = true; return;					
			} catch (Throwable t) {

			}

			isDone = true;
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

	private static Object recursiveSearch_findObjectWithClass(Object object, Class<?> targetClass) { // TODO: mimic the other overload
		if (object.getClass().equals(targetClass)) return object;

		Object targetObject; 

		try {
			MethodHandle getChildrenCopy = lookup.findVirtual(object.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) {
				targetObject = recursiveSearch_findObjectWithClass(child, targetClass); 
				
				if (targetObject != null) return targetObject; 
			}
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	private static Object recursiveSearch_findObjectWithMethod(Object object, String methodName) { // doesn't handle methods with arguments
		try { // search the current object for the methodName
			if (getDeclaredMethod.invoke(object.getClass(), methodName) != null) return object;
		} catch (Throwable t) {
			// no catch on purpose
		}

		try { // if not found, search children of the object recursively
			MethodHandle getChildrenCopy = lookup.findVirtual(object.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(object));

			for (Object child : children) 
				if (recursiveSearch_findObjectWithMethod(child, methodName) != null) return child; 
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	private static Object iterativeSearch_findChildWithClass(Object parent, Class<?> targetClass) {
		try {
			MethodHandle getChildrenCopy = lookup.findVirtual(parent.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenCopy.invoke(parent));

			for (Object child : children) {
				if (child.getClass().equals(targetClass)) return child;
			}
		} catch (Throwable t) {
			// no catch on purpose
		}

		return null;
	}

	/*
	// in freeroam, core houses refit stuff. in markets, marketCore does.

	com.fs.starfarer.coreui.refit.oOOo // designDisplay
	com.fs.starfarer.coreui.refit.V // refitPanel
	com.fs.starfarer.coreui.refit.F // refitTab
	com.fs.starfarer.ui.newui.o0OO // ???
	com.fs.starfarer.ui.newui.o0Oo // marketCore
	com.fs.starfarer.ui.newui.OO0O // core
	com.fs.starfarer.ui.v // screenPanel
	*/
	
	public static void refreshRefitShip() {		
		CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
		Object screenPanel = null;
		// Object core = null;
		Object refitTab = null;
		Object refitPanel = null;
		Object designDisplay = null;
		try {
			screenPanel = campaignUI_getScreenPanel.getMethodHandle().invoke(campaignUI);
			refitTab = recursiveSearch_findObjectWithClass(screenPanel, refitTabClass); // wider search but successful in market too
			// core = campaignUI_getCore.getMethodHandle().invoke(campaignUI);
			// refitTab = recursiveSearch_findObjectWithClass(core, refitTabClass); // fails in market, market ui has its own core and refit lives there
			refitPanel = refitTab_getRefitPanel.getMethodHandle().invoke(refitTab);
			designDisplay = refitPanel_getDesignDisplay.getMethodHandle().invoke(refitPanel);
		} catch (Throwable t) { // fallback to do a brute search on ALL the UI
			refitPanel = recursiveSearch_findObjectWithClass(screenPanel, refitPanelClass);
			designDisplay = recursiveSearch_findObjectWithMethod(refitPanel, "undo");
		} finally {
			try {
				refitPanel_saveCurrentVariant.getMethodHandle().invoke(refitPanel);
				designDisplay_undo.getMethodHandle().invoke(designDisplay);
			} catch (Throwable e) {
				logger.error("HERP");
			}
		}
	}
}
