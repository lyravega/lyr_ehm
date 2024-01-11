package lyravega.upgrades;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

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

	/**
	 * A method that may be utilized remotely to trigger any upgrade effects and apply them on the
	 * mutable stats. The caller should be from somewhere that these effects may be applied properly.
	 * @param stats of the entity that will receive the upgrade effects
	 * @param effectTier as a convenience shortcut for the current tier. If {@code null}, the tags will be searched again
	 */
	public void applyUpgradeEffect(MutableShipStatsAPI stats);

	/**
	 * Modifies a passed tooltip. Designed to be used on hullmod tooltips in their post description.
	 * There is no standard on how the tooltip may be modified.
	 * @param stats of the entity with the upgrade
	 * @param tooltip to modify
	 * @param padding
	 */
	public void addShortDescription(MutableShipStatsAPI stats, TooltipMakerAPI tooltip, float padding);
}
