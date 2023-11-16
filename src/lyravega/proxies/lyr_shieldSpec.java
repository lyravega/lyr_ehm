package lyravega.proxies;

import java.awt.Color;
import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class for {@link ShieldSpecAPI} that utilizes obfuscated
 * methods without referring to them.
 * <p> There are many bridge methods here that simply call the API methods
 * as long as there is one. Proxied methods are implemented on a use-case
 * basis.
 * <p> Use {@link #retrieve()} to grab the stored {@link ShieldSpecAPI}.
 * @author lyravega
 */
public final class lyr_shieldSpec {
	private ShieldSpecAPI shieldSpec;
	static Class<?> shieldSpecClass;
	private static MethodHandle clone;
	// private static MethodHandle setRingColor;
	// private static MethodHandle setInnerColor;
	private static MethodHandle setType;
	private static MethodHandle setFluxPerDamageAbsorbed;
	private static MethodHandle setUpkeepCost;
	private static MethodHandle setArc;
	private static MethodHandle setPhaseCost;
	private static MethodHandle setPhaseUpkeep;

	static {
		try {
			shieldSpecClass = methodReflection.findMethodByName("getShieldSpec", lyr_hullSpec.hullSpecClass).getReturnType();

			clone = methodReflection.findMethodByName("clone", shieldSpecClass).getMethodHandle();
			// setRingColor = methodReflection.findMethodByName("setRingColor", shieldSpecClass).getMethodHandle();
			// setInnerColor = methodReflection.findMethodByName("setInnerColor", shieldSpecClass).getMethodHandle();
			setType = methodReflection.findMethodByName("setType", shieldSpecClass).getMethodHandle();
			setFluxPerDamageAbsorbed = methodReflection.findMethodByName("setFluxPerDamageAbsorbed", shieldSpecClass).getMethodHandle();
			setUpkeepCost = methodReflection.findMethodByName("setUpkeepCost", shieldSpecClass).getMethodHandle();
			setArc = methodReflection.findMethodByName("setArc", shieldSpecClass).getMethodHandle();
			setPhaseCost = methodReflection.findMethodByName("setPhaseCost", shieldSpecClass).getMethodHandle();
			setPhaseUpkeep = methodReflection.findMethodByName("setPhaseUpkeep", shieldSpecClass).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * Creates a new proxy-like object instance for the passed {@link ShieldSpecAPI
	 * shieldSpec}. May be used as a reference and to access obfuscated accessors,
	 * but alterations shouldn't be performed on stock, non-cloned objects from
	 * the spec store.
	 * <p> Cloned hull specs also clone most relevant things like weapon and engine
	 * slots, shield and engine specs, so this may be used freely on those already
	 * cloned objects, but otherwise this should only be used strictly as a reference
	 * and/or a getter de-obfuscator.
	 * @param shieldSpec to be proxied
	 * @param clone (overload) if the shieldSpec needs to be cloned during construction
	 */
	public lyr_shieldSpec(ShieldSpecAPI shieldSpec) {
		this.shieldSpec = shieldSpec;
	}

	/** @see #lyr_shieldSpec(ShieldSpecAPI) */
	public lyr_shieldSpec(ShieldSpecAPI shieldSpec, boolean clone) {
		this.shieldSpec = (clone) ? this.duplicate(shieldSpec) : shieldSpec;
	}

	/**
	 * Used to retrieve the stored {@link ShieldSpecAPI} in the proxy to
	 * access the API methods through the proxy itself, or to use it if
	 * it needs to be applied on something.
	 * @return the stored {@link ShieldSpecAPI}
	 */
	public ShieldSpecAPI retrieve() {
		return this.shieldSpec;
	}

	/**
	 * Used to exchange the {@link ShieldSpecAPI} stored in the proxy
	 * class in order to re-use this proxy instead of creating new ones.
	 * @param shieldSpec to exchange with the stored one
	 * @return the proxy itself for chaining purposes
	 */
	public lyr_shieldSpec recycle(ShieldSpecAPI shieldSpec) {
		this.shieldSpec = shieldSpec;
		return this;
	}

	/**
	 * Clones the stored {@link ShieldSpecAPI}, and returns it. For
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link ShieldSpecAPI}
	 * @category Proxy method
	 */
	protected ShieldSpecAPI duplicate(ShieldSpecAPI shieldSpec) {
		try {
			return (ShieldSpecAPI) clone.invoke(shieldSpec);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'duplicate()' in 'lyr_shieldSpec'", t);
		} return shieldSpec; // java, pls...
	}

	/**
	 * A cheap clone that creates and returns a new instance of this
	 * proxy with a duplicate of its stored object.
	 * @return a cloned {@link lyr_shieldSpec}
	 */
	@Override
	public lyr_shieldSpec clone() {
		return new lyr_shieldSpec(this.shieldSpec, true);
	}

	//#region PROXY METHODS
	/**
	 * @param shieldType
	 * @category Proxy method
	 */
	public void setType(ShieldType shieldType) {
		try {
			setType.invoke(this.shieldSpec, shieldType);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setType()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param absorbtionRatio
	 * @category Proxy method
	 */
	public void setFluxPerDamageAbsorbed(float absorbtionRatio) {
		try {
			setFluxPerDamageAbsorbed.invoke(this.shieldSpec, absorbtionRatio);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setFluxPerDamageAbsorbed()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param upkeepCost
	 * @category Proxy method
	 */
	public void setUpkeepCost(float upkeepCost) {
		try {
			setUpkeepCost.invoke(this.shieldSpec, upkeepCost);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setUpkeepCost()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param arcSize
	 * @category Proxy method
	 */
	public void setArc(float arcSize) {
		try {
			setArc.invoke(this.shieldSpec, arcSize);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setArc()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param phaseCost
	 * @category Proxy method
	 */
	public void setPhaseCost(float phaseCost) {
		try {
			setPhaseCost.invoke(this.shieldSpec, phaseCost);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setPhaseCost()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param phaseUpkeep
	 * @category Proxy method
	 */
	public void setPhaseUpkeep(float phaseUpkeep) {
		try {
			setPhaseUpkeep.invoke(this.shieldSpec, phaseUpkeep);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setPhaseUpkeep()' in 'lyr_shieldSpec'", t);
		}
	}
	//#endregion
	// END OF PROXY METHODS

	//#region BRIDGE METHODS
	public void setRingColor(Color colour) { this.shieldSpec.setRingColor(colour); }

	public void setInnerColor(Color colour) { this.shieldSpec.setInnerColor(colour); }

	public ShieldType getType() { return this.shieldSpec.getType(); }
	//#endregion
	// END OF BRIDGE METHODS
}
