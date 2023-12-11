package lyravega.proxies;

import java.awt.Color;
import java.lang.invoke.MethodHandle;

import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;

import lyravega.utilities.lyr_reflectionUtilities.methodReflection;
import lyravega.utilities.logger.lyr_logger;

/**
 * A proxy-like class for the obfuscated class that implements the {@link ShieldSpecAPI} interface.
 * <p> There are many bridge methods implemented here that simply call the API methods if there is
 * one. Proxy methods are implemented on a use-case basis, and utilize the obfuscated class' methods.
 * Proxy utility methods simply exist to fill in the certain gaps, extending the API in a way.
 * @author lyravega
 */
public class lyr_shieldSpec {
	protected ShieldSpecAPI shieldSpec;
	static Class<?> shieldSpecClass;
	private static MethodHandle clone;
	// private static MethodHandle setRingColor;
	// private static MethodHandle setInnerColor;
	private static MethodHandle setType;
	private static MethodHandle setFluxPerDamageAbsorbed;
	private static MethodHandle setUpkeepCost;
	private static MethodHandle setArc;
	private static MethodHandle setRadius;
	private static MethodHandle setCenterX;
	private static MethodHandle setCenterY;
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
			setRadius = methodReflection.findMethodByName("setRadius", shieldSpecClass).getMethodHandle();
			setCenterX = methodReflection.findMethodByName("setCenterX", shieldSpecClass).getMethodHandle();
			setCenterY = methodReflection.findMethodByName("setCenterY", shieldSpecClass).getMethodHandle();
			setPhaseCost = methodReflection.findMethodByName("setPhaseCost", shieldSpecClass).getMethodHandle();
			setPhaseUpkeep = methodReflection.findMethodByName("setPhaseUpkeep", shieldSpecClass).getMethodHandle();
		} catch (Throwable t) {
			lyr_logger.fatal("Failed to find a method in 'lyr_shieldSpec'", t);
		}
	}

	protected lyr_shieldSpec() {}

	/**
	 * Creates a new proxy-like object instance for the passed {@link ShieldSpecAPI
	 * shieldSpec}. May be used as a reference and to access obfuscated accessors,
	 * but alterations shouldn't be performed on stock, non-cloned objects from
	 * the spec store.
	 * <p> Cloned hull specs also clone some relevant things shield and engine specs,
	 * so this may be used freely on those already cloned objects, but otherwise this
	 * should only be used strictly as a reference and/or a getter de-obfuscator.
	 * <p> While engine and weapon slots are also cloned, their locations will share
	 * the same nodes with the stock hull spec; any change in their locations will
	 * affect all unless the node is unique.
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
			lyr_logger.error("Failed to use 'duplicate()' in 'lyr_shieldSpec'", t); return shieldSpec;
		}
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
	 * @param radius
	 * @category Proxy method
	 */
	public void setRadius(float radius) {
		try {
			setRadius.invoke(this.shieldSpec, radius);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setRadius()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param x
	 * @category Proxy method
	 */
	public void setCenterX(float x) {
		try {
			setCenterX.invoke(this.shieldSpec, x);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setCenterX()' in 'lyr_shieldSpec'", t);
		}
	}

	/**
	 * @param y
	 * @category Proxy method
	 */
	public void setCenterY(float y) {
		try {
			setCenterY.invoke(this.shieldSpec, y);
		} catch (Throwable t) {
			lyr_logger.error("Failed to use 'setCenterY()' in 'lyr_shieldSpec'", t);
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
	public float getPhaseCost() { return this.shieldSpec.getPhaseCost(); }

	public float getPhaseUpkeep() { return this.shieldSpec.getPhaseUpkeep(); }

	public float getFluxPerDamageAbsorbed() { return this.shieldSpec.getFluxPerDamageAbsorbed(); }

	public ShieldType getType() { return this.shieldSpec.getType(); }

	public Color getRingColor() { return this.shieldSpec.getRingColor(); }

	public Color getInnerColor() { return this.shieldSpec.getInnerColor(); }

	public float getUpkeepCost() { return this.shieldSpec.getUpkeepCost(); }

	public float getArc() { return this.shieldSpec.getArc(); }

	public float getRadius() { return this.shieldSpec.getRadius(); }

	public float getCenterX() { return this.shieldSpec.getCenterX(); }

	public float getCenterY() { return this.shieldSpec.getCenterY(); }

	public void setRingColor(Color color) { this.shieldSpec.setRingColor(color); }

	public void setInnerColor(Color innerColor) { this.shieldSpec.setInnerColor(innerColor); }
	//#endregion
	// END OF BRIDGE METHODS
}
