package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.loading.SpecStore;
import com.fs.starfarer.loading.specs.D;
import com.fs.starfarer.loading.specs.D.Oo;
import com.fs.starfarer.loading.specs.g;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class ehm_er_automated extends _ehm_base_master {
	private static final String automated = "automated";

	// TODO: Finish this
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		g hullSpec = (g) variant.getHullSpec();

		for (D engineSlot : hullSpec.getEngineSlots()) {
			//Oo test = (Oo) SpecStore.o00000(Oo.class, "MIDLINE");
			engineSlot.o00000(com.fs.starfarer.loading.specs.D.o.õ00000);
			//engineSlot.o00000(test);
		}
	}

	public static void apply(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		//applyEffectsBeforeShipCreation(hullSize, stats, id);
	}
	
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return "Ship does not exist"; 
		
		if (ship.getVariant().hasHullMod(automated)) return "Ship is automated";
		// if (ship.getVariant().hasHullMod("ehm_er_manned")) return "herp";

		return null; 
	}

	@Override
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		ShipVariantAPI variant = ship.getVariant();
		
		// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

		return null;
	}
}
