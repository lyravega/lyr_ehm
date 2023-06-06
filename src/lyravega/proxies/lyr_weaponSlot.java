package lyravega.proxies;

import java.lang.invoke.MethodHandle;
// import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.misc.lyr_internals;
import lyravega.tools.lyr_proxyTools;

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
 */
public final class lyr_weaponSlot extends lyr_proxyTools {
	private WeaponSlotAPI weaponSlot;
	private static MethodHandle clone = null;
	private static MethodHandle setWeaponType = null; 
	// private static MethodHandle isWeaponSlot = null;
	private static MethodHandle setId = null;
	private static MethodHandle setSlotSize = null;
	// private static MethodHandle newNode = null;
	private static MethodHandle setNode = null;
	// private static MethodHandle setNode_alt = null;

	static {
		try {
			clone = inspectMethod("clone", weaponSlotClass).getMethodHandle();
			setWeaponType = inspectMethod("setWeaponType", weaponSlotClass, WeaponType.class).getMethodHandle();
			// isWeaponSlot = inspectMethod("isWeaponSlot", weaponSlotClass).getMethodHandle();
			setId = inspectMethod("setId", weaponSlotClass, String.class).getMethodHandle();
			setSlotSize = inspectMethod("setSlotSize", weaponSlotClass).getMethodHandle();
			// newNode = lookup.findConstructor(nodeClass, MethodType.methodType(void.class, String.class, Vector2f.class));
			setNode = inspectMethod("setNode", weaponSlotClass, String.class, Vector2f.class).getMethodHandle();
			// setNode_alt = inspectMethod("setNode", weaponSlotClass, nodeClass).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"Failed to find a method in 'lyr_weaponSlot'", t);
		}
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
			logger.error(lyr_internals.logPrefix+"Failed to use 'duplicate()' in 'lyr_weaponSlot'", t);
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

	//#region BRIDGE / PROXY METHODS
	/**
	 * @param weaponType to be set on the slot
	 * @category Proxy method
	 */
	public void setWeaponType(WeaponType weaponType) {
		try {
			setWeaponType.invoke(weaponSlot, weaponType);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setWeaponType()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @return is it a weapon slot?
	 * @category Bridge method
	 */
	public boolean isWeaponSlot() {
		return weaponSlot.isWeaponSlot();
	}

	/**
	 * @param weaponSlotId a unique id to assign
	 * @category Proxy method
	 */
	public void setId(String weaponSlotId) {
		try {
			setId.invoke(weaponSlot, weaponSlotId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setId()' in 'lyr_weaponSlot'", t);
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
			logger.error(lyr_internals.logPrefix+"Failed to use 'setSlotSize()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @param nodeId an id to assign to the node (using slotId is fine)
	 * @param location a ship-relative vector to create the node at
	 * @category Proxy method
	 * @see {@link lyravega.misc.lyr_utilities#generateChildLocation} that
	 * calculates new node positions through passed offsets
	 */
	public void setNode(String nodeId, Vector2f location) {
		try {
			// setNode_alt.invoke(weaponSlot, nodeClass.cast(newNode.invoke(nodeId, location)));
			setNode.invoke(weaponSlot, nodeId, location);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setNode()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @return Location of slot
	 * @category Bridge method
	 */
	public Vector2f getLocation() {
		return weaponSlot.getLocation();
	}

	/**
	 * @return Angle of slot
	 * @category Bridge method
	 */
	public float getAngle() {
		return weaponSlot.getAngle();
	}

	/**
	 * @param angle
	 * @category Bridge method
	 */
	public void setAngle(float angle) {
		weaponSlot.setAngle(angle);
	}

	/**
	 * @return Arc of slot
	 * @category Bridge method
	 */
	public float getArc() {
		return weaponSlot.getArc();
	}

	/**
	 * @param angle
	 * @category Bridge method
	 */
	public void setArc(float angle) {
		weaponSlot.setArc(angle);
	}
	//#endregion 
	// END OF BRIDGE / PROXY METHODS
}
