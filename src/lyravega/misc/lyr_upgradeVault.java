package lyravega.misc;

import java.util.HashMap;
import java.util.Map;

import lunalib.lunaRefit.BaseRefitButton;
import lunalib.lunaRefit.LunaRefitManager;

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
