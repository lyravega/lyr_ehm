package lyr.proxies;

import java.awt.Color;
import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;

import lyr.settings.lyr_internals;
import lyr.tools._lyr_proxyTools;

/**
 * A proxy-like class for {@link ShieldSpecAPI} that utilizes obfuscated 
 * methods without referring to them. 
 * <p> Some of the methods in the proxy may have API variants, but they're 
 * also implemented here simply to get suggestions. In addition, such 
 * methods avoid using the API variants even when their arguments and/or
 * return types aren't from an obfuscated class.
 * <p> Use {@link #retrieve()} to grab the stored {@link ShieldSpecAPI}.
 * @author lyravega
 */
public final class lyr_shieldSpec extends _lyr_proxyTools {
	private ShieldSpecAPI shieldSpec;
	private static MethodHandle clone = null;
	private static MethodHandle setRingColor = null;
	private static MethodHandle setInnerColor = null;
	private static MethodHandle setType = null;
	private static MethodHandle setFluxPerDamageAbsorbed = null;
	private static MethodHandle setUpkeepCost = null;
	private static MethodHandle setArc = null;
	private static MethodHandle setPhaseCost = null;
	private static MethodHandle setPhaseUpkeep = null;
	
	static {
		try {
			clone = inspectMethod(shieldSpecClass, "clone").getMethodHandle();
			setRingColor = inspectMethod(shieldSpecClass, "setRingColor").getMethodHandle();
			setInnerColor = inspectMethod(shieldSpecClass, "setInnerColor").getMethodHandle();
			setType = inspectMethod(shieldSpecClass, "setType").getMethodHandle();
			setFluxPerDamageAbsorbed = inspectMethod(shieldSpecClass, "setFluxPerDamageAbsorbed").getMethodHandle();
			setUpkeepCost = inspectMethod(shieldSpecClass, "setUpkeepCost").getMethodHandle();
			setArc = inspectMethod(shieldSpecClass, "setArc").getMethodHandle();
			setPhaseCost = inspectMethod(shieldSpecClass, "setPhaseCost").getMethodHandle();
			setPhaseUpkeep = inspectMethod(shieldSpecClass, "setPhaseUpkeep").getMethodHandle();
		} catch (Throwable t) {
			logger.fatal(lyr_internals.logPrefix+"Failed to find a method in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * Creates a new instance for the passed {@link ShieldSpecAPI}, and 
	 * clones it if necessary. 
	 * <p> The clone argument MUST be set to true if the shieldSpec is not 
	 * unique; not cloned prior to the creation of this instance of the 
	 * proxy-class. Otherwise changes WILL apply to ALL ships with the same 
	 * shieldSpec.
	 * <p> If this is a shieldSpec of an  already cloned hullSpec, then the 
	 * shieldSpec is already cloned. No need to clone it for yet another
	 * time in that case.
	 * @param shieldSpec to be proxied
	 * @param clone if the shieldSpec needs to be cloned
	 */
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
		return shieldSpec;
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

	//#region API-LIKE & PROXIED METHODS
	/**
	 * Clones the stored {@link ShieldSpecAPI}, and returns it. For 
	 * internal use if necessary. {@link #retrieve()} should be used
	 * if access to the API is needed.
	 * @return a cloned {@link ShieldSpecAPI}
	 * @category Proxied method
	 */
	protected ShieldSpecAPI duplicate(ShieldSpecAPI shieldSpec) {
		try {
			return (ShieldSpecAPI) clone.invoke(shieldSpec);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'duplicate()' in 'lyr_shieldSpec'", t);
		} return shieldSpec; // java, pls...
	}
	
	/**
	 * A cheap clone that creates and returns a new instance of this
	 * object with a duplicate of its stored object. 
	 * @return a cloned {@link lyr_shieldSpec}
	 */
	@Override
	public lyr_shieldSpec clone() {
		return new lyr_shieldSpec(shieldSpec, true);
	}

	/**
	 * Sets the ring colour of the stored {@link ShieldSpecAPI} to
	 * the passed one. 
	 * @param colour object to apply
	 * @category Proxied method
	 */
	public void setRingColor(Color colour) {
		try {
			setRingColor.invoke(shieldSpec, colour);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setRingColor()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * Sets the inner colour of the stored {@link ShieldSpecAPI} to
	 * the passed one. 
	 * @param colour object to apply
	 * @category Proxied method
	 */
	public void setInnerColor(Color colour) {
		try {
			setInnerColor.invoke(shieldSpec, colour);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setInnerColor()' in 'lyr_shieldSpec'", t);
		}
	}

	public void setType(ShieldType shieldType) {
		try {
			setType.invoke(shieldSpec, shieldType);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setType()' in 'lyr_shieldSpec'", t);
		}
	}

	public void setFluxPerDamageAbsorbed(float absorbtionRatio) {
		try {
			setFluxPerDamageAbsorbed.invoke(shieldSpec, absorbtionRatio);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setFluxPerDamageAbsorbed()' in 'lyr_shieldSpec'", t);
		}
	}

	public void setUpkeepCost(float upkeepCost) {
		try {
			setUpkeepCost.invoke(shieldSpec, upkeepCost);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setUpkeepCost()' in 'lyr_shieldSpec'", t);
		}
	}

	public void setArc(float arcSize) {
		try {
			setArc.invoke(shieldSpec, arcSize);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setArc()' in 'lyr_shieldSpec'", t);
		}
	}

	public void setPhaseCost(float phaseCost) {
		try {
			setPhaseCost.invoke(shieldSpec, phaseCost);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setPhaseCost()' in 'lyr_shieldSpec'", t);
		}
	}

	public void setPhaseUpkeep(float phaseUpkeep) {
		try {
			setPhaseUpkeep.invoke(shieldSpec, phaseUpkeep);
		} catch (Throwable t) {
			logger.error(lyr_internals.logPrefix+"Failed to use 'setPhaseUpkeep()' in 'lyr_shieldSpec'", t);
		}
	}
	//#endregion 
	// END OF API-LIKE & PROXIED METHODS
}
