package experimentalHullModifications.hullmods.ehm_mr;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_wr.ehm_wr_missileslotretrofit;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;

/**
 * @category Effect Extension
 * @see Master: {@link ehm_wr_missileslotretrofit}
 * @author lyravega
 */
public final class ehm_mr_expensivemissiles extends BaseHullMod {
	private static final String masterHullModId = lyr_internals.id.hullmods.missileslotretrofit;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!checkSource(stats.getVariant(), masterHullModId)) return;

		DynamicStatsAPI dynamicStats = stats.getDynamic();
		dynamicStats.getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, 2);
		dynamicStats.getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, 4);
		dynamicStats.getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, 8);
	}

	private boolean checkSource(ShipVariantAPI variant, String sourceId) {
		if (!variant.hasHullMod(sourceId)) {
			normalEvents hullModEffect = (normalEvents) Global.getSettings().getHullModSpec(sourceId).getEffect();
			hullModEffect.onRemove(variant);
			return false;
		}
		return true;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}
