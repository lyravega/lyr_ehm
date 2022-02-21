package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.loading.specs.g;

/**
 * A proxy-like class for {@link ShipHullSpecAPI} that utilizes obfuscated 
 * methods without referring to them. Also has overloads for the methods 
 * that do not require a proxy to be executed; that are visible, and 
 * non-obfuscated. 
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
     * Overrides the default {@link #clone()} method; instead of cloning the
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
     * <p> Use {@link #retrieve()} to use the API version through the proxy.
     * @param hullModSpecId the id of the hullModSpec
     * @category Proxied methods
     * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#addBuiltInMod(String)}
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
     * Sets the manufacturer of the stored {@link ShipHullSpecAPI} to the passed 
     * value.
     * <p> Use {@link #retrieve()} to use the API version through the proxy.
     * @param manufacturer to set
     * @category Proxied methods
     * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#setManufacturer(String)}
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
     * <p> Use {@link #retrieve()} to use the API version through the proxy.
     * @param shipSystemId to set
     * @category Proxied methods
     * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#setShipSystemId(String)}
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
     * Gets the weapon slot with the matching id, and creates a  {@link lyr_weaponSlot} 
     * proxy for it. The created proxy is returned, which is necessary to access the 
     * obfuscated methods for it. 
     * <p> Use {@link #retrieve()} to use the API version through the proxy.
     * <p> The created proxy is recycled through {@link lyr_weaponSlot#recycle(WeaponSlotAPI)}
     * @param weaponSlotId to get
     * @category Proxy spawner
     * @see Non-Obfuscated: {@link com.fs.starfarer.api.combat.ShipHullSpecAPI#getWeaponSlotAPI(String)}
     */
    public lyr_weaponSlot getWeaponSlot(String weaponSlotId) {
        WeaponSlotAPI weaponSlot = hullSpec.getWeaponSlotAPI(weaponSlotId);

        this.weaponSlot = (this.weaponSlot == null) ? new lyr_weaponSlot(weaponSlot, false) : this.weaponSlot.recycle(weaponSlot);
        
        return this.weaponSlot;
    }
}
