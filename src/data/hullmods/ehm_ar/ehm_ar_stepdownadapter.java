package data.hullmods.ehm_ar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**@category Adapter Retrofit 
 * @author lyravega
 */
public class ehm_ar_stepdownadapter extends _ehm_ar_base {
	/**
	 * An inner class to supply the adapters with relevant child data
	 */
	static class childrenParameters {
		private Set<String> children; // childIds are used as position identifier, and used as a suffix
		private Map<String, Vector2f> childrenOffsets;
		private Map<String, WeaponSize> childrenSizes;

		private childrenParameters() {
			children = new HashSet<String>();
			childrenOffsets = new HashMap<String, Vector2f>();
			childrenSizes = new HashMap<String, WeaponSize>();
		}

		private void addChild(String childId, WeaponSize childSize, Vector2f childOffset) {
			this.children.add(childId);
			this.childrenOffsets.put(childId, childOffset);
			this.childrenSizes.put(childId, childSize);
		}

		Set<String> getChildren() {
			return this.children;
		}

		Vector2f getChildOffset(String childPrefix) {
			return this.childrenOffsets.get(childPrefix);
		}

		WeaponSize getChildSize(String childPrefix) {
			return this.childrenSizes.get(childPrefix);
		}
	}

	static final Map<String, childrenParameters> adapters = new HashMap<String, childrenParameters>();
	private static final childrenParameters mediumDual = new childrenParameters();
	private static final childrenParameters largeDual = new childrenParameters();
	private static final childrenParameters largeTriple = new childrenParameters();
	private static final childrenParameters largeQuad = new childrenParameters();
	static {
		mediumDual.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		mediumDual.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		adapters.put(lyr_internals.id.shunts.adapters.mediumDual, mediumDual);

		largeDual.addChild("L", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
		largeDual.addChild("R", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
		adapters.put(lyr_internals.id.shunts.adapters.largeDual, largeDual);

		largeTriple.addChild("L", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // left
		largeTriple.addChild("R", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // right
		largeTriple.addChild("C", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
		adapters.put(lyr_internals.id.shunts.adapters.largeTriple, largeTriple);

		largeQuad.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		largeQuad.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // far left
		largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // far right
		adapters.put(lyr_internals.id.shunts.adapters.largeQuad, largeQuad);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		// DUMMY MOD / DATA CLASS, ACTIONS ARE HANDLED THROUGH BASE
	}

	//#region INSTALLATION CHECKS / DESCRIPTION
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (variant.hasHullMod(hullModSpecId)) {
			if (extraActiveInfoInHullMods) {
				Map<String, Integer> adapters = ehm_shuntCount(variant, lyr_internals.tag.adapterShunt);

				if (!adapters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE ADAPTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					for (String shuntId: adapters.keySet()) {
						tooltip.addPara(adapters.get(shuntId) + "x " + settings.getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (extraInactiveInfoInHullMods) {
					tooltip.addSectionHeading("NO ADAPTERS", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
					tooltip.addPara("No adapters are installed. Adapters turn bigger slots into smaller ones.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);

		if (!canBeAddedOrRemovedNow(ship, null, null)) {
			String inOrOut = variant.hasHullMod(hullModSpecId) ? lyr_tooltip.header.lockedIn : lyr_tooltip.header.lockedOut;

			tooltip.addSectionHeading(inOrOut, lyr_tooltip.header.locked_textColour, lyr_tooltip.header.locked_bgColour, Alignment.MID, lyr_tooltip.header.padding);

			if (ehm_hasWeapons(ship, lyr_internals.affix.adaptedSlot)) tooltip.addPara(lyr_tooltip.text.hasWeaponsOnAdaptedSlots, lyr_tooltip.text.padding);
		}
	}
	
	@Override
	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (ship == null) return false; 

		if (ehm_hasWeapons(ship, lyr_internals.affix.adaptedSlot)) return false;

		return true;
	}
	//#endregion
}
