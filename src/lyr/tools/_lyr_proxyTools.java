package lyr.tools;

import com.fs.starfarer.api.Global;

import org.apache.log4j.Logger;

/**
 * Provides methods to search for the obfuscated classes so that they 
 * can be utilized through proxy-like classes.
 * @author lyravega
 */
public class _lyr_proxyTools extends _lyr_reflectionTools { // TODO: move methodHandles to a static block, use reflectionTools for assistance if necessary
	protected static final Class<?> hullSpecClass;
	protected static final Class<?> shieldSpecClass;
	protected static final Class<?> weaponSlotClass;
	protected static final Class<?> nodeClass;
	protected static final Class<?> engineBuilderClass;
	protected static final Class<?> engineStyleEnum;
	protected static final Object engineStyleSetter;

	protected static final Logger logger = Logger.getLogger("lyr");

	static {
		hullSpecClass = lyr_findHullSpecClass();
		shieldSpecClass = lyr_findShieldSpecClass();
		weaponSlotClass = lyr_findWeaponSlotClass();
		nodeClass = lyr_findNodeClass();
		engineBuilderClass = lyr_findEngineBuilderClass();
		engineStyleEnum = lyr_findEngineStyleEnum();
		engineStyleSetter = lyr_findEngineStyleSetter();
	}

	private static Class<?> lyr_findHullSpecClass() {
		try {
			return Global.getSettings().getAllShipHullSpecs().iterator().next().getClass();
		} catch (Exception e) { // this doesn't throw jack shit, but 'MUH GAEMUR OCD' (TM)
			logger.fatal("'hullSpecClass' not found in '_lyr_proxyTools'", e); return null;
		}
	}

	private static Class<?> lyr_findShieldSpecClass() {
		try {
			return inspectMethod(hullSpecClass, "getShieldSpec").getReturnType();
		} catch (Throwable t) {
			logger.fatal("'shieldSpecClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findWeaponSlotClass() {
		try {
			return inspectMethod(hullSpecClass, "getWeaponSlot").getReturnType();
		} catch (Throwable t) {
			logger.fatal("'weaponSlotClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findNodeClass() {
		try {
			return inspectMethod(weaponSlotClass, "getNode").getReturnType();
		} catch (Throwable t) {
			logger.fatal("'nodeClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findEngineBuilderClass() {
		try {
			return inspectMethod(hullSpecClass, "addEngineSlot").getParameterTypes()[0];
		} catch (Throwable t) {
			logger.fatal("'engineBuilderClass' not found in '_lyr_proxyTools'", t); return null;
		}
	}

	private static Class<?> lyr_findEngineStyleEnum() {
		for (Class<?> clazz : engineBuilderClass.getDeclaredClasses()) {
			if (clazz.isEnum()) return clazz;
		} logger.fatal("'engineStyleEnum' not found in '_lyr_proxyTools'"); return null;
	}

	private static Object lyr_findEngineStyleSetter() {
		for (Object method : engineBuilderClass.getMethods()) {
			Class<?>[] parameterTypes = null;
			try {
				parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);
			} catch (Throwable e) {}

			if (parameterTypes == null || parameterTypes.length == 0) continue;

			if (parameterTypes[0].equals(engineStyleEnum)) return method;
		} logger.fatal("'engineStyleSetterName' not found in '_lyr_proxyTools'"); return null;
	}
}
