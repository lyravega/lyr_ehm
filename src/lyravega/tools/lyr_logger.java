package lyravega.tools;

import org.apache.log4j.Logger;

public interface lyr_logger {
	public static final Logger logger = Logger.getLogger("EHM");
	public static final String logPrefix = "(Experimental Hull Modifications) - ";
	public static final boolean eventInfo = false;
	public static final boolean marketInfo = true;
}
