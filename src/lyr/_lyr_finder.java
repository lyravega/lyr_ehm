package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import org.apache.log4j.Logger;

public class _lyr_finder {
	protected static Class<?> hullSpecClass;
	protected static Class<?> shieldSpecClass;
	protected static Class<?> weaponSlotClass;
	protected static Class<?> engineBuilderClass;
	protected static Class<?> engineStyleEnum;
	protected static String engineStyleSetterName;
	protected static Class<?> nodeClass;

	private static final List<ShipHullSpecAPI> allHullSpecs = Global.getSettings().getAllShipHullSpecs();

	private static final Logger logger = Logger.getLogger("lyr");
	private static final Lookup lookup = MethodHandles.lookup();
	private static Class<?> methodClass;
	private static MethodHandle getName;
	private static MethodHandle getParameterTypes;
	private static MethodHandle getReturnType;

	static {
		try {
			methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
			getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
			getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
			getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}

		try { // ORDER SENSITIVE
			if (!findHullSpecClass()) { logger.error("hullSpec class not found"); }
			if (!findShieldSpecClass()) { logger.error("shieldSpec class not found"); }
			if (!findWeaponSlotClass()) { logger.error("weaponSlot class not found"); }
			if (!findNodeClass()) { logger.error("node class not found"); }
			if (!findEngineBuilderClass()) { logger.error("engineBuilder class not found"); }
			if (!findEngineStyleEnum()) { logger.error("engineStyle enum not found"); }
			if (!findEngineStyleSetterName()) { logger.error("styleSetter method name not found"); }
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static boolean findHullSpecClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			if (hullSpec == null) continue; 

			hullSpecClass = hullSpec.getClass();

			return true;
		} return false;
	}

	private static boolean findShieldSpecClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			if (hullSpec.getShieldSpec() == null) continue; 

			shieldSpecClass = hullSpec.getShieldSpec().getClass();
			
			return true;
		} return false;
	}

	private static boolean findWeaponSlotClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			Iterator<WeaponSlotAPI> i = hullSpec.getAllWeaponSlotsCopy().iterator();

			if (i.hasNext()) weaponSlotClass = hullSpec.getWeaponSlotAPI(i.next().getId()).getClass(); 
			
			return true;
		} return false;
	}

	private static boolean findNodeClass() throws ClassNotFoundException {
		for (Object method : weaponSlotClass.getDeclaredMethods()) { 
			String stringToButcher = method.toString(); 

			if (!stringToButcher.contains("getNode()")) continue;

			nodeClass = Class.forName(stringToButcher.split(" ")[1]); 
			
			return true;
		} return false;
	}

	private static boolean findEngineBuilderClass() throws Throwable {
		List<?> engineSlots = new ArrayList<>();
		
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			MethodHandle getEngineSlots = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "getEngineSlots", MethodType.methodType(List.class));
			engineSlots = (List<?>) getEngineSlots.invoke(hullSpec);

			if (engineSlots.size() == 0) continue;

			engineBuilderClass = engineSlots.get(0).getClass(); 

			return true;
		} return false;
	}

	private static boolean findEngineStyleEnum() {
		for (Class<?> clazz : engineBuilderClass.getDeclaredClasses()) {
			if (clazz.isEnum()) {
				engineStyleEnum = clazz; 
				
				return true;
			}
		} return false;
	}

	private static boolean findEngineStyleSetterName() {
		String enumName = "("+engineStyleEnum.getName()+")"; 
		for (Object method : engineBuilderClass.getMethods()) {
			String stringToButcher = method.toString(); 
			
			if (!stringToButcher.contains(enumName)) continue;

			String[] bitsAndPieces = stringToButcher.split(" ")[2].replace(enumName, "").split("\\.");
			engineStyleSetterName = bitsAndPieces[bitsAndPieces.length-1]; 
			
			return true;
		} return false;
	}

	/**
	 * Pass a class, and a methodName as a string to get the returnType and
	 * parameterTypes returned, alonside a MethodType and a ready-to-use 
	 * MethodHandle to use the said method. Useful in places where reflect
	 * package isn't accessible due to the script classLoader.  
	 * @param clazz to search the methodName on
	 * @param methodName in String, no "()"
	 * @return a map with "returnType", "parameterTypes", "methodType" and "methodHandle" keys
	 * @throws Throwable
	 */
	public static Map<String, Object> findTypesForMethod(Class<?> clazz, String methodName) throws Throwable {
		Class<?> returnType = null;
		Class<?>[] parameterTypes = null;
		Object[] methods = clazz.getDeclaredMethods();

		for (Object method : methods) {
			if (!String.class.cast(getName.invoke(method)).equals(methodName)) continue;

			parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);
			returnType = (Class<?>) getReturnType.invoke(method);
			break;
		}

		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		MethodHandle methodHandle = MethodHandles.lookup().findVirtual(clazz, methodName, MethodType.methodType(returnType, parameterTypes));
		Map<String, Object> typeMap = new HashMap<String, Object>();
		typeMap.put("returnType", returnType);
		typeMap.put("parameterTypes", parameterTypes);
		typeMap.put("methodType", methodType);
		typeMap.put("methodHandle", methodHandle);
		return typeMap;
	}
}
