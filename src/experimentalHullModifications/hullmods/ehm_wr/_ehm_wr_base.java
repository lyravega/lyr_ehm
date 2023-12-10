package experimentalHullModifications.hullmods.ehm_wr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.EnumMap;
import java.util.Set;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals.hullmods.weaponRetrofits;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;

/**
 * This class is used by weapon retrofit hullmods. They are pretty
 * straightforward in their operation; change all of the weapon slots
 * on a ship to a different type.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_wr_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		Set<String> modGroup = this.getModsFromSameGroup(stats);

		if (modGroup.size() > 1) stats.getVariant().removeMod(modGroup.iterator().next());

		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		this.restoreWeaponTypes(stats);	// unlike the other mutually exclusive mods, this needs to happen without a check here otherwise type conversion may target altered types

		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	protected EnumMap<WeaponType, WeaponType> typeConversionMap = new EnumMap<WeaponType, WeaponType>(WeaponType.class);
	protected WeaponSize applicableSlotSize = null;

	public _ehm_wr_base() {
		super();

		this.extendedData.groupTag = weaponRetrofits.tag;
	}

	public abstract void updateData();

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.updateData();
	}

	/**
	 * Alters the weapon slots on the ship. Uses the stored internal {@link #applicableSlotSize} and
	 * {@link #typeConversionMap}.
	 * @param variant whose hullSpec will be altered
	 * @param conversions is a map that pairs slot types
	 * @param slotSize of the applicable slots, all sizes if {@code null}
	 * @see {@link #ehm_weaponSlotRestore()} reverses this process one slot at a time
	 */
	protected final void changeWeaponTypes(MutableShipStatsAPI stats) {
		this.registerModInGroup(stats);

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		for (WeaponSlotAPI slot: lyr_hullSpec.getAllWeaponSlotsCopy()) {
			if (this.applicableSlotSize != null && slot.getSlotSize() != this.applicableSlotSize) continue;

			String slotId = slot.getId();
			WeaponType convertFrom = slot.getWeaponType();

			if (this.typeConversionMap.containsKey(convertFrom)) {
				WeaponType convertTo = this.typeConversionMap.get(convertFrom);
				lyr_hullSpec.getWeaponSlot(slotId).setWeaponType(convertTo);
			}
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	/**
	 * Restores the weapon slot types of the ship by applying a stock hullSpec on the variant.
	 * <p> This is a lazy method that restores the hull spec instead of unmodifying the changes.
	 * @param stats of the ship/member whose hullSpec will be restored
	 */
	protected final void restoreWeaponTypes(MutableShipStatsAPI stats) {
		this.restoreHullSpec(stats.getVariant());
	}
}