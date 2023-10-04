package lyravega.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
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
	private static _lyr_sectorListener instance = null;

	private lyr_fleetTracker() {
		super(CoreUITabId.REFIT);
	}

	public static _lyr_sectorListener get() {
		if (instance == null) instance = new lyr_fleetTracker();

		return instance;
	}

	private Map<String, lyr_shipTracker> shipTrackers = new HashMap<String, lyr_shipTracker>();

	@Override
	public void onOpen() {
		addTrackers();

		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"FT: Fleet Tracker initialized");
	}

	@Override
	public void onClose() {
		removeTrackers();

		if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"FT: Fleet Tracker terminated");
	}

	public lyr_shipTracker getShipTracker(ShipVariantAPI variant) {
		return shipTrackers.get(getTrackerId(variant));
	}

	public String getTrackerId(ShipVariantAPI variant) {
		for (String tag : variant.getTags())
			if (tag.startsWith(lyr_internals.uuid.shipPrefix)) return tag.substring(lyr_internals.uuid.shipPrefix.length());
		return null;
	}

	private void addTracker(ShipVariantAPI variant, String parentId) {
		String trackerId = UUID.randomUUID().toString();

		variant.addTag(lyr_internals.uuid.shipPrefix+trackerId);	// ship's own id
		if (parentId != null) variant.addTag(lyr_internals.uuid.parentPrefix+parentId);	// parent's id

		shipTrackers.put(trackerId, new lyr_shipTracker(variant));
		// if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"ST-"+shipId+": Ship Tracker initialized");

		for (String moduleSlotId : variant.getStationModules().keySet()) {
			addTracker(variant.getModuleVariant(moduleSlotId), trackerId);
		}
	}

	private void addTrackers() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			addTracker(member.getVariant(), null);
		}
	}

	private void removeTracker(ShipVariantAPI variant) {
		String trackerId = getTrackerId(variant);

		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); )
			if (iterator.next().startsWith(lyr_internals.uuid.prefix)) iterator.remove();

		shipTrackers.remove(trackerId);
		// if (lyr_ehm.settings.getLogTrackerInfo()) logger.info(logPrefix+"ST-"+shipId+": Ship Tracker terminated");

		for (String moduleSlotId : variant.getStationModules().keySet()) {
			removeTracker(variant.getModuleVariant(moduleSlotId));
		}
	}

	private void removeTrackers() {
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			removeTracker(member.getVariant());
		}

		shipTrackers.clear();	// just in case
	}
}
