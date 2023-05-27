package lyr.proxies;

import java.lang.invoke.MethodHandle;
import java.util.List;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyr.misc.lyr_internals;
import lyr.tools._lyr_proxyTools;

/**
 * A proxy-like class for {@link ShipHullSpecAPI} that utilizes obfuscated 
 * methods without referring to them. 
 * <p> Some of the methods in the proxy may have API variants, but they're 
 * also implemented here simply to get suggestions. In addition, such 
 * methods avoid using the API variants even when their arguments and/or
 * return types aren't from an obfuscated class.
 * <p> Use {@link #retrieve()} to grab the stored {@link ShipHullSpecAPI}.
 * @author lyravega
 */
public final class lyr_hullSpec extends _lyr_proxyTools {
	private ShipHullSpecAPI hullSpec;
	private lyr_weaponSlot weaponSlot = null;
	private lyr_shieldSpec shieldSpec = null;
	private List<Object> engineSlots = null;
	public static MethodHandle clone = null;
	public static MethodHandle getEngineSlots = null;
	public static MethodHandle setShieldSpec = null;
	public static MethodHandle addBuiltInMod = null;
	public static MethodHandle setManufacturer = null;
	public static MethodHandle setDescriptionPrefix = null;
	public static MethodHandle setShipSystemId = null;
	public static MethodHandle addWeaponSlot = null;
	public static MethodHandle addBuiltInWeapon = null;
	public static MethodHandle addBuiltInWing = null;
	public static MethodHandle setShipDefenseId = null;
	public static MethodHandle getOrdnancePoints = null;
	public static MethodHandle setOrdnancePoints = null;
	public static MethodHandle setDParentHullId = null;
	public static MethodHandle setBaseHullId = null;

	static {
		try {
			clone = inspectMethod("clone", hullSpecClass).getMethodHandle();
			getEngineSlots = inspectMethod("getEngineSlots", hullSpecClass).getMethodHandle();
			setShieldSpec = inspectMethod("setShieldSpec", hullSpecClass).getMethodHandle();
			addBuiltInMod = inspectMethod("addBuiltInMod", hullSpecClass).getMethodHandle();
			setManufacturer = inspectMethod("setManufacturer", hullSpecClass).getMethodHandle();
			setDescriptionPrefix = inspectMethod("setDescriptionPrefix", hullSpecClass).getMethodHandle();
			setShipSystemId = inspectMethod("setShipSystemId", hullSpecClass).getMethodHandle();
			addWeaponSlot = inspectMethod("addWeaponSlot", hullSpecClass).getMethodHandle();
			addBuiltInWeapon = inspectMethod("addBuiltInWeapon", hullSpecClass).getMethodHandle();
			addBuiltInWing = inspectMethod("addBuiltInWing", hullSpecClass).getMethodHandle();
			setShipDefenseId = inspectMethod("setShipDefenseId", hullSpecClass).getMethodHandle();
			getOrdnancePoints = inspectMethod("getOrdnancePoints", hullSpecClass).getMethodHandle();
			setOrdnancePoints = inspectMethod("setOrdnancePoints", hullSpecClass).getMethodHandle();
			setDParentHullId = inspectMethod("setDParentHullId", hullSpecClass).getMethodHandle();
			setBaseHullId = inspectMethod("setBaseHullId", hullSpecClass).getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"Failed to find a method in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * Creates a new proxy-like object instance for the passed {@link ShipHullSpecAPI
	 * hullSpec}, and clones it if needed. 
	 * <p> If the spec is not unique, it must be cloned first using the argument, as
	 * otherwise changes on this spec will affect all other specs of the same type.
	 * <p> Cloning should be done as early as possible, and should be avoided on
	 * already cloned hullSpecs. Otherwise loose hullSpecs will float around till
	 * they are garbage-collected, which is, unnecessary (duh)
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

	//#region API-LIKE & PROXIED METHODS
	/**
	 * Clones the stored {@link ShipHullSpecAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link ShipHullSpecAPI}
	 * @category Proxied method
	 */
	private ShipHullSpecAPI duplicate(ShipHullSpecAPI hullSpec) {
		try {
			return (ShipHullSpecAPI) clone.invoke(hullSpec);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'duplicate()' in 'lyr_hullSpec'", t);
		} return hullSpec; // java, pls...
	}
	
	/**
	 * A cheap clone that creates and returns a new instance of this
	 * object with a duplicate of its stored object. 
	 * @return a cloned {@link lyr_hullSpec}
	 */
	@Override
	public lyr_hullSpec clone() {
		return new lyr_hullSpec(hullSpec, true);
	}
	
	/**
	 * Gets the weapon slot with the matching id, and creates a {@link lyr_weaponSlot} 
	 * proxy for it. The created proxy is returned, which is necessary to access the 
	 * obfuscated methods for it. 
	 * <p> The created proxy is recycled through {@link lyr_weaponSlot#recycle(WeaponSlotAPI) recycle(WeaponSlotAPI)}.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param weaponSlotId to get
	 * @return {@link lyr_weaponSlot} proxy
	 * @category Proxy spawner
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#getWeaponSlotAPI(String) getWeaponSlotAPI(String)}
	 */
	public lyr_weaponSlot getWeaponSlot(String weaponSlotId) {
		this.weaponSlot = (this.weaponSlot == null) ? new lyr_weaponSlot(hullSpec.getWeaponSlotAPI(weaponSlotId), false) : this.weaponSlot.recycle(hullSpec.getWeaponSlotAPI(weaponSlotId));
		
		return this.weaponSlot;
	}

	/**
	 * Gets the shieldSpec of the hullSpec, and creates a {@link lyr_shieldSpec} 
	 * proxy for it. The created proxy is returned, which is necessary to access the 
	 * obfuscated methods for it. 
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @return {@link lyr_weaponSlot} proxy
	 * @category Proxy spawner
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#getShieldSpec() getShieldSpec()}
	 */
	public lyr_shieldSpec getShieldSpec() {
		this.shieldSpec = (this.shieldSpec == null) ? new lyr_shieldSpec(hullSpec.getShieldSpec(), false) : this.shieldSpec;

		return this.shieldSpec;
	}

	@Deprecated // this shouldn't be used as cloning the hullSpec also clones the shieldSpec (and engineSpec)
	public lyr_shieldSpec getShieldSpec(boolean clone) {
		this.shieldSpec = (this.shieldSpec == null) ? new lyr_shieldSpec(hullSpec.getShieldSpec(), true) : this.shieldSpec.recycle(this.shieldSpec.duplicate(hullSpec.getShieldSpec()));
		
		return this.shieldSpec;
	}


	/**
	 * Invokes a getter on the obfuscated hullSpec and returns the result 
	 * as an object list. The returned objects aren't actually engineSlots 
	 * as expected, but rather engineBuilders I believe. The engineBuilders
	 * seem to use an engineStyle to create a new engine, and place it on 
	 * an engineSlot. Check the long-winded javadoc for the 
	 * {@link lyr_engineBuilder} for more information.
	 * @return an object list with engineBuilders (?) in it
	 */
	public List<?> getEngineSlots() {
		if (engineSlots != null) return this.engineSlots; 

		try {
			this.engineSlots = (List<Object>) getEngineSlots.invoke(hullSpec);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'getEngineSlots()' in 'lyr_hullSpec'", t);
		} 

		return this.engineSlots; 
	}

	/**
	 * Sets the shieldSpec of the hullSpec to the passed one. 
	 * @category Proxied method
	 */
	public void setShieldSpec(ShieldSpecAPI shieldSpec) {
		try {
			setShieldSpec.invoke(hullSpec, shieldSpec);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setShieldSpec()' in 'lyr_hullSpec'", t);
		}
	}
	
	/**
	 * Adds a hullModSpec as a built-in one on the stored {@link ShipHullSpecAPI}
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param hullModSpecId the id of the hullModSpec
	 * @category Proxied method
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#addBuiltInMod(String) addBuiltInMod(String)}
	 */
	public void addBuiltInMod(String hullModSpecId) { 
		try {
			addBuiltInMod.invoke(hullSpec, hullModSpecId);
		} catch (Throwable t) {
			logger.warn(lyr_internals.logPrefix+"Failed to use 'addBuiltInMod()' in 'lyr_hullSpec', using API version", t);
			hullSpec.addBuiltInMod(hullModSpecId);
		}
	}
	
	/**
	 * Sets the manufacturer of the stored {@link ShipHullSpecAPI} to the passed 
	 * value.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param manufacturer to set
	 * @category Proxied method
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#setManufacturer(String) setManufacturer(String)}
	 */
	public void setManufacturer(String manufacturer) {
		try {
			setManufacturer.invoke(hullSpec, manufacturer);
		} catch (Throwable t) {
			logger.warn(lyr_internals.logPrefix+"Failed to use 'setManufacturer()' in 'lyr_hullSpec', using API version", t);
			hullSpec.setManufacturer(manufacturer);
		}
	}

	/**
	 * Sets the description prefix of the stored {@link ShipHullSpecAPI} to the 
	 * passed value. Might overwrite or get overwritten; doesn't care about the
	 * existing value, so to speak.
	 * @param destriptionPrefix to set
	 * @category Proxied method
	 */
	public void setDescriptionPrefix(String destriptionPrefix) {
		try {
			setDescriptionPrefix.invoke(hullSpec, destriptionPrefix);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setDescriptionPrefix()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * Sets the system id of the stored {@link ShipHullSpecAPI} to the passed 
	 * value. 
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param shipSystemId to set
	 * @category Proxied method
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#setShipSystemId(String) setShipSystemId(String)}
	 */
	public void setShipSystemId(String shipSystemId) {
		try {
			setShipSystemId.invoke(hullSpec, shipSystemId);
		} catch (Throwable t) {
			logger.warn(lyr_internals.logPrefix+"Failed to use 'setShipSystemId()' in 'lyr_hullSpec', using API version", t);
			hullSpec.setShipSystemId(shipSystemId);
		}
	}

	/**
	 * Adds a {@link WeaponSlotAPI} on the stored {@link ShipHullSpecAPI}. 
	 * Removal of slots is rather tricky, and it is easier to grab a stock
	 * hullSpec and go from there. The slot needs to have its own node
	 * and its own id, but the rest can just be cloned.
	 * @param weaponSlot to be added
	 * @category Proxied method
	 */
	public void addWeaponSlot(WeaponSlotAPI weaponSlot) {
		try {
			addWeaponSlot.invoke(hullSpec, weaponSlotClass.cast(weaponSlot));
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * Adds a weapon as a built-in one on the stored {@link ShipHullSpecAPI}. 
	 * There are no checks, be aware of what you are installing on what slot.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param slotId of the slot that will have the weapon installed as built-in
	 * @param weaponSpecId of the weapon that will be installed on the slot
	 * @category Proxied method
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#addBuiltInWeapon(String, String) addBuiltInWeapon(String, String)}
	 */
	public void addBuiltInWeapon(String slotId, String weaponSpecId) {
		try { 
			addBuiltInWeapon.invoke(hullSpec, slotId, weaponSpecId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'addBuiltInWeapon()' in 'lyr_hullSpec', using API version", t);
			hullSpec.addBuiltInWeapon(slotId, weaponSpecId);
		}
	}

	/**
	 * Adds a wing as a built-in one on the stored {@link ShipHullSpecAPI}. 
	 * There is no API version of this, however adding the wingId to the 
	 * {@code getBuiltInWings()} might work. In any case, here it is proxied.
	 * @param wingId of the wing that will be added as built-in
	 * @category Proxied method
	 */
	public void addBuiltInWing(String wingId) {
		try { 
			addBuiltInWing.invoke(hullSpec, wingId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'addBuiltInWing()' in 'lyr_hullSpec'", t);
		}
	}
	
	public void setShipDefenseId(String defenseId) {
		try { 
			setShipDefenseId.invoke(hullSpec, defenseId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setShipDefenseId()' in 'lyr_hullSpec'", t);
		}
	}
	
	/**
	 * Gets the ordnance points of the stored {@link ShipHullSpecAPI}. Argument
	 * can be null to get the base.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param shipSystemId to set
	 * @category Proxied method
	 * @see Non-Obfuscated: {@link ShipHullSpecAPI#getOrdnancePoints(MutableCharacterStatsAPI) getOrdnancePoints(MutableCharacterStatsAPI)}
	 */
	public int getOrdnancePoints(MutableCharacterStatsAPI characterStats) {
		try { 
			return (int) getOrdnancePoints.invoke(hullSpec, characterStats);
		} catch (Throwable t) {
			logger.warn(lyr_internals.logPrefix+"Failed to use 'getOrdnancePoints()' in 'lyr_hullSpec', using API version", t);
			return hullSpec.getOrdnancePoints(characterStats);
		}
	}
	
	/**
	 * Sets the ordnance points of the stored {@link ShipHullSpecAPI} to the passed 
	 * value.
	 * @param ordnancePoints to set
	 * @category Proxied method
	 */
	public void setOrdnancePoints(int ordnancePoints) {
		try { 
			setOrdnancePoints.invoke(hullSpec, ordnancePoints);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setOrdnancePoints()' in 'lyr_hullSpec'", t);
		}
	}

	public void setBaseHullId(String baseHullId) {
		try { 
			setBaseHullId.invoke(hullSpec, baseHullId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setBaseHullId()' in 'lyr_hullSpec'", t);
		}
	}

	public void setDParentHullId(String parentHullId) {
		try { 
			setDParentHullId.invoke(hullSpec, parentHullId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setDParentHullId()' in 'lyr_hullSpec'", t);
		}
	}
	//#endregion 
	// END OF API-LIKE & PROXIED METHODS
}
