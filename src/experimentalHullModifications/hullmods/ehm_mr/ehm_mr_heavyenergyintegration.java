package experimentalHullModifications.hullmods.ehm_mr;

import static com.fs.starfarer.api.impl.hullmods.HeavyBallisticsIntegration.COST_REDUCTION;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm_wr.ehm_wr_energyslotretrofit;
import experimentalHullModifications.misc.ehm_internals;

/**
 * @category Effect Extension
 * @see Master: {@link ehm_wr_energyslotretrofit}
 * @author lyravega
 */
public final class ehm_mr_heavyenergyintegration extends _ehm_base {
	public static void installExtension(ShipVariantAPI variant) {
		if (variant.getHullSpec().isBuiltInMod("hbi") || variant.getPermaMods().contains("hbi")) {
			variant.addSuppressedMod("hbi");
			variant.addPermaMod(ehm_internals.hullmods.extensions.heavyenergyintegration, false);
		}
	}

	public static void removeExtension(ShipVariantAPI variant) {
		if (variant.getSuppressedMods().contains("hbi")) {
			if (!variant.hasHullMod(ehm_internals.hullmods.weaponRetrofits.missileslotretrofit)) variant.removeSuppressedMod("hbi");
			variant.removePermaMod(ehm_internals.hullmods.extensions.heavyenergyintegration);
		}
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!stats.getVariant().hasHullMod(ehm_internals.hullmods.weaponRetrofits.energyslotretrofit)) { removeExtension(stats.getVariant()); return; }

		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
		return null;
	}

	@Override public boolean affectsOPCosts() { return true; }

	@Override public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {}

	@Override public int getDisplayCategoryIndex() { return 0; }	// to have the game treat this permaMod as a built-in
}
