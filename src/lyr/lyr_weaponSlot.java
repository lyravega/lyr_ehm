package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

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
 * @version 0.7
 * @since 0.6
 */
public class lyr_weaponSlot {
	private WeaponSlotAPI weaponSlot;
	private static final Class<?> obfuscatedWeaponSlotClass = _lyr_finder.obfuscatedWeaponSlotClass;
	private static final Class<?> obfuscatedNodeClass = _lyr_finder.obfuscatedNodeClass;

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

	//#region API-LIKE & PROXIED METHODS
	/**
	 * Clones the stored {@link WeaponSlotAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link WeaponSlotAPI}
	 * @category Proxied method
	 */
	protected WeaponSlotAPI duplicate(WeaponSlotAPI weaponSlot) {
		try {
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "clone", MethodType.methodType(obfuscatedWeaponSlotClass));
			return (WeaponSlotAPI) clone.invoke(weaponSlot);
		} catch (Throwable t) {
			t.printStackTrace(); 
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

	/**
	 * Sets the weapon type of the slot to the passed weaponType. 
	 * @param weaponType to be set on the slot
	 * @category Proxied method
	 */
	public void setWeaponType(WeaponType weaponType) {
		try {
			MethodHandle setWeaponType = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "setWeaponType", MethodType.methodType(void.class, WeaponType.class));
			setWeaponType.invoke(weaponSlot, weaponType);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * The API lacks this check. It can easily be implemented, it is
	 * proxied here however.
	 * @return is it a weapon slot?
	 * @category Proxied method
	 */
	public boolean isWeaponSlot() {
		try {
			MethodHandle isWeaponSlot = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "isWeaponSlot", MethodType.methodType(boolean.class));
			return (boolean) isWeaponSlot.invoke(weaponSlot);
		} catch (Throwable t) {
			t.printStackTrace();
		} return false; // java, pls...
	}

	/**
	 * Sets the id of the stored {@link WeaponSlotAPI} to the given one.
	 * Must be unique, otherwise the game will get confused, and only 
	 * one of the slots that share the same id will function.
	 * @param weaponSlotId a unique id to assign
	 * @category Proxied method
	 */
	public void setId(String weaponSlotId) {
		try {
			MethodHandle setId = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "setId", MethodType.methodType(void.class, String.class));
			setId.invoke(weaponSlot, weaponSlotId);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Sets the {@link WeaponSize} of the stored {@link WeaponSlotAPI} 
	 * to the given one.
	 * @param weaponSize a different size
	 * @category Proxied method
	 */
	public void setSlotSize(WeaponSize weaponSize) {
		try {
			MethodHandle setSlotSize = MethodHandles.lookup().findVirtual(obfuscatedWeaponSlotClass, "setSlotSize", MethodType.methodType(void.class, WeaponSize.class));
			setSlotSize.invoke(weaponSlot, weaponSize);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Constructs a new node with the passed parameters, and sets it
	 * as the node of the {@link WeaponSlotAPI}.
	 * <p> A node holds an id, and a vector point that is relative to 
	 * the ship. Game knows where the weapon slots are through the node
	 * data. 
	 * <p> If a slot was created or cloned, it will require its own,
	 * unique node. Otherwise, node manipulations through a  method such 
	 * as {@code setLocation()} will affect all slots with a shared node.
	 * @param nodeId an id to assign to the node (using slotId is fine)
	 * @param location a ship-relative vector to create the node at
	 * @category Proxied method
	 * @see {@link data.hullmods._ehm_util#generateChildLocation} that
	 * calculates new node positions through passed offsets
	 */
	public void setNode(String nodeId, Vector2f location) {
		try {
			Lookup lookup = MethodHandles.lookup();

			MethodHandle newNode = lookup.findConstructor(obfuscatedNodeClass, MethodType.methodType(void.class, String.class, Vector2f.class));
			MethodHandle setNode = lookup.findVirtual(obfuscatedWeaponSlotClass, "setNode", MethodType.methodType(void.class, obfuscatedNodeClass));
			setNode.invoke(weaponSlot, obfuscatedNodeClass.cast(newNode.invoke(nodeId, location)));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	//#endregion 
	// END OF API-LIKE & PROXIED METHODS
}
