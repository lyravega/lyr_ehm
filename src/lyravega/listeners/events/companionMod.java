package lyravega.listeners.events;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

/**
 * An interface used by companion hull modifications that get installed alongside other ones. Such
 * mods are usually hidden and/or cannot be installed/removed individually, and they do not utilize
 * events like {@code onInstalled(...)} or {@code onRemoved(...))}, but the hull modifications that
 * also install these may utilize these methods.
 * @author lyravega
 * @category Event Handler
 */
public interface companionMod {
	public void installCompanionMod(MutableShipStatsAPI stats);

	public void removeCompanionMod(MutableShipStatsAPI stats);
}
