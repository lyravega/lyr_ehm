package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Iterator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import org.lwjgl.util.vector.Vector2f;

/**
 * A proxy-like class for {@link WeaponSlotAPI} that utilizes obfuscated 
 * methods without referring to them. 
 * <p> Some of the methods in the proxy may have API variants, but they're 
 * also implemented here simply to get suggestions. In addition, such 
 * methods avoid using the API variants even when their arguments and/or
 * return types aren't from an obfuscated class.
 * <p> Use {@link #retrieve()} to grab the stored {@link WeaponSlotAPI}.
 * @author lyravega
 * @version 0.6
 * @since 0.6
 */
public class lyr_weaponSlot {
	private WeaponSlotAPI weaponSlot;
	private static Class<?> obfuscatedWeaponSlotClass = null;
	private static Class<?> obfuscatedNodeClass = null;

	static {
		SettingsAPI settings = Global.getSettings();
		for (String variantId : settings.getAllVariantIds()) { // get all the variant ids
			ShipVariantAPI variant = settings.getVariant(variantId); // start with a variant
			Iterator<String> i = variant.getNonBuiltInWeaponSlots().iterator(); // assign an iterator for weapon slots
			
			if (!i.hasNext()) continue; // check if there is a weapon slot

			obfuscatedWeaponSlotClass = variant.getSlot(i.next()).getClass(); break; // retrieve the class of the weapon slot
		}

		try { String searchMethod = "method";
			switch (searchMethod) {
				// case "hacky": { // avoid, as the whole reason we're doing this is staying within API
				// 	ShipVariantAPI hackVariant = Global.getSettings().createEmptyVariant("lyr_hack", Global.getSettings().getHullSpec("omen"));
				// 	obfuscatedNodeClass = HullVariantSpec.class.cast(hackVariant).getHullSpec().getWeaponSlot("WS 001").getNode().getClass();
				// } break;
				case "field": { // could be unsafe, as changes in the field might mess up with the string filter or give the wrong result
					for (Object field : obfuscatedWeaponSlotClass.getDeclaredFields()) { // you can get fields, but you cannot use any Field methods
						String stringToButcher = field.toString().split(" ")[1]; // so we butcher field.toString() instead
		
						if (!stringToButcher.startsWith("starfarer") || !stringToButcher.contains("specs") || stringToButcher.contains("$")) continue;
						
						obfuscatedNodeClass = Class.forName(stringToButcher); break;
					}
				} break;
				case "method": { // much safer, but relies on method name staying the same but it should stay the same unless refactored
					for (Object method : obfuscatedWeaponSlotClass.getDeclaredMethods()) { // you can get methods, but you cannot use any Method methods
						String stringToButcher = method.toString(); // so we butcher method.toString() instead
	
						if (!stringToButcher.contains("getNode()")) continue;
						
						obfuscatedNodeClass = Class.forName(stringToButcher.split(" ")[1]); break;
					}
				} break;
			}
		} catch (Throwable t) {
			t.printStackTrace(); 
		}
	}

	/**
	 * Creates a new instance for the passed weaponSlot, and clones it if
	 * necessary. Alterations should be done on a clone if it is going to
	 * be a new slot.
	 * @param weaponSlot to be proxied
	 * @param clone if the weaponSlot needs to be cloned
	 */
	public lyr_weaponSlot(WeaponSlotAPI weaponSlot, boolean clone) {
		this.weaponSlot = weaponSlot;
		this.weaponSlot = (clone) ? this.clone() : weaponSlot;
	}
	
	/**
	 * Used to retrieve the stored {@link WeaponSlotAPI} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link WeaponSlotAPI}
	 */
	public WeaponSlotAPI retrieve() {
		return weaponSlot;
	}
	
	/**
	 * Used to exchange the {@link WeaponSlotAPI} stored in the proxy
	 * class in order to re-use this proxy instead of creating new ones.
	 * @param weaponSlot to exchange with the stored one
	 * @return the proxy itself for chaining purposes
	 */
	public lyr_weaponSlot recycle(WeaponSlotAPI weaponSlot) {
		this.weaponSlot = weaponSlot;
		return this;
	}

	/**
	 * Overrides the default {@link #clone()} method; instead of cloning the
	 * instance of the class, clones the stored {@link WeaponSlotAPI}, 
	 * and returns it. If clone fails, returns the original. 
	 * @return a cloned {@link WeaponSlotAPI}
	 * @category Proxied methods
	 */
	@Override
	public WeaponSlotAPI clone() {
		try {
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "clone", MethodType.methodType(obfuscatedWeaponSlotClass));
			return (WeaponSlotAPI) clone.invoke(weaponSlot);
		} catch (Throwable t) {
			t.printStackTrace(); 
		} return weaponSlot; // java, pls...
	}

	/**
	 * Sets the weapon type of the slot to the passed weaponType. 
	 * @param weaponType to be set on the slot
	 * @category Proxied methods
	 */
	public void setWeaponType(WeaponType weaponType) {
		try {
			MethodHandle setWeaponType = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "setWeaponType", MethodType.methodType(void.class, WeaponType.class));
			setWeaponType.invoke(weaponSlot, weaponType);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public boolean isWeaponSlot() {
		try {
			MethodHandle isWeaponSlot = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "isWeaponSlot", MethodType.methodType(boolean.class));
			return (boolean) isWeaponSlot.invoke(weaponSlot);
		} catch (Throwable t) {
			t.printStackTrace();
		} return false; // java, pls...
	}

	public void setId(String weaponSlotId) {
		try {
			MethodHandle setId = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "setId", MethodType.methodType(void.class, String.class));
			setId.invoke(weaponSlot, weaponSlotId);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void setSlotSize(WeaponSize weaponSize) {
		try {
			MethodHandle setSlotSize = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "setSlotSize", MethodType.methodType(void.class, WeaponSize.class));
			setSlotSize.invoke(weaponSlot, weaponSize);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void setNode(String nodeId, Vector2f location) {
		try {
			Lookup lookup = MethodHandles.lookup();

			MethodHandle newNode = lookup.findConstructor(obfuscatedNodeClass, MethodType.methodType(void.class, String.class, Vector2f.class));
			MethodHandle setNode = lookup.findVirtual(obfuscatedWeaponSlotClass, "setNode", MethodType.methodType(void.class, obfuscatedNodeClass));
			setNode.invoke(weaponSlot, obfuscatedNodeClass.cast(newNode.invoke(nodeId, location)));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
