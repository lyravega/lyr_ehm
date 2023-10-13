package lyravega.listeners.events;

import lyravega.listeners.lyr_eventDispatcher;

/**
 * Any hull modification effect that implement this interface may utilize the
 * {@code applyCustomization()} method when a change in for example LunaLib's
 * settings is detected.
 * <p> The hull modifications should be registered in {@code onApplicationLoad()}
 * through {@link lyr_eventDispatcher#registerModsWithEvents(String, String)}
 * @author lyravega
 * @category Event Handler
 */
public interface customizableMod {
    /**
     * Called whenever the listener detects a change in settings for this hull modification
     */
    public void applyCustomization();
}
