package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.EngineSlotAPI;

/**
 * A proxy-like class for... engine builder? I have no idea what 
 * this is. As far as I can tell, there is no API equalivent to
 * whatever {@code getEngineSlots()} retrieves.
 * <p> The retrieved objects have EngineSlot data in them, but
 * there is a method that formats all of those slots, hence my 
 * reason calling this a 'builder', which probably formats the 
 * engines according to their 'style' enum,and sets them on the 
 * ship.
 * <p> Accessing the EngineSlot data directly, and trying to 
 * alter anything has no visible results as far as I can tell. 
 * There also is an EngineSlotAPI, and I was expecting a relation
 * between the core code and the API similar to hullSpec, 
 * shieldSpec, weaponSlot, etc... but that doesn't seem to be 
 * the case.
 * <p> And as there isn't an API equalivent, whatever the method
 * {@code getEngineSlots()} grabs are bound to objects instead.
 * And due to the obfuscation, I had to access the methods / 
 * classes in a way that I do NOT like.
 * <p> As I don't know what this class does what, and exactly 
 * how, the best I can provide is finding the 'style enum' and
 * generate a few hardcoded templates, but nothing more can be
 * added till EngineSlotAPI<->EngineSlot link is found.
 * @author lyravega
 * @version 0.7
 * @since 0.7
 */
public class lyr_engineSlot {
	private Object engineSlot;
	private static final Class<?> obfuscatedEngineBuilderClass = _lyr_finder.obfuscatedEngineBuilderClass;
	private static final Class<?> obfuscatedEngineStyleEnum = _lyr_finder.obfuscatedEngineStyleEnum;
	private static final String obfuscatedEngineStyleSetterName = _lyr_finder.obfuscatedEngineStyleSetterName;
	
	/**
	 * Creates a new instance for the passed {@link EngineSlotAPI}, and 
	 * clones it if necessary. Alterations should be done on a clone if 
	 * it is going to be a new slot.
	 * @param engineSlot to be proxied
	 * @param clone if the engineSlot needs to be cloned
	 */
	public lyr_engineSlot(Object engineSlot, boolean clone) {
		this.engineSlot = (clone) ? this.duplicate(engineSlot) : engineSlot;
	}
	
	/**
	 * Used to retrieve the stored {@link EngineSlotAPI} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link EngineSlotAPI}
	 */
	public Object retrieve() {
		return engineSlot;
	}
	
	/**
	 * Used to exchange the {@link EngineSlotAPI} stored in the proxy
	 * class in order to re-use this proxy instead of creating new ones.
	 * @param engineSlot to exchange with the stored one
	 * @return the proxy itself for chaining purposes
	 */
	public lyr_engineSlot recycle(Object engineSlot) {
		this.engineSlot = engineSlot;
		return this;
	}

	/**
	 * Clones the stored {@link EngineSlotAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link EngineSlotAPI}
	 */
	protected Object duplicate(Object engineSlot) {
		try {
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedEngineBuilderClass, "clone", MethodType.methodType(obfuscatedEngineBuilderClass));
			return (Object) clone.invoke(engineSlot);
		} catch (Throwable t) {
			t.printStackTrace(); 
		} return engineSlot; // java, pls...
	}
	
	//#region API-like methods
	@Override
	public lyr_engineSlot clone() {
		return new lyr_engineSlot(engineSlot, true);
	}
	
	public void setEngineStyle(int enumNumber) {
		try {
			MethodHandle setEngineStyle = MethodHandles.lookup().findVirtual(obfuscatedEngineBuilderClass, obfuscatedEngineStyleSetterName, MethodType.methodType(void.class, obfuscatedEngineStyleEnum));
			setEngineStyle.invoke(obfuscatedEngineBuilderClass.cast(engineSlot), obfuscatedEngineStyleEnum.getEnumConstants()[enumNumber]);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
