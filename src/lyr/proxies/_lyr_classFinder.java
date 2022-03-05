package lyr.proxies;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import org.apache.log4j.Logger;

/**
 * Provides methods to search for the obfuscated classes so that they 
 * can be utilized through proxy-like classes.
 * @author lyravega
 */
public class _lyr_classFinder {
	protected static Class<?> hullSpecClass;
	protected static Class<?> shieldSpecClass;
	protected static Class<?> weaponSlotClass;
	protected static Class<?> engineBuilderClass;
	protected static Class<?> engineStyleEnum;
	protected static String engineStyleSetterName;
	protected static Class<?> nodeClass;

	protected static final Logger logger = Logger.getLogger("lyr");
	private static final List<ShipHullSpecAPI> allHullSpecs = Global.getSettings().getAllShipHullSpecs();

	static {
		try { // ORDER SENSITIVE
			if (!lyr_findHullSpecClass()) { logger.error("hullSpec class not found"); }
			if (!lyr_findShieldSpecClass()) { logger.error("shieldSpec class not found"); }
			if (!lyr_findWeaponSlotClass()) { logger.error("weaponSlot class not found"); }
			if (!lyr_findNodeClass()) { logger.error("node class not found"); }
			if (!lyr_findEngineBuilderClass()) { logger.error("engineBuilder class not found"); }
			if (!lyr_findEngineStyleEnum()) { logger.error("engineStyle enum not found"); }
			if (!lyr_findEngineStyleSetterName()) { logger.error("styleSetter method name not found"); }
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static boolean lyr_findHullSpecClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			if (hullSpec == null) continue; 

			hullSpecClass = hullSpec.getClass();

			return true;
		} return false;
	}

	private static boolean lyr_findShieldSpecClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			if (hullSpec.getShieldSpec() == null) continue; 

			shieldSpecClass = hullSpec.getShieldSpec().getClass();
			
			return true;
		} return false;
	}

	private static boolean lyr_findWeaponSlotClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			Iterator<WeaponSlotAPI> i = hullSpec.getAllWeaponSlotsCopy().iterator();

			if (i.hasNext()) weaponSlotClass = hullSpec.getWeaponSlotAPI(i.next().getId()).getClass(); 
			
			return true;
		} return false;
	}

	private static boolean lyr_findNodeClass() throws ClassNotFoundException {
		for (Object method : weaponSlotClass.getDeclaredMethods()) { 
			String stringToButcher = method.toString(); 

			if (!stringToButcher.contains("getNode()")) continue;

			nodeClass = Class.forName(stringToButcher.split(" ")[1]); 
			
			return true;
		} return false;
	}

	private static boolean lyr_findEngineBuilderClass() throws Throwable {
		List<?> engineSlots = new ArrayList<>();
		
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			MethodHandle getEngineSlots = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "getEngineSlots", MethodType.methodType(List.class));
			engineSlots = (List<?>) getEngineSlots.invoke(hullSpec);

			if (engineSlots.size() == 0) continue;

			engineBuilderClass = engineSlots.get(0).getClass(); 

			return true;
		} return false;
	}

	private static boolean lyr_findEngineStyleEnum() {
		for (Class<?> clazz : engineBuilderClass.getDeclaredClasses()) {
			if (clazz.isEnum()) {
				engineStyleEnum = clazz; 
				
				return true;
			}
		} return false;
	}

	private static boolean lyr_findEngineStyleSetterName() {
		String enumName = "("+engineStyleEnum.getName()+")"; 
		for (Object method : engineBuilderClass.getMethods()) {
			String stringToButcher = method.toString(); 
			
			if (!stringToButcher.contains(enumName)) continue;

			String[] bitsAndPieces = stringToButcher.split(" ")[2].replace(enumName, "").split("\\.");
			engineStyleSetterName = bitsAndPieces[bitsAndPieces.length-1]; 
			
			return true;
		} return false;
	}
}
