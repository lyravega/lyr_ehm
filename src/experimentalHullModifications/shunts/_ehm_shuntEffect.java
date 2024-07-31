package experimentalHullModifications.shunts;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.proxies.lyr_hullSpec;

/**
 * This class serves as an effect base for all of the shunt types. Introduced to separate these functions
 * from the hullmod effect classes.
 * @author lyravega
 */
public abstract class _ehm_shuntEffect<T> {
	private final _ehm_ar_base activatorEffect;
	protected final String shuntActivatorId;
	protected final String shuntGroupTag;
	protected final String childSlotPrefix;
	protected final List<String> invalidSlotPrefixes;
	protected final Map<String, T> shuntDataMap = new HashMap<String, T>();

	protected _ehm_shuntEffect(_ehm_ar_base activatorEffect, String shuntGroupTag, String childSlotPrefix, String... invalidSlotPrefixes) {
		this.activatorEffect = activatorEffect;
		this.activatorEffect.statSet.add(shuntGroupTag);
		this.shuntActivatorId = activatorEffect.getHullModSpec().getId();
		this.shuntGroupTag = shuntGroupTag;
		this.childSlotPrefix = childSlotPrefix;
		this.invalidSlotPrefixes = Arrays.asList(invalidSlotPrefixes);
	}

	/**
	 * Adds shunt data that will be utilized during effects. This data may include something as simple
	 * as a modifier for a stat, or an object that holds child slot parameters and/or location.
	 * @param shuntId
	 * @param shuntData
	 */
	public void addShuntData(String shuntId, T shuntData) {
		this.activatorEffect.shuntIdSet.add(shuntId);
		this.shuntDataMap.put(shuntId, shuntData);
	}

	/**
	 * Checks if the slot is applicable for the shunt activation.
	 * @param slotId of the slot
	 * @return {@code true} if the slot prefix does not match any of the blocked prefixes
	 */
	protected final boolean isValidSlot(String slotId) {
		return !this.invalidSlotPrefixes.contains(slotId.substring(0,3));
	}

	/**
	 * Registers the shunt in the {@code dynamicStats} of the ship by adding and changing relevant
	 * flat modifiers such as {@code groupTag} and {@code shuntId} with {@code slotId} as their source
	 * for tracking purposes.
	 * <p> The {@code groupTag} modifiers with {@code slotId} sources are used to locate the installed
	 * shunts directly, as querying the dynamic stats for this modifier will return a map where the
	 * keyset will contain all the slot ids.
	 * <p> The value is {@code 1} for the shunts that do not provide or require any calculations that
	 * may be applied on other stats of the ship. {@code shuntId} as the modifier may be used to
	 * provide and display information on a specific shunt type rather than a shunt group.
	 * @param stats of the ship
	 * @param dynamicStats of the ship
	 * @param variant of the ship
	 * @param slot that the shunt is installed on
	 * @param slotId of the slot
	 * @param shuntId of the shunt
	 */
	public abstract void registerShunt(MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant, WeaponSlotAPI slot, String slotId, String shuntId);

	/**
	 * Processes the effects for all of the installed shunts on the ship. Such effects may be
	 * activating them if requirements are met, altering stats and/or dynamic stats of the ship and
	 * more.
	 * <p> The variant isn't altered from here, only the hull specification is. After these effects
	 * are processed, the hull specification should be applied on the variant through the activator.
	 * @param hullSpec proxy of the ship
	 * @param stats of the ship
	 * @param dynamicStats of the ship
	 * @param variant of the ship
	 */
	public abstract void processShunts(lyr_hullSpec hullSpec, MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant);

	/**
	 * Activates a shunt, turning it into a decorative piece. Default behaviour is for generic shunts
	 * that only deactivate. Generic shunts do not spawn child slots or have any data needed by them.
	 * Must be overridden by other shunts that do more.
	 * @param hullSpec proxy of the ship that will be altered
	 * @param shuntId if not {@code null}, to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot
	 */
	public void activateShunt(lyr_hullSpec hullSpec, String slotId, String shuntId) {
		if (shuntId != null) hullSpec.addBuiltInWeapon(slotId, shuntId);
		hullSpec.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
	}

	public abstract void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec);

	protected final void printShuntCountsOnTooltip(TooltipMakerAPI tooltip, ShipVariantAPI variant, Set<String> slotIdSet) {
		Map<String, Integer> shunts = new HashMap<String, Integer>();

		for (String slotId : slotIdSet) {
			String shuntId = variant.getWeaponId(slotId);
			int shuntAmount = shunts.get(shuntId) == null ? 0 : shunts.get(shuntId);

			shunts.put(shuntId, shuntAmount+1);
		}

		for (String shuntId : shunts.keySet()) {
			WeaponSpecAPI shuntSpec = Global.getSettings().getWeaponSpec(shuntId);

			tooltip.beginImageWithText(shuntSpec.getTurretSpriteName(), 16, tooltip.getWidthSoFar(), true)
				.addPara(Math.round(shunts.get(shuntId)) + "x " + shuntSpec.getWeaponName(), 0f).setAlignment(Alignment.LMID);
			tooltip.addImageWithText(text.padding);
		}
	}
}