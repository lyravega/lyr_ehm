package lyravega.utilities.logger;

import org.apache.log4j.Level;

/**
 * A class that adds a few custom log levels to better control
 * the flow of information
 * @author lyravega
 */
public class lyr_levels extends Level {
	protected lyr_levels(int level, String levelStr, int syslogEquivalent) {
		super(level, levelStr, syslogEquivalent);
	}

	public static final int LSTNR_INT = INFO_INT - 10;
	public static final String LSTNR_STR = "LSTNR";
	public static final Level LSTNR;

	public static final int EVENT_INT = INFO_INT - 100;
	public static final String EVENT_STR = "EVENT";
	public static final Level EVENT;

	public static final int RFLCT_INT = DEBUG_INT + 1000;
	public static final String RFLCT_STR = "RFLCT";
	public static final Level RFLCT;

	/**
	 * Noticed a weird issue with statics. If the static block isn't below here and assignments are done above, and
	 * if the class isn't getting forcibly loaded with something like 'Class.forName(lyr_levels.class.getName())'
	 * then the class may not get loaded / initialized at all
	 * 
	 * This is an issue because if a static field is referenced in another class, it will register as a null, and
	 * possibly result in null pointer exceptions everywhere. To avoid that bizarre issue, the class needs to be
	 * force-loaded or the assignments needs to be done in the static block below
	 */

	static {
		LSTNR = new lyr_levels(LSTNR_INT, LSTNR_STR, 6);	// info-like
		EVENT = new lyr_levels(EVENT_INT, EVENT_STR, 6);	// info-like
		RFLCT = new lyr_levels(RFLCT_INT, RFLCT_STR, 7);	// debug-like
	}

	public static Level toLevel(String logArgument) {
		return toLevel(logArgument, Level.DEBUG);
	}

	public static Level toLevel(int val) {
		return toLevel(val, Level.DEBUG);
	}

	public static Level toLevel(int val, Level defaultLevel) {
		switch (val) {
			case EVENT_INT: return EVENT;
			case LSTNR_INT: return LSTNR;
			case RFLCT_INT: return RFLCT;
			default: return Level.toLevel(val, defaultLevel);
		}
	}

	public static Level toLevel(String logArgument, Level defaultLevel) {
		// switch (String.valueOf(logArgument).toUpperCase()) {	// janky null string handling to be used in a switch case
		switch (logArgument == null ? "" : logArgument.toUpperCase()) {	// less janky I guess? meh
			case EVENT_STR: return EVENT;
			case LSTNR_STR: return LSTNR;
			case RFLCT_STR: return RFLCT;
			default: return Level.toLevel(logArgument, defaultLevel);
		}
	}
}
