package lyravega.misc;

import java.util.HashMap;
import java.util.Map;

import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;

/**
 * A class that is dedicated to house all of the upgrades in a single place, and to provide a method
 * to register, and retrieve them. Main reason of its existence is maintability.
 * <p> All of the real, usable upgrades implement an interface {@link _lyr_upgradeEffect} which
 * allows their effects to be called from elsewhere after it is retrieved from here.
 * @author lyravega
 * @see {@link lyr_upgrade} / {@link lyr_upgradeLayer} / {@link _lyr_upgradeEffect}
 */
public class lyr_upgradeVault {
	private static final Map<String, _lyr_upgradeEffect> upgrades = new HashMap<String, _lyr_upgradeEffect>();

	public static _lyr_upgradeEffect getUpgrade(String upgradeId) {
		return lyr_upgradeVault.upgrades.get(upgradeId);
	}

	public static void registerUpgrade(_lyr_upgradeEffect upgrade) {
		LunaRefitManager.addRefitButton(BaseRefitButton.class.cast(upgrade));
		lyr_upgradeVault.upgrades.put(upgrade.getUpgradeId(), upgrade);
	}
}
