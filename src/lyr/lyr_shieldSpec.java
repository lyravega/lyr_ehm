package lyr;

import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;

/**
 * A proxy-like class for {@link ShieldSpecAPI} that utilizes obfuscated 
 * methods without referring to them. 
 * <p> Some of the methods in the proxy may have API variants, but they're 
 * also implemented here simply to get suggestions. In addition, such 
 * methods avoid using the API variants even when their arguments and/or
 * return types aren't from an obfuscated class.
 * <p> Use {@link #retrieve()} to grab the stored {@link ShieldSpecAPI}.
 * @author lyravega
 * @version 0.7
 * @since 0.7
 */
public class lyr_shieldSpec {
	private ShieldSpecAPI shieldSpec;
	private static final Class<?> obfuscatedShieldSpecClass = _lyr_finder.obfuscatedShieldSpecClass;

	/**
	 * Creates a new instance for the passed {@link ShieldSpecAPI}, and 
	 * clones it if necessary. 
	 * <p> The clone argument MUST be set to true if the shieldSpec is not 
	 * unique; not cloned prior to the creation of this instance of the 
	 * proxy-class. Otherwise changes WILL apply to ALL ships with the same 
	 * shieldSpec.
	 * <p> If this is a shieldSpec of an  already cloned hullSpec, then the 
	 * shieldSpec is already cloned. No need to clone it for yet another
	 * time in that case.
	 * @param shieldSpec to be proxied
	 * @param clone if the shieldSpec needs to be cloned
	 */
	public lyr_shieldSpec(ShieldSpecAPI shieldSpec, boolean clone) {
		this.shieldSpec = (clone) ? this.duplicate(shieldSpec) : shieldSpec;
	}
	
	/**
	 * Used to retrieve the stored {@link ShieldSpecAPI} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link ShieldSpecAPI}
	 */
	public ShieldSpecAPI retrieve() {
		return shieldSpec;
	}
	
	/**
	 * Used to exchange the {@link ShieldSpecAPI} stored in the proxy
	 * class in order to re-use this proxy instead of creating new ones.
	 * @param shieldSpec to exchange with the stored one
	 * @return the proxy itself for chaining purposes
	 */
	public lyr_shieldSpec recycle(ShieldSpecAPI shieldSpec) {
		this.shieldSpec = shieldSpec;
		return this;
	}

	//#region API-LIKE & PROXIED METHODS
	/**
	 * Clones the stored {@link ShieldSpecAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link ShieldSpecAPI}
	 * @category Proxied method
	 */
	protected ShieldSpecAPI duplicate(ShieldSpecAPI shieldSpec) {
		try {
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedShieldSpecClass, "clone", MethodType.methodType(obfuscatedShieldSpecClass));
			return (ShieldSpecAPI) clone.invoke(shieldSpec);
		} catch (Throwable t) {
			t.printStackTrace();
		} return shieldSpec; // java, pls...
	}
	
	/**
	 * A cheap clone that creates and returns a new instance of this
	 * object with a duplicate of its stored object. 
	 * @return a cloned {@link lyr_shieldSpec}
	 */
	@Override
	public lyr_shieldSpec clone() {
		return new lyr_shieldSpec(shieldSpec, true);
	}

	/**
	 * Sets the ring colour of the stored {@link ShieldSpecAPI} to
	 * the passed one. 
	 * @param colour object to apply
	 * @category Proxied method
	 */
	public void setRingColor(Color colour) {
		try {
			MethodHandle setRingColor = MethodHandles.lookup().findVirtual(obfuscatedShieldSpecClass, "setRingColor", MethodType.methodType(void.class, Color.class));
			setRingColor.invoke(shieldSpec, colour);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Sets the inner colour of the stored {@link ShieldSpecAPI} to
	 * the passed one. 
	 * @param colour object to apply
	 * @category Proxied method
	 */
	public void setInnerColor(Color colour) {
		try {
			MethodHandle setInnerColor = MethodHandles.lookup().findVirtual(obfuscatedShieldSpecClass, "setInnerColor", MethodType.methodType(void.class, Color.class));
			setInnerColor.invoke(shieldSpec, colour);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	//#endregion 
	// END OF API-LIKE & PROXIED METHODS
}
