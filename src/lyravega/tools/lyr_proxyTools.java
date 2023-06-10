package lyravega.tools;

import com.fs.starfarer.api.Global;

/**
 * Provides methods to seek and capture the obfuscated classes 
 * @author lyravega
 */
public class lyr_proxyTools extends lyr_reflectionTools {
	protected static final Class<?> hullSpecClass;
	protected static final Class<?> shieldSpecClass;
	protected static final Class<?> weaponSlotClass;
	protected static final Class<?> nodeClass;
	protected static final Class<?> engineBuilderClass;
	protected static final Class<?> engineStyleIdEnum;
	protected static final Class<?> engineStyleSpecClass;

	static {
		hullSpecClass = lyr_findHullSpecClass();
		shieldSpecClass = lyr_findShieldSpecClass();
		weaponSlotClass = lyr_findWeaponSlotClass();
		nodeClass = lyr_findNodeClass();
		engineBuilderClass = lyr_findEngineBuilderClass();
		engineStyleIdEnum = lyr_findEngineStyleIdEnum();
		engineStyleSpecClass = lyr_findEngineStyleSpecClass();
	}

	private static Class<?> lyr_findHullSpecClass() {
		try {
			return Global.getSettings().getAllShipHullSpecs().iterator().next().getClass();
		} catch (Exception e) { // this doesn't throw jack shit, but 'MUH GAEMUR OCD' (TM)
			logger.fatal(logPrefix+"'hullSpecClass' not found in '_lyr_proxyTools'", e); return null;
		}
	}

	private static Class<?> lyr_findShieldSpecClass() {
		try {
			return inspectMethod("getShieldSpec", hullSpecClass).getReturnType();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"'shieldSpecClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findWeaponSlotClass() {
		try {	// special case: there also is a public synthetic bridge method for this and as such the class has two of the same methods
			return inspectMethod(true, "getWeaponSlot", 1, hullSpecClass).getReturnType();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"'weaponSlotClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findNodeClass() {
		try {
			return inspectMethod("getNode", weaponSlotClass).getReturnType();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"'nodeClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findEngineBuilderClass() {
		try {
			return inspectMethod("addEngineSlot", hullSpecClass).getParameterTypes()[0];
		} catch (Throwable t) {
			logger.fatal(logPrefix+"'engineBuilderClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findEngineStyleIdEnum() {
		for (Class<?> clazz : engineBuilderClass.getDeclaredClasses()) {
			if (clazz.isEnum()) return clazz;
		} logger.fatal(logPrefix+"'engineStyleIdEnum' not found in '_lyr_proxyTools'"); return null;
	}

	private static Class<?> lyr_findEngineStyleSpecClass() {
		for (Class<?> clazz : engineBuilderClass.getDeclaredClasses()) {
			if (!clazz.isEnum()) return clazz;
		} logger.fatal(logPrefix+"'engineStyleSpecClass' not found in '_lyr_proxyTools'"); return null;
	}
}
