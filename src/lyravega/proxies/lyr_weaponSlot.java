package lyravega.proxies;

import java.lang.invoke.MethodHandle;
// import java.lang.invoke.MethodType;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class for {@link WeaponSlotAPI} that utilizes obfuscated
 * methods without referring to them.
 * <p> There are many bridge methods here that simply call the API methods
 * as long as there is one. Proxied methods are implemented on a use-case
 * basis.
 * <p> Use {@link #retrieve()} to grab the stored {@link WeaponSlotAPI}.
 * @author lyravega
 */
public final class lyr_weaponSlot {
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
			weaponSlotClass = methodReflection.findMethodByName("getWeaponSlot", lyr_hullSpec.hullSpecClass, 1).getReturnType();
			nodeClass = methodReflection.findMethodByName("getNode", weaponSlotClass).getReturnType();
			slotTypeEnum = methodReflection.findMethodByName("getSlotType", weaponSlotClass).getReturnType();

			clone = methodReflection.findMethodByName("clone", weaponSlotClass).getMethodHandle();
			setWeaponType = methodReflection.findMethodByName("setWeaponType", weaponSlotClass, WeaponType.class).getMethodHandle();
			// isWeaponSlot = methodReflection.findMethodByName("isWeaponSlot", weaponSlotClass).getMethodHandle();
			setId = methodReflection.findMethodByName("setId", weaponSlotClass, String.class).getMethodHandle();
			setSlotSize = methodReflection.findMethodByName("setSlotSize", weaponSlotClass).getMethodHandle();
			// newNode = lookup.findConstructor(nodeClass, MethodType.methodType(void.class, String.class, Vector2f.class));
			setNode = methodReflection.findMethodByName("setNode", weaponSlotClass, String.class, Vector2f.class).getMethodHandle();
			// setNode_alt = methodReflection.findMethodByName("setNode", weaponSlotClass, nodeClass).getMethodHandle();
			getSlotType = methodReflection.findMethodByName("getSlotType", weaponSlotClass).getMethodHandle();
			setSlotType = methodReflection.findMethodByName("setSlotType", weaponSlotClass, slotTypeEnum).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_weaponSlot'", t);
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
	 * @param clone (overload) if the weaponSlot needs to be cloned during construction
	 */
	public lyr_weaponSlot(WeaponSlotAPI weaponSlot) {
		this.weaponSlot = weaponSlot;
	}

	/** @see #lyr_weaponSlot(WeaponSlotAPI) */
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
		return this.weaponSlot;
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
			lyr_logger.error("Failed to use 'duplicate()' in 'lyr_weaponSlot'", t);
		} return weaponSlot; // java, pls...
	}

	/**
	 * A cheap clone that creates and returns a new instance of this
	 * proxy with a duplicate of its stored object.
	 * @return a cloned {@link lyr_weaponSlot}
	 */
	@Override
	public lyr_weaponSlot clone() {
		return new lyr_weaponSlot(this.weaponSlot, true);
	}

	//#region PROXY METHODS
	/**
	 * @param weaponType to be set on the slot
	 * @category Proxy method
	 */
	public void setWeaponType(WeaponType weaponType) {
		try {
			setWeaponType.invoke(this.weaponSlot, weaponType);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setWeaponType()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param weaponSlotId a unique id to assign
	 * @category Proxy method
	 */
	public void setId(String weaponSlotId) {
		try {
			setId.invoke(this.weaponSlot, weaponSlotId);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setId()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param slotSize
	 * @category Proxy method
	 */
	public void setSlotSize(WeaponSize slotSize) {
		try {
			setSlotSize.invoke(this.weaponSlot, slotSize);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setSlotSize()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param nodeId an id to assign to the node (using slotId is fine)
	 * @param location a ship-relative vector to create the node at
	 * @category Proxy method
	 * @see {@link lyravega.utilities.lyr_vectorUtilities#generateChildLocation} that
	 * calculates new node positions through passed offsets
	 */
	public void setNode(String nodeId, Vector2f location) {
		try {
			// setNode_alt.invoke(weaponSlot, nodeClass.cast(newNode.invoke(nodeId, location)));
			setNode.invoke(this.weaponSlot, nodeId, location);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setNode()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * Gets the type of the slot; it's different from the weapon type of the slot.
	 * @return an enum entry for turret, hardpoint or hidden
	 */
	public Enum<?> getSlotType() {
		try {
			return (Enum<?>) getSlotType.invoke(this.weaponSlot);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getSlotType()' in 'lyr_weaponSlot'", t);
		}	return null;
	}

	/**
	 * Sets the type of the slot; it's different from the weapon type of the slot.
	 * @param slotType an enum constant to set; 0=turret, 1=hardpoint, 2=hidden
	 */
	public void setSlotType(slotTypeConstants slotType) {
		try {
			setSlotType.invoke(this.weaponSlot, slotTypeEnum.getEnumConstants()[slotType.ordinal()]);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setSlotType()' in 'lyr_weaponSlot'", t);
		}
	}
	//#endregion
	// END OF PROXY METHODS

	//#region BRIDGE METHODS
	public boolean isWeaponSlot() { return this.weaponSlot.isWeaponSlot(); }

	public Vector2f getLocation() { return this.weaponSlot.getLocation(); }

	public float getAngle() { return this.weaponSlot.getAngle(); }

	public void setAngle(float angle) { this.weaponSlot.setAngle(angle); }

	public float getArc() { return this.weaponSlot.getArc(); }

	public void setArc(float angle) { this.weaponSlot.setArc(angle); }
	//#endregion
	// END OF BRIDGE METHODS
}
