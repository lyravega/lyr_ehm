package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

/**
 * A proxy-like class for {@link WeaponSlotAPI} that utilizes obfuscated 
 * methods without referring to them. Also has overloads for the methods 
 * that do not require a proxy to be executed; that are visible, and 
 * non-obfuscated. 
 * <p> Some of the methods in the proxy may have API variants, but they're 
 * also implemented here simply to get suggestions. In addition, such 
 * methods avoid using the API variants even when their arguments and/or
 * return types aren't from an obfuscated class.
 * <p> Use {@link #retrieve()} to grab the stored {@link WeaponSlotAPI}.
 * @author lyravega
 * @version 0.6
 * @since 0.6
 */
public class lyr_weaponSlot {
    private WeaponSlotAPI weaponSlot;

    /**
     * Creates a new instance for the passed weaponSlot, and clones it if
     * necessary. Alterations should be done on a clone if it is going to
     * be a new slot.
     * @param weaponSlot to be proxied
     * @param clone if the weaponSlot needs to be cloned
     */
    public lyr_weaponSlot(WeaponSlotAPI weaponSlot, boolean clone) {
        this.weaponSlot = weaponSlot;
        this.weaponSlot = (clone) ? this.clone() : weaponSlot;
    }
    
    /**
     * Used to retrieve the stored {@link WeaponSlotAPI} in the proxy to
     * access the API methods through the proxy itself, or to use it if
     * it needs to be applied on something.
     * @return the stored {@link WeaponSlotAPI}
     */
    public WeaponSlotAPI retrieve() {
        return weaponSlot;
    }
    
    /**
     * Used to exchange the {@link WeaponSlotAPI} stored in the proxy
     * class in order to re-use this proxy instead of creating new ones.
     * @param weaponSlot to exchange with the stored one
     * @return the proxy itself for chaining purposes
     */
    public lyr_weaponSlot recycle(WeaponSlotAPI weaponSlot) {
        this.weaponSlot = weaponSlot;
        return this;
    }

    /**
     * Overrides the default {@link #clone()} method; instead of cloning the
     * instance of the class, clones the stored {@link WeaponSlotAPI}, 
     * and returns it. If clone fails, returns the original. 
     * @return a cloned {@link WeaponSlotAPI}
     * @category Proxied methods
     */
    @Override
    public WeaponSlotAPI clone() {
        try {
            MethodHandle clone = MethodHandles.lookup().findVirtual(weaponSlot.getClass(), "clone", MethodType.methodType(weaponSlot.getClass()));
            return (WeaponSlotAPI) clone.invoke(weaponSlot);
        } catch (Throwable t) {
            t.printStackTrace(); 
        } return weaponSlot; // java, pls...
    }

    /**
     * Sets the weapon type of the slot to the passed weaponType. 
     * @param weaponType to be set on the slot
     * @category Proxied methods
     */
    public void setWeaponType(WeaponType weaponType) {
        try {
            MethodHandle setWeaponType = MethodHandles.lookup().findVirtual(weaponSlot.getClass(), "setWeaponType", MethodType.methodType(void.class, WeaponType.class));
            setWeaponType.invoke(weaponSlot, weaponType);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
