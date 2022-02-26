package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A proxy-like class for... engine builder? I have no idea what 
 * this is. As far as I can tell, there is no API equalivent to
 * whatever {@code getEngineSlots()} retrieves.
 * <p> The retrieved objects have EngineSlot data in them, but
 * there is a method that formats all of those slots, hence my 
 * reason calling this a 'builder'. There is a method with 
 * several 'style enums' that keeps all the data, with the last
 * enum being 'CUSTOM' and grabs stuff from the SpecStore. 
 * Accessing them is... meh, I won't even try, to be honest. 
 * Future you (me), just don't. 
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
public class lyr_engineBuilder {
	private Object engineBuilder;
	private static final Class<?> obfuscatedEngineBuilderClass = _lyr_finder.obfuscatedEngineBuilderClass;
	private static final Class<?> obfuscatedEngineStyleEnum = _lyr_finder.obfuscatedEngineStyleEnum;
	private static final String obfuscatedEngineStyleSetterName = _lyr_finder.obfuscatedEngineStyleSetterName;

	public static enum engineStyle { ;
		public static final int lowTech = 0;
		public static final int midline = 1;
		public static final int highTech = 2;
		// public static final int cobraBomber = 3; // nothing
		// public static final int lowTechMissile = 4;
		// public static final int midlineMissile = 5;
		// public static final int highTechMissile = 6;
		// public static final int lowTechFighter = 7;
		public static final int torpedo = 8; // pretty
		// public static final int torpedoAtropos = 9; // ugly
		// public static final int lowTechRocket = 10; // weird cut-off
		// public static final int doritos = 11; // nothing
		public static final int custom = 12;
	}
	
	/**
	 * Creates a new instance for the passed {@link Object}, and 
	 * clones it if necessary. Alterations should be done on a clone if 
	 * it is going to be a new slot.
	 * @param enginebuilder to be proxied
	 * @param clone if the enginebuilder needs to be cloned
	 */
	public lyr_engineBuilder(Object enginebuilder, boolean clone) {
		this.engineBuilder = (clone) ? this.duplicate(enginebuilder) : enginebuilder;
	}
	
	/**
	 * Used to retrieve the stored {@link Object} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link Object}
	 */
	public Object retrieve() {
		return engineBuilder;
	}
	
	/**
	 * Used to exchange the {@link Object} stored in the proxy
	 * class in order to re-use this proxy instead of creating new ones.
	 * @param enginebuilder to exchange with the stored one
	 * @return the proxy itself for chaining purposes
	 */
	public lyr_engineBuilder recycle(Object enginebuilder) {
		this.engineBuilder = enginebuilder;
		return this;
	}

	//#region API-LIKE / PROXIED METHODS
	/**
	 * Clones the stored {@link Object}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link Object}
	 * @category Proxied method
	 */
	protected Object duplicate(Object enginebuilder) {
		try {
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedEngineBuilderClass, "clone", MethodType.methodType(obfuscatedEngineBuilderClass));
			return (Object) clone.invoke(enginebuilder);
		} catch (Throwable t) {
			t.printStackTrace(); 
		} return enginebuilder; // java, pls...
	}
	
	/**
	 * A cheap clone that creates and returns a new instance of this
	 * object with a duplicate of its stored object. 
	 * @return a cloned {@link lyr_engineBuilder}
	 */
	@Override
	public lyr_engineBuilder clone() {
		return new lyr_engineBuilder(engineBuilder, true);
	}
	
	/**
	 * Uses the passed enumNumber to grab an engine style from the
	 * obfuscated code, and invoke the obfuscated engineBuilder 
	 * method with it. 
	 * @param enumNumber
	 * @category Proxied method
	 */
	public void setEngineStyle(int enumNumber) {
		try {
			MethodHandle setEngineStyle = MethodHandles.lookup().findVirtual(obfuscatedEngineBuilderClass, obfuscatedEngineStyleSetterName, MethodType.methodType(void.class, obfuscatedEngineStyleEnum));
			setEngineStyle.invoke(obfuscatedEngineBuilderClass.cast(engineBuilder), obfuscatedEngineStyleEnum.getEnumConstants()[enumNumber]);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	//#endregion 
	// END OF API-LIKE & PROXIED METHODS
}
