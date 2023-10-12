package experimentalHullModifications.scripts;

import java.util.LinkedHashSet;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import lyravega.misc.lyr_internals;

/**
 * {@link FieldRepairsScript} replacement with a dirty hack to avoid getting the cloned
 * hull specs replaced in {@link #restoreToNonDHull}, as it might result in a crash due
 * to replaced hull specs lacking weapon slots that the variant may be using.
 * <p> Uses the same alias through {@link lyravega.plugin.lyr_ehm#configureXStream} method so
 * that it can be removed later on, leaving the original script pick the data up. 
 * @author Alex
 */
public class ehm_fieldRepairsScript extends FieldRepairsScript {
	// @Override
	Object readResolve() {
		if (seen == null) {
			seen =  new LinkedHashSet<String>();
		}
		if (tracker2 == null) {
			tracker2 = new IntervalUtil(3f, 5f);
		}
		if (newRandom == null) {
			newRandom = new Random(Misc.genRandomSeed());
		}
		return this;
	}
	
	@Override
	public void advance(float amount) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return;
		
		if (Global.getSector().getPlayerStats().getSkillLevel(Skills.HULL_RESTORATION) <= 0) {
			picked = null;
			dmod = null;
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		float rateMult = 1f / (float) MONTHS_PER_DMOD_REMOVAL;
		//days *= 100f;
		tracker.advance(days * rateMult * 0.5f); // * 0.5f since the tracker interval averages 15 days
		if (tracker.intervalElapsed()) {
			// pick which ship to remove which d-mod from half a month ahead of time
			// if it's no longer present in the fleet when it's time to remove the d-mod,
			// don't remove a d-mod at all
			if (picked == null || dmod == null) {
				pickNext();
			} else {
				if (fleet.getFleetData().getMembersListCopy().contains(picked) &&
						DModManager.getNumDMods(picked.getVariant()) > 0) {
					DModManager.removeDMod(picked.getVariant(), dmod);
					
					HullModSpecAPI spec = DModManager.getMod(dmod);
					MessageIntel intel = new MessageIntel(picked.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
					intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, picked);
					
					int dmods = DModManager.getNumDMods(picked.getVariant());
					if (dmods <= 0) {
						restoreToNonDHull(picked.getVariant());
					}
				}
				picked = null;
				pickNext();
			}
		}
		
		tracker2.advance(days);
		if (tracker2.intervalElapsed() && REMOVE_DMOD_FROM_NEW_SHIPS) {
			if (pickedNew == null || dmodNew == null) {
				pickNextNew();
			} else {
				seen.add(pickedNew.getId());
				
				float numDmods = DModManager.getNumDMods(pickedNew.getVariant());
				if (fleet.getFleetData().getMembersListCopy().contains(pickedNew) && numDmods > 0) {
					float probRemove = MIN_NEW_REMOVE_PROB + numDmods * NEW_REMOVE_PROB_PER_DMOD;
					if (newRandom.nextFloat() < probRemove) {
						DModManager.removeDMod(pickedNew.getVariant(), dmodNew);
						
						HullModSpecAPI spec = DModManager.getMod(dmodNew);
						MessageIntel intel = new MessageIntel(pickedNew.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
						intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
						Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, pickedNew);
						
						int dmods = DModManager.getNumDMods(pickedNew.getVariant());
						if (dmods <= 0) {
							restoreToNonDHull(pickedNew.getVariant());
						}
					}
				}
				pickedNew = null;
				pickNextNew();
			}
		}
	}
	
	public static void restoreToNonDHull(ShipVariantAPI v) {
		if (v.hasHullMod(lyr_internals.id.hullmods.base)) return;	// dirty hack to avoid getting the cloned hullSpec replaced

		ShipHullSpecAPI base = v.getHullSpec().getDParentHull();
		
		// so that a skin with dmods can be "restored" - i.e. just dmods suppressed w/o changing to
		// actual base skin
		//if (!v.getHullSpec().isDHull()) base = v.getHullSpec();
		if (!v.getHullSpec().isDefaultDHull() && !v.getHullSpec().isRestoreToBase()) base = v.getHullSpec();
		
		if (base == null && v.getHullSpec().isRestoreToBase()) {
			base = v.getHullSpec().getBaseHull();
		}
		
		if (base != null) {
			//v.clearPermaMods();
			v.setHullSpecAPI(base);
		}
	}
}


