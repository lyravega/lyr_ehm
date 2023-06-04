package lyravega.proxies;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import lyravega.misc.lyr_internals;
import lyravega.tools._lyr_proxyTools;

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
public final class lyr_engineBuilder extends _lyr_proxyTools {
	private Object engineBuilder;
	private static MethodHandle clone = null;
	private static MethodHandle setEngineStyle = null;
	private static MethodHandle setEngineDataFromJson = null;	// I'm making names up
	private static MethodHandle newEngineData = null;
	private static MethodHandle setEngineData = null;

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

	public static final Map<String, Object> customEngineData = new HashMap<String, Object>();

	static {
		try {
			clone = inspectMethod("clone", engineBuilderClass).getMethodHandle();
			setEngineStyle = inspectMethod(engineBuilderClass, void.class, engineStyleEnum).getMethodHandle();
			setEngineDataFromJson = inspectMethod(engineBuilderClass, void.class, JSONObject.class, String.class).getMethodHandle();
			newEngineData = lookup.findConstructor(engineDataClass, MethodType.methodType(void.class, JSONObject.class, String.class));
			setEngineData = inspectMethod(engineBuilderClass, void.class, engineDataClass).getMethodHandle();
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
	 * @param enumNumber
	 * @category Proxy method
	 * @see engineStyle
	 */
	public void setEngineStyle(int enumNumber) {
		try {
			setEngineStyle.invoke(engineBuilderClass.cast(engineBuilder), engineStyleEnum.getEnumConstants()[enumNumber]);
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
	 * @param jsonObject must have relevant stuff found in the "engine_styles.json"!
	 * @param name
	 * @category Proxy method
	 * @see #newEngineData(JSONObject, String)
	 * @see #setEngineData(Object)
	 */
	@Deprecated
	public void setEngineDataFromJson(JSONObject jsonObject, String name) {
		try {
			setEngineDataFromJson.invoke(engineBuilderClass.cast(engineBuilder), jsonObject, name);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setEngineDataFromJson()' in 'lyr_engineBuilder'", t);
		}
	}

	/**
	 * Tosses an existing engine data object to the engine builder, and makes the engine
	 * builder use it.
	 * <p> Should be used in conjunction with {@link #newEngineData(JSONObject, String)},
	 * which would create the objects required by this method, and ideally stored in the
	 * public map {@link #customEngineData} for later use.
	 * @param engineDataObject
	 * @category Proxy method
	 */
	public void setEngineData(Object engineDataObject) {
		try {
			setEngineData.invoke(engineBuilderClass.cast(engineBuilder), engineDataClass.cast(engineDataObject));
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setEngineData()' in 'lyr_engineBuilder'", t);
		}
	}

	/**
	 * Constructs a new engine data from a JSON object, with relevant fields found in
	 * "engine_styles.json" file.
	 * <p> Visibility is set to private as this should NOT be used directly since there
	 * is another method {@link #addEngineData(JSONObject, String)} which adds the new
	 * engine style data to the {@link #customEngineData} map; storing it for later use.
	 * @param jsonObject must have relevant stuff found in the "engine_styles.json"!
	 * @param name
	 * @category Proxy constructor
	 */
	private static Object newEngineData(JSONObject jsonObject, String name) {
		try {
			return newEngineData.invoke(jsonObject, name);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'newEngineData()' in 'lyr_engineBuilder'", t); return null;
		}
	}

	/**
	 * Uses {@link #newEngineData(JSONObject, String)} to construct a new engine style
	 * data object from the JSON object. Returns it after adding it to the {@link
	 * #customEngineData}. {@link #setEngineData(Object)} should be utilized to use
	 * these stored custom engine styles.
	 * @param jsonObject must have relevant stuff found in the "engine_styles.json"!
	 * @param name
	 * @category Utility
	 */
	public static void addEngineData(JSONObject jsonObject, String name) {
		customEngineData.put(name, newEngineData(jsonObject, name));
	}
	//#endregion 
	// END OF BRIDGE / PROXY METHODS
}
