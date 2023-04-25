package lyr.tools;

import static lyr.tools._lyr_uiTools.isRefitTab;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.EveryFrameScriptWithCleanup;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import lyr.misc.lyr_internals;

public class _lyr_scriptTools extends _lyr_reflectionTools {
	protected static final Logger logger = Logger.getLogger(lyr_internals.logName);

	//#region INNER CLASS: refreshRefitScript
	private static refreshRefitScript refreshRefitScript;

	public static void refreshRefit() {
		if (!isRefitTab()) return;

		for(EveryFrameScript script : Global.getSector().getTransientScripts()) {
			if(script instanceof refreshRefitScript) {
				refreshRefitScript = (refreshRefitScript) script; 
			}
		}

		if (refreshRefitScript == null) { 
			refreshRefitScript = new refreshRefitScript();
		}
	}

	// TODO: either use these from here or from the base; keep one
	private static class refreshRefitScript implements EveryFrameScript {
		private boolean isDone = false;
		private float frameCount = 0f;
		private static Robot robot;
	
		static {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}

		private refreshRefitScript() {
			Global.getSector().addTransientScript(this);
		}

		@Override
		public void advance(float amount) {
			if (!isRefitTab()) { isDone = true; return; }

			frameCount++;
			if (frameCount < 5) {
				robot.keyPress(KeyEvent.VK_ENTER);
			} else {
				robot.keyPress(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_R);
				robot.keyRelease(KeyEvent.VK_ENTER);
				refreshRefitScript = null; // clean the parent
				isDone = true;
				return;
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
	}
	//#endregion
	// END OF INNER CLASS: refreshRefitScript

	//#region INNER CLASS: fleetTrackerScript
	public class fleetTrackerScript implements EveryFrameScriptWithCleanup {
		private Map<String, shipTrackerScript> shipTrackers = new HashMap<String, shipTrackerScript>();
		// private Set<FleetMember> members = new HashSet<FleetMember>();
		// private CampaignFleet playerFleet = (CampaignFleet) Global.getSector().getPlayerFleet();
		private boolean isDone = false;
		private float runTime = 0f;
		protected Robot robot = null;
		
		//#region CONSTRUCTORS & ACCESSORS
		public fleetTrackerScript() {
			logger.setLevel(Level.INFO);
			logger.info(lyr_internals.logPrefix+"FT: Initialized fleet tracking");
	
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
			
			Global.getSector().addScript(this);
		}
	
		public void addshipTracker(String memberId, shipTrackerScript shipTracker) {
			shipTrackers.put(memberId, shipTracker);
			logger.info(lyr_internals.logPrefix+"FT: Keeping track of ST-"+memberId);
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {	
			CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
			if (tab == null || !tab.equals(CoreUITabId.REFIT)) { logger.info(lyr_internals.logPrefix+"FT: Stopping fleet tracking"); isDone = true; return; }
	
			if (runTime > 10f) {
				runTime = 0f;
				logger.info(lyr_internals.logPrefix+"FT: Tracking "+shipTrackers.size()+" ships");
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
	
			// if (shipTrackers.isEmpty()) { logger.info(lyr_internals.logPrefix+"FT: Stopping fleet tracking, no ship trackers remaining"); isDone = true; return; }
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
	//#endregion
	// END OF INNER CLASS: fleetTrackerScript

	//#region INNER CLASS: shipTrackerScript
	public class shipTrackerScript implements EveryFrameScriptWithCleanup {
		// private fleetTrackerScript fleetTracker = null;
		private ShipVariantAPI variant = null;
		private String memberId = null;
		private Set<String> hullMods = new HashSet<String>();
		private boolean isDone = false;
		
		//#region CONSTRUCTORS & ACCESSORS
		public void setVariant(ShipVariantAPI variant) { // this can be moved to initialize
			this.variant = variant;
		}
		
		public shipTrackerScript(ShipVariantAPI variant, String memberId, fleetTrackerScript fleetTracker) {
			this.variant = variant;
			this.memberId = memberId;
	
			// this.fleetTracker = fleetTracker;
			fleetTracker.addshipTracker(memberId, this);
	
			for (String hullModId : variant.getHullMods()) { if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue; 
				if (hullMods.contains(hullModId)) continue;
	
				hullMods.add(hullModId);
			}
			
			Global.getSector().addScript(this); 
	
			logger.info(lyr_internals.logPrefix+"ST-"+memberId+": Initial hull modifications '"+hullMods.toString()+"'");
		}
	
		public String getMemberId() {
			return this.memberId;
		}
	
		public void kill() {
			this.isDone = true;
		}
		//#endregion
		// END OF CONSTRUCTORS & ACCESSORS
		
		@Override
		public void advance(float amount) {
			CoreUITabId tab = Global.getSector().getCampaignUI().getCurrentCoreTab();
			if (tab == null || !tab.equals(CoreUITabId.REFIT)) { logger.info(lyr_internals.logPrefix+"ST-"+memberId+": Stopping ship tracking"); isDone = true; return; }
	
			Set<String> newHullMods = new HashSet<String>();
			Set<String> removedHullMods = new HashSet<String>();
			
			for (String hullModId : variant.getHullMods()) { if (!hullModId.startsWith(lyr_internals.affix.allRetrofit)) continue; 
				if (hullMods.contains(hullModId)) continue;
	
				logger.info(lyr_internals.logPrefix+"ST-"+memberId+": New hull modification '"+hullModId+"'");
	
				newHullMods.add(hullModId);
			} 
	
			for (Iterator<String> i = hullMods.iterator(); i.hasNext();) { String hullModId = i.next(); 
				if (variant.hasHullMod(hullModId)) continue;
	
				logger.info(lyr_internals.logPrefix+"ST-"+memberId+": Removed hull modification '"+hullModId+"'");
	
				removedHullMods.add(hullModId);
			} 
			
			if (!newHullMods.isEmpty()) {
				for (Iterator<String> i = newHullMods.iterator(); i.hasNext();) { 
					String hullModId = i.next(); 
					String hullModType = hullModId.substring(0, 7); // all affixes (not tags) are fixed to 0-7
					switch (hullModType) { // any weaponSlot changes require refresh
						// case lyr_internals.affix.adapterRetrofit: break; // handled through hullMod methods
						// case lyr_internals.affix.systemRetrofit: playSound = true; break;
						// case lyr_internals.affix.weaponRetrofit: playSound = true; refresh = true; break;
						// case lyr_internals.affix.shieldCosmetic: playSound = true; break;
						// case lyr_internals.affix.engineCosmetic: playSound = true; break;
						default: break;
					}
				} hullMods.addAll(newHullMods); newHullMods.clear();
			}
			
			if (!removedHullMods.isEmpty()) {
				for (Iterator<String> i = removedHullMods.iterator(); i.hasNext();) { 
					String hullModId = i.next(); 
					String hullModType = hullModId.substring(0, 7); 
					switch (hullModType) { // any weaponSlot changes and cheap removal methods require refresh
						// case lyr_internals.affix.adapterRetrofit: break; // handled through hullMod methods
						// case lyr_internals.affix.systemRetrofit: _ehm_sr_base.ehm_systemRestore(variant); playSound = true; break;
						// case lyr_internals.affix.weaponRetrofit: _ehm_wr_base.ehm_weaponSlotRestore(variant); playSound = true; refresh = true; break;
						// case lyr_internals.affix.shieldCosmetic: _ehm_sc_base.ehm_restoreShield(variant); playSound = true; break;
						// case lyr_internals.affix.engineCosmetic: _ehm_ec_base.ehm_restoreEngineSlots(variant); playSound = true;  refresh = true; break;
						default: break;
					}
				} hullMods.removeAll(removedHullMods); removedHullMods.clear();
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
	//#endregion
	// END OF INNER CLASS: shipTrackerScript


	//#region EXPERIMENTS
	/*
	private static remoteInvokerScript remoteInvokerScript;
	
	protected static void remoteInvoker(boolean isStaticMethod, Class<?> clazz, String methodName, Class<?> returnType, List<Object> parameters) {
		for(EveryFrameScript script : Global.getSector().getTransientScripts()) {
			if(script instanceof remoteInvokerScript) {
				remoteInvokerScript = (remoteInvokerScript) script; 
			}
		}

		if (remoteInvokerScript == null) { 
			remoteInvokerScript = new remoteInvokerScript(isStaticMethod, clazz, methodName, returnType, parameters);
		}
	}

	private static class remoteInvokerScript implements EveryFrameScript {
		private boolean isDone = false;
		private MethodHandle methodHandle = null;
		private List<Object> parameters;

		private remoteInvokerScript(boolean isStaticMethod, Class<?> clazz, String methodName, Class<?> returnType, List<Object> parameters) {
			List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

			for (Iterator<Object> i = parameters.iterator(); i.hasNext(); )
				parameterTypes.add(i.next().getClass());

			this.methodHandle = findMethodHandle(isStaticMethod, clazz, methodName, returnType, parameterTypes);

			Global.getSector().addTransientScript(this);
		}

		@Override
		public void advance(float amount) {
			methodHandle.invoke(null);

			remoteInvokerScript = null; // clean the parent
			isDone = true;
			return;
		}

		@Override
		public boolean runWhilePaused() {
			return true;
		}

		@Override
		public boolean isDone() {
			return isDone;
		}
	}
	*/
	//#endregion
	// END OF EXPERIMENTS
}
