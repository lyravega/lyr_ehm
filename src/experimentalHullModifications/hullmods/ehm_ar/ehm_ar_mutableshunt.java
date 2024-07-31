package experimentalHullModifications.hullmods.ehm_ar;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals.shunts;
import experimentalHullModifications.misc.ehm_internals.shunts.capacitors;
import experimentalHullModifications.misc.ehm_internals.shunts.dissipators;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.proxies.ehm_hullSpec;
import experimentalHullModifications.shunts._ehm_shuntEffect;
import experimentalHullModifications.shunts.ehm_capacitorEffect;
import experimentalHullModifications.shunts.ehm_dissipatorEffect;

/**
 * Activator retrofit that controls the listed shunts below. Their effects and such are separated to
 * keep this class relatively small.
 * @see ehm_capacitorEffect Capacitors
 * @see ehm_dissipatorEffect Dissipators
 * @category Activator Retrofit
 * @author lyravega
 */
public final class ehm_ar_mutableshunt extends _ehm_ar_base {
	public static _ehm_shuntEffect<Integer> capacitorEffect;
	public static _ehm_shuntEffect<Integer> dissipatorEffect;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		capacitorEffect = new ehm_capacitorEffect(this);
		capacitorEffect.addShuntData(capacitors.ids.large, shunts.slotValues.get(WeaponSize.LARGE));
		capacitorEffect.addShuntData(capacitors.ids.medium, shunts.slotValues.get(WeaponSize.MEDIUM));
		capacitorEffect.addShuntData(capacitors.ids.small, shunts.slotValues.get(WeaponSize.SMALL));

		dissipatorEffect = new ehm_dissipatorEffect(this);
		dissipatorEffect.addShuntData(dissipators.ids.large, shunts.slotValues.get(WeaponSize.LARGE));
		dissipatorEffect.addShuntData(dissipators.ids.medium, shunts.slotValues.get(WeaponSize.MEDIUM));
		dissipatorEffect.addShuntData(dissipators.ids.small, shunts.slotValues.get(WeaponSize.SMALL));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		dissipatorEffect.processShunts(hullSpec, stats, dynamicStats, variant);
		capacitorEffect.processShunts(hullSpec, stats, dynamicStats, variant);

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "dissipators";
			case 1: return "capacitors";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			if (lyr_ehm.lunaSettings.getShowInfoForActivators()) {
				dissipatorEffect.addShuntInfoToActivatorDescription(tooltip, hullSize, ship, width, isForModSpec);
				capacitorEffect.addShuntInfoToActivatorDescription(tooltip, hullSize, ship, width, isForModSpec);
			}
		}


		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
}
