package lyravega.proxies;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.misc.lyr_internals;
import lyravega.tools._lyr_proxyTools;

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
	private static MethodHandle clone = null;
	private static MethodHandle getEngineSlots = null;
	private static MethodHandle setShieldSpec = null;
	// private static MethodHandle addBuiltInMod = null;
	// private static MethodHandle setManufacturer = null;
	// private static MethodHandle setDescriptionPrefix = null;
	// private static MethodHandle setShipSystemId = null;
	private static MethodHandle addWeaponSlot = null;
	// private static MethodHandle addBuiltInWeapon = null;
	private static MethodHandle addBuiltInWing = null;
	// private static MethodHandle setShipDefenseId = null;
	// private static MethodHandle getOrdnancePoints = null;
	private static MethodHandle setOrdnancePoints = null;
	// private static MethodHandle setDParentHullId = null;
	private static MethodHandle setBaseHullId = null;
	// private static MethodHandle setRestoreToBase = null;
	// private static MethodHandle getBaseValue = null;
	private static MethodHandle setBaseValue = null;

	static {
		try {
			clone = inspectMethod("clone", hullSpecClass).getMethodHandle();
			getEngineSlots = inspectMethod("getEngineSlots", hullSpecClass).getMethodHandle();
			setShieldSpec = inspectMethod("setShieldSpec", hullSpecClass).getMethodHandle();
			// addBuiltInMod = inspectMethod("addBuiltInMod", hullSpecClass).getMethodHandle();
			// setManufacturer = inspectMethod("setManufacturer", hullSpecClass).getMethodHandle();
			// setDescriptionPrefix = inspectMethod("setDescriptionPrefix", hullSpecClass).getMethodHandle();
			// setShipSystemId = inspectMethod("setShipSystemId", hullSpecClass).getMethodHandle();
			addWeaponSlot = inspectMethod("addWeaponSlot", hullSpecClass).getMethodHandle();
			// addBuiltInWeapon = inspectMethod("addBuiltInWeapon", hullSpecClass).getMethodHandle();
			addBuiltInWing = inspectMethod("addBuiltInWing", hullSpecClass).getMethodHandle();
			// setShipDefenseId = inspectMethod("setShipDefenseId", hullSpecClass).getMethodHandle();
			// getOrdnancePoints = inspectMethod("getOrdnancePoints", hullSpecClass).getMethodHandle();
			setOrdnancePoints = inspectMethod("setOrdnancePoints", hullSpecClass).getMethodHandle();
			// setDParentHullId = inspectMethod("setDParentHullId", hullSpecClass).getMethodHandle();
			setBaseHullId = inspectMethod("setBaseHullId", hullSpecClass).getMethodHandle();
			// setRestoreToBase = inspectMethod("setRestoreToBase", hullSpecClass).getMethodHandle();
			// getBaseValue = inspectMethod("getBaseValue", hullSpecClass).getMethodHandle();
			setBaseValue = inspectMethod("setBaseValue", hullSpecClass).getMethodHandle();
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

	/**
	 * Clones the stored {@link ShipHullSpecAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link ShipHullSpecAPI}
	 * @category Proxy method
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

	//#region BRIDGE / PROXY METHODS
	/**
	 * Gets the weapon slot with the matching id, and creates a {@link lyr_weaponSlot} 
	 * proxy for it. The created proxy is returned, which is necessary to access the 
	 * obfuscated methods for it. 
	 * <p> The created proxy is recycled through {@link lyr_weaponSlot#recycle(WeaponSlotAPI) recycle(WeaponSlotAPI)}.
	 * <p> Use {@link #retrieve()} to use the API version through the proxy.
	 * @param weaponSlotId to get
	 * @return {@link lyr_weaponSlot} proxy
	 * @category Proxy spawner
	 * @see {@link ShipHullSpecAPI#getWeaponSlotAPI(String) getWeaponSlotAPI(String)}
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
	 * @see {@link ShipHullSpecAPI#getShieldSpec() getShieldSpec()}
	 */
	public lyr_shieldSpec getShieldSpec() {
		this.shieldSpec = (this.shieldSpec == null) ? new lyr_shieldSpec(hullSpec.getShieldSpec(), false) : this.shieldSpec;

		return this.shieldSpec;
	}

	/**
	 * @see #getShieldSpec()
	 */
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
	 * @category Proxy method
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
	 * @param shieldSpec
	 * @category Proxy method
	 */
	public void setShieldSpec(ShieldSpecAPI shieldSpec) {
		try {
			setShieldSpec.invoke(hullSpec, shieldSpec);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setShieldSpec()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param hullModSpecId
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#addBuiltInMod(String) addBuiltInMod(String)}
	 */
	public void addBuiltInMod(String hullModSpecId) { 
		hullSpec.addBuiltInMod(hullModSpecId);
	}

	/**
	 * @param manufacturer
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#setManufacturer(String) setManufacturer(String)}
	 */
	public void setManufacturer(String manufacturer) {
		hullSpec.setManufacturer(manufacturer);
	}

	/**
	 * @param destriptionPrefix (overwrites any existing)
	 * @category Bridge method
	 */
	public void setDescriptionPrefix(String destriptionPrefix) {
		hullSpec.setDescriptionPrefix(destriptionPrefix);
	}

	/**
	 * @param shipSystemId
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#setShipSystemId(String) setShipSystemId(String)}
	 */
	public void setShipSystemId(String shipSystemId) {
		hullSpec.setShipSystemId(shipSystemId);
	}

	/**
	 * @param weaponSlot
	 * @category Proxy method
	 * @see #addWeaponSlot(lyr_weaponSlot)
	 */
	@Deprecated
	public void addWeaponSlot(WeaponSlotAPI weaponSlot) {
		try {
			addWeaponSlot.invoke(hullSpec, weaponSlotClass.cast(weaponSlot));
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param weaponSlot to be added
	 * @category Proxy method
	 */
	public void addWeaponSlot(lyr_weaponSlot weaponSlot) {
		try {
			addWeaponSlot.invoke(hullSpec, weaponSlotClass.cast(weaponSlot.retrieve()));
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param slotId of the slot that will have the weapon installed as built-in
	 * @param weaponSpecId of the weapon that will be installed on the slot
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#addBuiltInWeapon(String, String) addBuiltInWeapon(String, String)}
	 */
	public void addBuiltInWeapon(String slotId, String weaponSpecId) {
		hullSpec.addBuiltInWeapon(slotId, weaponSpecId);
	}

	/**
	 * @param wingId of the wing that will be added as built-in
	 * @category Proxy method
	 */
	public void addBuiltInWing(String wingId) {
		try { 
			addBuiltInWing.invoke(hullSpec, wingId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'addBuiltInWing()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param defenseId
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#setShipDefenseId(String) setShipDefenseId(String)}
	 */
	public void setShipDefenseId(String defenseId) {
		hullSpec.setShipDefenseId(defenseId);
	}

	/**
	 * @param characterStats (can be null)
	 * @return Ordnance Points
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getOrdnancePoints(MutableCharacterStatsAPI) getOrdnancePoints(MutableCharacterStatsAPI)}
	 */
	public int getOrdnancePoints(MutableCharacterStatsAPI characterStats) {
		return hullSpec.getOrdnancePoints(characterStats);
	}

	/**
	 * @param ordnancePoints
	 * @category Proxy method
	 */
	public void setOrdnancePoints(int ordnancePoints) {
		try { 
			setOrdnancePoints.invoke(hullSpec, ordnancePoints);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setOrdnancePoints()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param parentHullId
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#setDParentHullId(String) setDParentHullId(String)}
	 */
	public void setDParentHullId(String parentHullId) {
		hullSpec.setDParentHullId(parentHullId);
	}

	/**
	 * @param baseHullId
	 * @category Proxy method
	 */
	public void setBaseHullId(String baseHullId) {
		try { 
			setBaseHullId.invoke(hullSpec, baseHullId);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setBaseHullId()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param restoreToBase
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#setRestoreToBase(boolean) setRestoreToBase(boolean)}
	 */
	public void setRestoreToBase(boolean restoreToBase) {
		hullSpec.setRestoreToBase(restoreToBase);
	}

	/**
	 * @return Value of the hull
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getBaseValue() getBaseValue()}
	 */
	public float getBaseValue() {
		return hullSpec.getBaseValue();
	}

	/**
	 * @param value
	 * @category Proxy method
	 */
	public void setBaseValue(float value) {
		try {
			setBaseValue.invoke(hullSpec, value);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setBaseValue()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @return Name of the hull
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getTags() getTags()}
	 */
	public String getHullName() {
		return hullSpec.getHullName();
	}

	/**
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getTags() getTags()}
	 */
	public void setHullName(String hullName) {
		hullSpec.setHullName(hullName);
	}

	/**
	 * @return Tags of the hull
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getTags() getTags()}
	 */
	public Set<String> getTags() {
		return hullSpec.getTags();
	}

	/**
	 * @return Copied list of the all weapon slots
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getTags() getTags()}
	 */
	public List<WeaponSlotAPI> getAllWeaponSlotsCopy() {
		return hullSpec.getAllWeaponSlotsCopy();
	}
	//#endregion 
	// END OF BRIDGE / PROXY METHODS
}
