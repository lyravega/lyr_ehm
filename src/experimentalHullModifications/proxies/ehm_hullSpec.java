package experimentalHullModifications.proxies;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterData.converterParameters;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_launchtube.hangarData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterData.adapterParameters;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_tooltip.text;
import experimentalHullModifications.plugin.lyr_ehm;
import lyravega.proxies.lyr_hullSpec;
import lyravega.proxies.lyr_weaponSlot;
import lyravega.proxies.lyr_weaponSlot.slotTypeConstants;
import lyravega.utilities.lyr_vectorUtilities;

/**
 * A derivation of the proxy-like {@link lyr_hullSpec} class that serves more specific needs, whereas
 * the parent serves the general ones.
 * @author lyravega
 */
public final class ehm_hullSpec extends lyr_hullSpec {
	/**
	 * A specialized constructor that clones the hull spec when necessary or forced. Uses damaged
	 * hull specs instead of normal ones as the game swaps them in two places. The alterations that
	 * the game does on these damaged hull specs are reversed during cloning.
	 * <p> The reason for using damaged hull specs is, the game swaps them in two cases, which may
	 * result in slot not found errors. One instance of this error is avoided by using these instead,
	 * while the other requires a script replacement.
	 * <p> The script in question is linked to the 'Hull Restoration' skill that repairs the d-mods
	 * over time. When the ship is fully repaired of those, the damaged hull spec is swapped. Script:
	 * {@link com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript#restoreToNonDHull
	 * FieldRepairsScript}
	 * <p> The other swap happens when a ship gets destroyed; when it receives d-mods, its hull is
	 * also swapped unless it already has a damaged hull spec. The method in question: {@link
	 * com.fs.starfarer.api.impl.campaign.DModManager#setDHull DModManager}
	 * @param hullSpec to be proxied
	 * @param forceClone ignores detection and forces cloning, necessary for restorations
	*/
	public ehm_hullSpec(ShipHullSpecAPI hullSpec, boolean forceClone) {
		super(hullSpec);

		ShipHullSpecAPI dHullSpec = this.referenceDamaged();	// damaged hull spec
		ShipHullSpecAPI oHullSpec = this.referenceNonDamaged();	// original hull spec

		if (forceClone || dHullSpec == hullSpec || oHullSpec == hullSpec) {
			this.hullSpec = this.duplicate(dHullSpec);	// should be absolutely first here

			for (String hullSpecTag : oHullSpec.getTags()) // this is a set, so there cannot be any duplicates, but still
				if (!this.getTags().contains(hullSpecTag))
					this.addTag(hullSpecTag);

			// for (String builtInHullModSpecId : oHullSpec.getBuiltInMods()) // this is a list, there can be duplicates so check first
			// 	if (!this.isBuiltInMod(builtInHullModSpecId))
			// 		this.addBuiltInMod(builtInHullModSpecId);

			this.setDParentHullId(null);
			this.setBaseHullId(oHullSpec.isRestoreToBase() ? oHullSpec.getBaseHullId()+Misc.D_HULL_SUFFIX : null);	// the check ensures ships like 'wolf_d' may be restored to 'wolf'
			this.setDescriptionPrefix(oHullSpec.getDescriptionPrefix());	// remove damaged description prefix
			this.setHullName(oHullSpec.getHullName());	// restore the name to get rid of "(D)"
			this.setBaseValue(oHullSpec.getBaseValue());	// restore the value as damaged hulls lose 25% in value
			// this.setSpriteSpec(test.getSpriteSpec());	// maybe reduces memory imprint?

			if (lyr_ehm.lunaSettings.getShowExperimentalFlavour()) {
				this.setManufacturer(text.flavourManufacturer);
				this.setDescriptionPrefix(text.flavourDescription);
				this.setHullName(oHullSpec.getHullName() + " (E)");	// append "(E)"
			}
		}
	}

	/**
	 * Activates the adapter shunts on the slot. The {@link adapterData} object contains children data
	 * that is used to alter the spawned slots. The parent slot is turned into a built-in decorative
	 * in the process.
	 * @param shuntId to determine the shunt type and to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot while deriving info for children
	 */
	public final void activateAdapterShunt(String shuntId, String slotId) {
		adapterParameters childrenParameters = adapterData.dataMap.get(shuntId);
		lyr_weaponSlot parentSlot = this.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = ehm_internals.affixes.adaptedSlot + slotId + childId; // also used as nodeId
			Vector2f childSlotLocation = lyr_vectorUtilities.calculateRelativePoint(parentSlot.getLocation(), parentSlot.getAngle(), childrenParameters.getChildOffset(childId));
			WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

		 	this.addWeaponSlot(childSlot);
		}

		this.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.lunaSettings.getHideAdapters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);	// sometimes the activated shunts (decoratives) on these new slots (especially hardpoint ones) are rendered below the adapter, hence the change
	}

	/**
	 * Activates the converter shunts on the slot. The {@link converterData} object contains child data
	 * that is used to alter the spawned slot. The parent slot is turned into a built-in decorative
	 * in the process.
	 * @param shuntId to determine the shunt type and to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot while deriving info for child
	 */
	public final void activateConverterShunt(String shuntId, String slotId) {
		converterParameters childParameters = converterData.dataMap.get(shuntId);
		lyr_weaponSlot parentSlot = this.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = ehm_internals.affixes.convertedSlot + slotId + childParameters.getChildSuffix(); // also used as nodeId

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, parentSlot.getLocation());
		childSlot.setSlotSize(childParameters.getChildSize());

		this.addWeaponSlot(childSlot);

		this.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.lunaSettings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);
	}

	/**
	 * Activates the hangar shunt on the slot. The {@link hangarData} object contains child data
	 * that is used to alter the spawned slot. The parent slot is turned into a built-in decorative
	 * in the process.
	 * <p> The other shunts that spawn child slots have their children inherit their original weapon
	 * type. For hangar slots however, the slot type needs to be launch bay as otherwise the game
	 * will not know where to launch the wings from.
	 * <p> While one slot is enough to both activate and launch the wings, it will show up on the
	 * weapon groups as the slot will not be empty. Leaving the parent slot as a decorative while
	 * spawning a child slot with launch points and a launch bay type solves all issues.
	 * @param shuntId to determine the shunt type and to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot while deriving info for child
	 */
	public final void activateHangarShunt(String shuntId, String slotId) {
		float[][] launchPoints = hangarData.dataMap.get(shuntId);
		lyr_weaponSlot parentSlot = this.getWeaponSlot(slotId);

		lyr_weaponSlot childSlot = parentSlot.clone();
		String childSlotId = ehm_internals.affixes.launchSlot + slotId; // also used as nodeId

		childSlot.setId(childSlotId);
		childSlot.setNode(childSlotId, new Vector2f(parentSlot.getLocation()));
		childSlot.addLaunchPoints(null, launchPoints);
		childSlot.setWeaponType(WeaponType.LAUNCH_BAY);

		this.addWeaponSlot(childSlot);

		this.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (lyr_ehm.lunaSettings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);	// TODO: add an option to hide the hangars
		else parentSlot.setRenderOrderMod(-1f);
	}

	/**
	 * Activates a generic shunt on the slot. Generic shunts do not spawn child slots or have any
	 * data needed by them; this method simply turns them into a(n optionally built-in) decorative.
	 * @param shuntId if not {@code null}, to add the weapon as a built-in
	 * @param slotId to get and alter the parent slot
	 */
	public final void activateGenericShunt(String shuntId, String slotId) {
		if (shuntId != null) this.addBuiltInWeapon(slotId, shuntId);
		this.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
	}

	/**
	 * Uses a dynamic stat ({@link ehm_internals.stats#ordnancePoints ehm_ordnancePoints}) to alter
	 * the hull spec's ordnance points. Will set it to zero if modded total is negative.
	 * <p> Usage of the dynamic stat is like any other dynamic stat, flat/percentage/multipliers may
	 * be used; {@code stats.getDynamic().getMod(ehm_internals.stats.ordnancePoints)}
	 * <p> Base ordnance points is calculated with the captain's stats; if the captain has any
	 * modifiers for the {@code personStats.getShipOrdnancePointBonus()}, that will affect the base.
	 * @param stats to derive the dynamic ordnance point stat mod from
	 */
	public final void modOrdnancePoints(MutableShipStatsAPI stats) {
		FleetMemberAPI member = stats.getFleetMember();
		MutableCharacterStatsAPI captainStats = (member != null && !member.getCaptain().isDefault()) ? member.getCaptain().getStats() : null;
		int ordnancePoints = Math.round(stats.getDynamic().getMod(ehm_internals.stats.ordnancePoints).computeEffective(this.referenceNonDamaged().getOrdnancePoints(captainStats)));

		this.setOrdnancePoints(Math.max(0, ordnancePoints));
	}
}
