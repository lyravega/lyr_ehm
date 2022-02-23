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
import data.hullmods.ehm_ec._ehm_ec_base;
import data.hullmods.ehm_sc._ehm_sc_base;
import data.hullmods.ehm_sr._ehm_sr_base;
import data.hullmods.ehm_wr._ehm_wr_base;

public class shipTrackerScript implements EveryFrameScriptWithCleanup {
	// private fleetTrackerScript fleetTracker = null;
	private ShipVariantAPI variant = null;
	private String memberId = null;
	private Set<String> hullMods = new HashSet<String>();
	private boolean isDone = false;
	private boolean refresh = false;
	private boolean playSound = false;
	private float frameCount = 0f;
	private Robot robot = null;
	private Logger logger = null;
	
	//#region CONSTRUCTORS & ACCESSORS
	public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize
		this.variant = variant;
	}
	
	public shipTrackerScript(ShipVariantAPI variant, String memberId, fleetTrackerScript fleetTracker) {
		this.variant = variant;
		this.memberId = memberId;

		// this.fleetTracker = fleetTracker;
		this.robot = fleetTracker.robot;
		this.logger = fleetTracker.logger;
		fleetTracker.addshipTracker(memberId, this);

		for (String hullModId : variant.getHullMods()) { if (!hullModId.startsWith(ehm.affix.allRetrofit)) continue; 
			if (hullMods.contains(hullModId)) continue;

			hullMods.add(hullModId);
		}
		
		Global.getSector().addScript(this); 

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
	// END OF CONSTRUCTORS & ACCESSORS
	
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
		} 

		for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
			if (variant.hasHullMod(hullModId)) continue;

			logger.info("ST-"+memberId+": Removed hull modification '"+hullModId+"'");

			removedHullMods.add(hullModId);
		} 
		
		if (!newHullMods.isEmpty()) {
			for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) { 
				String hullModId = i.next(); 
				String hullModType = hullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
				switch (hullModType) {
					case ehm.affix.systemRetrofit: playSound = true; refresh = true; break;
					case ehm.affix.weaponRetrofit: playSound = true; refresh = true; break;
					case ehm.affix.shieldCosmetic: playSound = true; break;
					case ehm.affix.engineCosmetic: playSound = true; break;
					default: break;
				}
			} hullMods.addAll(newHullMods); newHullMods.clear();
		}
		
		if (!removedHullMods.isEmpty()) {
			for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) { 
				String hullModId = i.next(); 
				String hullModType = hullModId.substring(0, 7); 
				switch (hullModType) {
					case ehm.affix.systemRetrofit: playSound = true; refresh = true; _ehm_sr_base.ehm_systemRestore(variant); break;
					case ehm.affix.weaponRetrofit: playSound = true; refresh = true; _ehm_wr_base.ehm_weaponSlotRestore(variant); break;
					case ehm.affix.shieldCosmetic: playSound = true; _ehm_sc_base.ehm_restoreShield(variant); break;
					case ehm.affix.engineCosmetic: playSound = true; _ehm_ec_base.ehm_restoreEngineSlots(variant); break;
					default: break;
				}
			} hullMods.removeAll(removedHullMods); removedHullMods.clear();
		}

		if (refresh) { frameCount++;
			if (frameCount < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				refresh = false;
				frameCount = 0f;
				logger.info("ST-"+memberId+": Refreshed refit tab");
			}
		}

		if (playSound) {
			Global.getSoundPlayer().playUISound("drill", 1.0f, 0.75f);
			
			playSound = false;
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
