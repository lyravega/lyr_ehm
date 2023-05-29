package lyr.proxies;

import java.lang.invoke.MethodHandle;

import lyr.misc.lyr_internals;
import lyr.tools._lyr_proxyTools;

/**
 * A proxy-like class for... engine builder? I have no idea what 
 * this is. As far as I can tell, there is no API equalivent to
 * whatever {@code getEngineSlots()} retrieves.
 * <p> I believe these objects are responsible of creating the
 * engines that go on top of the engineSlots, but for some reason
 * you can only access these, not the engines. If you can feed
 * the correct data to these builders, you'll most probably get
 * the results you want, but it's way too obfuscated for my 
 * tastes to do so, and as such I decided to go for pre-set
 * engines.
 * <p> Unlike the other proxies, whose obfuscated classes has a
 * base on the API, these do not, so they are treated as objects.
 * {@code retrieve()} is still there, but as they are objects,
 * there are no API functions that can be taken advantage of.
 * @author lyravega
 */
public final class lyr_engineBuilder extends _lyr_proxyTools {
	private Object engineBuilder;
	protected static MethodHandle clone = null;
	protected static MethodHandle setEngineStyle = null;

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

	static {
		try {
			clone = inspectMethod("clone", engineBuilderClass).getMethodHandle();
			setEngineStyle = inspectMethod(engineBuilderClass, null, engineStyleEnum).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"Failed to find a method in 'lyr_engineBuilder'", t);
		}
	}
	
	/**
	 * Creates a new instance for the passed {@link Object}, and 
	 * clones it if necessary. Alterations should be done on a clone if 
	 * it is going to be a new slot.
	 * @param enginebuilder to be proxied
	 * @param clone if the enginebuilder needs to be cloned
	 */
	public lyr_engineBuilder(Object enginebuilder, boolean clone) { // clone what? it's a general object, clone should never be true
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
			return (Object) clone.invoke(enginebuilder);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'duplicate()' in 'lyr_engineBuilder'", t); return engineBuilder;
		}
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
			setEngineStyle.invoke(engineBuilderClass.cast(engineBuilder), engineStyleEnum.getEnumConstants()[enumNumber]);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setEngineStyle()' in 'lyr_engineBuilder'", t);
		}
	}
	//#endregion 
	// END OF API-LIKE & PROXIED METHODS
}
