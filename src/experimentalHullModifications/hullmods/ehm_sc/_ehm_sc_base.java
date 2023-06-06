package experimentalHullModifications.hullmods.ehm_sc;

import static lyravega.tools.lyr_uiTools.commitChanges;
import static lyravega.tools.lyr_uiTools.playSound;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods._ehm_base;
import experimentalHullModifications.hullmods.ehm.events.normalEvents;
import lunalib.lunaSettings.LunaSettings;
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
public class _ehm_sc_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_restoreShield(variant));
		commitChanges(); playSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

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
	 */
	public static final ShipHullSpecAPI ehm_restoreShield(ShipVariantAPI variant) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		ShieldSpecAPI stockShieldSpec = ehm_hullSpecReference(variant).getShieldSpec();

		hullSpec.setShieldSpec(stockShieldSpec);
		
		return hullSpec.retrieve();
	}

	protected static Color getLunaRGBAColour(String settingIdPrefix) {
		String colourString = LunaSettings.getString(lyr_internals.id.mod, settingIdPrefix+"Colour");
		int[] rgba = {0,0,0,0};
		rgba[0] = Integer.parseInt(colourString.substring(1, 3), 16);
		rgba[1] = Integer.parseInt(colourString.substring(3, 5), 16);
		rgba[2] = Integer.parseInt(colourString.substring(5, 7), 16);
		rgba[3] = LunaSettings.getInt(lyr_internals.id.mod, settingIdPrefix+"Alpha");

		return new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
	}

	protected static String getLunaName(String settingIdPrefix) {
		return LunaSettings.getString(lyr_internals.id.mod, settingIdPrefix+"name");
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

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (ehm_hasRetrofitTag(ship, lyr_internals.tag.shieldCosmetic, hullModSpecId)) tooltip.addPara(text.hasShieldCosmetic[0], text.padding).setHighlight(text.hasShieldCosmetic[1]);

			if (hullModSpec.getTags().contains(lyr_internals.tag.reqShields) && ship.getShield() == null) tooltip.addPara(text.noShields[0], text.padding).setHighlight(text.noShields[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false;

		if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) return false;
		if (ehm_hasRetrofitTag(ship, lyr_internals.tag.shieldCosmetic, hullModSpecId)) return false;

		Set<String> hullModSpecTags = hullModSpec.getTags();
		if (hullModSpecTags.contains(lyr_internals.tag.reqShields) && ship.getShield() == null) return false;

		return true;
	}
	//#endregion
}