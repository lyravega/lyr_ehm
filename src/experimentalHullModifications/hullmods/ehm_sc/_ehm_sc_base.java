package experimentalHullModifications.hullmods.ehm_sc;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.awt.Color;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_shieldSpec;
import lyravega.utilities.lyr_miscUtilities;

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
	public void onInstalled(ShipVariantAPI variant) {
		if (lyr_miscUtilities.removeHullModWithTag(variant, ehm_internals.tags.shieldCosmetic, this.hullModSpecId)) return;
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		if (!lyr_miscUtilities.hasHullModWithTag(variant, ehm_internals.tags.shieldCosmetic, this.hullModSpecId))
			variant.setHullSpecAPI(ehm_restoreShield(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	/**
	 * Alters the shield colours of the ship. Inner and ring colours
	 * can be different.
	 * @param variant whose shieldSpec will be altered
	 * @return an altered hullSpec with altered shieldSpec colours
	 */
	protected static final ShipHullSpecAPI ehm_applyShieldCosmetics(ShipVariantAPI variant, Color inner, Color ring) {
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		lyr_shieldSpec shieldSpec = lyr_hullSpec.getShieldSpec();

		shieldSpec.setInnerColor(inner);
		shieldSpec.setRingColor(ring);

		return lyr_hullSpec.retrieve();
	}

	/**
	 * Restores the shieldSpec of the passed variant's hullSpec by
	 * referring to a stock one.
	 * @param variant whose shieldSpec will be restored
	 * @return an altered hullSpec with its shieldSpec is restored
	 */
	public static final ShipHullSpecAPI ehm_restoreShield(ShipVariantAPI variant) {
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShieldSpec(lyr_hullSpec.referenceNonDamaged().getShieldSpec());

		return lyr_hullSpec.retrieve();
	}
}