package experimentalHullModifications.hullmods.ehm_ar;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals.shunts.adapters;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.proxies.ehm_hullSpec;
import experimentalHullModifications.shunts._ehm_shuntEffect;
import experimentalHullModifications.shunts.ehm_adapterEffect;
import experimentalHullModifications.shunts.ehm_adapterEffect.adapterParameters;

/**
 * Activator retrofit that controls the listed shunts below. Their effects and such are separated to
 * keep this class relatively small.
 * @see ehm_adapterEffect Adapters
 * @category Activator Retrofit
 * @author lyravega
 */
public final class ehm_ar_stepdownadapter extends _ehm_ar_base {
	public static _ehm_shuntEffect<adapterParameters> adapterEffect;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		adapterEffect = new ehm_adapterEffect(this);

		final adapterParameters mediumDual = new adapterParameters();
		mediumDual.addChild("FL", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		mediumDual.addChild("FR", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		adapterEffect.addShuntData(adapters.ids.mediumDual, mediumDual);

		final adapterParameters largeDual = new adapterParameters();
		largeDual.addChild("FL", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
		largeDual.addChild("FR", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
		adapterEffect.addShuntData(adapters.ids.largeDual, largeDual);

		final adapterParameters largeTriple = new adapterParameters();
		largeTriple.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // left
		largeTriple.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // right
		largeTriple.addChild("FC", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
		adapterEffect.addShuntData(adapters.ids.largeTriple, largeTriple);

		final adapterParameters largeQuad = new adapterParameters();
		largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		largeQuad.addChild("RL", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // far left
		largeQuad.addChild("RR", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // far right
		adapterEffect.addShuntData(adapters.ids.largeQuad, largeQuad);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		adapterEffect.processShunts(hullSpec, stats, dynamicStats, variant);

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		switch (index) {
			case 0: return "adapters";
			default: return null;
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;
		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(this.hullModSpecId)) {
			if (lyr_ehm.lunaSettings.getShowInfoForActivators()) {
				adapterEffect.addShuntInfoToActivatorDescription(tooltip, hullSize, ship, width, isForModSpec);
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
