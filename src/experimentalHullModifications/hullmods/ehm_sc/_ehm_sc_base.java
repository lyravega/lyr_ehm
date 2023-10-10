package experimentalHullModifications.hullmods.ehm_sc;

import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import java.awt.Color;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm._ehm_helpers;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_shieldSpec;

/**
 * This class is used by shield cosmetic hullmods. The changes are 
 * permanent, and does not use {@code advanceInCombat()}.
 * </p> Reason to split this as another base was primarily maintenance.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @author lyravega
 */
public abstract class _ehm_sc_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		if (_ehm_helpers.removeHullModsWithSameTag(variant, lyr_internals.tag.shieldCosmetic, this.hullModSpecId)) return;
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		if (!_ehm_helpers.hasHullModWithTag(variant, lyr_internals.tag.shieldCosmetic, this.hullModSpecId))
			variant.setHullSpecAPI(ehm_restoreShield(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	/**
	 * Alters the shield colours of the ship. Inner and ring colours
	 * can be different. 
	 * @param variant whose shieldSpec will be altered
	 * @return an altered hullSpec with altered shieldSpec colours
	 */
	protected static final ShipHullSpecAPI ehm_applyShieldCosmetics(ShipVariantAPI variant, Color inner, Color ring) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_shieldSpec shieldSpec = hullSpec.getShieldSpec();
		
		shieldSpec.setInnerColor(inner);
		shieldSpec.setRingColor(ring);

		return hullSpec.retrieve();
	}

	/**
	 * Restores the shieldSpec of the passed variant's hullSpec by
	 * referring to a stock one.
	 * @param variant whose shieldSpec will be restored
	 * @return an altered hullSpec with its shieldSpec is restored
	 */
	public static final ShipHullSpecAPI ehm_restoreShield(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		ShieldSpecAPI stockShieldSpec = ehm_hullSpecReference(variant).getShieldSpec();

		hullSpec.setShieldSpec(stockShieldSpec);
		
		return hullSpec.retrieve();
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (this.hullModSpec.hasTag(lyr_internals.tag.customizable)) {
			tooltip.addSectionHeading(header.customizable, header.customizable_textColour, header.customizable_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.customizable[0], text.padding).setHighlight(text.customizable[1]);
		}

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			// if (_ehm_helpers.ehm_hasHullModWithTag(ship, lyr_internals.tag.shieldCosmetic, id)) tooltip.addPara(text.hasShieldCosmetic[0], text.padding).setHighlight(text.hasShieldCosmetic[1]);

			if (ship.getShield() == null) tooltip.addPara(text.noShields[0], text.padding).setHighlight(text.noShields[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) return false;
		// if (_ehm_helpers.ehm_hasHullModWithTag(ship, lyr_internals.tag.shieldCosmetic, id)) return false;

		if (ship.getShield() == null) return false;

		return true;
	}
	//#endregion
}