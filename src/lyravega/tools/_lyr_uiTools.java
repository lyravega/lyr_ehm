package lyravega.tools;

import static lyravega.plugin.lyr_lunaSettings.playDrillSound;
import static lyravega.tools._lyr_scriptTools.refreshRefit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;

import lyravega.misc.lyr_internals;

/**
 * Provides specialized MethodHandles and a few methods that are 
 * designed to be used on the Starsector UI, which is why these 
 * are splitted from the parent class. 
 * <p> And yes, all this is to get the refit screen to refresh 
 * the design. However may serve as a point to do other stuff
 * too, as the relevant classes are known here. 
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
	private static Class<?> shipDisplayClass;

	private static MethodHandle campaignUI_getScreenPanel;
	private static MethodHandle campaignUI_getEncounterDialog;
	private static MethodHandle campaignUI_getCore;
	private static MethodHandle encounterDialog_getCoreUI;
	private static MethodHandle refitTab_getRefitPanel;
	private static MethodHandle refitPanel_getDesignDisplay;
	private static MethodHandle refitPanel_getShipDisplay;
	
	private static MethodHandle refitPanel_saveCurrentVariant;
	private static MethodHandle refitPanel_getMember;
	private static MethodHandle refitPanel_syncWithCurrentVariant;
	private static MethodHandle shipDisplay_setFleetMember;
	private static MethodHandle designDisplay_undo;

	private static MethodHandle shipDisplay_getCurrentVariant;
	private static MethodHandle refitPanel_addAllWeaponsFromVariantToCargo;

	private static MethodHandle refitPanel_setEditedSinceLoad;
	private static MethodHandle refitPanel_setEditedSinceSave;

	/**
	 * A simple method that initializes an every frame script that waits
	 * till the relevant UI parts are available and can be fished for classes
	 */
	public static void findUIClasses() {
		logger.info(lyr_internals.logPrefix + "Initializing UI class finder");

		new _lyr_delayedFinder();
	}

	/**
	 * An everyFrameScript to delay the process of finding the obfuscated
	 * classes and providing methodHandles for them. Normally, it is not
	 * needed, however one class I call as 'wrapper' has no easy way to
	 * get access to, so need to delay the process till there is an 
	 * object instance of the class. 
	 * <p> Alternatively, a recursive search can be done to skip that
	 * class and find a grandchild directly from a known class, but that
	 * is sub-optimal. 
	 */
	private static class _lyr_delayedFinder implements EveryFrameScript {
		private boolean isDone = false;

		public _lyr_delayedFinder() {
			if (!Global.getSector().hasTransientScript(this.getClass())) {
				Global.getSector().addTransientScript(this);
			
				logger.info(lyr_internals.logPrefix+"Waiting to find the UI classes");
			}
		}

		@Override
		public void advance(float amount) {
			try {
				// due to 'wrapperClass' not being extractable, need to wait for the refit tab
				if (!isRefitTab()) return;

				campaignUIClass = Global.getSector().getCampaignUI().getClass();
				screenPanelClass = inspectMethod("getScreenPanel", campaignUIClass).getReturnType();
				encounterDialogueClass = inspectMethod("getEncounterDialog", campaignUIClass).getReturnType();
				coreClass = inspectMethod("getCore", campaignUIClass).getReturnType();
				refitPanelClass = inspectMethod("notifyFleetMemberChanged", campaignUIClass).getParameterTypes()[0]; 
				refitTabClass = inspectMethod("getRefitTab", refitPanelClass).getReturnType();
				designDisplayClass = inspectMethod("getDesignDisplay", refitPanelClass).getReturnType();
				shipDisplayClass = inspectMethod("getShipDisplay", refitPanelClass).getReturnType();

				campaignUI_getScreenPanel = inspectMethod("getScreenPanel", campaignUIClass).getMethodHandle();
				campaignUI_getEncounterDialog = inspectMethod("getEncounterDialog", campaignUIClass).getMethodHandle(); // same as 'Global.getSector().getCampaignUI().getCurrentInteractionDialog();'
				campaignUI_getCore = inspectMethod("getCore", campaignUIClass).getMethodHandle();
				encounterDialog_getCoreUI = inspectMethod("getCoreUI", encounterDialogueClass).getMethodHandle();
				refitTab_getRefitPanel = inspectMethod("getRefitPanel", refitTabClass).getMethodHandle();
				refitPanel_getDesignDisplay = inspectMethod("getDesignDisplay", refitPanelClass).getMethodHandle();
				refitPanel_getShipDisplay = inspectMethod("getShipDisplay", refitPanelClass).getMethodHandle();

				refitPanel_saveCurrentVariant = inspectMethod("saveCurrentVariant", refitPanelClass).getMethodHandle(); // there is an overload for this, beware
				refitPanel_getMember = inspectMethod("getMember", refitPanelClass).getMethodHandle();
				refitPanel_syncWithCurrentVariant = inspectMethod("syncWithCurrentVariant", refitPanelClass).getMethodHandle();
				shipDisplay_setFleetMember = inspectMethod("setFleetMember", shipDisplayClass).getMethodHandle();
				designDisplay_undo = inspectMethod("undo", designDisplayClass).getMethodHandle(); // not used anymore because fucks up for ships with officers

				refitPanel_setEditedSinceLoad = inspectMethod("setEditedSinceLoad", refitPanelClass).getMethodHandle();
				refitPanel_setEditedSinceSave = inspectMethod("setEditedSinceSave", refitPanelClass).getMethodHandle();

				shipDisplay_getCurrentVariant = inspectMethod("getCurrentVariant", shipDisplayClass).getMethodHandle();
				refitPanel_addAllWeaponsFromVariantToCargo = inspectMethod("addAllWeaponsFromVariantToCargo", refitPanelClass).getMethodHandle();

				// redoing stuff just to find the wrapper, or whatever the fuck it is
				Object campaignUI = Global.getSector().getCampaignUI();
				Object screenPanel = campaignUI_getScreenPanel.invoke(campaignUI);
				Object encounterDialogue = campaignUI_getEncounterDialog.invoke(campaignUI);
				Object core = (encounterDialogue != null) ? encounterDialog_getCoreUI.invoke(encounterDialogue) : campaignUI_getCore.invoke(campaignUI);
				Object wrapper = adaptiveSearch_findObjectWithChildClass(core, refitTabClass, false, 1); // as the 'wrapperClass' is unknown here, search for a grandChild's class and get parent object
				// Object refitTab = adaptiveSearch_findObjectWithChildClass(wrapper, refitTabClass, true, 0); // as the 'refitTabClass' is known, search for a child's class and get the child object
				// Object refitPanel = refitTab_getRefitPanel.invoke(refitTab);
				// Object designDisplay = refitPanel_getDesignDisplay.invoke(refitPanel);
				// Object shipDisplay = refitPanel_getDesignDisplay.invoke(refitPanel);
				
				// campaignUIClass = campaignUI.getClass();					// com.fs.starfarer.campaign.CampaignState;
				// screenPanelClass = screenPanel.getClass();				// com.fs.starfarer.ui.v;
				// encounterDialogueClass = encounterDialogue.getClass();	// com.fs.starfarer.ui.newui.o0Oo;
				// coreClass = core.getClass();								// com.fs.starfarer.ui.newui.OO0O;
				wrapperClass = wrapper.getClass();							// com.fs.starfarer.ui.newui.o0OO;
				// refitTabClass = refitTab.getClass();						// com.fs.starfarer.coreui.refit.F;
				// refitPanelClass = refitPanel.getClass();					// com.fs.starfarer.coreui.refit.V;
				// designDisplayClass = designDisplay.getClass();			// com.fs.starfarer.coreui.refit.oOOo;
				// shipDisplayClass = shipDisplay.getClass();				// com.fs.starfarer.coreui.refit.oOOO;

				logger.info(lyr_internals.logPrefix+"Found the UI classes");
				isDone = true; return;
			} catch (Throwable t) {
				logger.fatal(lyr_internals.logPrefix+"Failed to find the UI classes"); t.printStackTrace();
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

	//#region SEARCH TOOLS
	/**
	 * Does a recursive search on an UI object, trying to find a child object with 
	 * the given declared methodName. Useful for finding the class of a child, or
	 * for finding a unique one to utilize. Useless if the goal is neither finding
	 * the class, nor utilizing the child object.
	 * <p> maxDepth parameter controls the search depth; if it is 0, then only the
	 * children for the given parent will be searched, as if this is an iterative
	 * method. For values above 0, recursive searching till the given maxDepth 
	 * will be performed. 
	 * <p> Does NOT handle methods with parameters, only suitable for methods with
	 * no arguments. And will stop at the first found. For methods with overloads,
	 * or parameters, not suitable.
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
	 * @param depth (overload) initial depth, default 0
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
	//#endregion
	// END OF SEARCH TOOLS
	
	/**
	 * Just a simple check to see if it is the refit tab or not.
	 * @return true if it is refit tab, false otherwise
	 */
	public static boolean isRefitTab() {
		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		return (tab != null && tab.equals(CoreUITabId.REFIT));
	}

	/**
	 * Plays a hardcoded UI sound effect, used in {@code onRemove()}
	 * and {@code onInstall()}
	 */
	public static void playSound() {
		if (!isRefitTab() || !playDrillSound) return;
		Global.getSoundPlayer().playUISound(lyr_internals.id.drillSound, 1.0f, 0.75f);
	}

	/**
	 * This method will immediately save the refit variant and refresh the 
	 * UI by utilizing the available UI methods. Used to be called as 
	 * {@code refreshRefit()}, however the new name suits it more.
	 * <p> Refresh is achieved by executing an 'undo-like' method. Before,
	 * it was using the 'undo' function directly, however ships with
	 * officers were acting weirdly. Now, the 'undo-like' method is
	 * utilized, however ships with officers do not clear the undo button
	 * properly for some reason. 
	 * <p> By committing changes immediately and using 'undo' up and thus 
	 * disabling it, some odd unwanted behaviour and problems are dealt 
	 * with as a side effect. Even with problematic ships where 'undo' is
	 * not cleared properly, it'll have no effect, so it should be safe.
	 */
	public static void commitChanges() {
		if (!isRefitTab()) return; // necessary for calls that are not from 'onInstalled()' or 'onRemoved()'; that originate due to 'onGameLoad()'
		try {
			Object campaignUI = Global.getSector().getCampaignUI();
			// Object screenPanel = campaignUI_getScreenPanel.invoke(campaignUI);
			Object encounterDialogue = campaignUI_getEncounterDialog.invoke(campaignUI);
			Object core = (encounterDialogue != null) ? encounterDialog_getCoreUI.invoke(encounterDialogue) : campaignUI_getCore.invoke(campaignUI);
			Object wrapper = adaptiveSearch_findObjectWithChildClass(core, wrapperClass, true, 0);
			Object refitTab = adaptiveSearch_findObjectWithChildClass(wrapper, refitTabClass, true, 0);
			// Object refitTab = adaptiveSearch_findObjectWithChildClass(core, refitTabClass, true, 1); // to find refitTab without knowing/getting wrapper
			Object refitPanel = refitTab_getRefitPanel.invoke(refitTab);
			// Object designDisplay = refitPanel_getDesignDisplay.invoke(refitPanel);
			Object shipDisplay = refitPanel_getShipDisplay.invoke(refitPanel);
			Object member = refitPanel_getMember.invoke(refitPanel);

			refitPanel_saveCurrentVariant.invoke(refitPanel);	// this will fail on ship restoration
			// designDisplay_undo.invoke(designDisplay); // commented for posterity; below is gutted version of 'undo'
			shipDisplay_setFleetMember.invoke(shipDisplay, null, null);
			refitPanel_syncWithCurrentVariant.invoke(refitPanel);
			shipDisplay_setFleetMember.invoke(shipDisplay, member, null);
			refitPanel_syncWithCurrentVariant.invoke(refitPanel);
			refitPanel_setEditedSinceLoad.invoke(refitPanel, false);
			refitPanel_setEditedSinceSave.invoke(refitPanel, false);
		} catch (Throwable t) {
			refreshRefit();
			logger.error(lyr_internals.logPrefix+"Failure in 'commitChanges()', possible hull restoration");
		}
	}

	/**
	 * Sets the 'undo' button as inactive. It is necessary for any commits
	 * that were done remotely and not from the {@code onRemove()} and 
	 * {@code onInstalled()} methods.
	 * <p> Ideally, the external commit call should also set the 'undo' 
	 * button as inactive, however if the hullSpecs of the variants are
	 * set after the commit call is being made, then the 'undo' button 
	 * will not be set as inactive. 
	 * <p> In essence, this method serves as a supplement for that issue, 
	 * to be used from {@code onRemove()} and {@code onInstalled()} methods 
	 * if {@code commitChanges()} is not called from those two.
	 * 
	 * @deprecated - {@code commitChanges()} is used instead to do the same. 
	 * <p> This is/was supposed to clean the undo button by resetting the last
	 * save and load, executing a purely visual function.
	 * <p> However, if a different ship is selected right after this is used,
	 * going back to the former ship effectively does the real undo. In other
	 * words, had an issue that caused this to malfunction. Probable cause
	 * is lack of {@code syncWithCurrentVariant()}. Added it for now.
	 * <p> Removal of this function all together is also a valid solution,
	 * since this effectively does only a visual function, and the undo
	 * button doesn't really do anything in cases where/when this is needed.
	 */
	@Deprecated
	public static void clearUndo() {
		if (!isRefitTab()) return; // just in case
		try {
			Object campaignUI = Global.getSector().getCampaignUI();
			// Object screenPanel = campaignUI_getScreenPanel.invoke(campaignUI);
			Object encounterDialogue = campaignUI_getEncounterDialog.invoke(campaignUI);
			Object core = (encounterDialogue != null) ? encounterDialog_getCoreUI.invoke(encounterDialogue) : campaignUI_getCore.invoke(campaignUI);
			Object wrapper = adaptiveSearch_findObjectWithChildClass(core, wrapperClass, true, 0);
			Object refitTab = adaptiveSearch_findObjectWithChildClass(wrapper, refitTabClass, true, 0);
			Object refitPanel = refitTab_getRefitPanel.invoke(refitTab);
			
			refitPanel_syncWithCurrentVariant.invoke(refitPanel);	// added line for a potential fix; works if it activates something, fails otherwise
			refitPanel_setEditedSinceLoad.invoke(refitPanel, false);
			refitPanel_setEditedSinceSave.invoke(refitPanel, false);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failure in 'clearUndo()'"); t.printStackTrace();
		}
	}
}
