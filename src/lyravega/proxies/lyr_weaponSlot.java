package lyravega.proxies;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
// import java.lang.invoke.MethodType;
import java.lang.invoke.MethodType;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class for the obfuscated class that implements the {@link WeaponSlotAPI} interface.
 * Also contains a few methods for the obfuscated node class here, as it's a tiny one.
 * <p> There are many bridge methods implemented here that simply call the API methods if there is
 * one. Proxy methods are implemented on a use-case basis, and utilize the obfuscated class' methods.
 * Proxy utility methods simply exist to fill in the certain gaps, extending the API in a way.
 * @author lyravega
 */
public class lyr_weaponSlot {
	protected WeaponSlotAPI weaponSlot;
	static Class<?> weaponSlotClass;
	static Class<?> nodeClass;
	static Class<?> slotTypeEnum;
	private static MethodHandle clone;
	private static MethodHandle setWeaponType;
	// private static MethodHandle isWeaponSlot;
	private static MethodHandle setId;
	private static MethodHandle setSlotSize;
	private static MethodHandle getSlotType;
	private static MethodHandle setSlotType;
	private static MethodHandle addLaunchPoint;

	private static MethodHandle getNode;
	private static MethodHandle setNode;
	// private static MethodHandle setNode_alt;
	private static MethodHandle getNodeId;
	private static MethodHandle newNode;	// constructor for the node class

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
			getSlotType = methodReflection.findMethodByName("getSlotType", weaponSlotClass).getMethodHandle();
			setSlotType = methodReflection.findMethodByName("setSlotType", weaponSlotClass, slotTypeEnum).getMethodHandle();
			addLaunchPoint = methodReflection.findMethodByName("addLaunchPoint", weaponSlotClass).getMethodHandle();

			getNode = methodReflection.findMethodByName("getNode", weaponSlotClass).getMethodHandle();
			setNode = methodReflection.findMethodByName("setNode", weaponSlotClass, String.class, Vector2f.class).getMethodHandle();
			// setNode_alt = methodReflection.findMethodByName("setNode", weaponSlotClass, nodeClass).getMethodHandle();	// outdated; newer version above
			getNodeId = methodReflection.findMethodByClass(nodeClass, String.class).getMethodHandle();	// this technically belongs to nodeClass
			newNode = MethodHandles.lookup().findConstructor(nodeClass, MethodType.methodType(void.class, String.class, Vector2f.class));	// this technically belongs to nodeClass
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * An enum class to hold the constants of the obfuscated enum class.
	 * Note that the constants are in enum format; they're used as such.
	 * @see {@link lyr_weaponSlot#setSlotType(slotTypeConstants)} setter using this enum
	 * @see {@link lyr_weaponSlot#getSlotType()} getter using this enum
	 */
	public static enum slotTypeConstants {
		turret,
		hardpoint,
		hidden;
	}

	protected lyr_weaponSlot() {}

	/**
	 * Creates a new proxy-like object instance for the passed {@link WeaponSlotAPI
	 * weaponSlot}. May be used as a reference and to access obfuscated accessors,
	 * but alterations shouldn't be performed on stock, non-cloned objects from
	 * the spec store.
	 * <p> Cloned hull specs also clone some relevant things shield and engine specs,
	 * so this may be used freely on those already cloned objects, but otherwise this
	 * should only be used strictly as a reference and/or a getter de-obfuscator.
	 * <p> While engine and weapon slots are also cloned, their locations will share
	 * the same nodes with the stock hull spec; any change in their locations will
	 * affect all unless the node is unique.
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
			lyr_logger.error("Failed to use 'duplicate()' in 'lyr_weaponSlot'", t); return weaponSlot;
		}
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
	 * Gets the type of the slot; it's different from the weapon type of the slot.
	 * @return a converted enum entry where matching ordinal is returned
	 * @category Proxy method
	 */
	public slotTypeConstants getSlotType() {
		try {
			return slotTypeConstants.values()[((Enum<?>) getSlotType.invoke(this.weaponSlot)).ordinal()];
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getSlotType()' in 'lyr_weaponSlot'", t); return null;
		}
	}

	@Deprecated
	public Enum<?> getSlotTypeRaw() {
		try {
			return (Enum<?>) getSlotType.invoke(this.weaponSlot);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getSlotTypeRaw()' in 'lyr_weaponSlot'", t); return null;
		}
	}

	/**
	 * Sets the type of the slot; it's different from the weapon type of the slot.
	 * @param slotType an enum constant to set; 0=turret, 1=hardpoint, 2=hidden
	 * @category Proxy method
	 */
	public void setSlotType(slotTypeConstants slotType) {
		try {
			setSlotType.invoke(this.weaponSlot, slotTypeEnum.getEnumConstants()[slotType.ordinal()]);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setSlotType()' in 'lyr_weaponSlot'", t);
		}
	}

	@Deprecated
	public void setSlotTypeRaw(Enum<?> slotType) {
		try {
			setSlotType.invoke(this.weaponSlot, slotTypeEnum.getEnumConstants()[slotType.ordinal()]);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setSlotTypeRaw()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * Adds a node as a launch point for this weapon slot. Node may be a new or an existing node.
	 * @param node to add as a launch point. If null, this weapon's node will be used
	 * @see {@link #getNode()} method where a node from a slot may be grabbed
	 * @see {@link #newNode(String, Vector2f)} method where a new node is constructed
	 * @category Proxy method
	 */
	public void addLaunchPoint(Object node) {
		try {
			if (node != null) addLaunchPoint.invoke(this.weaponSlot, node);
			else addLaunchPoint.invoke(this.weaponSlot, this.getNode());
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'addLaunchPoint()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * Adds several nodes as launch points for this weapon slot. All of the new nodes use the slot's
	 * node as the base, while the offset array provides the {@code x} and {@code y} offset values
	 * for each node.
	 * <p> This method will not create a launch point at the base node; an array of {0,0} must be passed
	 * for that purpose. The number of {@code float[]} in the offsets determines the amount of launch
	 * points that will be added to the slot.
	 * @param nodeId to be use as a prefix by each new node. If null, this weapon's node will be used as a base. Each new node receives {@code .#} as a suffix
	 * @param nodeOffsets float arrays that contain {@code x} and {@code y} values to be used as offsets for the new nodes
	 * @see {@link #getNode()} method where a node from a slot may be grabbed
	 * @see {@link #newNode(String, Vector2f)} method where a new node is constructed
	 * @category Proxy utility method
	 */
	public void addLaunchPoints(String nodeId, float[]... nodeOffsets) {
		try {
			nodeId = nodeId != null ? nodeId : this.getNodeId();
			Vector2f location = this.weaponSlot.getLocation();

			for (int i = 0; i < nodeOffsets.length; i++) {
				addLaunchPoint.invoke(this.weaponSlot, newNode(nodeId+"."+i, new Vector2f(location.x + nodeOffsets[i][0], location.y + nodeOffsets[i][1])));
			}
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'addLaunchPoint()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @return the node of the weapon slot as an object
	 * @category Proxy method
	 */
	public Object getNode() {
		try {
			return getNode.invoke(this.weaponSlot);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getNode()' in 'lyr_weaponSlot'", t); return null;
		}
	}

	/**
	 * @param nodeId an id to assign to the node (using slotId is fine)
	 * @param location a ship-relative vector to create the node at
	 * @category Proxy method
	 * @see {@link lyravega.utilities.lyr_vectorUtilities#generateChildLocation} that calculates new node positions through passed offsets
	 */
	public void setNode(String nodeId, Vector2f location) {
		try {
			// setNode_alt.invoke(this.weaponSlot, nodeClass.cast(newNode.invoke(nodeId, location)));
			setNode.invoke(this.weaponSlot, nodeId, location);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setNode()' in 'lyr_weaponSlot'", t);
		}
	}

	/**
	 * @return the id of the weapon slot's node
	 * @category Proxy utility method
	 */
	public String getNodeId() {
		try {
			return (String) getNodeId.invoke(this.getNode());
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getNodeId()' in 'lyr_weaponSlot'", t); return null;
		}
	}

	/**
	 * Constructs a new node using the passed parameters. The obfuscated weapon slot class does not
	 * have such a method, however creating a proxy for the node which only has a few methods is not
	 * necessary and may be handled through this proxy instead.
	 * @param nodeId to assign to the new node
	 * @param location to create a vector and assign it to the new node
	 * @return the new node as an object
	 * @category Proxy utility method
	 */
	public static Object newNode(String nodeId, Vector2f location) {
		try {
			return newNode.invoke(nodeId, location);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'newNode()' in 'lyr_weaponSlot'", t); return null;
		}
	}

	/**
	 * This utility method gives the slot a new node with the same node id, but with a unique location.
	 * Giving it a unique location ensures that any changes to its location will not affect all hull
	 * specs.
	 * @category Proxy utility method
	 */
	public void makeNodeUnique() {
		this.setNode(this.getNodeId(), new Vector2f(this.getLocation()));
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

	public WeaponType getWeaponType() { return this.weaponSlot.getWeaponType(); }

	public String getId() { return this.weaponSlot.getId(); }

	public void setRenderOrderMod(float renderOrderMod) { this.weaponSlot.setRenderOrderMod(renderOrderMod); }

	public float getRenderOrderMod() { return this.weaponSlot.getRenderOrderMod(); }
	//#endregion
	// END OF BRIDGE METHODS
}
