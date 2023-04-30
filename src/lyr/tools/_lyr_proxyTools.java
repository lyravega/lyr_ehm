package lyr.tools;

import com.fs.starfarer.api.Global;

import lyr.misc.lyr_internals;

import org.apache.log4j.Logger;

/**
 * Provides methods to seek and capture the obfuscated classes 
 * @author lyravega
 */
public class _lyr_proxyTools extends _lyr_reflectionTools {
	protected static final Class<?> hullSpecClass;
	protected static final Class<?> shieldSpecClass;
	protected static final Class<?> weaponSlotClass;
	protected static final Class<?> nodeClass;
	protected static final Class<?> engineBuilderClass;
	protected static final Class<?> engineStyleEnum;

	protected static final Logger logger = Logger.getLogger(lyr_internals.logName);

	static {
		hullSpecClass = lyr_findHullSpecClass();
		shieldSpecClass = lyr_findShieldSpecClass();
		weaponSlotClass = lyr_findWeaponSlotClass();
		nodeClass = lyr_findNodeClass();
		engineBuilderClass = lyr_findEngineBuilderClass();
		engineStyleEnum = lyr_findEngineStyleEnum();
	}

	private static Class<?> lyr_findHullSpecClass() {
		try {
			return Global.getSettings().getAllShipHullSpecs().iterator().next().getClass();
		} catch (Exception e) { // this doesn't throw jack shit, but 'MUH GAEMUR OCD' (TM)
			logger.fatal(lyr_internals.logPrefix+"'hullSpecClass' not found in '_lyr_proxyTools'", e); return null;
		}
	}

	private static Class<?> lyr_findShieldSpecClass() {
		try {
			return inspectMethod("getShieldSpec", hullSpecClass).getReturnType();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"'shieldSpecClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findWeaponSlotClass() {
		try {
			return inspectMethod("getWeaponSlot", hullSpecClass).getReturnType();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"'weaponSlotClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findNodeClass() {
		try {
			return inspectMethod("getNode", weaponSlotClass).getReturnType();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"'nodeClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findEngineBuilderClass() {
		try {
			return inspectMethod("addEngineSlot", hullSpecClass).getParameterTypes()[0];
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"'engineBuilderClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findEngineStyleEnum() {
		for (Class<?> clazz : engineBuilderClass.getDeclaredClasses()) {
			if (clazz.isEnum()) return clazz;
		} logger.fatal(lyr_internals.logPrefix+"'engineStyleEnum' not found in '_lyr_proxyTools'"); return null;
	}
}
