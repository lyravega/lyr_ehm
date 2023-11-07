package lyravega.misc;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public interface _lyr_upgradeEffect {
	public String getUpgradeId();

	public String getUpgradeName();

	public int getUpgradeTier(ShipVariantAPI variant);

	public void applyUpgradeEffect(MutableShipStatsAPI stats, String tag);
}
