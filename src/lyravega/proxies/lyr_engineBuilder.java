package lyravega.proxies;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import lyravega.misc.lyr_internals;
import lyravega.tools.lyr_proxyTools;

/**
 * A proxy-like class for... engine builder? When {@code getEngineSlots()}
 * is used on a hull, these objects which seems to be creating the engines
 * on the slots is returned. 
 * <p> Unlike the other proxies, whose obfuscated classes has a base on the
 * API, these do not, so they are treated as objects.
 * <p> Contains some custom methods unlike the other proxies, with long
 * javadocs to hopefully properly describe what they do.
 * @author lyravega
 */
public final class lyr_engineBuilder extends lyr_proxyTools {
	private Object engineBuilder;
	private static MethodHandle clone = null;
	private static MethodHandle setEngineStyleId = null;
	private static MethodHandle setEngineStyleSpecFromJSON = null;
	private static MethodHandle newEngineStyleSpec = null;
	private static MethodHandle setEngineStyleSpec = null;

	public static enum engineStyleIds { ;
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
		public static final int custom = 12;	// with this, custom engine style specs can be utilized
	}

	public static final Map<String, Object> customEngineStyleSpecs = new HashMap<String, Object>();

	static {
		try {
			clone = inspectMethod("clone", engineBuilderClass).getMethodHandle();
			setEngineStyleId = inspectMethod(engineBuilderClass, void.class, engineStyleIdEnum).getMethodHandle();
			setEngineStyleSpecFromJSON = inspectMethod(engineBuilderClass, void.class, JSONObject.class, String.class).getMethodHandle();
			newEngineStyleSpec = lookup.findConstructor(engineStyleSpecClass, MethodType.methodType(void.class, JSONObject.class, String.class));
			setEngineStyleSpec = inspectMethod(engineBuilderClass, void.class, engineStyleSpecClass).getMethodHandle();
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
	 * Used to retrieve the stored {@link Object} in the proxy. However,
	 * since this is an object and not some instance of an API member,
	 * using this is somewhat pointless.
	 * @return the stored {@link Object}
	 */
	@Deprecated
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

	/**
	 * Clones the stored {@link Object}, and returns it. For 
	 * internal use if necessary.
	 * @return a cloned {@link Object}
	 * @category Proxy method
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
	
	//#region BRIDGE / PROXY METHODS
	/**
	 * Uses the passed enumNumber to grab an engine style from the
	 * obfuscated code, and invoke the obfuscated engineBuilder 
	 * method with it. 
	 * @param engineStyleId
	 * @category Proxy method
	 * @see engineStyleIds
	 */
	public void setEngineStyleId(int engineStyleId) {
		try {
			setEngineStyleId.invoke(engineBuilderClass.cast(engineBuilder), engineStyleIdEnum.getEnumConstants()[engineStyleId]);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setEngineStyle()' in 'lyr_engineBuilder'", t);
		}
	}

	/**
	 * Tosses a JSON object with engine style data to the engine builder, creating a new
	 * engine data object from the JSON object and uses it immediately.
	 * <p> Shouldn't be used as there is another method that takes an engine data object
	 * directly, making it safer to use as this might leak somewhere without proper
	 * supervision. Creating the engine data beforehand, storing it somewhere and then
	 * using those stored ones is much safer in theory.
	 * @param engineStyleSpecJSON must have relevant stuff found in the "engine_styles.json"!
	 * @param engineStyleSpecName
	 * @category Proxy method
	 * @see #newEngineStyleSpec(JSONObject, String)
	 * @see #setEngineStyleSpec(Object)
	 */
	@Deprecated
	public void setEngineStyleSpecFromJSON(JSONObject engineStyleSpecJSON, String engineStyleSpecName) {
		try {
			setEngineStyleSpecFromJSON.invoke(engineBuilderClass.cast(engineBuilder), engineStyleSpecJSON, engineStyleSpecName);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setEngineDataFromJson()' in 'lyr_engineBuilder'", t);
		}
	}

	/**
	 * Tosses an existing engine data object to the engine builder, and makes the engine
	 * builder use it.
	 * <p> Should be used in conjunction with {@link #newEngineStyleSpec(JSONObject, String)},
	 * which would create the objects required by this method, and ideally stored in the
	 * public map {@link #customEngineStyleSpecs} for later use.
	 * @param engineStyleSpec
	 * @category Proxy method
	 */
	public void setEngineStyleSpec(Object engineStyleSpec) {
		try {
			setEngineStyleSpec.invoke(engineBuilderClass.cast(engineBuilder), engineStyleSpecClass.cast(engineStyleSpec));
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setEngineStyleSpec()' in 'lyr_engineBuilder'", t);
		}
	}

	/**
	 * Constructs a new engine data from a JSON object, with relevant fields found in
	 * "engine_styles.json" file.
	 * <p> Visibility is set to private as this should NOT be used directly since there
	 * is another method {@link #addEngineStyleSpec(JSONObject, String)} which adds the new
	 * engine style data to the {@link #customEngineStyleSpecs} map; storing it for later use.
	 * @param engineStyleSpecJSON must have relevant stuff found in the "engine_styles.json"!
	 * @param engineStyleSpecName
	 * @category Proxy constructor
	 */
	private static Object newEngineStyleSpec(JSONObject engineStyleSpecJSON, String engineStyleSpecName) {
		try {
			return newEngineStyleSpec.invoke(engineStyleSpecJSON, engineStyleSpecName);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'newEngineStyleSpec()' in 'lyr_engineBuilder'", t); return null;
		}
	}

	/**
	 * Uses {@link #newEngineStyleSpec(JSONObject, String)} to construct a new engine style
	 * data object from the JSON object. Returns it after adding it to the {@link
	 * #customEngineStyleSpecs}. {@link #setEngineStyleSpec(Object)} should be utilized to use
	 * these stored custom engine styles.
	 * @param engineStyleSpecJSON must have relevant stuff found in the "engine_styles.json"!
	 * @param engineStyleSpecName
	 * @category Utility
	 */
	public static void addEngineStyleSpec(JSONObject engineStyleSpecJSON, String engineStyleSpecName) {
		customEngineStyleSpecs.put(engineStyleSpecName, newEngineStyleSpec(engineStyleSpecJSON, engineStyleSpecName));
	}
	//#endregion 
	// END OF BRIDGE / PROXY METHODS
}
