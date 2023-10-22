package lyravega.proxies.ui;

import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

public class lyr_campaignUI {
	private CampaignUIAPI campaignUI;
	// private lyr_encounterDialog encounterDialog;
	// private lyr_coreUI coreUI;
	static Class<?> clazz;
	private static MethodHandle getScreenPanel;
	// private static MethodHandle getEncounterDialog;
	private static MethodHandle getCore;

	static {
		try {
			clazz = Global.getSector().getCampaignUI().getClass();

			getScreenPanel = methodReflection.findMethodByName("getScreenPanel", clazz).getMethodHandle();
			// getEncounterDialog = methodReflection.findMethodByName("getEncounterDialog", clazz).getMethodHandle();
			getCore = methodReflection.findMethodByName("getCore", clazz).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_campaignUI'", t);
		}
	}

	public static lyr_campaignUI proxify() {
		return new lyr_campaignUI();
	}

	public lyr_campaignUI() {
		this.campaignUI = Global.getSector().getCampaignUI();
	}

	public CampaignUIAPI retrieve() {
		return this.campaignUI;
	}

	public void recycle(CampaignUIAPI campaignUI) {
		this.campaignUI = campaignUI;
	}

	public Object getScreenPanel() {
		try {
			return getScreenPanel.invoke(this.campaignUI);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getScreenPanel()' in 'lyr_campaignUI'", t);
		}	return null;
	}

	public lyr_encounterDialog getEncounterDialog() {
		if (this.campaignUI.getCurrentInteractionDialog() != null) try {
			return new lyr_encounterDialog();
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getEncounterDialog()' in 'lyr_campaignUI'", t);
		}	return null;
	}

	public lyr_coreUI getCore() {
		if (this.campaignUI.getCurrentInteractionDialog() == null) try {
			return new lyr_coreUI(getCore.invoke(this.campaignUI));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getCore()' in 'lyr_campaignUI'", t);
		} return this.getEncounterDialog().getCoreUI();
	}
}
