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
 * NOTE: id of this shield in the .csv remains "ehm_sc_redShields" for save compatibility
 * @category Custom Shield Cosmetic
 * @author lyravega
 */
public final class ehm_csc_redShields extends _ehm_sc_base implements customizableMod {
	private static Color innerColour;
	private static Color ringColour;

	@Override
	public void applyCustomization() {
		String settingIdPrefix = this.getClass().getSimpleName()+"_";

		innerColour = lyr_lunaUtilities.getLunaRGBAColour(ehm_internals.id.mod, settingIdPrefix+"inner");
		ringColour = lyr_lunaUtilities.getLunaRGBAColour(ehm_internals.id.mod, settingIdPrefix+"ring");
		this.hullModSpec.setDisplayName(lyr_lunaUtilities.getLunaName(ehm_internals.id.mod, settingIdPrefix));
	}

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.applyCustomization();
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		variant.setHullSpecAPI(ehm_applyShieldCosmetics(variant, innerColour, ringColour));
	}
}
