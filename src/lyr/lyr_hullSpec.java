package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.loading.specs.g;

/**
 * A proxy-like class for {@link ShipHullSpecAPI} that utilizes obfuscated 
 * methods without referring to them. Also has overloads for the methods 
 * that do not require a proxy to be executed; that are visible, and 
 * non-obfuscated. 
 * <p> Use {@code retrieve()} to grab the stored hullSpec.
 * @author lyravega
 * @version 0.6
 * @since 0.6
 */
public class lyr_hullSpec {
    private ShipHullSpecAPI hullSpec;
    private g test; // TODO: delete this

    /**
     * Creates a new instance for the passed hullSpec, and clones it if
     * necessary. 
     * <p>
     * The clone argument MUST be set to true if the hullSpec
     * is not unique; not cloned prior to the creation of this instance of 
     * the proxy-class. Otherwise changes WILL apply to ALL ships of the 
     * same hullSpec.
     * <p>
     * Cloning should be done as early as possible, and should be avoided
     * on already cloned hullSpecs. Otherwise loose hullSpecs will float
     * around till they are garbage-collected, which is, unnecessary (duh)
     * @param hullSpec to be proxied
     * @param clone if the hullSpec needs to be cloned
     */
    public lyr_hullSpec(ShipHullSpecAPI hullSpec, boolean clone) {
        this.hullSpec = hullSpec;
        this.hullSpec = (clone) ? this.clone() : hullSpec;
    }
    
    /**
     * Used to retrieve the cloned {@link ShipHullSpecAPI} stored in the 
     * class. Don't try to cast or whatever, just retrieve.
     * @return a cloned {@link ShipHullSpecAPI} to be applied on a variant
     */
    public ShipHullSpecAPI retrieve() {
        return hullSpec;
    }

    /**
     * Overrides the default {@code clone()} method; instead of cloning the
     * instance of the class, clones the stored {@link ShipHullSpecAPI}, 
     * and returns it. If clone fails, returns the original. 
     * @return a cloned {@link ShipHullSpecAPI}
     * @category Proxied methods
     */
    @Override
    public ShipHullSpecAPI clone() {
        try {
            MethodHandle clone = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "clone", MethodType.methodType(hullSpec.getClass()));
            return (ShipHullSpecAPI) clone.invoke(hullSpec);
        } catch (Throwable t) {
            t.printStackTrace();
        } return hullSpec; // java, pls...
    }
    
    /**
     * Adds a hullModSpec as a built-in one on the stored {@link ShipHullSpecAPI}
     * @param hullModSpecId the id of the hullModSpec
     * @category Proxied methods
     * @see Non-Obfuscated: {@link #addBuiltInMod(String, boolean)}
     */
    public void addBuiltInMod(String hullModSpecId) { 
        try {
            MethodHandle addBuiltInMod = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "addBuiltInMod", MethodType.methodType(void.class, String.class));
            addBuiltInMod.invoke(hullSpec, hullModSpecId);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Adds a hullModSpec as a built-in one on the stored {@link ShipHullSpecAPI}
     * @param hullModSpecId the id of the hullModSpec
     * @param useNonObfuscated trash overload parameter
     * @category Non-Obfuscated methods
     * @see Proxied: {@link #addBuiltInMod(String)}
     */
    public void addBuiltInMod(String hullModSpecId, boolean useNonObfuscated) { 
        hullSpec.addBuiltInMod(hullModSpecId);
    }
    
    /**
     * Sets the manufacturer of the stored {@link ShipHullSpecAPI} to the passed 
     * value.
     * @param manufacturer to set
     * @category Proxied methods
     * @see Non-Obfuscated: {@link #setManufacturer(String, boolean)}
     */
    public void setManufacturer(String manufacturer) {
        try {
            MethodHandle setManufacturer = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "setManufacturer", MethodType.methodType(void.class, String.class));
            setManufacturer.invoke(hullSpec, manufacturer);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets the manufacturer of the stored {@link ShipHullSpecAPI} to the passed 
     * value.
     * @param manufacturer to set
     * @param useNonObfuscated trash overload parameter
     * @category Non-Obfuscated methods
     * @see Proxied: {@link #setManufacturer(String)}
     */
    public void setManufacturer(String manufacturer, boolean useNonObfuscated) {
        hullSpec.setManufacturer(manufacturer);
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
            MethodHandle setDescriptionPrefix = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "setDescriptionPrefix", MethodType.methodType(void.class, String.class));
            setDescriptionPrefix.invoke(hullSpec, destriptionPrefix);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets the system id of the stored {@link ShipHullSpecAPI} to the passed 
     * value.
     * @param shipSystemId to set
     * @category Proxied methods
     * @see Non-Obfuscated: {@link #setShipSystemId(String, boolean)}
     */
    public void setShipSystemId(String shipSystemId) {
        try {
            MethodHandle setShipSystemId = MethodHandles.lookup().findVirtual(hullSpec.getClass(), "setShipSystemId", MethodType.methodType(void.class, String.class));
            setShipSystemId.invoke(hullSpec, shipSystemId);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Sets the system id of the stored {@link ShipHullSpecAPI} to the passed 
     * value.
     * @param shipSystemId to set
     * @param useNonObfuscated trash overload parameter
     * @category Non-Obfuscated methods
     * @see Proxied: {@link #setShipSystemId(String)}
     */
    public void setShipSystemId(String shipSystemId, boolean useNonObfuscated) {
        hullSpec.setShipSystemId(shipSystemId);
    }
}
