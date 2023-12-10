package experimentalHullModifications.hullmods.ehm_sc;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.awt.Color;
import java.util.Set;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals.hullmods.shieldCosmetics;
import lyravega.listeners.events.customizableMod;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_shieldSpec;

/**
 * This class is used by shield cosmetic hullmods. The changes are
 * permanent, and does not use {@code advanceInCombat()}.
 * </p> Reason to split this as another base was primarily maintenance.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @author lyravega
 */
public abstract class _ehm_sc_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		Set<String> modGroup = this.getModsFromSameGroup(stats);

		if (modGroup.size() > 1) stats.getVariant().removeMod(modGroup.iterator().next());

		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		Set<String> modGroup = this.getModsFromSameGroup(stats);

		if (modGroup.isEmpty()) this.restoreShields(stats);

		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public _ehm_sc_base() {
		super();

		this.extendedData.groupTag = shieldCosmetics.tag;
	}

	protected Color innerColour;
	protected Color ringColour;

	/**
	 * A setter method that sets the relevant fields for the non-customizable methods. The customizable
	 * ones actually implement the {@link customizableMod} interface, but having a common method name
	 * for both makes it easier to work with them through the base.
	 */
	public abstract void applyCustomization();

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.applyCustomization();
	}

	/**
	 * Alters the shield colours of the shield spec. Uses the stored internal {@link #innerColour}
	 * and {@link #ringColour}.
	 * @param stats of the ship/member whose shieldSpec will be altered
	 */
	protected final void changeShields(MutableShipStatsAPI stats) {
		this.registerModInGroup(stats);

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		lyr_shieldSpec shieldSpec = lyr_hullSpec.getShieldSpec();

		shieldSpec.setInnerColor(this.innerColour);
		shieldSpec.setRingColor(this.ringColour);

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	/**
	 * Restores the shieldSpec of the passed variant's hullSpec by
	 * referring to a stock one.
	 * @param stats of the ship/member whose shieldSpec will be restored
	 * @return an altered hullSpec with its shieldSpec is restored
	 */
	protected final void restoreShields(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShieldSpec(lyr_hullSpec.referenceNonDamaged().getShieldSpec());

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}
}