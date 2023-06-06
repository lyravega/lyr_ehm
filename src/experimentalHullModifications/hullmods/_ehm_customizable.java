package experimentalHullModifications.hullmods;

public interface _ehm_customizable {
    /**
     * Called whenever the listener detects a change in settings.
     * <p>Is executed remotely and allows re-applying settings on relevant
     * things.
     */
    public void ehm_applyCustomization();
}
