package experimentalHullModifications.hullmods.ehm_sc;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.customizableMod;
import lyravega.utilities.lyr_lunaUtilities;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_greenShields" for save compatibility
 * @category Custom Shield Cosmetic
 * @author lyravega
 */
public final class ehm_csc_greenShields extends _ehm_sc_base implements customizableMod {
	@Override
	public void applyCustomization() {
		String id = this.getClass().getSimpleName();

		this.innerColour = lyr_lunaUtilities.getLunaRGBAColour(ehm_internals.ids.mod, id+"_inner");
		this.ringColour = lyr_lunaUtilities.getLunaRGBAColour(ehm_internals.ids.mod, id+"_ring");
		this.hullModSpec.setDisplayName(lyr_lunaUtilities.getLunaName(ehm_internals.ids.mod, id));
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyShieldCosmetics(variant, this.innerColour, this.ringColour));
	}
}
