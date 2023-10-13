package lyravega.listeners;

import com.fs.starfarer.api.Global;

import lyravega.utilities.logger.lyr_logger;

/**
 * A listener base that provides simple methods for attaching/detaching a listener to/from the sector
 * @author lyravega
 */
public abstract class _lyr_sectorListener {
	public final void attach(boolean isTransient) {	// used in plugin's onLoad()
		if (!Global.getSector().getListenerManager().hasListener(this)) {
			Global.getSector().getListenerManager().addListener(this, isTransient);

			lyr_logger.listenerInfo("Attached '"+this.getClass().getSimpleName()+"' listener");
		}
	}

	public final void detach() {
		if (Global.getSector().getListenerManager().hasListener(this)) {
			Global.getSector().getListenerManager().removeListener(this);

			lyr_logger.listenerInfo("Detached '"+this.getClass().getSimpleName()+"' listener");
		}
	}
}
