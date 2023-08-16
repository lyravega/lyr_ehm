package lyravega.proxies;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;
// import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.tools.lyr_logger;

import org.lwjgl.util.vector.Vector2f;

/**
 * A proxy-like class for {@link WeaponSlotAPI} that utilizes obfuscated 
 * methods without referring to them. 
 * <p> There are many bridge methods here that simply call the API methods
 * as long as there is one. Proxied methods are implemented on a use-case
 * basis.
 * <p> Use {@link #retrieve()} to grab the stored {@link WeaponSlotAPI}.
 * @author lyravega
 */
public final class lyr_weaponSlot implements lyr_logger {
	private WeaponSlotAPI weaponSlot;
	static Class<?> weaponSlotClass;
	static Class<?> nodeClass;
	static Class<?> slotTypeEnum;
	private static MethodHandle clone;
	private static MethodHandle setWeaponType; 
	// private static MethodHandle isWeaponSlot;
	private static MethodHandle setId;
	private static MethodHandle setSlotSize;
	// private static MethodHandle newNode;
	private static MethodHandle setNode;
	// private static MethodHandle setNode_alt;
	private static MethodHandle getSlotType;
	private static MethodHandle setSlotType;

	static {
		try {
			weaponSlotClass = inspectMethod(true, "getWeaponSlot", 1, lyr_hullSpec.hullSpecClass).getReturnType();
			nodeClass = inspectMethod("getNode", weaponSlotClass).getReturnType();
			slotTypeEnum = inspectMethod("getSlotType", weaponSlotClass).getReturnType();

			clone = inspectMethod("clone", weaponSlotClass).getMethodHandle();
			setWeaponType = inspectMethod("setWeaponType", weaponSlotClass, WeaponType.class).getMethodHandle();
			// isWeaponSlot = inspectMethod("isWeaponSlot", weaponSlotClass).getMethodHandle();
			setId = inspectMethod("setId", weaponSlotClass, String.class).getMethodHandle();
			setSlotSize = inspectMethod("setSlotSize", weaponSlotClass).getMethodHandle();
			// newNode = lookup.findConstructor(nodeClass, MethodType.methodType(void.class, String.class, Vector2f.class));
			setNode = inspectMethod("setNode", weaponSlotClass, String.class, Vector2f.class).getMethodHandle();
			// setNode_alt = inspectMethod("setNode", weaponSlotClass, nodeClass).getMethodHandle();
			getSlotType = inspectMethod("getSlotType", weaponSlotClass).getMethodHandle();
			setSlotType = inspectMethod("setSlotType", weaponSlotClass, slotTypeEnum).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * An enum class to hold the constants of the obfuscated enum class.
	 * Note that the constants are in enum format; they're used as such.
	 * @see #setSlotType(slotTypeConstants) for the method that utilizes this.
	 */
	public static enum slotTypeConstants { 
		turret,
		hardpoint,
		hidden;
	}

	/**
	 * Creates a new instance for the passed {@link WeaponSlotAPI}, and 
	 * clones it if necessary. Alterations should be done on a clone if 
	 * it is going to be a new slot.
	 * @param weaponSlot to be proxied
	 * @param clone if the weaponSlot needs to be cloned
	 */
	public lyr_weaponSlot(WeaponSlotAPI weaponSlot, boolean clone) {
		this.weaponSlot = (clone) ? this.duplicate(weaponSlot) : weaponSlot;
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
	 * Clones the stored {@link WeaponSlotAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link WeaponSlotAPI}
	 * @category Proxy method
	 */
	protected WeaponSlotAPI duplicate(WeaponSlotAPI weaponSlot) {
		try {
			return (WeaponSlotAPI) clone.invoke(weaponSlot);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'duplicate()' in 'lyr_weaponSlot'", t);
		} return weaponSlot; // java, pls...
	}
	
	/**
	 * A cheap clone that creates and returns a new instance of this
	 * object with a duplicate of its stored object. 
	 * @return a cloned {@link lyr_weaponSlot}
	 */
	@Override
	public lyr_weaponSlot clone() {
		return new lyr_weaponSlot(weaponSlot, true);
	}

	//#region PROXY METHODS
	/**
	 * @param weaponType to be set on the slot
	 * @category Proxy method
	 */
	public void setWeaponType(WeaponType weaponType) {
		try {
			setWeaponType.invoke(weaponSlot, weaponType);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setWeaponType()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param weaponSlotId a unique id to assign
	 * @category Proxy method
	 */
	public void setId(String weaponSlotId) {
		try {
			setId.invoke(weaponSlot, weaponSlotId);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setId()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param slotSize
	 * @category Proxy method
	 */
	public void setSlotSize(WeaponSize slotSize) {
		try {
			setSlotSize.invoke(weaponSlot, slotSize);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setSlotSize()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param nodeId an id to assign to the node (using slotId is fine)
	 * @param location a ship-relative vector to create the node at
	 * @category Proxy method
	 * @see {@link lyravega.misc.lyr_vectorUtility#generateChildLocation} that
	 * calculates new node positions through passed offsets
	 */
	public void setNode(String nodeId, Vector2f location) {
		try {
			// setNode_alt.invoke(weaponSlot, nodeClass.cast(newNode.invoke(nodeId, location)));
			setNode.invoke(weaponSlot, nodeId, location);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setNode()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * Gets the type of the slot; it's different from the weapon type of the slot.
	 * @return an enum entry for turret, hardpoint or hidden
	 */
	public Enum<?> getSlotType() {
		try {
			return (Enum<?>) getSlotType.invoke(weaponSlot);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'getSlotType()' in 'lyr_weaponSlot'", t);
		}	return null;
	}

	/**
	 * Sets the type of the slot; it's different from the weapon type of the slot.
	 * @param slotType an enum constant to set; 0=turret, 1=hardpoint, 2=hidden
	 */
	public void setSlotType(slotTypeConstants slotType) {
		try {
			setSlotType.invoke(weaponSlot, slotTypeEnum.getEnumConstants()[slotType.ordinal()]);
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'setSlotType()' in 'lyr_weaponSlot'", t);
		}
	}
	//#endregion 
	// END OF PROXY METHODS

	//#region BRIDGE METHODS
	public boolean isWeaponSlot() {
		return weaponSlot.isWeaponSlot();
	}

	public Vector2f getLocation() {
		return weaponSlot.getLocation();
	}

	public float getAngle() {
		return weaponSlot.getAngle();
	}

	public void setAngle(float angle) {
		weaponSlot.setAngle(angle);
	}

	public float getArc() {
		return weaponSlot.getArc();
	}

	public void setArc(float angle) {
		weaponSlot.setArc(angle);
	}
	//#endregion
	// END OF BRIDGE METHODS
}
