package experimentalHullModifications.hullmods.ehm_ar;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import lyravega.listeners.events.normalEvents;
import lyravega.listeners.events.weaponEvents;


/**
 * This class is used by slot activator hullmods. Slot activators are designed
 * to search the ship for specific weapons, and perform operations on the
 * hullSpec to yield interesting results, such as creating a new weapon slot.
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_ar_base extends _ehm_base implements normalEvents, weaponEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		this.cleanWeaponGroups(stats);

		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		this.removeActivator(stats);

		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onWeaponInstalled(MutableShipStatsAPI stats, String weaponId, String slotId) {
		if (!this.shuntIdSet.contains(weaponId)) return;

		this.cleanWeaponGroups(stats);

		commitVariantChanges();
	}

	@Override
	public void onWeaponRemoved(MutableShipStatsAPI stats, String weaponId, String slotId) {
		if (!this.shuntIdSet.contains(weaponId)) return;

		commitVariantChanges();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public final Set<String> shuntIdSet = new HashSet<String>();
	public final Set<String> statSet = new HashSet<String>();

	protected void cleanWeaponGroups(MutableShipStatsAPI stats) {
		for (Iterator<WeaponGroupSpec> iterator = stats.getVariant().getWeaponGroups().iterator(); iterator.hasNext();) {
			WeaponGroupSpec weaponGroup = iterator.next();

			for (String statId : this.statSet)
				weaponGroup.getSlots().removeAll(stats.getDynamic().getMod(statId).getFlatBonuses().keySet());

			if (weaponGroup.getSlots().isEmpty()) iterator.remove();
		}
	}

	/**
	 * Restores the hull spec on the variant, undoing any changes to the slots.
	 * <p> This is a lazy method that restores the hull spec instead of unmodifying the changes.
	 * @param stats of the ship/member whose hullSpec will be restored
	 */
	public final void removeActivator(MutableShipStatsAPI stats) {
		this.refreshHullSpec(stats);
	}
}