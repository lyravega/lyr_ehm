package lyravega.listeners;

import com.fs.starfarer.api.Global;

import lyravega.plugin.lyr_ehm;
import lyravega.tools.lyr_logger;

/**
 * A listener base that provides simple methods for attaching/detaching a listener to/from the sector
 * @author lyravega
 */
public abstract class _lyr_sectorListener implements lyr_logger {
	public final void attach(boolean isTransient) {	// used in plugin's onLoad()
		if (!Global.getSector().getListenerManager().hasListener(this)) {
			Global.getSector().getListenerManager().addListener(this, isTransient);

			if (lyr_ehm.settings.getLogListenerInfo()) logger.info(logPrefix + "Attached '"+this.getClass().getSimpleName()+"' listener");
		}
	}

	public final void detach() {
		if (Global.getSector().getListenerManager().hasListener(this)) {
			Global.getSector().getListenerManager().removeListener(this);

			if (lyr_ehm.settings.getLogListenerInfo()) logger.info(logPrefix + "Detached '"+this.getClass().getSimpleName()+"' listener");
		}
	}
}
