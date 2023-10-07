package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;

import lyravega.tools.lyr_logger;
import lyravega.tools.lyr_reflectionTools;

public class lyr_encounterDialog implements lyr_logger {
	private InteractionDialogAPI encounterDialog;	// InteractionDialogAPI, VisualPanelAPI, UIPanelAPI, UIComponentAPI
	// private lyr_campaignUI campaignUI;
	// private lyr_coreUI coreUI;
	static Class<?> clazz;
	private static MethodHandle getCoreUI;

	static {
		try {
			clazz = lyr_reflectionTools.findMethodByName("getEncounterDialog", lyr_campaignUI.clazz).getReturnType();

			getCoreUI = lyr_reflectionTools.findMethodByName("getCoreUI", clazz).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_encounterDialog'", t);
		}
	}

	public lyr_encounterDialog() {
		this.encounterDialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
	}

	public InteractionDialogAPI retrieve() {
		return encounterDialog;
	}

	public void recycle(InteractionDialogAPI encounterDialog) {
		this.encounterDialog = encounterDialog;
	}

	public lyr_coreUI getCoreUI() {
		try {
			return new lyr_coreUI(getCoreUI.invoke(encounterDialog));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getCoreUI()' in 'lyr_encounterDialog'", t);
		}	return null;
	}

	// public void openLunaSettings() {
	// 	try {
	// 		lunalib.lunaExtensions.DialogExtensionsKt.openLunaCustomPanel(encounterDialog, new LunaSettingsUIMainPanel(false));
	// 	} catch (Throwable t) {
	// 		logger.error(logPrefix+"Failed to use 'openLunaSettings()' in 'lyr_encounterDialog'", t);
	// 	}
	// }
}
