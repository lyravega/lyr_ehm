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
			addShipTrackerUUID(member.getVariant(), null);

		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"FT: Fleet Tracker initialized");
	}

	@Override
	public void onClose() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy())
			removeShipTrackerUUID(member.getVariant());	// to make the tags transient

		shipTrackers.clear();

		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"FT: Fleet Tracker terminated");
	}

	@Override public void onAdvance(float amount) {}

	public static void updateShipTracker(ShipAPI ship) {
		if (!isRefitTab()) return;

		getShipTracker(ship.getVariant()).updateVariant(ship.getVariant());
	}

	public static void updateShipTracker(MutableShipStatsAPI stats) {
		if (!isRefitTab() || !ShipAPI.class.isInstance(stats.getEntity())) return;

		getShipTracker(stats.getVariant()).updateVariant(stats.getVariant());
	}

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

	public static void terminateShipTracker(ShipAPI ship) {
		if (!isRefitTab()) return;

		removeShipTracker(ship.getVariant());
	}

	public static void terminateShipTracker(MutableShipStatsAPI stats) {
		if (!isRefitTab() || !ShipAPI.class.isInstance(stats.getEntity())) return;

		removeShipTracker(stats.getVariant());
	}

	private static void removeShipTracker(ShipVariantAPI variant) {
		String shipTrackerUUID = getShipTrackerUUID(variant);
		lyr_shipTracker shipTracker = instance.shipTrackers.get(shipTrackerUUID);

		if (shipTracker == null) return;
		
		instance.shipTrackers.remove(shipTrackerUUID);
		
		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"ST-"+shipTrackerUUID+": Ship Tracker terminated");
	}

	static String getShipTrackerUUID(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (tag.startsWith(lyr_internals.uuid.shipPrefix)) return tag.substring(lyr_internals.uuid.shipPrefix.length());
		}; return addShipTrackerUUID(variant, null);	// this here ensures the ship (and its modules) has tracker UUIDs
	}

	public static String addShipTrackerUUID(ShipVariantAPI variant, String parentTrackerUUID) {
		boolean createNewUUID = true;
		String shipTrackerUUID = null;

		for (String tag : variant.getTags()) {
			if (!tag.startsWith(lyr_internals.uuid.shipPrefix)) continue;
			
			createNewUUID = false; shipTrackerUUID = tag.substring(lyr_internals.uuid.shipPrefix.length());
		}; if (createNewUUID) shipTrackerUUID = UUID.randomUUID().toString();

		if (shipTrackerUUID != null && !variant.hasTag(lyr_internals.uuid.shipPrefix+shipTrackerUUID))
			variant.addTag(lyr_internals.uuid.shipPrefix+shipTrackerUUID);	// ship's uuid

		if (parentTrackerUUID != null && !variant.hasTag(lyr_internals.uuid.parentPrefix+parentTrackerUUID))
			variant.addTag(lyr_internals.uuid.parentPrefix+parentTrackerUUID);	// parent's uuid
		
		for (String moduleSlot : variant.getStationModules().keySet()) {
			ShipVariantAPI moduleVariant = variant.getModuleVariant(moduleSlot);

			addShipTrackerUUID(moduleVariant, shipTrackerUUID);
		}

		return shipTrackerUUID;
	}

	public static void removeShipTrackerUUID(ShipVariantAPI variant) {	// if UUIDs are added 'onOpen()', this needs to be called on 'onClose()' to properly clean the UUIDs up
		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); )
			if (iterator.next().startsWith(lyr_internals.uuid.prefix)) iterator.remove();
		
		for (String moduleSlotId : variant.getStationModules().keySet()) {
			removeShipTrackerUUID(variant.getModuleVariant(moduleSlotId));
		}
	}
}
