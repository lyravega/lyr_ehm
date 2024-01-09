package experimentalHullModifications.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fs.starfarer.api.Global;

import lyravega.utilities.logger.lyr_logger;

public class ehm_lostAndFound {
	private static Map<String, Integer> recoveryStash = null;

	public static void addLostItem(String weaponId) {
		if (recoveryStash == null) recoveryStash = new HashMap<String, Integer>();

		Integer amount = recoveryStash.get(weaponId);

		recoveryStash.put(weaponId, amount == null ? 1 : amount+1);
	}

	public static void returnStuff() {
		lyr_logger.warn("Returning stashed weapons to player cargo");
		Global.getSector().getCampaignUI().addMessage("EHM (Experimental Hull Modifications): Load successful. Please save the game and disable the 'Clear Unknown Slots' debug option. Removed weapons are returned to your cargo", Color.RED);

		if (recoveryStash == null || !recoveryStash.isEmpty()) return;

		for (Entry<String, Integer> weaponEntry : recoveryStash.entrySet()) {
			Global.getSector().getPlayerFleet().getCargo().addWeapons(weaponEntry.getKey(), weaponEntry.getValue());
		}

		recoveryStash.clear(); recoveryStash = null;
	}
}
