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

import experimentalHullModifications.misc.ehm_internals;

/**
 * {@link FieldRepairsScript} replacement with a dirty hack to avoid getting the cloned
 * hull specs replaced in {@link #restoreToNonDHull}, as it might result in a crash due
 * to replaced hull specs lacking weapon slots that the variant may be using.
 * <p> Uses the same alias through {@link experimentalHullModifications.plugin.lyr_ehm#configureXStream} method so
 * that it can be removed later on, leaving the original script pick the data up.
 * @author Alex
 */
public class ehm_fieldRepairsScript extends FieldRepairsScript {
	// @Override
	Object readResolve() {
		if (this.seen == null) {
			this.seen =  new LinkedHashSet<String>();
		}
		if (this.tracker2 == null) {
			this.tracker2 = new IntervalUtil(3f, 5f);
		}
		if (this.newRandom == null) {
			this.newRandom = new Random(Misc.genRandomSeed());
		}
		return this;
	}

	@Override
	public void advance(float amount) {
		CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
		if (fleet == null) return;

		if (Global.getSector().getPlayerStats().getSkillLevel(Skills.HULL_RESTORATION) <= 0) {
			this.picked = null;
			this.dmod = null;
			return;
		}

		float days = Global.getSector().getClock().convertToDays(amount);
		float rateMult = 1f / (float) MONTHS_PER_DMOD_REMOVAL;
		//days *= 100f;
		this.tracker.advance(days * rateMult * 0.5f); // * 0.5f since the tracker interval averages 15 days
		if (this.tracker.intervalElapsed()) {
			// pick which ship to remove which d-mod from half a month ahead of time
			// if it's no longer present in the fleet when it's time to remove the d-mod,
			// don't remove a d-mod at all
			if (this.picked == null || this.dmod == null) {
				this.pickNext();
			} else {
				if (fleet.getFleetData().getMembersListCopy().contains(this.picked) &&
						DModManager.getNumDMods(this.picked.getVariant()) > 0) {
					DModManager.removeDMod(this.picked.getVariant(), this.dmod);

					HullModSpecAPI spec = DModManager.getMod(this.dmod);
					MessageIntel intel = new MessageIntel(this.picked.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
					intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
					Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, this.picked);

					int dmods = DModManager.getNumDMods(this.picked.getVariant());
					if (dmods <= 0) {
						restoreToNonDHull(this.picked.getVariant());
					}
				}
				this.picked = null;
				this.pickNext();
			}
		}

		this.tracker2.advance(days);
		if (this.tracker2.intervalElapsed() && REMOVE_DMOD_FROM_NEW_SHIPS) {
			if (this.pickedNew == null || this.dmodNew == null) {
				this.pickNextNew();
			} else {
				this.seen.add(this.pickedNew.getId());

				float numDmods = DModManager.getNumDMods(this.pickedNew.getVariant());
				if (fleet.getFleetData().getMembersListCopy().contains(this.pickedNew) && numDmods > 0) {
					float probRemove = MIN_NEW_REMOVE_PROB + numDmods * NEW_REMOVE_PROB_PER_DMOD;
					if (this.newRandom.nextFloat() < probRemove) {
						DModManager.removeDMod(this.pickedNew.getVariant(), this.dmodNew);

						HullModSpecAPI spec = DModManager.getMod(this.dmodNew);
						MessageIntel intel = new MessageIntel(this.pickedNew.getShipName() + " - repaired " + spec.getDisplayName(), Misc.getBasePlayerColor());
						intel.setIcon(Global.getSettings().getSpriteName("intel", "repairs_finished"));
						Global.getSector().getCampaignUI().addMessage(intel, MessageClickAction.REFIT_TAB, this.pickedNew);

						int dmods = DModManager.getNumDMods(this.pickedNew.getVariant());
						if (dmods <= 0) {
							restoreToNonDHull(this.pickedNew.getVariant());
						}
					}
				}
				this.pickedNew = null;
				this.pickNextNew();
			}
		}
	}

	public static void restoreToNonDHull(ShipVariantAPI v) {
		if (v.hasHullMod(ehm_internals.ids.hullmods.base)) return;	// dirty hack to avoid getting the cloned hullSpec replaced

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


