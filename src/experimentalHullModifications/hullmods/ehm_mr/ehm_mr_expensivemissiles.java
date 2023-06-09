package experimentalHullModifications.hullmods.ehm_mr;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import experimentalHullModifications.hullmods.ehm_wr.ehm_wr_missileslotretrofit;

/**
 * @category Effect Extension
 * @see Master: {@link ehm_wr_missileslotretrofit}
 * @author lyravega
 */
public class ehm_mr_expensivemissiles extends BaseHullMod {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, 2);
		stats.getDynamic().getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, 4);
		stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, 8);
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}
