package data.hullmods.ehm_sc;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.hullmods._ehm_base;
import data.hullmods._ehm_hullmodevents;
import lyr.misc.lyr_externals;
import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;
import lyr.misc.lyr_externals.lyr_shieldSettings;
import lyr.proxies.lyr_hullSpec;
import lyr.proxies.lyr_shieldSpec;

/**
 * This class is used by shield cosmetic hullmods. The changes are 
 * permanent, and does not use {@code advanceInCombat()}.
 * </p> Reason to split this as another base was primarily maintenance.
 * @see {@link data.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link data.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link data.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link data.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @author lyravega
 */
public class _ehm_sc_base extends _ehm_base implements _ehm_hullmodevents {
	@Override
	public boolean onInstall(ShipVariantAPI variant) {
		return true;
	}

	@Override
	public boolean onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_restoreShield(variant));
		return true;
	}
	
	protected lyr_shieldSettings shieldSettings;
	protected Color innerColour;
	protected Color ringColour;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);
		if (lyr_externals.shieldSettings.containsKey(this.getClass().getSimpleName())) {
			this.shieldSettings = lyr_externals.shieldSettings.get(this.getClass().getSimpleName());
			this.innerColour = shieldSettings.getInnerColour();
			this.ringColour = shieldSettings.getRingColour();
			this.hullModSpec.setDisplayName(shieldSettings.getName());
		}
	}

	/**
	 * Alters the shield colours of the ship. Inner and ring colours
	 * can be different. 
	 * @param variant whose shieldSpec will be altered
	 * @return an altered hullSpec with altered shieldSpec colours
	 */
	protected static final ShipHullSpecAPI ehm_pimpMyShield(ShipVariantAPI variant, Color inner, Color ring) {
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
	 * @see {@link data.hullmods.ehm_base#onRemoved(ShipVariantAPI, Set) onRemoved()}
	 * invokes {@link #onRemove(ShipVariantAPI) onRemove()} of this class, which uses
	 * this method
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

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(lyr_tooltip.header.notApplicable, lyr_tooltip.header.notApplicable_textColour, lyr_tooltip.header.notApplicable_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(lyr_tooltip.text.lacksBase, lyr_tooltip.text.padding);
			if (ehm_hasRetrofitTag(ship, lyr_internals.tag.shieldCosmetic, hullModSpecId)) tooltip.addPara(lyr_tooltip.text.hasShieldCosmetic, lyr_tooltip.text.padding);

			if (hullModSpec.getTags().contains(lyr_internals.tag.reqShields) && ship.getShield() == null) tooltip.addPara(lyr_tooltip.text.noShields, lyr_tooltip.text.padding);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return false;
		if (ehm_hasRetrofitTag(ship, lyr_internals.tag.shieldCosmetic, hullModSpecId)) return false;

		Set<String> hullModSpecTags = hullModSpec.getTags();
		if (hullModSpecTags.contains(lyr_internals.tag.reqShields) && ship.getShield() == null) return false;

		return true;
	}
	//#endregion
}