package lyravega.listeners;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;

import lyravega.utilities.lyr_scriptUtilities;

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
	private boolean delayedOnOpenExecuted = false;
	private float elapsed = 0f;
	private float intervalElapsed = 0f;
	private final float interval;

	public _lyr_tabListener(CoreUITabId targetTab) {
		this.targetTab = targetTab;
		this.executeOnOpenOnce = true;
		this.interval = 0f;
	}

	public _lyr_tabListener(CoreUITabId targetTab, boolean executeOnOpenOnce) {
		this.targetTab = targetTab;
		this.executeOnOpenOnce = executeOnOpenOnce;
		this.interval = 0f;
	}

	public _lyr_tabListener(CoreUITabId targetTab, float interval) {
		this.targetTab = targetTab;
		this.executeOnOpenOnce = true;
		this.interval = interval;
	}

	public _lyr_tabListener(CoreUITabId targetTab, boolean executeOnOpenOnce, float interval) {
		this.targetTab = targetTab;
		this.executeOnOpenOnce = executeOnOpenOnce;
		this.interval = interval;
	}

	/**
	 * Called when the target tab is opened
	 */
	protected abstract void onOpen();

	/**
	 * Called when the target tab is closed
	 */
	protected abstract void onClose();

	/**
	 * Called when the target tab is opened, with a frame or two delay
	 */
	protected abstract void onOpenDelayed();

	/**
	 * Alternative to {@code advance(amount)}, if such a method is necessary. Called
	 * during {@code advance(amount)} is being executed, as long as the target tab
	 * is the correct one. If not, the script terminates itself and calls {@code onClose()}
	 * <p> Abstraction is required to ensure {@code onClose()} executes properly as
	 * it is detected through the {@code advance(amount)}. Overriding it would break
	 * the detection, hence the abstraction
	 * @param amount
	 */
	protected abstract void onAdvance(final float amount);

	protected abstract void onInterval();

	//#region CoreUITabListener
	@Override
	public final void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
		if (tab != this.targetTab) return;

		if (!this.executeOnOpenOnce || !this.onOpenExecuted) {
			this.onOpenExecuted = true;
			this.delayedOnOpenExecuted = false;
			this.elapsed = 0f;
			this.attachScript();
			this.onOpen();
		}
	}
	//#endregion
	// END OF CoreUITabListener

	//#region EveryFrameScriptWithCleanup
	private final void attachScript() {
		if (lyr_scriptUtilities.getTransientScriptsOfClass(this.getClass()).isEmpty()) Global.getSector().addTransientScript(this);
	}

	private final void removeScript() {
		Global.getSector().removeTransientScript(this);
	}

	@Override
	public final void advance(float amount) {
		if (Global.getSector().getCampaignUI().getCurrentCoreTab() != this.targetTab) this.cleanup();

		if (!this.delayedOnOpenExecuted) {
			if (this.elapsed > 0.1) {
				this.delayedOnOpenExecuted = true; this.onOpenDelayed();
			}; this.elapsed += amount;
		}

		if (this.interval > 0f) {
			if (this.intervalElapsed > this.interval) {
				this.intervalElapsed = 0f; this.onInterval();
			}; this.intervalElapsed += amount;
		}

		this.onAdvance(amount);
	}

	@Override public final boolean isDone() { return false; }	// as the script is removed at the end, isDone never returns true

	@Override public final boolean runWhilePaused() { return true; }

	@Override
	public final void cleanup() {
		this.onOpenExecuted = false;
		// this.delayedOnOpenExecuted = false;
		this.removeScript();
		this.onClose();
	}
	//#endregion
	// END OF EveryFrameScriptWithCleanup
}
