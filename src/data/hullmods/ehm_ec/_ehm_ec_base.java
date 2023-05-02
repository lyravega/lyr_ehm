package data.hullmods.ehm_ec;

import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods._ehm_basetracker.hullModEventListener;
import data.hullmods._ehm_hullmodeventmethods;
import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;
import lyr.proxies.lyr_engineBuilder;
import lyr.proxies.lyr_hullSpec;

/**
 * This class is used by engine cosmetic hullmods. The changes are 
 * permanent, and does not use {@code advanceInCombat()}.
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_ec_base extends _ehm_base implements _ehm_hullmodeventmethods {
	//#region LISTENER & EVENT REGISTRATION
	protected hullModEventListener hullModEventListener;

	@Override	// not used
	public boolean onInstall(ShipVariantAPI variant) {
		return true;
	}

	@Override
	public boolean onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_restoreEngineSlots(variant));
		return true;
	}

	@Override 
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);
		this.hullModEventListener = new hullModEventListener(this.hullModSpecId, this);
		hullModEventListener.registerInstallEvent(true, true);
		hullModEventListener.registerRemoveEvent(true, true, null);
	}
	//#endregion
	// END OF LISTENER & EVENT REGISTRATION

	/**
	 * Alters the engine visuals of the ship. Uses the vanilla engine styles
	 * (as I haven't found a way to alter engine colours directly)
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum somewhat hardcoded {@link lyr.proxies.lyr_engineBuilder.engineStyle engineStyle}
	 * @return a hullSpec with the altered engine visuals
	 */
	protected static final ShipHullSpecAPI ehm_pimpMyEngineSlots(ShipVariantAPI variant, int styleEnum) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyle(styleEnum);
		}

		return hullSpec.retrieve();
	}

	/**
	 * Restores the engine visuals of the ship by applying a stock hullSpec
	 * on the variant.
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum somewhat hardcoded {@link lyr.proxies.lyr_engineBuilder.engineStyle engineStyle}
	 * @return a hullSpec with restored engine visuals
	 * @see {@link data.hullmods.ehm_base#onRemoved(ShipVariantAPI, Set) onRemoved()}
	 * invokes {@link #onRemove(ShipVariantAPI) onRemove()} of this class, which uses
	 * this method
	 */
	public static final ShipHullSpecAPI ehm_restoreEngineSlots(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(lyr_tooltip.header.notApplicable, lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(lyr_tooltip.text.lacksBase, lyr_tooltip.text.padding);
			if (ehm_hasRetrofitTag(ship, lyr_internals.tag.engineCosmetic, hullModSpecId)) tooltip.addPara(lyr_tooltip.text.hasEngineCosmetic, lyr_tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false; 
		if (ehm_hasRetrofitTag(ship, lyr_internals.tag.engineCosmetic, hullModSpecId)) return false;

		return true; 
	}
	//#endregion
}