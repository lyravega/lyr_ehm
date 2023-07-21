package experimentalHullModifications.hullmods.ehm_mr;

import static com.fs.starfarer.api.impl.hullmods.HeavyBallisticsIntegration.COST_REDUCTION;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import experimentalHullModifications.hullmods.ehm_wr.ehm_wr_energyslotretrofit;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;

/**
 * @category Effect Extension 
 * @see Master: {@link ehm_wr_energyslotretrofit}
 * @author lyravega
 */
public final class ehm_mr_heavyenergyintegration extends BaseHullMod {
	private static final String masterHullModId = lyr_internals.id.hullmods.energyslotretrofit;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!checkSource(stats.getVariant(), masterHullModId)) return;

		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
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
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

	@Override
	public int getDisplayCategoryIndex() {
		return 0;
	}
}
