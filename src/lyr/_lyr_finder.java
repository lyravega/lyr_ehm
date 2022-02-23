package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class _lyr_finder {
	private static final int toFind = 7;
	protected static Class<?> obfuscatedHullSpecClass = null;
	protected static Class<?> obfuscatedShieldSpecClass = null;
	protected static Class<?> obfuscatedWeaponSlotClass = null;
	protected static Class<?> obfuscatedEngineBuilderClass = null;
	protected static Class<?> obfuscatedEngineStyleEnum = null;
	protected static String obfuscatedEngineStyleSetterName = null;
	protected static Class<?> obfuscatedNodeClass = null;

	static {
		try {	
			SettingsAPI settings = Global.getSettings();
			int found = 0;
			for (String variantId : settings.getAllVariantIds()) { // get all the variant ids
				ShipVariantAPI variant = settings.getVariant(variantId); // start with a variant
				ShipHullSpecAPI hullSpec = variant.getHullSpec(); // set a hullSpec
				ShieldSpecAPI shieldSpec = hullSpec.getShieldSpec(); // and a shieldSpec
				Iterator<String> i = variant.getNonBuiltInWeaponSlots().iterator(); // but an iterator for weaponSlot
				
				if (obfuscatedWeaponSlotClass == null && i.hasNext()) { // check if there is a slot (no, for real. for some, there isn't)
					obfuscatedWeaponSlotClass = variant.getSlot(i.next()).getClass(); found++; // retrieve the class of the weapon slot

					for (Object method : obfuscatedWeaponSlotClass.getDeclaredMethods()) { // you can get methods, but you cannot use any Method methods
						String stringToButcher = method.toString(); // so we butcher method.toString() instead

						if (!stringToButcher.contains("getNode()")) continue;

						obfuscatedNodeClass = Class.forName(stringToButcher.split(" ")[1]); found++; break;
					}
				}

				if (obfuscatedHullSpecClass == null && hullSpec != null) { // I have seen weird things happen.
					obfuscatedHullSpecClass = hullSpec.getClass(); found++; // get the obfuscated hullSpec class
				}

				if (obfuscatedShieldSpecClass == null && shieldSpec != null) { // null checks are 'healthy'. better than an unexpected crash
					obfuscatedShieldSpecClass = hullSpec.getShieldSpec().getClass(); found++; // get the obfuscated shieldSpec class
				}

				if (obfuscatedEngineBuilderClass == null && hullSpec != null) {
					List<?> engineSlots = new ArrayList<>();

					MethodHandle getEngineSlots = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "getEngineSlots", MethodType.methodType(List.class));
					engineSlots = (List<?>) getEngineSlots.invoke(hullSpec);

					if (engineSlots.get(0) != null) {
						obfuscatedEngineBuilderClass = engineSlots.get(0).getClass(); found++; // yoink

						for (Class<?> clazz : obfuscatedEngineBuilderClass.getDeclaredClasses()) { // this is somewhat hacky but should be reliable. usage will be pretty hardcoded though
							if (clazz.isEnum()) {
								obfuscatedEngineStyleEnum = clazz; found++; break; // there are 2 declared classes, one of them isEnum and the one we seek
							}
						}

						String enumName = "("+obfuscatedEngineStyleEnum.getName()+")"; // this may look extremely hacky, but should be safe; working with full class name
						for (Object method : obfuscatedEngineBuilderClass.getMethods()) {
							String stringToButcher = method.toString(); 
							
							if (!stringToButcher.contains(enumName)) continue;

							String[] bitsAndPieces = stringToButcher.split(" ")[2].replace(enumName, "").split("\\.");
							obfuscatedEngineStyleSetterName = bitsAndPieces[bitsAndPieces.length-1]; found++; break; 
						}
					}
				}

				if (found >= toFind) break;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
