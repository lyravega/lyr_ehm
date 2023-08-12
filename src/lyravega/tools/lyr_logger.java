package lyravega.tools;

import org.apache.log4j.Logger;

public interface lyr_logger {
	public static final Logger logger = Logger.getLogger("EHM");
	public static final String logPrefix = "(Experimental Hull Modifications) - ";
	public static final boolean eventInfo = true;
	public static final boolean trackerInfo = true;
	public static final boolean listenerInfo = true;
	public static final boolean debug = true;
}
