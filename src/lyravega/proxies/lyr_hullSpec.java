package lyravega.proxies;

import java.lang.invoke.MethodHandle;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class for the obfuscated class that implements the {@link ShipHullSpecAPI} interface.
 * <p> There are many bridge methods implemented here that simply call the API methods if there is
 * one. Proxy methods are implemented on a use-case basis, and utilize the obfuscated class' methods.
 * Proxy utility methods simply exist to fill in the certain gaps, extending the API in a way.
 * @author lyravega
 */
public class lyr_hullSpec {
	protected ShipHullSpecAPI hullSpec;
	protected lyr_weaponSlot weaponSlot;
	protected lyr_shieldSpec shieldSpec;
	protected List<Object> engineSlots;
	static Class<?> hullSpecClass;
	private static MethodHandle clone;
	private static MethodHandle getEngineSlots;
	private static MethodHandle getAllWeaponSlots;
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
	private static MethodHandle getSpriteSpec;
	private static MethodHandle setSpriteSpec;
	private static MethodHandle setHullSize;

	static {
		try {
			hullSpecClass = Global.getSettings().getAllShipHullSpecs().iterator().next().getClass();

			clone = methodReflection.findMethodByName("clone", hullSpecClass).getMethodHandle();
			getEngineSlots = methodReflection.findMethodByName("getEngineSlots", hullSpecClass).getMethodHandle();
			getAllWeaponSlots = methodReflection.findMethodByName("getAllWeaponSlots", hullSpecClass).getMethodHandle();
			setShieldSpec = methodReflection.findMethodByName("setShieldSpec", hullSpecClass).getMethodHandle();
			// addBuiltInMod = methodReflection.findMethodByName("addBuiltInMod", hullSpecClass).getMethodHandle();
			// setManufacturer = methodReflection.findMethodByName("setManufacturer", hullSpecClass).getMethodHandle();
			// setDescriptionPrefix = methodReflection.findMethodByName("setDescriptionPrefix", hullSpecClass).getMethodHandle();
			// setShipSystemId = methodReflection.findMethodByName("setShipSystemId", hullSpecClass).getMethodHandle();
			addWeaponSlot = methodReflection.findMethodByName("addWeaponSlot", hullSpecClass).getMethodHandle();
			// addBuiltInWeapon = methodReflection.findMethodByName("addBuiltInWeapon", hullSpecClass).getMethodHandle();
			addBuiltInWing = methodReflection.findMethodByName("addBuiltInWing", hullSpecClass).getMethodHandle();
			// setShipDefenseId = methodReflection.findMethodByName("setShipDefenseId", hullSpecClass).getMethodHandle();
			// getOrdnancePoints = methodReflection.findMethodByName("getOrdnancePoints", hullSpecClass).getMethodHandle();
			setOrdnancePoints = methodReflection.findMethodByName("setOrdnancePoints", hullSpecClass).getMethodHandle();
			// setDParentHullId = methodReflection.findMethodByName("setDParentHullId", hullSpecClass).getMethodHandle();
			setBaseHullId = methodReflection.findMethodByName("setBaseHullId", hullSpecClass).getMethodHandle();
			// setRestoreToBase = methodReflection.findMethodByName("setRestoreToBase", hullSpecClass).getMethodHandle();
			// getBaseValue = methodReflection.findMethodByName("getBaseValue", hullSpecClass).getMethodHandle();
			setBaseValue = methodReflection.findMethodByName("setBaseValue", hullSpecClass).getMethodHandle();
			getSpriteSpec = methodReflection.findMethodByName("getSpriteSpec", hullSpecClass).getMethodHandle();
			setSpriteSpec = methodReflection.findMethodByName("setSpriteSpec", hullSpecClass).getMethodHandle();
			setHullSize = methodReflection.findMethodByName("setHullSize", hullSpecClass).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_hullSpec'", t);
		}
	}

	protected lyr_hullSpec() {}

	/**
	 * Creates a new proxy-like object instance for the passed {@link ShipHullSpecAPI hullSpec}. If
	 * the hull spec is a stock one that is not cloned before, then this should only be used as a
	 * reference, only to gain access to certain obfuscated getters.
	 * @param hullSpec to be proxied
	 */
	public lyr_hullSpec(ShipHullSpecAPI hullSpec) {
		this.hullSpec = hullSpec;
	}

	/**
	 * Creates a new proxy-like object instance for the passed {@link ShipHullSpecAPI hullSpec}, and
	 * clones it if directed to do so. This should be used if there are going to be alterations on
	 * the hull spec, which will ensure changes to the spec will only affect the current variant only.
	 * @param hullSpec to be proxied
	 * @param clone if alterations are going to be done on the spec
	 */
	public lyr_hullSpec(ShipHullSpecAPI hullSpec, boolean clone) {
		this.hullSpec = (clone) ? this.duplicate(hullSpec) : hullSpec;
	}

	/**
	 * Creates a new proxy-like object instance for the passed {@link ShipHullSpecAPI hullSpec}, and
	 * clones it if it is forced or necessary. If not forced, then the passed hull spec is compared
	 * against the hull spec from the spec store, and if they're the same then it will get cloned.
	 * <p> The additional boolean forces the use of damaged hull specs, which is recommended if slots
	 * are going to be added or removed. This is necessary as the game swaps the hull specs in two
	 * places, and if the variant cannot find a slot, it will cause a slot not found error. Having
	 * a damaged hull spec prevents one of these, while the other needs a script replacement.
	 * <p> Note that damaged hull specs may lack some tags, have their value reduced, and have a "(D)"
	 * suffix in their hull name, all of which may be undone through the use of methods that this
	 * class offers.
	 * @param hullSpec to be proxied
	 * @param forceClone to bypass the check and clone the spec
	 * @param useDamagedSpec to use a damaged spec even if the passed one is not damaged
	 */
	public lyr_hullSpec(ShipHullSpecAPI hullSpec, boolean forceClone, boolean useDamagedSpec) {
		ShipHullSpecAPI dHullSpec = Global.getSettings().getHullSpec(Misc.getDHullId(hullSpec));	// damaged hull spec
		ShipHullSpecAPI oHullSpec = Global.getSettings().getHullSpec(hullSpec.getHullId().replace(Misc.D_HULL_SUFFIX, ""));	// original hull spec

		this.hullSpec = (forceClone || oHullSpec == hullSpec || dHullSpec == hullSpec) ? this.duplicate(useDamagedSpec ? dHullSpec : oHullSpec) : hullSpec;
	}

	/**
	 * Used to retrieve the stored {@link ShipHullSpecAPI} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link ShipHullSpecAPI}
	 */
	public ShipHullSpecAPI retrieve() {
		return this.hullSpec;
	}

	/**
	 * @return original hull spec from the spec store as a reference
	 */
	public ShipHullSpecAPI reference() {
		return Global.getSettings().getHullSpec(this.hullSpec.getHullId());
	}

	/**
	 * @return the non-damaged, original hull spec from the spec store as a reference
	 */
	public ShipHullSpecAPI referenceNonDamaged() {
		return Global.getSettings().getHullSpec(this.hullSpec.getHullId().replace(Misc.D_HULL_SUFFIX, ""));
	}

	/**
	 * @return the damaged hull spec from the spec store as a reference
	 */
	public ShipHullSpecAPI referenceDamaged() {
		return Global.getSettings().getHullSpec(Misc.getDHullId(this.hullSpec));
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
	protected ShipHullSpecAPI duplicate(ShipHullSpecAPI hullSpec) {
		try {
			return (ShipHullSpecAPI) clone.invoke(hullSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'duplicate()' in 'lyr_hullSpec'", t); return hullSpec;
		}
	}

	/**
	 * A cheap clone that creates and returns a new instance of this
	 * object with a duplicate of its stored object.
	 * @return a cloned {@link lyr_hullSpec}
	 */
	@Override
	public lyr_hullSpec clone() {
		return new lyr_hullSpec(this.hullSpec, true);
	}

	//#region PROXY METHODS
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
		this.weaponSlot = (this.weaponSlot == null) ? new lyr_weaponSlot(this.hullSpec.getWeaponSlotAPI(weaponSlotId)) : this.weaponSlot.recycle(this.hullSpec.getWeaponSlotAPI(weaponSlotId));

		return this.weaponSlot;
	}

	/** @see {@link #getWeaponSlot(String)} */
	public lyr_weaponSlot getWeaponSlot(WeaponSlotAPI slot) {
		return this.getWeaponSlot(slot.getId());
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
		this.shieldSpec = (this.shieldSpec == null) ? new lyr_shieldSpec(this.hullSpec.getShieldSpec()) : this.shieldSpec;

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
		if (this.engineSlots != null) return this.engineSlots;

		try {
			this.engineSlots = (List<Object>) getEngineSlots.invoke(this.hullSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getEngineSlots()' in 'lyr_hullSpec'", t);
		}

		return this.engineSlots;
	}

	public List<WeaponSlotAPI> getAllWeaponSlots() {
		try {
			return (List<WeaponSlotAPI>) getAllWeaponSlots.invoke(this.hullSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getAllWeaponSlots()' in 'lyr_hullSpec'", t); return null;
		}
	}

	/**
	 * @param shieldSpec
	 * @category Proxy method
	 */
	public void setShieldSpec(ShieldSpecAPI shieldSpec) {
		try {
			setShieldSpec.invoke(this.hullSpec, shieldSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setShieldSpec()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param weaponSlot
	 * @category Proxy method
	 * @see #addWeaponSlot(lyr_weaponSlot)
	 */
	@Deprecated
	public void addWeaponSlot(WeaponSlotAPI weaponSlot) {
		try {
			addWeaponSlot.invoke(this.hullSpec, lyr_weaponSlot.weaponSlotClass.cast(weaponSlot));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param weaponSlot to be added
	 * @category Proxy method
	 */
	public void addWeaponSlot(lyr_weaponSlot weaponSlot) {
		try {
			addWeaponSlot.invoke(this.hullSpec, lyr_weaponSlot.weaponSlotClass.cast(weaponSlot.retrieve()));
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'addWeaponSlot()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param wingId of the wing that will be added as built-in
	 * @category Proxy method
	 */
	public void addBuiltInWing(String wingId) {
		try {
			addBuiltInWing.invoke(this.hullSpec, wingId);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'addBuiltInWing()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param ordnancePoints
	 * @category Proxy method
	 */
	public void setOrdnancePoints(int ordnancePoints) {
		try {
			setOrdnancePoints.invoke(this.hullSpec, ordnancePoints);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setOrdnancePoints()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param baseHullId
	 * @category Proxy method
	 */
	public void setBaseHullId(String baseHullId) {
		try {
			setBaseHullId.invoke(this.hullSpec, baseHullId);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setBaseHullId()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param value
	 * @category Proxy method
	 */
	public void setBaseValue(float value) {
		try {
			setBaseValue.invoke(this.hullSpec, value);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setBaseValue()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @category Proxy method
	 */
	public Object getSpriteSpec() {
		try {
			return getSpriteSpec.invoke(this.hullSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'getSpriteSpec()' in 'lyr_hullSpec'", t); return null;
		}
	}

	/**
	 * @param spriteSpec
	 * @category Proxy method
	 */
	public void setSpriteSpec(Object spriteSpec) {
		try {
			setSpriteSpec.invoke(this.hullSpec, spriteSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setSpriteSpec()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * @param size
	 * @category Proxy method
	 */
	public void setHullSize(HullSize size) {
		try {
			setHullSize.invoke(this.hullSpec, size);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setHullSize()' in 'lyr_hullSpec'", t);
		}
	}

	/**
	 * Slots on a hull spec are cloned for the most part, however the nodes that these slots utilize
	 * do not get cloned. As such, a cloned hull spec still shares something with its original. In
	 * order to alter the locations of the slots, they need a unique node. Nodes hold an id and a
	 * location field in it, and querying location of a slot returns the location of the node.
	 * <p> This utility method checks the location of the first slot of a hull spec against its clone
	 * and if they lead to the same memory address, gives the clone a new, unique node instead. This
	 * ensures that alterations on the node (slot) locations will not affect the other ships, and
	 * the check ensures that it will be done only if necessary.
	 * @category Proxy utility method
	 */
	public void makeNodesUnique() {
		for (WeaponSlotAPI slot : this.getAllWeaponSlots()) {
			WeaponSlotAPI referenceSlot = this.reference().getWeaponSlot(slot.getId());

			if (referenceSlot == null) continue;
			if (referenceSlot.getLocation() != slot.getLocation()) continue;

			this.getWeaponSlot(slot).makeNodeUnique();
		}
	}
	//#endregion
	// END OF PROXY METHODS

	//#region BRIDGE METHODS
	public List<String> getBuiltInMods() { return this.hullSpec.getBuiltInMods(); }

	public void addBuiltInMod(String hullModSpecId) { this.hullSpec.addBuiltInMod(hullModSpecId); }

	public void setManufacturer(String manufacturer) { this.hullSpec.setManufacturer(manufacturer); }

	public void setDescriptionPrefix(String destriptionPrefix) { this.hullSpec.setDescriptionPrefix(destriptionPrefix); }

	public String getDescriptionPrefix() { return this.hullSpec.getDescriptionPrefix(); }

	public void setShipSystemId(String shipSystemId) { this.hullSpec.setShipSystemId(shipSystemId); }

	public void addBuiltInWeapon(String slotId, String weaponSpecId) { this.hullSpec.addBuiltInWeapon(slotId, weaponSpecId); }

	public void setShipDefenseId(String defenseId) { this.hullSpec.setShipDefenseId(defenseId); }

	public int getOrdnancePoints(MutableCharacterStatsAPI characterStats) { return this.hullSpec.getOrdnancePoints(characterStats); }

	public void setDParentHullId(String parentHullId) { this.hullSpec.setDParentHullId(parentHullId); }

	public void setRestoreToBase(boolean restoreToBase) { this.hullSpec.setRestoreToBase(restoreToBase); }

	public float getBaseValue() { return this.hullSpec.getBaseValue(); }

	public String getHullName() { return this.hullSpec.getHullName(); }

	public void setHullName(String hullName) { this.hullSpec.setHullName(hullName); }

	public Set<String> getTags() { return this.hullSpec.getTags(); }

	public void addTag(String tag) { this.hullSpec.addTag(tag); }

	public List<WeaponSlotAPI> getAllWeaponSlotsCopy() { return this.hullSpec.getAllWeaponSlotsCopy(); }

	public String getShipSystemId() { return this.hullSpec.getShipSystemId(); }

	public EnumSet<ShipTypeHints> getHints() { return this.hullSpec.getHints(); }

	public boolean isBuiltInMod(String hullModId) { return this.hullSpec.isBuiltInMod(hullModId); }
	//#endregion
	// END OF BRIDGE METHODS
}
