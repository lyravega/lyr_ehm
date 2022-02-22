package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

/**
 * A proxy-like class for {@link ShipHullSpecAPI} that utilizes obfuscated 
 * methods without referring to them. 
 * <p> Some of the methods in the proxy may have API variants, but they're 
 * also implemented here simply to get suggestions. In addition, such 
 * methods avoid using the API variants even when their arguments and/or
 * return types aren't from an obfuscated class.
 * <p> Use {@link #retrieve()} to grab the stored {@link ShipHullSpecAPI}.
 * @author lyravega
 * @version 0.6
 * @since 0.6
 */
public class lyr_hullSpec {
	private ShipHullSpecAPI hullSpec;
	private lyr_weaponSlot weaponSlot = null;
	private lyr_shieldSpec shieldSpec = null;
	private static Class<?> obfuscatedHullSpecClass;
	private static Class<?> obfuscatedWeaponSlotClass;
	private static Class<?> obfuscatedShieldSpecClass;

	static {
		SettingsAPI settings = Global.getSettings();
		for (String variantId : settings.getAllVariantIds()) { // get all the variant ids
			ShipVariantAPI variant = settings.getVariant(variantId); // start with a variant
			ShipHullSpecAPI hullSpec = variant.getHullSpec();

			Iterator<String> i = variant.getNonBuiltInWeaponSlots().iterator(); // assign an iterator for weapon slots
			if (!i.hasNext()) continue; // check if there is a weapon slot

			obfuscatedHullSpecClass = hullSpec.getClass(); // retrieve the class of the hull spec here too, why not
			obfuscatedShieldSpecClass = hullSpec.getShieldSpec().getClass(); 
			obfuscatedWeaponSlotClass = variant.getSlot(i.next()).getClass(); break; // retrieve the class of the weapon slot
		} 
	}

	/**
	 * Creates a new instance for the passed {@link ShipHullSpecAPI}, and 
	 * clones it if necessary. 
	 * <p>
	 * The clone argument MUST be set to true if the hullSpec is not unique; 
	 * not cloned prior to the creation of this instance of the proxy-class. 
	 * Otherwise changes WILL apply to ALL ships of the same hullSpec.
	 * <p>
	 * Cloning should be done as early as possible, and should be avoided
	 * on already cloned hullSpecs. Otherwise loose hullSpecs will float
	 * around till they are garbage-collected, which is, unnecessary (duh)
	 * @param hullSpec to be proxied
	 * @param clone if the hullSpec needs to be cloned
	 */
	public lyr_hullSpec(ShipHullSpecAPI hullSpec, boolean clone) {
		this.hullSpec = (clone) ? this.duplicate(hullSpec) : hullSpec;
	}
	
	/**
	 * Used to retrieve the stored {@link ShipHullSpecAPI} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link ShipHullSpecAPI}
	 */
	public ShipHullSpecAPI retrieve() {
		return hullSpec;
	}
	
	/**
	 * Used to exchange the {@link ShipHullSpecAPI} stored in the proxy
	 * class in order to re-use this proxy instead of creating new ones.
	 * @param hullSpec to exchange with the stored one
	 * @return the proxy itself for chaining purposes
	 */
	public lyr_hullSpec recycle(ShipHullSpecAPI hullSpec) {
		this.hullSpec = hullSpec;
		return this;
	}

	/**
	 * Clones the stored {@link ShipHullSpecAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link ShipHullSpecAPI}
	 * @category Proxied methods
	 */
	private ShipHullSpecAPI duplicate(ShipHullSpecAPI hullSpec) {
		try {
			MethodHandle clone = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "clone", MethodType.methodType(obfuscatedHullSpecClass));
			return (ShipHullSpecAPI) clone.invoke(hullSpec);
		} catch (Throwable t) {
			t.printStackTrace();
		} return hullSpec; // java, pls...
	}
	
	//#region API-like methods
	@Override
	public lyr_hullSpec clone() {
		return new lyr_hullSpec(hullSpec, true);
	}
	
	/**
	 * Gets the weapon slot with the matching id, and creates a {@link lyr_weaponSlot} 
	 * proxy for it. The created proxy is returned, which is necessary to access the 
	 * obfuscated methods for it. 
	 * <p> The created proxy is recycled through {@link lyr_weaponSlot#recycle(WeaponSlotAPI)}.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param weaponSlotId to get
	 * @return {@link lyr_weaponSlot} proxy
	 * @category Proxy spawner
	 * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#getWeaponSlotAPI(String)}
	 */
	public lyr_weaponSlot getWeaponSlot(String weaponSlotId) {
		WeaponSlotAPI weaponSlot = hullSpec.getWeaponSlotAPI(weaponSlotId);

		this.weaponSlot = (this.weaponSlot == null) ? new lyr_weaponSlot(weaponSlot, false) : this.weaponSlot.recycle(weaponSlot);
		
		return this.weaponSlot;
	}

	public lyr_shieldSpec getShieldSpec() {
		ShieldSpecAPI shieldSpec = hullSpec.getShieldSpec();

		this.shieldSpec = (this.shieldSpec == null) ? new lyr_shieldSpec(shieldSpec, false) : this.shieldSpec;
		
		return this.shieldSpec;
	}

	@Deprecated // this shouldn't be used as cloning the hullSpec also clones the shieldSpec
	public lyr_shieldSpec getShieldSpec(boolean clone) {
		ShieldSpecAPI shieldSpec = hullSpec.getShieldSpec();

		this.shieldSpec = (this.shieldSpec == null) ? new lyr_shieldSpec(shieldSpec, true) : this.shieldSpec.recycle(this.shieldSpec.duplicate(shieldSpec));
		
		return this.shieldSpec;
	}

	public void setShieldSpec(ShieldSpecAPI shieldSpec) {
		try {
			MethodHandle setShieldSpec = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "setShieldSpec", MethodType.methodType(void.class, obfuscatedShieldSpecClass));
			setShieldSpec.invoke(hullSpec, shieldSpec);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Adds a hullModSpec as a built-in one on the stored {@link ShipHullSpecAPI}
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param hullModSpecId the id of the hullModSpec
	 * @category Proxied methods
	 * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#addBuiltInMod(String)}
	 */
	public void addBuiltInMod(String hullModSpecId) { 
		try {
			MethodHandle addBuiltInMod = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "addBuiltInMod", MethodType.methodType(void.class, String.class));
			addBuiltInMod.invoke(hullSpec, hullModSpecId);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Sets the manufacturer of the stored {@link ShipHullSpecAPI} to the passed 
	 * value.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param manufacturer to set
	 * @category Proxied methods
	 * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#setManufacturer(String)}
	 */
	public void setManufacturer(String manufacturer) {
		try {
			MethodHandle setManufacturer = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "setManufacturer", MethodType.methodType(void.class, String.class));
			setManufacturer.invoke(hullSpec, manufacturer);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Sets the description prefix of the stored {@link ShipHullSpecAPI} to the 
	 * passed value. Might overwrite or get overwritten; no checks for that (for
	 * now) 
	 * @param destriptionPrefix to set
	 * @category Proxied methods
	 */
	public void setDescriptionPrefix(String destriptionPrefix) {
		try {
			MethodHandle setDescriptionPrefix = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "setDescriptionPrefix", MethodType.methodType(void.class, String.class));
			setDescriptionPrefix.invoke(hullSpec, destriptionPrefix);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Sets the system id of the stored {@link ShipHullSpecAPI} to the passed 
	 * value. 
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param shipSystemId to set
	 * @category Proxied methods
	 * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#setShipSystemId(String)}
	 */
	public void setShipSystemId(String shipSystemId) {
		try {
			MethodHandle setShipSystemId = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "setShipSystemId", MethodType.methodType(void.class, String.class));
			setShipSystemId.invoke(hullSpec, shipSystemId);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Adds a {@link WeaponSlotAPI} on the stored {@link ShipHullSpecAPI}. 
	 * Removal of slots is rather tricky, and it is easier to grab a stock
	 * hullSpec and go from there. The slot needs to have its own node
	 * and its own id, but the rest can just be cloned.
	 * @param weaponSlot to be added
	 * @category Proxied methods
	 */
	public void addWeaponSlot(WeaponSlotAPI weaponSlot) {
		try {
			MethodHandle addWeaponSlot = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "addWeaponSlot", MethodType.methodType(void.class, obfuscatedWeaponSlotClass));
			addWeaponSlot.invoke(hullSpec, obfuscatedWeaponSlotClass.cast(weaponSlot));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Adds a weapon as a built-in one on the stored {@link ShipHullSpecAPI}. 
	 * There are no checks, be aware of what you are installing on what slot.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param slotId of the slot that will have the weapon installed as built-in
	 * @param weaponSpecId of the weapon that will be installed on the slot
	 * @category Proxied methods
	 * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#addBuiltInWeapon(String, String)}
	 */
	public void addBuiltInWeapon(String slotId, String weaponSpecId) {
		try { 
			MethodHandle addBuiltInWeapon = MethodHandles.lookup().findVirtual(obfuscatedHullSpecClass, "addBuiltInWeapon", MethodType.methodType(void.class, String.class, String.class));
			addBuiltInWeapon.invoke(hullSpec, slotId, weaponSpecId);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
