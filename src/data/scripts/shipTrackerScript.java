package data.scripts;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import org.apache.log4j.Logger;

import data.hullmods._ehm_base.ehm;
import data.hullmods.ehm_sr._ehm_sr_base;
import data.hullmods.ehm_wr._ehm_wr_base;

public class shipTrackerScript implements EveryFrameScriptWithCleanup {
	// private fleetTrackerScript fleetTracker = null;
	private ShipVariantAPI variant = null;
	// private g hullSpec = null;
	private String memberId = null;
	private Set<String> hullMods = new HashSet<String>();
	private boolean isDone = false;
	private boolean refresh = false;
	private float runTime = 0f;
	private Robot robot = null;
	private Logger logger = null;
	
	//#region INITIALIZATION & SETTERS & GETTERS
	public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize
		this.variant = variant;
		// this.hullSpec = (g) variant.getHullSpec();
	}
	
	public void initialize(ShipVariantAPI variant, String memberId, fleetTrackerScript fleetTracker) {
		this.variant = variant;
		// this.hullSpec = variant.getHullSpec();
		this.memberId = memberId;

		// this.fleetTracker = fleetTracker;
		this.robot = fleetTracker.robot;
		this.logger = fleetTracker.logger;

		for (String hullModId : variant.getHullMods()) { if (!hullModId.startsWith(ehm.affix.allRetrofit)) continue; 
			if (hullMods.contains(hullModId)) continue;

			hullMods.add(hullModId);
		}

		logger.info("ST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
	}

	public String getMemberId() {
		return this.memberId;
	}

	public void refresh() {
		this.refresh = true;
	}

	public void kill() {
		this.isDone = true;
	}
	//#endregion
	// END OF INITIALIZATION & SETTERS & GETTERS
	
	@Override
	public void advance(float amount) {
		CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
		if (tab == null || !tab.equals(CoreUITabId.REFIT)) { logger.info("ST-"+memberId+": Stopping ship tracking"); isDone = true; return; }

		Set<String> newHullMods = new HashSet<String>();
		Set<String> removedHullMods = new HashSet<String>();
		
		for (String hullModId : variant.getHullMods()) { if (!hullModId.startsWith(ehm.affix.allRetrofit)) continue; 
			if (hullMods.contains(hullModId)) continue;

			logger.info("ST-"+memberId+": New hull modification '"+hullModId+"'");

			newHullMods.add(hullModId);
		} hullMods.addAll(newHullMods);

		for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
			if (variant.hasHullMod(hullModId)) continue;

			logger.info("ST-"+memberId+": Removed hull modification '"+hullModId+"'");

			removedHullMods.add(hullModId);
		} hullMods.removeAll(removedHullMods);
		
		for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) { 
			String hullModId = i.next(); 

			if (hullModId.startsWith(ehm.tag.systemRetrofit)) refresh = true;
			else if (hullModId.startsWith(ehm.tag.weaponRetrofit)) refresh = true;
		} newHullMods.clear();
		
		for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) { 
			String hullModId = i.next(); 

			if (hullModId.startsWith(ehm.tag.systemRetrofit)) { refresh = true; _ehm_sr_base.ehm_systemRestore(variant); }
			else if (hullModId.startsWith(ehm.tag.weaponRetrofit)) { refresh = true; _ehm_wr_base.ehm_weaponSlotRestore(variant); }
		} removedHullMods.clear();

		if (refresh) { runTime++;
			if (runTime < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				refresh = false;
				runTime = 0f;
				logger.info("ST-"+memberId+": Refreshed refit tab");
			}
		}
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
		this.isDone = true;
	}
}
