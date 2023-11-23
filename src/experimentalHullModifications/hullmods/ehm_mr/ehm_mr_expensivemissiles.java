package experimentalHullModifications.hullmods.ehm_mr;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm_wr.ehm_wr_missileslotretrofit;
import experimentalHullModifications.misc.ehm_internals;

/**
 * @category Effect Extension
 * @see Master: {@link ehm_wr_missileslotretrofit}
 * @author lyravega
 */
public final class ehm_mr_expensivemissiles extends _ehm_base {
	public static void installExtension(ShipVariantAPI variant) {
		if (variant.getHullSpec().isBuiltInMod("hbi") || variant.getPermaMods().contains("hbi")) {
			variant.addSuppressedMod("hbi");
		}
		variant.addPermaMod(ehm_internals.ids.hullmods.extensions.expensivemissiles, false);
	}

	public static void removeExtension(ShipVariantAPI variant) {
		if (variant.getSuppressedMods().contains("hbi")) {
			if (!variant.hasHullMod(ehm_internals.ids.hullmods.energyslotretrofit)) variant.removeSuppressedMod("hbi");
		}
		variant.removePermaMod(ehm_internals.ids.hullmods.extensions.expensivemissiles);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!stats.getVariant().hasHullMod(ehm_internals.ids.hullmods.missileslotretrofit)) { removeExtension(stats.getVariant()); return; }

		DynamicStatsAPI dynamicStats = stats.getDynamic();
		dynamicStats.getMod(Stats.SMALL_MISSILE_MOD).modifyFlat(id, 2);
		dynamicStats.getMod(Stats.MEDIUM_MISSILE_MOD).modifyFlat(id, 4);
		dynamicStats.getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, 8);
	}

	@Override public boolean affectsOPCosts() { return true; }

	@Override public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {}

	@Override public int getDisplayCategoryIndex() { return 0; }	// to have the game treat this permaMod as a built-in
}
