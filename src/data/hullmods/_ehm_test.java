package data.hullmods;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.EngineSlotAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.loading.specs.D;
import com.fs.starfarer.loading.specs.EngineSlot;
import com.fs.starfarer.loading.specs.HullVariantSpec;
import com.fs.starfarer.loading.specs.g;

public class _ehm_test extends _ehm_base {
	private static final String automated = "automated";

	// TODO: Finish this
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		HullVariantSpec variant = HullVariantSpec.class.cast(stats.getVariant()); 
		ShipHullSpecAPI hullSpecAPI = variant.getHullSpec();
		g hullSpec = (g) variant.getHullSpec();
		List<String> tesst = hullSpec.getBuiltInMods();

		HullModSpecAPI test = Global.getSettings().getHullModSpec(hullModSpecId);
		Class<?> qwe = test.getClass();
		Object[] asd = qwe.getMethods();
		test.getClass();
		/*
		List<?> test = new ArrayList<>();

		Class<?> qwe;
		boolean yup = false;

		try {
			MethodHandle getEngineSlots = MethodHandles.lookup().findVirtual(variant.getHullSpec().getClass(), "getEngineSlots", MethodType.methodType(List.class));
			test = (List<?>) getEngineSlots.invoke(hullSpec);
		} catch (Throwable t) {
			t.printStackTrace();
		} 
 
		Class<?> herp = test.get(0).getClass();
		Class<?>[] testtest = herp.getDeclaredClasses();
		boolean asd = testtest[0].isEnum();
		boolean zxc = testtest[1].isEnum();
		Enum<?>[] qweqwe = (Enum<?>[]) testtest[1].getEnumConstants();
		Class<?> HURR = qweqwe.getClass();
		*/

		for (D engineSlot : hullSpec.getEngineSlots()) {
			//Oo test = (Oo) SpecStore.o00000(Oo.class, "MIDLINE");
			engineSlot.o00000(com.fs.starfarer.loading.specs.D.o.oO0000);
			EngineSlotAPI herp = engineSlot.Ò00000(false);
			herp.setColor(new java.awt.Color(255, 255, 255, 255));
			//engineSlot.o00000(test);
		}
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		// ship.getEngineController().getShipEngines();
		// g hullSpec = (g) ship.getHullSpec();

		// for (ShipEngineAPI engineSlot : ship.getEngineController().getShipEngines()) {
			
		// 	ShipEngineAPI engineSlut = engineSlot;
		// 	//engineSlot.o00000(test); 
		// }
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		// ship.getEngineController().getShipEngines();
		// g hullSpec = (g) ship.getHullSpec();

		// for (ShipEngineAPI engineSlot : ship.getEngineController().getShipEngines()) {
			
		// 	ShipEngineAPI engineSlut = engineSlot;
		// 	//engineSlot.o00000(test); 
		// }

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
		HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());
		
		// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

		return null;
	}
}
