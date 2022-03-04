package data.hullmods;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.CombatUIAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.combat.entities.Ship;

public class _ehm_test extends _ehm_base {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {

	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		List<FleetMemberAPI> membersListCopy = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
		List<Integer> herp = new ArrayList<>();

		Collection<String> hullMods = ship.getVariant().getHullMods();
		
		CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
		CombatUIAPI combatUI = Global.getCombatEngine().getCombatUI();
		PersistentUIDataAPI uiData = Global.getSector().getUIData();
		LinkedHashSet<String> checkedRefitTags = Global.getSector().getUIData().getCheckedRefitTags();
       // CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();

        try {
			Lookup lookup = MethodHandles.lookup();

			Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
			Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
			
			MethodHandle getDeclaredField = lookup.findVirtual(Class.class, "getDeclaredField", MethodType.methodType(fieldClass, String.class));
			Object screenPanelField = getDeclaredField.invoke(campaignUI.getClass(), "screenPanel");
			
			MethodHandle getType = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
			//MethodHandle get = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class));
			Class<?> screenPanelClass = Class.class.cast(getType.invoke(screenPanelField)); 
			//Object screenPanel = get.invoke(screenPanelField); 
			




			campaignUI = campaignUI;
			

        } catch (Throwable t) {
            t.printStackTrace();
        }

		Global.getSector().getUIData().getCheckedRefitTags();
	}

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {

	}

	@Override
	protected String ehm_unapplicableReason(ShipAPI ship) {
		if (ship == null) return "Ship does not exist"; 

		// if (!Global.getSector().getPlayerFleet().getFlagship().equals(ship.getFleetMember())) return "This ain't the flagship";

		return null; 
	}

	@Override
	protected String ehm_cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		// HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());

		// if (variant.getSuppressedMods().contains(automated)) return "Automated gone";

		return null;
	}
}
