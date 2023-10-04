package lyravega.listeners;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;

import lyravega.tools.lyr_scriptTools;

/**
 * A sector listener base specifically aimed towards detecting when a core tab is opened and/or
 * closed, and provides simple {@code onOpen()} and {@code onClose()} methods for those.
 * <p> Detection for opening the tab is done through a listener, and closing the tab is done
 * through an every frame script. Script is added as transient, and removes itself just in case.
 * <p> With the default constructor, a flag is set to execute {@code onOpen()} only once. This
 * flag may be ignored with the overload constructor if necessary.
 * @author lyravega
 */
public abstract class _lyr_tabListener extends _lyr_sectorListener implements CoreUITabListener, EveryFrameScriptWithCleanup {
	private final CoreUITabId targetTab;
	private final boolean executeOnOpenOnce;
	private boolean onOpenExecuted = false;

	public _lyr_tabListener(CoreUITabId targetTab) {
		this.targetTab = targetTab;
		this.executeOnOpenOnce = true;
	}

	public _lyr_tabListener(CoreUITabId targetTab, boolean executeOnOpenOnce) {
		this.targetTab = targetTab;
		this.executeOnOpenOnce = executeOnOpenOnce;
	}

	public abstract void onOpen();

	public abstract void onClose();

	//#region CoreUITabListener
	@Override
	public final void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
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
	private final void attachTabScript() {
		if (lyr_scriptTools.getTransientScriptsOfClass(this.getClass()).isEmpty()) Global.getSector().addTransientScript(this);
	}

	private final void removeTabScript() {
		Global.getSector().removeTransientScript(this);
	}

	@Override
	public final void advance(float amount) {
		if (Global.getSector().getCampaignUI().getCurrentCoreTab() == targetTab) return;

		this.cleanup();
	}

	@Override public final boolean isDone() { return false; }	// as the script is removed at the end, isDone never returns true

	@Override public final boolean runWhilePaused() { return true; }

	@Override
	public final void cleanup() {
		onOpenExecuted = false;
		removeTabScript();
		onClose();
	}
	//#endregion
	// END OF EveryFrameScriptWithCleanup
}
