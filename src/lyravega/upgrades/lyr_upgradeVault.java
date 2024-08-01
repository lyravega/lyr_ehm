package lyravega.upgrades;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;

/**
 * A class that is dedicated to house all of the upgrades in a single place, and to provide a method
 * to register, and retrieve them. Main reason of its existence is maintability.
 * <p> All of the real, usable upgrades implement an interface {@link lyr_upgradeEffect} which
 * allows their effects to be called from elsewhere after it is retrieved from here.
 * @author lyravega
 * @see {@link lyr_upgrade} / {@link lyr_tierSpec} / {@link lyr_upgradeEffect}
 */
public final class lyr_upgradeVault {
	public final static class ids {
		public final static String
			modId = "lyr_upgrader",
			statId = "lyr_upgrades",
			prefix = "upgrade";
	}

	private static final Map<String, lyr_upgrade> upgrades = new HashMap<String, lyr_upgrade>();

	/**
	 * Retrieves a registered upgrade, which allows its effects to be applied remotely.
	 * @param upgradeId to retrieve
	 * @return an object whose class extends {@link BaseRefitButton} and implements {@link lyr_upgradeEffect} which latter also represents
	 */
	public static lyr_upgradeEffect getUpgrade(String upgradeId) {
		return upgrades.get(upgradeId);
	}

	/**
	 * All upgrades use and are bundled together with LunaLib's refit buttons. This method adds these
	 * upgrades to LunaLib's refit manager. Additionally, they are saved in a map for ease of access,
	 * with an interface that allows it to have their effects applied remotely.
	 * @param upgrade to register with the LunaLib
	 */
	public static void registerUpgrade(lyr_upgrade upgrade) {
		LunaRefitManager.addRefitButton(BaseRefitButton.class.cast(upgrade));
		upgrades.put(upgrade.getUpgradeId(), upgrade);
	}

	// TODO: clean-up
	public static class lyr_upgrader extends BaseHullMod {
		@Override
		public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
			for (String tag : stats.getVariant().getTags()) {
				if (!tag.startsWith(ids.prefix)) continue;

				final String[] splitTag = tag.split(":",2);
				final lyr_upgradeEffect upgrade = upgrades.get(splitTag[0]); if (upgrade == null) continue;

				stats.getDynamic().getMod(ids.statId).modifyFlat(splitTag[0], Float.valueOf(splitTag[1]));
				upgrade.applyUpgradeEffect(stats);
			}
		}
	}
}
