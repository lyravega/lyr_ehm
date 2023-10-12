package lyravega.tools.logger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class lyr_logger {
	private static final Logger logger = Logger.getLogger("Test Logger");

	public static void setLevel(Level level) { logger.setLevel(level); }

	public static void eventInfo(Object message) { logger.log(lyr_levels.EVENT, message, null); }

	public static void eventInfo(Object message, Throwable t) { logger.log(lyr_levels.EVENT, message, t); }

	public static void reflectionInfo(Object message) { logger.log(lyr_levels.RFLCT, message, null); }

	public static void reflectionInfo(Object message, Throwable t) { logger.log(lyr_levels.RFLCT, message, t); }

	public static void listenerInfo(Object message) { logger.log(lyr_levels.LSTNR, message, null); }

	public static void listenerInfo(Object message, Throwable t) { logger.log(lyr_levels.LSTNR, message, t); }

	public static void warn(Object message) { logger.warn(message);}

	public static void warn(Object message, Throwable t) { logger.warn(message, t); }

	public static void info(Object message) { logger.info(message); }

	public static void info(Object message, Throwable t) { logger.info(message, t); }

	public static void fatal(Object message) { logger.fatal(message); }

	public static void fatal(Object message, Throwable t) { logger.fatal(message, t); }

	public static void error(Object message) { logger.error(message); }

	public static void error(Object message, Throwable t) { logger.error(message, t); }

	public static void debug(Object message) { logger.debug(message); }

	public static void debug(Object message, Throwable t) { logger.debug(message, t); }

	public static void log(Priority priority, Object message) { logger.log(priority, message); }

	public static void log(Priority priority, Object message, Throwable t) { logger.log(priority, message, t); }
}
