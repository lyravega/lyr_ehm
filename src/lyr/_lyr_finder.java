package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class _lyr_finder {
	protected static Class<?> obfuscatedHullSpecClass;
	protected static Class<?> obfuscatedShieldSpecClass;
	protected static Class<?> obfuscatedWeaponSlotClass;
	protected static Class<?> obfuscatedEngineBuilderClass;
	protected static Class<?> obfuscatedEngineStyleEnum;
	protected static String obfuscatedEngineStyleSetterName;
	protected static Class<?> obfuscatedNodeClass;

	static {
		try {
			if (!findHullSpecClass()) { throw new ClassNotFoundException("hullSpec class not found"); }
			if (!findShieldSpecClass()) { throw new ClassNotFoundException("shieldSpec class not found"); }
			if (!findWeaponSlotClass()) { throw new ClassNotFoundException("weaponSlot class not found"); }
			if (!findNodeClass()) { throw new ClassNotFoundException("node class not found"); }
			if (!findEngineBuilderClass()) { throw new ClassNotFoundException("engineBuilder class not found"); }
			if (!findEngineStyleEnum()) { throw new ClassNotFoundException("engineStyle enum not found"); }
			if (!findEngineStyleSetterName()) { throw new ClassNotFoundException("styleSetter method name not found"); }
		} catch (Throwable t) {
			Global.getLogger(_lyr_finder.class).info(t.getMessage());
		}
	}

	private static final List<ShipHullSpecAPI> allHullSpecs = Global.getSettings().getAllShipHullSpecs();

	private static boolean findHullSpecClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			obfuscatedHullSpecClass = hullSpec.getClass();

			return true;
		} return false;
	}

	private static boolean findShieldSpecClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			if (hullSpec.getShieldSpec() == null) continue; 

			obfuscatedShieldSpecClass = hullSpec.getShieldSpec().getClass();
			
			return true;
		} return false;
	}

	private static boolean findWeaponSlotClass() {
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			Iterator<WeaponSlotAPI> i = hullSpec.getAllWeaponSlotsCopy().iterator();

			if (i.hasNext()) obfuscatedWeaponSlotClass = hullSpec.getWeaponSlotAPI(i.next().getId()).getClass(); 
			
			return true;
		} return false;
	}

	private static boolean findNodeClass() throws ClassNotFoundException {
		for (Object method : obfuscatedWeaponSlotClass.getDeclaredMethods()) { 
			String stringToButcher = method.toString(); 

			if (!stringToButcher.contains("getNode()")) continue;

			obfuscatedNodeClass = Class.forName(stringToButcher.split(" ")[1]); 
			
			return true;
		} return false;
	}

	private static boolean findEngineBuilderClass() throws Throwable {
		List<?> engineSlots = new ArrayList<>();
		
		for (ShipHullSpecAPI hullSpec : allHullSpecs) {
			MethodHandle getEngineSlots = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "getEngineSlots", MethodType.methodType(List.class));
			engineSlots = (List<?>) getEngineSlots.invoke(hullSpec);
	
			if (engineSlots.get(0) != null) {
				obfuscatedEngineBuilderClass = engineSlots.get(0).getClass(); 
				
				return true;
			}
		} return false;
	}

	private static boolean findEngineStyleEnum() {
		for (Class<?> clazz : obfuscatedEngineBuilderClass.getDeclaredClasses()) {
			if (clazz.isEnum()) {
				obfuscatedEngineStyleEnum = clazz; 
				
				return true;
			}
		} return false;
	}

	private static boolean findEngineStyleSetterName() {
		String enumName = "("+obfuscatedEngineStyleEnum.getName()+")"; 
		for (Object method : obfuscatedEngineBuilderClass.getMethods()) {
			String stringToButcher = method.toString(); 
			
			if (!stringToButcher.contains(enumName)) continue;

			String[] bitsAndPieces = stringToButcher.split(" ")[2].replace(enumName, "").split("\\.");
			obfuscatedEngineStyleSetterName = bitsAndPieces[bitsAndPieces.length-1]; 
			
			return true;
		} return false;
	} 
}
