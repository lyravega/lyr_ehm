package experimentalHullModifications.shunts;

import java.util.HashMap;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;

import experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_launchtube;
import experimentalHullModifications.misc.ehm_internals.affixes;
import experimentalHullModifications.misc.ehm_internals.shunts.hangars;
import experimentalHullModifications.misc.ehm_tooltip.header;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;

/**
 * This shunt effect class serves as a companion object for its own activator retrofit.
 * See {@link ehm_ar_launchtube} hullmod effect class where these will be utilized.
 * @category Shunt Effect
 * @author lyravega
 */
public final class ehm_hangarEffect extends _ehm_shuntEffect<float[][]> {
	public ehm_hangarEffect(_ehm_ar_base activatorEffect) {
		super(activatorEffect, hangars.groupTag, affixes.launchSlot, affixes.convertedSlot);
	}

	@Override
	public void registerShunt(MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant, WeaponSlotAPI slot, String slotId, String shuntId) {
		if (!variant.hasHullMod(this.shuntActivatorId)) return;
		if (!this.isValidSlot(slotId)) return;

		dynamicStats.getMod(shuntId).modifyFlat(slotId, 1);
		dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, 1);
	}

	@Override
	public void processShunts(lyr_hullSpec hullSpec, MutableShipStatsAPI stats, DynamicStatsAPI dynamicStats, ShipVariantAPI variant) {
		HashMap<String, StatMod> hangarShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!hangarShunts.isEmpty()) {
			for (String slotId : hangarShunts.keySet()) {
				if (hullSpec.getWeaponSlot(slotId).getWeaponType() == WeaponType.DECORATIVE) continue;	// parent slot turns into decorative, spawns a child launch bay
				String shuntId = variant.getWeaponId(slotId);

				dynamicStats.getMod(this.shuntGroupTag).modifyFlat(slotId, 1);
				this.activateShunt(hullSpec, slotId, shuntId);
			}

			float hangarMod = hangarShunts.size();	// hangars always give 1 bonus since there is only one large type, so use size

			stats.getNumFighterBays().modifyFlat(this.shuntActivatorId, hangarMod);
		}
	}

	/**
	 * Activates the hangar shunt on the slot. The parent slot is turned into a built-in decorative
	 * in the process. The data that is populated through the activator will be utilized as child
	 * slot parameters.
	 * <p> The other shunts that spawn child slots have their children inherit their original weapon
	 * type. For hangar slots however, the slot type needs to be launch bay as otherwise the game
	 * will not know where to launch the wings from.
	 * <p> While one slot is enough to both activate and launch the wings, it will show up on the
	 * weapon groups as the slot will not be empty. Leaving the parent slot as a decorative while
	 * spawning a child slot with launch points and a launch bay type solves all issues.
	 * @param hullSpec proxy of the ship that will be altered
	 * @param shuntId to determine the shunt type and to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot while deriving info for child
	 */
	@Override
	public final void activateShunt(lyr_hullSpec hullSpec, String slotId, String shuntId) {
		float[][] launchPoints = this.shuntDataMap.get(shuntId);
		lyr_weaponSlot parentSlot = hullSpec.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = this.childSlotPrefix + slotId; // also used as nodeId

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, new Vector2f(parentSlot.getLocation()));
		childSlot.addLaunchPoints(null, launchPoints);
		childSlot.setWeaponType(WeaponType.LAUNCH_BAY);

		hullSpec.addWeaponSlot(childSlot);

		hullSpec.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.lunaSettings.getHideHangars()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);
	}

	@Override
	public void addShuntInfoToActivatorDescription(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		ShipVariantAPI variant = ship.getVariant();
		DynamicStatsAPI dynamicStats = ship.getMutableStats().getDynamic();
		HashMap<String, StatMod> hangarShunts = dynamicStats.getMod(this.shuntGroupTag).getFlatBonuses();

		if (!hangarShunts.isEmpty()) {
			tooltip.addSectionHeading("EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			this.printShuntCountsOnTooltip(tooltip, variant, hangarShunts.keySet());
		} else if (lyr_ehm.lunaSettings.getShowFullInfoForActivators()) {
			tooltip.addSectionHeading("NO EXTRA HANGARS", header.info_textColour, header.invisible_bgColour, Alignment.MID, header.padding);
			tooltip.addPara("No large weapon slots are turned into hangars. Each large slot is turned into a single fighter bay with a launch tube.", text.padding);
		}
	}
}