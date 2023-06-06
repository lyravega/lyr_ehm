package experimentalHullModifications.hullmods.ehm.interfaces;

public interface customizableHullMod {
    /**
     * Called whenever the listener detects a change in settings.
     * <p>Is executed remotely and allows re-applying settings on relevant
     * things.
     */
    public void ehm_applyCustomization();
}
