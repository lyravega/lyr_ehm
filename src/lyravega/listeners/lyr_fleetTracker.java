package lyravega.listeners;

import static lyravega.tools.lyr_uiTools.isRefitTab;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import lyravega.misc.lyr_internals;
import lyravega.plugin.lyr_ehm;
import lyravega.tools.lyr_uiTools;

/**
 * A tab listener class that implements several interfaces. There is a
 * method that triggers when the tabs are opened, but not when they are
 * closed so a script is initialized when a tab is opened to determine
 * when it is closed.
 * @author lyravega
 */
public class lyr_fleetTracker extends _lyr_tabListener {
	private static final lyr_fleetTracker instance = new lyr_fleetTracker();	//  if this is null and not instantiated before onGameLoad(), will yield a NPE as hullmod effects load earlier

	private lyr_fleetTracker() {
		super(CoreUITabId.REFIT);
	}

	public static lyr_fleetTracker get() {
		// if (instance == null) instance = new lyr_fleetTracker();

		return instance;
	}

	private Map<String, lyr_shipTracker> shipTrackers = new HashMap<String, lyr_shipTracker>();

	@Override
	public void onOpen() {
		shipTrackers.clear();

		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy())
			addTrackerUUIDs(member.getVariant(), null);

		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"FT: Fleet Tracker initialized");
	}

	@Override
	public void onClose() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy())
			removeTrackerUUIDs(member.getVariant());	// to make the tags transient

		shipTrackers.clear();

		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"FT: Fleet Tracker terminated");
	}

	@Override public void onAdvance(float amount) {
		lyr_uiTools.clearUndoAfter();
	}

	/** @see {@link lyr_shipTracker#updateVariant(ShipVariantAPI)} */ 
	public static void updateShipTracker(ShipAPI ship) {
		if (!isRefitTab()) return;

		getShipTracker(ship.getVariant()).updateVariant(ship.getVariant());
	}

	/** @see {@link lyr_shipTracker#updateVariant(ShipVariantAPI)} */ 
	public static void updateShipTracker(MutableShipStatsAPI stats) {
		if (!isRefitTab() || !ShipAPI.class.isInstance(stats.getEntity())) return;	// the cast check needs to be done because parts of the UI has outdated variant data unless it is a ShipAPI

		getShipTracker(stats.getVariant()).updateVariant(stats.getVariant());
	}

	/**
	 * Gets a ship tracker. Calls {@link #getShipTrackerUUID(ShipVariantAPI)}
	 * to get the UUID of the tracker, which may also assign any missing UUIDs
	 * if necessary
	 * @param variant of the ship to remove from the tracker set
	 * @return the tracker assigned to the variant
	 */
	private static lyr_shipTracker getShipTracker(ShipVariantAPI variant) {
		String shipTrackerUUID = getShipTrackerUUID(variant);
		lyr_shipTracker shipTracker = instance.shipTrackers.get(shipTrackerUUID);

		if (shipTracker == null) {
			shipTracker = new lyr_shipTracker(variant, shipTrackerUUID);
			instance.shipTrackers.put(shipTrackerUUID, shipTracker);
			
			if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"ST-"+shipTrackerUUID+": Ship Tracker initialized");
		}

		return shipTracker;
	}

	/**
	 * If the variant is a module and has its parent's UUID as a tag, returns
	 * the parent's variant. Returns null otherwise
	 * @param moduleVariant of the module
	 * @return parent's variant
	 */
	public static ShipVariantAPI getParentVariant(ShipVariantAPI moduleVariant) {
		lyr_shipTracker parentTracker = instance.shipTrackers.get(getParentTrackerUUID(moduleVariant));

		if (parentTracker != null) return parentTracker.getVariant();

		return null;
	}

	/** @see {@link #removeShipTracker(ShipVariantAPI)} */ 
	public static void terminateShipTracker(ShipAPI ship) {
		if (!isRefitTab()) return;

		removeShipTracker(ship.getVariant());
	}

	/** @see {@link #removeShipTracker(ShipVariantAPI)} */ 
	public static void terminateShipTracker(MutableShipStatsAPI stats) {
		if (!isRefitTab() || !ShipAPI.class.isInstance(stats.getEntity())) return;	// the cast check needs to be done because parts of the UI has outdated variant data unless it is a ShipAPI

		removeShipTracker(stats.getVariant());
	}

	/**
	 * Removes a ship tracker. Does not touch UUID tags, they need to be removed
	 * separately with its own method {@link #removeTrackerUUIDs(ShipVariantAPI)}
	 * @param variant of the ship to remove from the tracker set
	 */
	private static void removeShipTracker(ShipVariantAPI variant) {
		String shipTrackerUUID = getShipTrackerUUID(variant);
		lyr_shipTracker shipTracker = instance.shipTrackers.get(shipTrackerUUID);

		if (shipTracker == null) return;
		
		instance.shipTrackers.remove(shipTrackerUUID);
		
		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"ST-"+shipTrackerUUID+": Ship Tracker terminated");
	}

	/**
	 * Used by {@link #addTrackerUUIDs(ShipVariantAPI, String)} and {@link
	 * #removeShipTracker(ShipVariantAPI)}. Goes over the variant tags, and extracts
	 * the relevant ship UUID from the tags
	 * <p> If the ship is missing such a tag, calls another method {@link
	 * #addTrackerUUIDs(ShipVariantAPI, String)} to add UUID tags directly
	 * @param variant
	 * @return
	 */
	public static String getShipTrackerUUID(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (tag.startsWith(lyr_internals.uuid.shipPrefix)) return tag.substring(lyr_internals.uuid.shipPrefix.length());
		}; return addTrackerUUIDs(variant, null);	// this here ensures the ship (and its modules) has tracker UUIDs
	}

	public static String getParentTrackerUUID(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (tag.startsWith(lyr_internals.uuid.parentPrefix)) return tag.substring(lyr_internals.uuid.parentPrefix.length());
		}; return null;
	}

	/**
	 * Adds a random UUID to a variant as a tag. These tags are used instead of any
	 * other ID to spawn ship trackers and track the changes on the variants. These
	 * tags have prefixes located at {@link lyravega.misc.lyr_internals.uuid} 
	 * <p> If used on a variant that has child module variants, they will automatically
	 * receive their own UUID tag, along with their parent's. Using this on a child
	 * module variant is not recommended as the parent's UUID will not be assigned on
	 * the child variant that way
	 * <p> Modules do not have proper fleet members, and on the refit tab they get
	 * assigned a new, fake one which demands a new tracker to be spawned every time,
	 * which is not ideal. By assigning these UUIDs on module variants, tracking them
	 * through a single tracker becomes possible
	 * <p> {@link #removeTrackerUUIDs(ShipVariantAPI)} needs to be executed to clean the
	 * tags up afterwards if transience is desired. In case a ship is missing such a tag,
	 * will also be called from {@link #getShipTracker(ShipVariantAPI)} directly
	 * @param variant of the ship or the module
	 * @param parentTrackerUUID use {@code null}; automatically populated for children
	 * @return Generated UUID for the variant as a string
	 */
	public static String addTrackerUUIDs(ShipVariantAPI variant, String parentTrackerUUID) {
		String shipTrackerUUID = null;

		for (String tag : variant.getTags()) {
			if (!tag.startsWith(lyr_internals.uuid.shipPrefix)) continue;
			
			shipTrackerUUID = tag.substring(lyr_internals.uuid.shipPrefix.length()); break;
		}; if (shipTrackerUUID == null) shipTrackerUUID = UUID.randomUUID().toString();

		if (shipTrackerUUID != null && !variant.hasTag(lyr_internals.uuid.shipPrefix+shipTrackerUUID))
			variant.addTag(lyr_internals.uuid.shipPrefix+shipTrackerUUID);	// ship's uuid

		if (parentTrackerUUID != null && !variant.hasTag(lyr_internals.uuid.parentPrefix+parentTrackerUUID))
			variant.addTag(lyr_internals.uuid.parentPrefix+parentTrackerUUID);	// parent's uuid
		
		for (String moduleSlot : variant.getStationModules().keySet()) {
			ShipVariantAPI moduleVariant = variant.getModuleVariant(moduleSlot);

			addTrackerUUIDs(moduleVariant, shipTrackerUUID);
		}

		return shipTrackerUUID;
	}

	/**
	 * Removes the UUID tags from the variant, and from its module variants
	 * <p> If {@link #addTrackerUUIDs(ShipVariantAPI, String)} is used in {@link
	 * #onOpen()}, this should be executed in {@link #onClose()} to have the tags
	 * get properly cleaned up
	 * @param variant of the ship to have its tags cleaned-up
	 */
	public static void removeTrackerUUIDs(ShipVariantAPI variant) {
		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); )
			if (iterator.next().startsWith(lyr_internals.uuid.prefix)) iterator.remove();
		
		for (String moduleSlotId : variant.getStationModules().keySet()) {
			removeTrackerUUIDs(variant.getModuleVariant(moduleSlotId));
		}
	}
}
