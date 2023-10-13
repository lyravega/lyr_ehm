package lyravega.utilities;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.plugin.ehm_settings;
import lyravega.proxies.ui.*;
import lyravega.utilities.logger.lyr_logger;

/**
 * Provides specialized MethodHandles and a few methods that are 
 * designed to be used on the Starsector UI, which is why these 
 * are splitted from the parent class. 
 * <p> And yes, all this is to get the refit screen to refresh 
 * the design. However may serve as a point to do other stuff
 * too, as the relevant classes are known here. 
 * @author lyravega
 */
public class lyr_interfaceUtilities extends lyr_reflectionUtilities {
	/**
	 * Just a simple check to see if it is the refit tab or not.
	 * @return true if it is refit tab
	 */
	public static boolean isRefitTab() {
		return Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.REFIT;
	}

	/**
	 * Another simple check to see if it is the title screen or not
	 * @return true if it is the title screen
	 */
	public static boolean isTitleScreen() {
		return Global.getCurrentState() == GameState.TITLE;
	}

	/**
	 * Plays a hardcoded UI sound effect, used in {@code onRemove()}
	 * and {@code onInstall()}
	 */
	public static void playDrillSound() {
		if (!ehm_settings.getPlayDrillSound() || !isRefitTab()) return;
		Global.getSoundPlayer().playUISound(ehm_internals.id.drillSound, 1.0f, 0.75f);
	}

	private static boolean clearUndo = false;

	/**
	 * This method will immediately save the refit variant and refresh the UI by utilizing the
	 * obfuscated UI methods. Will also disable the undo button on the refit panel. Checks if
	 * it's the refit tab which is necessary for calls that originate outside refit tab.
	 * <p> In some cases, the undo button will remain enabled, however it will not do anything
	 * upon interaction. Disabling this button is optional. This issue is caused by having
	 * another UI element like the mod or weapon picker open when this method is called.
	 * <p> To deal with the problem above, this method sets a flag called {@link #clearUndo},
	 * which is reset by {@link #clearUndoAfter()}. It should get called after a delay when
	 * there are no additional UI elements, ideally through an EFS to disable the button.
	 */
	public static void commitVariantChanges() {
		if (!isRefitTab()) return; // necessary for calls that that originate while 'onGameLoad()'
		try {
			lyr_refitPanel refitPanel = new lyr_campaignUI().getCore().getCurrentTab().getRefitPanel(); 
			lyr_shipDisplay shipDisplay = refitPanel.getShipDisplay();

			refitPanel.saveCurrentVariant();
			shipDisplay.setFleetMember(null, null);
			refitPanel.syncWithCurrentVariant();
			shipDisplay.setFleetMember(refitPanel.getMember(), null);
			refitPanel.syncWithCurrentVariant();
			refitPanel.setEditedSinceLoad(false);
			refitPanel.setEditedSinceSave(false);

			clearUndo = true;
		} catch (Throwable t) {
			// refreshRefit();
			lyr_logger.error("Failure in 'commitChanges()'");
		}
	}

	/**
	 * Checks if it's the refit tab and then sets the 'undo' button as inactive. 
	 * @deprecated If some other UI element like the the hullmod or the weapon picker is open,
	 * will not clear the undo button properly.
	 */
	@Deprecated
	public static void clearUndo() {
		if (!isRefitTab()) return;	// just in case
		try {
			lyr_refitPanel refitPanel = new lyr_campaignUI().getCore().getCurrentTab().getRefitPanel();

			refitPanel.setEditedSinceLoad(false);
			refitPanel.setEditedSinceSave(false);
		} catch (Throwable t) {
			lyr_logger.error("Failure in 'clearUndo()'", t);
		}
	}

	/**
	 * If {@link #clearUndo} flag is set, clears it and sets the 'undo' button as inactive.
	 * <p> Needs to be run after a delay (through an EFS) after the flag is set, otherwise
	 * will fail to clear the button(s) like its deprecated sibling {@link #clearUndo()}.
	 */
	public static void clearUndoAfter() {
		if (!clearUndo) return;	// works through a flag, and doesn't check if it's the refit tab. Origin of caller should do that check there instead
		try {
			lyr_refitPanel refitPanel = new lyr_campaignUI().getCore().getCurrentTab().getRefitPanel();
			lyr_designDisplay designDisplay = refitPanel.getDesignDisplay();

			refitPanel.setEditedSinceLoad(false);
			refitPanel.setEditedSinceSave(false);
			designDisplay.getSaveButton().setEnabled(false);
			designDisplay.getUndoButton().setEnabled(false);

			clearUndo = false;
		} catch (Throwable t) {
			lyr_logger.error("Failure in 'clearUndoAfter()'", t);
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
	public static Object adaptiveSearch_findChildObjectWithDeclaredMethod(Object parent, String methodName, int maxDepth) {
		maxDepth = (maxDepth < 0) ? 0 : maxDepth;
		return adaptiveSearch_findChildObjectWithDeclaredMethod(parent, methodName, 0, maxDepth);
	}
	public static Object adaptiveSearch_findChildObjectWithDeclaredMethod(Object parent, String methodName, int depth, int maxDepth) {
		try {
			MethodHandle getChildrenNonCopy = lookup.findVirtual(parent.getClass(), "getChildrenNonCopy", MethodType.methodType(List.class));
			List<?> children = List.class.cast(getChildrenNonCopy.invoke(parent));

			for (Object child : children) {
				try {
					if (methodReflection.findMethodByName(methodName, child.getClass(), false) != null) return child;
				} catch (Throwable t) {
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
	public static Object adaptiveSearch_findObjectWithChildClass(Object object, Class<?> childClass, boolean getChild, int maxDepth) {
		maxDepth = (maxDepth < 0) ? 0 : maxDepth;
		return adaptiveSearch_findObjectWithChildClass(object, childClass, getChild, 0, maxDepth);
	}
	public static Object adaptiveSearch_findObjectWithChildClass(Object object, Class<?> childClass, boolean getChild, int depth, int maxDepth) {
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
}
