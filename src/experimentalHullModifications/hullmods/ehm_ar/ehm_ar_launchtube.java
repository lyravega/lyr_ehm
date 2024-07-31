package experimentalHullModifications.hullmods.ehm_ar;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals.shunts.hangars;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.proxies.ehm_hullSpec;
import experimentalHullModifications.shunts._ehm_shuntEffect;
import experimentalHullModifications.shunts.ehm_hangarEffect;

/**
 * Activator retrofit that controls the listed shunts below. Their effects and such are separated to
 * keep this class relatively small.
 * @see ehm_hangarEffect Hangars
 * @category Activator Retrofit
 * @author lyravega
 */
public final class ehm_ar_launchtube extends _ehm_ar_base {
	public static _ehm_shuntEffect<float[][]> hangarEffect;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		hangarEffect = new ehm_hangarEffect(this);
		hangarEffect.addShuntData(hangars.ids.large, new float[][]{{0f,0f}, {4f,4f}, {4f,-4f}, {-4f,4f}, {-4f,-4f}});
	}

	// com.fs.starfarer.api.impl.hullmods.ConvertedHangar
	// private static final HullModEffect convertedHangarEffect = Global.getSettings().getHullModSpec("converted_hangar").getEffect();
	// com.fs.starfarer.api.impl.hullmods.VastHangar
	// private static final HullModEffect vastHangarEffect = Global.getSettings().getHullModSpec("vast_hangar").getEffect();

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		hangarEffect.processShunts(hullSpec, stats, dynamicStats, variant);

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "launch tubes";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			if (lyr_ehm.lunaSettings.getShowInfoForActivators()) {
				hangarEffect.addShuntInfoToActivatorDescription(tooltip, hullSize, ship, width, isForModSpec);
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
