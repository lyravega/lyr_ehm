package data.scripts;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class fleetTrackerScript implements EveryFrameScriptWithCleanup {
	private Map<String, shipTrackerScript> shipTrackers = new HashMap<String, shipTrackerScript>();
	// private Set<FleetMember> members = new HashSet<FleetMember>();
	// private CampaignFleet playerFleet = (CampaignFleet) Global.getSector().getPlayerFleet();
	private boolean isDone = false;
	private float runTime = 0f;
	protected Robot robot = null;
	public Logger logger = null;
	
	//#region CONSTRUCTORS & ACCESSORS
	public fleetTrackerScript() {
		logger = Logger.getLogger("lyr");
		logger.setLevel(Level.INFO);
		logger.info("FT: Initialized fleet tracking");

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		Global.getSector().addScript(this);
	}

	public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
		shipTrackers.put(memberId, shipTracker);
		logger.info("FT: Keeping track of ST-"+memberId);
	}
	//#endregion
	// END OF CONSTRUCTORS & ACCESSORS
	
	@Override
	public void advance(float amount) {	
		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		if (tab == null || !tab.equals(CoreUITabId.REFIT)) { logger.info("FT: Stopping fleet tracking"); isDone = true; return; }

		if (runTime > 10f) {
			runTime = 0f;
			logger.info("FT: Tracking "+shipTrackers.size()+" ships");
		} runTime += amount;

		// Set<FleetMember> newMembers = new HashSet<FleetMember>();
		// Set<FleetMember> oldMembers = new HashSet<FleetMember>();

		// for (FleetMember member : playerFleet.getMembers()) {
		// 	if (members.contains(member)) continue;
			
		// 	newMembers.add(member);	
		// } members.addAll(newMembers); 

		// for (FleetMember member : members) {
		// 	if (playerFleet.getMembers().contains(member)) continue;

		// 	oldMembers.add(member);	
		// } members.removeAll(oldMembers); 
		
		// for (FleetMember member : newMembers) {
		// 	spawnshipTracker(member);
		// } newMembers.clear(); 

		// for (FleetMember member : oldMembers) {
		// 	killshipTracker(member.getId());
		// } oldMembers.clear();

		// if (shipTrackers.isEmpty()) { logger.info("FT: Stopping fleet tracking, no ship trackers remaining"); isDone = true; return; }
	}

	@Override
	public boolean runWhilePaused() {
		return true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void cleanup() {
		shipTrackers.clear();
	}
}
