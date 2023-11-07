package lyravega.misc;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * An interface that needs to beimplemented by the real upgrades as it is through this their effects
 * may be applied. A few methods allow a more fields to be accessed without needing to the upgrades.
 * @author lyravega
 * @see {@link lyr_upgrade} / {@link lyr_upgradeLayer} / {@link lyr_upgradeVault}
 */
public interface _lyr_upgradeEffect {
	public String getUpgradeId();

	public String getUpgradeName();

	public int getUpgradeTier(ShipVariantAPI variant);

	public void applyUpgradeEffect(MutableShipStatsAPI stats, String tag);
}
