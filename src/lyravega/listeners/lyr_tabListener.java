package lyravega.listeners;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;

import experimentalHullModifications.submarkets.ehm_submarket;
import lyravega.misc.lyr_internals;
import lyravega.plugin.lyr_ehm;
import lyravega.tools.lyr_logger;
import lyravega.tools.lyr_scriptTools;

/**
 * A tab listener class that implements several interfaces. There is a
 * method that triggers when the tabs are opened, but not when they are
 * closed so a script is initialized when a tab is opened to determine
 * when it is closed.
 * @author lyravega
 */
public class lyr_tabListener implements CoreUITabListener, EveryFrameScriptWithCleanup, lyr_logger {
	protected static final CoreUITabId targetTab = CoreUITabId.REFIT;
	private static final String targetTabString = targetTab.toString().toLowerCase();
	
	public static void attach(boolean isTransient) {	// used in plugin's onLoad()
		if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;

		if (!Global.getSector().getListenerManager().hasListenerOfClass(lyr_tabListener.class)) {
			Global.getSector().getListenerManager().addListener(new lyr_tabListener(), isTransient);

			if (lyr_ehm.settings.getLogListenerInfo()) logger.info(logPrefix + "Attached "+targetTabString+" tab listener");
		}
	}

	public static void detach() {
		if (Global.getSector().getListenerManager().hasListenerOfClass(lyr_tabListener.class)) {
			Global.getSector().getListenerManager().removeListenerOfClass(lyr_tabListener.class);

			if (lyr_ehm.settings.getLogListenerInfo()) logger.info(logPrefix + "Detached "+targetTabString+" tab listener");
		}
	}

	protected void onOpen() {
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		for (String shuntId : ehm_submarket.shunts) {
			playerCargo.addWeapons(shuntId, 1000);
		}
	}

	protected void onClose() {
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		for (CargoItemQuantity<String> weaponCargo : playerCargo.getWeapons()) {
			if (ehm_submarket.shunts.contains(weaponCargo.getItem())) playerCargo.removeWeapons(weaponCargo.getItem(), weaponCargo.getCount());
		}
	}

	//#region CoreUITabListener
	protected boolean executeOnOpenOnce = true;
	protected boolean onOpenExecuted = false;

	@Override
	public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
		if (tab != targetTab) return;

		if (!executeOnOpenOnce || !onOpenExecuted) {
			onOpenExecuted = true;
			attachTabScript();
			onOpen();
		}
	}
	//#endregion
	// END OF CoreUITabListener

	//#region EveryFrameScriptWithCleanup
	protected void attachTabScript() {
		if (lyr_scriptTools.getTransientScriptsOfClass(this.getClass()).isEmpty()) Global.getSector().addTransientScript(this);
	}

	protected void removeTabScript() {
		Global.getSector().removeTransientScript(this);
	}

	@Override
	public void advance(float amount) {
		if (Global.getSector().getCampaignUI().getCurrentCoreTab() == targetTab) return;

		this.cleanup();
	}

	@Override public boolean isDone() { return false; }	// as the script is removed at the end, isDone never returns true

	@Override public boolean runWhilePaused() { return true; }

	@Override
	public void cleanup() {
		onOpenExecuted = false;
		removeTabScript();
		onClose();
	}
	//#endregion
	// END OF EveryFrameScriptWithCleanup
}
