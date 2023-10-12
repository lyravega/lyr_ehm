package lyravega.tools.logger;

import org.apache.log4j.Level;

public class lyr_levels extends Level {
	protected lyr_levels(int level, String levelStr, int syslogEquivalent) {
		super(level, levelStr, syslogEquivalent);
	}

	public final static int LSTNR_INT = INFO_INT - 10;
	public final static String LSTNR_STR = "LSTNR";
	final static public Level LSTNR = new lyr_levels(LSTNR_INT, LSTNR_STR, 6);

	public final static int EVENT_INT = INFO_INT - 100;
	public final static String EVENT_STR = "EVENT";
	final static public Level EVENT = new lyr_levels(EVENT_INT, EVENT_STR, 6);

	public final static int RFLCT_INT = INFO_INT - 1000;
	public final static String RFLCT_STR = "RFLCT";
	final static public Level RFLCT = new lyr_levels(RFLCT_INT, RFLCT_STR, 6);

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
