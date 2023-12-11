package experimentalHullModifications.hullmods.ehm_ar;

import java.util.*;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.adapters;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.proxies.lyr_hullSpec;

/**@category Adapter Retrofit
 * @author lyravega
 */
public final class ehm_ar_stepdownadapter extends _ehm_ar_base {
	static final class adapterData {
		public static final class ids {
			public static final String
				mediumDual = adapters.ids.mediumDual,
				largeDual = adapters.ids.largeDual,
				largeTriple = adapters.ids.largeTriple,
				largeQuad = adapters.ids.largeQuad;
		}
		public static final String activatorId = adapters.activatorId;
		public static final String tag = adapters.groupTag;
		public static final String groupTag = adapters.groupTag;
		public static final Map<String, adapterParameters> dataMap = new HashMap<String, adapterParameters>();
		public static final Set<String> idSet = dataMap.keySet();
		private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.adaptedSlot, affixes.convertedSlot});

		public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
			return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
		}

		static {
			final adapterParameters mediumDual = new adapterParameters();
			mediumDual.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
			mediumDual.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
			dataMap.put(ids.mediumDual, mediumDual);

			final adapterParameters largeDual = new adapterParameters();
			largeDual.addChild("L", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
			largeDual.addChild("R", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
			dataMap.put(ids.largeDual, largeDual);

			final adapterParameters largeTriple = new adapterParameters();
			largeTriple.addChild("L", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // left
			largeTriple.addChild("R", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // right
			largeTriple.addChild("C", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
			dataMap.put(ids.largeTriple, largeTriple);

			final adapterParameters largeQuad = new adapterParameters();
			largeQuad.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
			largeQuad.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
			largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // far left
			largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // far right
			dataMap.put(ids.largeQuad, largeQuad);
		}

		public static class adapterParameters {
			private final Set<String> children; public Set<String> getChildren() { return this.children; }
			private final Map<String, Vector2f> childrenOffsets; public Vector2f getChildOffset(String childPrefix) { return this.childrenOffsets.get(childPrefix); }
			private final Map<String, WeaponSize> childrenSizes; public WeaponSize getChildSize(String childPrefix) { return this.childrenSizes.get(childPrefix); }

			private adapterParameters() {
				this.children = new HashSet<String>();
				this.childrenOffsets = new HashMap<String, Vector2f>();
				this.childrenSizes = new HashMap<String, WeaponSize>();
			}

			private void addChild(String childId, WeaponSize childSize, Vector2f childOffset) {
				this.children.add(childId);
				this.childrenOffsets.put(childId, childOffset);
				this.childrenSizes.put(childId, childSize);
			}
		}
	}

	public ehm_ar_stepdownadapter() {
		super();

		this.shuntSet.addAll(adapterData.idSet);
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		DynamicStatsAPI dynamicStats = stats.getDynamic();

		HashMap<String, StatMod> adapterShunts = dynamicStats.getMod(adapterData.groupTag).getFlatBonuses();
		if (!adapterShunts.isEmpty()) {
			for (String slotId : adapterShunts.keySet()) {
				if (lyr_hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;

				ehm_adaptSlot(lyr_hullSpec, variant.getWeaponId(slotId), slotId);
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
			DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> adapterShunts = dynamicStats.getMod(adapterData.groupTag).getFlatBonuses();
				if (!adapterShunts.isEmpty()) {
					tooltip.addSectionHeading("ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, variant, adapterShunts.keySet());
				} else if (ehm_settings.getShowFullInfoForActivators()) {
					tooltip.addSectionHeading("NO ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					tooltip.addPara("No adapters are installed. Adapters turn bigger slots into smaller ones.", text.padding);
				}
			}
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}
	//#endregion
}
