package experimentalHullModifications.proxies;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;

import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_diverterandconverter.converterData.converterParameters;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_launchtube.hangarData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterData;
import experimentalHullModifications.hullmods.ehm_ar.ehm_ar_stepdownadapter.adapterData.adapterParameters;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.hullmods;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.misc.ehm_tooltip.text;
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

			if (ehm_settings.getShowExperimentalFlavour()) {
				this.setManufacturer(text.flavourManufacturer);
				this.setDescriptionPrefix(text.flavourDescription);
				this.setHullName(oHullSpec.getHullName() + " (E)");	// append "(E)"
			}

			this.addBuiltInMod(hullmods.main.base);
		}
	}

	public final void adaptSlot(String shuntId, String slotId) {
		adapterParameters childrenParameters = adapterData.dataMap.get(shuntId);
		lyr_weaponSlot parentSlot = this.getWeaponSlot(slotId);

		for (String childId: childrenParameters.getChildren()) { // childId and childSlotId are not the same, be aware
			lyr_weaponSlot childSlot = parentSlot.clone();
			String childSlotId = ehm_internals.affixes.adaptedSlot + slotId + childId; // also used as nodeId
			Vector2f childSlotLocation = lyr_vectorUtilities.calculateChildSlotLocation(parentSlot.retrieve(), childrenParameters.getChildOffset(childId));
			WeaponSize childSlotSize = childrenParameters.getChildSize(childId);

			childSlot.setId(childSlotId);
			childSlot.setNode(childSlotId, childSlotLocation);
			childSlot.setSlotSize(childSlotSize);

		 	this.addWeaponSlot(childSlot);
		}

		this.addBuiltInWeapon(slotId, shuntId);
		parentSlot.setWeaponType(WeaponType.DECORATIVE);
		if (ehm_settings.getHideAdapters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);	// sometimes the activated shunts (decoratives) on these new slots (especially hardpoint ones) are rendered below the adapter, hence the change
	}

	public final void convertSlot(String shuntId, String slotId) {
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
		if (ehm_settings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);	// sometimes the activated shunts (decoratives) on these new slots (especially hardpoint ones) are rendered below the adapter, hence the change
	}

	public final void turnSlotIntoBay(String shuntId, String slotId) {
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
		if (ehm_settings.getHideConverters()) parentSlot.setSlotType(slotTypeConstants.hidden);
		else parentSlot.setRenderOrderMod(-1f);
	}

	public final void deactivateSlot(String shuntId, String slotId) {
		if (shuntId != null) this.addBuiltInWeapon(slotId, shuntId);
		this.getWeaponSlot(slotId).setWeaponType(WeaponType.DECORATIVE);
	}
}
