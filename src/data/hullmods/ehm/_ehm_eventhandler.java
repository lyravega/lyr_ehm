package data.hullmods.ehm;

import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.playSound;

import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * When a change is detected through the {@link _ehm_basetracker tracker}, additional events can
 * be fired. This class provides a handler for those events.
 * <p> These events can utilize custom methods in the hull modification classes implemented from
 * this {@link _ehm_eventmethod interface}.
* @see {@link _ehm_basetracker Tracker Base} Provides tracking features for the fleet/ship
* @see {@link _ehm_eventmethod Event Method Interface} Implemented by the hull modifications
 * @author lyravega
 */
public class _ehm_eventhandler {
	private _ehm_eventmethod hullModEffect;
	private onInstallEventProperties onInstallEvent;
	private onRemoveEventProperties onRemoveEvent;
	private sModCleanUpEventProperties sModCleanUpEvent;

	/**
	 * Create a new handler for the current hull modification. Depending on which event
	 * handlers are going to be used, they'll have to be registered through this object
	 * with the {@link #registerOnInstall()} {@link #registerOnRemove()} and 
	 * {@link #registerSModCleanUp()} methods individually.
	 * <p> For safety, this constructor and these methods should be wrapped in a check;
	 * even though initialization occurs only once, sometimes hot swapping the code
	 * causes access violations for whatever reason. 
	 * @param hullModId for storage and check purposes
	 * @param hullModEffect to properly access the custom methods, if any
	 */
	public _ehm_eventhandler(String hullModId, Object hullModEffect) {
		this.hullModEffect = (_ehm_eventmethod) hullModEffect;
		_ehm_basetracker.registeredHullMods.put(hullModId, this);
	}
	
	/** Inner class for the {@code onInstall} event */
	private static class onInstallEventProperties {
		private boolean executeMethod;
		private boolean commitChanges;
		private boolean playSound;

		private onInstallEventProperties(boolean commitChanges, boolean playSound, boolean executeMethod) {
			this.executeMethod = executeMethod;
			this.commitChanges = commitChanges;
			this.playSound = playSound;
		}

		private void execute(_ehm_eventmethod hullModObject, ShipVariantAPI variant) {
			if (this.executeMethod) hullModObject.onInstall(variant);
			if (this.commitChanges) commitChanges();
			if (this.playSound) playSound();
		}
	}

	/** Inner class for the {@code onRemove} event */
	private static class onRemoveEventProperties {
		private boolean executeMethod;
		private boolean commitChanges;
		private boolean playSound;

		private onRemoveEventProperties(boolean commitChanges, boolean playSound, boolean executeMethod) {
			this.executeMethod = executeMethod;
			this.commitChanges = commitChanges;
			this.playSound = playSound;
		}

		private void execute(_ehm_eventmethod hullModObject, ShipVariantAPI variant) {
			if (this.executeMethod) hullModObject.onRemove(variant);
			if (this.commitChanges) commitChanges();
			if (this.playSound) playSound();
		}
	}

	/** Inner class for the {@code sModCleanUp} event */
	private static class sModCleanUpEventProperties {
		private boolean executeMethod;
		private boolean commitChanges;
		private boolean playSound;

		private sModCleanUpEventProperties(boolean commitChanges, boolean playSound, boolean executeMethod) {
			this.executeMethod = executeMethod;
			this.commitChanges = commitChanges;
			this.playSound = playSound;
		}

		private void execute(_ehm_eventmethod hullModObject, ShipVariantAPI variant) {
			if (this.executeMethod) hullModObject.sModCleanUp(variant);
			if (this.commitChanges) commitChanges();
			if (this.playSound) playSound();
		}
	}

	/**
	 * Registers the {@code onInstall} event. If the ship's tracker detects a new
	 * hull modification in the refit panel, the registered event will be executed.
	 * @param commitChanges refreshes the refit panel on event if true
	 * @param playSound plays a sound on event if true
	 * @param executeEventMethod runs the custom overridden {@link 
	 * _ehm_eventmethod#onInstall() onInstall()} method on event if true
	 */
	public void registerOnInstall(boolean commitChanges, boolean playSound, boolean executeEventMethod) {
		this.onInstallEvent = new onInstallEventProperties(commitChanges, playSound, executeEventMethod);
	}

	/**
	 * Registers the {@code onRemove} event. If the ship's tracker detects an old
	 * hull modification in the refit panel, the registered event will be executed.
	 * @param commitChanges refreshes the refit panel on event if true
	 * @param playSound plays a sound on event if true
	 * @param executeEventMethod runs the custom overridden {@link 
	 * _ehm_eventmethod#onRemove() onRemove()} method on event if true
	 */
	public void registerOnRemove(boolean commitChanges, boolean playSound, boolean executeEventMethod) {
		this.onRemoveEvent = new onRemoveEventProperties(commitChanges, playSound, executeEventMethod);
	}

	/**
	 * Registers the {@code sModCleanUp} event. If the ship's tracker detects an old
	 * s-modification in the refit panel, the registered event will be executed.
	 * <p> The s-modifications apply their effects to the ships immediately and as
	 * such some of the effects become permanent even if the s-modification isn't
	 * committed to the variant. This event is to undo the permanent changes if any
	 * happens during window shopping.
	 * @param commitChanges refreshes the refit panel on event if true
	 * @param playSound plays a sound on event if true
	 * @param executeEventMethod runs the custom overridden {@link 
	 * _ehm_eventmethod#sModCleanUp() sModCleanUp()} method on event if true
	 */
	public void registerSModCleanUp(boolean commitChanges, boolean playSound, boolean executeEventMethod) {
		this.sModCleanUpEvent = new sModCleanUpEventProperties(commitChanges, playSound, executeEventMethod);
	}

	/**
	 * Used in {@link _ehm_basetracker#onInstalled() onInstalled()}; where the set of
	 * the new hull modifications are processed. If the hull modification in question
	 * has a registered install event, this method fires it. 
	 * @param variant of the ship that'll be passed to any custom methods
	 */
	protected void executeOnInstall(ShipVariantAPI variant) {
		if (this.onInstallEvent != null) this.onInstallEvent.execute(this.hullModEffect, variant);
	}

	/**
	 * Used in {@link _ehm_basetracker#onRemoved() onRemoved()}; where the set of the
	 * old hull modifications are processed. If the hull modification in question has 
	 * a registered remove event, this method fires it. 
	 * @param variant of the ship that'll be passed to any custom methods
	 */
	protected void executeOnRemove(ShipVariantAPI variant) {
		if (this.onRemoveEvent != null) this.onRemoveEvent.execute(this.hullModEffect, variant);
	}

	/**
	 * Used in {@link _ehm_basetracker#onSModRemoved() onSModRemoved()}; where the set
	 * of the old s-modifications are processed. If the s-modification in question has
	 * a registered s-mod clean-up event, this method fires it. 
	 * @param variant of the ship that'll be passed to any custom methods
	 */
	protected void executeSModCleanUp(ShipVariantAPI variant) {
		if (this.sModCleanUpEvent != null) this.sModCleanUpEvent.execute(this.hullModEffect, variant);
	}
}