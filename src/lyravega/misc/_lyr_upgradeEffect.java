package lyravega.misc;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;

public interface _lyr_upgradeEffect {
	public String getUpgradeId();

	public void applyUpgradeEffect(MutableShipStatsAPI stats, String tag);
}
