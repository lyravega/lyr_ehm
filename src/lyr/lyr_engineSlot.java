package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.EngineSlotAPI;

public class lyr_engineSlot {
	private Object engineSlot;
	private static final Class<?> obfuscatedEngineStylerClass = _lyr_finder.obfuscatedEngineStylerClass;
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
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedEngineStylerClass, "clone", MethodType.methodType(obfuscatedEngineStylerClass));
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
			MethodHandle setEngineStyle = MethodHandles.lookup().findVirtual(obfuscatedEngineStylerClass, obfuscatedEngineStyleSetterName, MethodType.methodType(void.class, obfuscatedEngineStyleEnum));
			setEngineStyle.invoke(obfuscatedEngineStylerClass.cast(engineSlot), obfuscatedEngineStyleEnum.getEnumConstants()[enumNumber]);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
