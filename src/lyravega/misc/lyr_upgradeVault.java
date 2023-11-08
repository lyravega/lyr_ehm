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

	/**
	 * Retrieves a registered upgrade, which allows its effects to be applied remotely.
	 * @param upgradeId to retrieve
	 * @return an object whose class extends {@link BaseRefitButton} and implements {@link _lyr_upgradeEffect} which latter also represents
	 */
	public static _lyr_upgradeEffect getUpgrade(String upgradeId) {
		return lyr_upgradeVault.upgrades.get(upgradeId);
	}

	/**
	 * All upgrades use and are bundled together with LunaLib's refit buttons. This method adds these
	 * upgrades to LunaLib's refit manager. Additionally, they are saved in a map for ease of access,
	 * with an interface that allows it to have their effects applied remotely.
	 * @param upgrade to register with the LunaLib
	 */
	public static void registerUpgrade(_lyr_upgradeEffect upgrade) {
		LunaRefitManager.addRefitButton(BaseRefitButton.class.cast(upgrade));
		lyr_upgradeVault.upgrades.put(upgrade.getUpgradeId(), upgrade);
	}
}
