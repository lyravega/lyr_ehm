package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.*;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.ids.shunts.adapters;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_stepdownadapter extends _ehm_ar_base {
	//#region CUSTOM EVENTS
	@Override
	public void onWeaponInstalled(ShipVariantAPI variant, String weaponId, String slotId) {
		if (adapterMap.keySet().contains(weaponId)) commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(ShipVariantAPI variant, String weaponId, String slotId) {
		if (adapterMap.keySet().contains(weaponId)) commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	/**
	 * An inner class to supply the adapters with relevant child data
	 */
	static class childrenParameters {
		private Set<String> children; // childIds are used as position identifier, and used as a suffix
		private Map<String, Vector2f> childrenOffsets;
		private Map<String, WeaponSize> childrenSizes;

		private childrenParameters() {
			this.children = new HashSet<String>();
			this.childrenOffsets = new HashMap<String, Vector2f>();
			this.childrenSizes = new HashMap<String, WeaponSize>();
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

	static final Map<String, childrenParameters> adapterMap = new HashMap<String, childrenParameters>();
	private static final childrenParameters mediumDual = new childrenParameters();
	private static final childrenParameters largeDual = new childrenParameters();
	private static final childrenParameters largeTriple = new childrenParameters();
	private static final childrenParameters largeQuad = new childrenParameters();
	static {
		mediumDual.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		mediumDual.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		adapterMap.put(ehm_internals.ids.shunts.adapters.mediumDual, mediumDual);

		largeDual.addChild("L", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
		largeDual.addChild("R", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
		adapterMap.put(ehm_internals.ids.shunts.adapters.largeDual, largeDual);

		largeTriple.addChild("L", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // left
		largeTriple.addChild("R", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // right
		largeTriple.addChild("C", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
		adapterMap.put(ehm_internals.ids.shunts.adapters.largeTriple, largeTriple);

		largeQuad.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
		largeQuad.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
		largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // far left
		largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // far right
		adapterMap.put(ehm_internals.ids.shunts.adapters.largeQuad, largeQuad);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		List<WeaponSlotAPI> shunts = lyr_hullSpec.getAllWeaponSlotsCopy();

		for (Iterator<WeaponSlotAPI> iterator = shunts.iterator(); iterator.hasNext();) {
			WeaponSlotAPI slot = iterator.next();
			// if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			if (variant.getWeaponSpec(slotId) == null) { iterator.remove(); continue; }
			if (!slotId.startsWith(ehm_internals.affixes.normalSlot)) { iterator.remove(); continue; }

			WeaponSpecAPI shuntSpec = variant.getWeaponSpec(slotId);
			if (shuntSpec.getSize() != slot.getSlotSize()) { iterator.remove(); continue; }
			if (!shuntSpec.hasTag(ehm_internals.tags.experimental)) { iterator.remove(); continue; }

			String shuntId = shuntSpec.getWeaponId();
			switch (shuntId) {
				case adapters.largeDual: case adapters.largeQuad: case adapters.largeTriple: case adapters.mediumDual: {
					break;
				} default: { iterator.remove(); break; }
			}
		}

		for (WeaponSlotAPI slot : shunts) {
			if (slot.isDecorative()) continue;

			String slotId = slot.getId();
			String shuntId = variant.getWeaponSpec(slotId).getWeaponId();

			switch (shuntId) {
				case adapters.largeDual: case adapters.largeQuad: case adapters.largeTriple: case adapters.mediumDual: {
					ehm_adaptSlot(lyr_hullSpec, shuntId, slotId);
					break;
				} default: break;
			}
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
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
			if (ehm_settings.getShowInfoForActivators()) {
				Map<String, Integer> adapters = ehm_shuntCount(ship, ehm_internals.tags.adapterShunt);

				if (!adapters.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					for (String shuntId: adapters.keySet()) {
						tooltip.addPara(adapters.get(shuntId) + "x " + Global.getSettings().getWeaponSpec(shuntId).getWeaponName(), 2f);
					}
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No adapters are installed. Adapters turn bigger slots into smaller ones.", 2f);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
