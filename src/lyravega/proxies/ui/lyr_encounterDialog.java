package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

public class lyr_encounterDialog {
	private InteractionDialogAPI encounterDialog;	// InteractionDialogAPI, VisualPanelAPI, UIPanelAPI, UIComponentAPI
	// private lyr_campaignUI campaignUI;
	// private lyr_coreUI coreUI;
	static Class<?> clazz;
	private static MethodHandle getCoreUI;

	static {
		try {
			clazz = methodReflection.findMethodByName("getEncounterDialog", lyr_campaignUI.clazz).getReturnType();

			getCoreUI = methodReflection.findMethodByName("getCoreUI", clazz).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_encounterDialog'", t);
		}
	}

	public lyr_encounterDialog() {
		this.encounterDialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
	}

	public InteractionDialogAPI retrieve() {
		return this.encounterDialog;
	}

	public void recycle(InteractionDialogAPI encounterDialog) {
		this.encounterDialog = encounterDialog;
	}

	public lyr_coreUI getCoreUI() {
		try {
			return new lyr_coreUI(getCoreUI.invoke(this.encounterDialog));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getCoreUI()' in 'lyr_encounterDialog'", t);
		}	return null;
	}

	// public void openLunaSettings() {
	// 	try {
	// 		lunalib.lunaExtensions.DialogExtensionsKt.openLunaCustomPanel(encounterDialog, new LunaSettingsUIMainPanel(false));
	// 	} catch (Throwable t) {
	// 		lyr_customLogger.error("Failed to use 'openLunaSettings()' in 'lyr_encounterDialog'", t);
	// 	}
	// }
}
