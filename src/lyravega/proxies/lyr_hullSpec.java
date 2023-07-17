package lyravega.proxies;

import static lyravega.tools.lyr_reflectionTools.inspectMethod;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import lyravega.tools.lyr_logger;

/**
 * A proxy-like class for {@link ShipHullSpecAPI} that utilizes obfuscated 
 * methods without referring to them.
 * <p> There are many bridge methods here that simply call the API methods
 * as long as there is one. Proxied methods are implemented on a use-case
 * basis.
 * <p> Use {@link #retrieve()} to grab the stored {@link ShipHullSpecAPI}.
 * @author lyravega
 */
public final class lyr_hullSpec implements lyr_logger {
	private ShipHullSpecAPI hullSpec;
	private lyr_weaponSlot weaponSlot;
	private lyr_shieldSpec shieldSpec;
	private List<Object> engineSlots;
	static Class<?> hullSpecClass;
	private static MethodHandle clone;
	private static MethodHandle getEngineSlots;
	private static MethodHandle setShieldSpec;
	// private static MethodHandle addBuiltInMod;
	// private static MethodHandle setManufacturer;
	// private static MethodHandle setDescriptionPrefix;
	// private static MethodHandle setShipSystemId;
	private static MethodHandle addWeaponSlot;
	// private static MethodHandle addBuiltInWeapon;
	private static MethodHandle addBuiltInWing;
	// private static MethodHandle setShipDefenseId;
	// private static MethodHandle getOrdnancePoints;
	private static MethodHandle setOrdnancePoints;
	// private static MethodHandle setDParentHullId;
	private static MethodHandle setBaseHullId;
	// private static MethodHandle setRestoreToBase;
	// private static MethodHandle getBaseValue;
	private static MethodHandle setBaseValue;

	static {
		try {
			hullSpecClass = Global.getSettings().getAllShipHullSpecs().iterator().next().getClass();

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
			logger.fatal(logPrefix+"Failed to find a method in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'duplicate()' in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'getEngineSlots()' in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'setShieldSpec()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @return list of built-in mods
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getBuiltInMods(String) getBuiltInMods()}
	 */
	public List<String> getBuiltInMods() { 
		return hullSpec.getBuiltInMods();
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
	 * @return Description prefix of the hull
	 * @category Bridge method
	 */
	public String getDescriptionPrefix() {
		return hullSpec.getDescriptionPrefix();
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
			addWeaponSlot.invoke(hullSpec, lyr_weaponSlot.weaponSlotClass.cast(weaponSlot));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param weaponSlot to be added
	 * @category Proxy method
	 */
	public void addWeaponSlot(lyr_weaponSlot weaponSlot) {
		try {
			addWeaponSlot.invoke(hullSpec, lyr_weaponSlot.weaponSlotClass.cast(weaponSlot.retrieve()));
		} catch (Throwable t) {
			logger.error(logPrefix+"Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'addBuiltInWing()' in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'setOrdnancePoints()' in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'setBaseHullId()' in 'lyr_hullSpec'", t);
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
			logger.error(logPrefix+"Failed to use 'setBaseValue()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @return Name of the hull
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getHullName() getHullName()}
	 */
	public String getHullName() {
		return hullSpec.getHullName();
	}

	/**
	 * @param hullName
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#setHullName(String) setHullName()}
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
	 * @param tag
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#addTag(String) addTag(tag)}
	 */
	public void addTag(String tag) {
		hullSpec.addTag(tag);
	}

	/**
	 * @return Copied list of the all weapon slots
	 * @category Bridge method
	 * @see {@link ShipHullSpecAPI#getAllWeaponSlotsCopy() getAllWeaponSlotsCopy()}
	 */
	public List<WeaponSlotAPI> getAllWeaponSlotsCopy() {
		return hullSpec.getAllWeaponSlotsCopy();
	}
	//#endregion 
	// END OF BRIDGE / PROXY METHODS
}
