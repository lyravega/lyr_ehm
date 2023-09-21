package lyravega.listeners.events;

/**
 * Implemented by any hull modification that have any setting which
 * can be changed through mod's settings menu created through LunaLib. 
 * <p> The {@link #applyCustomization()} will be executed on all of
 * those hull modifications when settings are saved.
 * @author lyravega
 */
public interface customizableHullMod {
    /**
     * Called whenever the listener detects a change in settings for this hull modification
     */
    public void applyCustomization();
}
