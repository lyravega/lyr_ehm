package experimentalHullModifications.hullmods.ehm_sc;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.customizableMod;
import lyravega.utilities.lyr_lunaUtilities;

/**
 * NOTE: id of this shield in the .csv remains "ehm_sc_blueShields" for save compatibility
 * @category Custom Shield Cosmetic
 * @author lyravega
 */
public final class ehm_csc_blueShields extends _ehm_sc_base implements customizableMod {
	private Color innerColour;
	private Color ringColour;

	@Override
	public void applyCustomization() {
		String id = this.getClass().getSimpleName();

		this.innerColour = lyr_lunaUtilities.getLunaRGBAColour(ehm_internals.id.mod, id+"_inner");
		this.ringColour = lyr_lunaUtilities.getLunaRGBAColour(ehm_internals.id.mod, id+"_ring");
		this.hullModSpec.setDisplayName(lyr_lunaUtilities.getLunaName(ehm_internals.id.mod, id));
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyShieldCosmetics(variant, this.innerColour, this.ringColour));
	}
}
