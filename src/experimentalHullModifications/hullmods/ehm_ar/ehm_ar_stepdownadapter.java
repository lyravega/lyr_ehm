package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.misc.ehm_internals.shunts.adapters;
import experimentalHullModifications.misc.ehm_internals.shunts.adapters.adapterParameters;
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

	static final Map<String, adapterParameters> adapterMap = adapters.dataMap;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		HashMap<String, StatMod> adapterShunts = stats.getDynamic().getMod(adapters.groupTag).getFlatBonuses();
		if (adapterShunts != null && !adapterShunts.isEmpty()) {
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
			final DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();

			if (ehm_settings.getShowInfoForActivators()) {
				HashMap<String, StatMod> adapterShunts = dynamicStats.getMod(adapters.groupTag).getFlatBonuses();
				if (adapterShunts != null && !adapterShunts.isEmpty()) {
					tooltip.addSectionHeading("ACTIVE ADAPTERS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
					ehm_printShuntCount(tooltip, dynamicStats, adapters.idSet);
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
