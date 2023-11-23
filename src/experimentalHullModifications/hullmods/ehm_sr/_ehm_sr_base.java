package experimentalHullModifications.hullmods.ehm_sr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.logger.lyr_logger;

/**
 * This class is used by system retrofit hullmods. They are pretty
 * straightforward in their operation; change the system of a hullSpec.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_sr_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(ShipVariantAPI variant) {
		if (lyr_miscUtilities.removeHullModWithTag(variant, ehm_internals.hullmods.systemRetrofits.tag, this.hullModSpecId)) return;
		commitVariantChanges(); playDrillSound();
	}

	@Override
	public void onRemoved(ShipVariantAPI variant) {
		if (!lyr_miscUtilities.hasHullModWithTag(variant, ehm_internals.hullmods.systemRetrofits.tag, this.hullModSpecId))
			variant.setHullSpecAPI(ehm_systemRestore(variant));
		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		String shipSystemId = this.getClass().getSimpleName().replace(ehm_internals.hullmods.systemRetrofits.tag+"_", "");

		if (Global.getSettings().getShipSystemSpec(shipSystemId) == null) {
			hullModSpec.setHidden(true);
			hullModSpec.setHiddenEverywhere(true);

			lyr_logger.warn("Ship system with systemId '"+shipSystemId+"' not found, hiding '"+hullModSpec.getId()+"'"); return;
		}

		Description shipSystemDescription = Global.getSettings().getDescription(shipSystemId, Description.Type.SHIP_SYSTEM);

		hullModSpec.setDescriptionFormat(shipSystemDescription.getText1());
		// hullModSpec.setShortDesc(shipSystemDescription.getText3());	// this is not on the API
		try {
			lyr_reflectionUtilities.methodReflection.invokeDirect(hullModSpec, "setShortDesc", shipSystemDescription.getText3());
		} catch (Throwable t) {
			lyr_logger.error("Could not set the short description of hull modification spec", t);
		}

		super.init(hullModSpec);
	}

	/**
	 * Alters the system on a hullSpec, and returns it. The returned hullSpec needs
	 * to be installed on the variant.
	 * @param variant of the ship that will have its system replaced
	 * @param systemId of the system to be installed on the passed variant
	 * @return a new hullSpec to be installed on the variant
	 * @see {@link #ehm_systemRestore()} reverses this process
	 */
	protected static final ShipHullSpecAPI ehm_systemRetrofit(ShipVariantAPI variant, String systemId) {
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShipSystemId(systemId);

		return lyr_hullSpec.retrieve();
	}

	/**
	 * Restores a system of a hullSpec to its stock one, and returns it. Returned hullSpec
	 * needs to be installed on the variant.
	 * @param variant that will have its system reset to factory defaults
	 * @return a hullspec to be installed on the variant
	 */
	public static final ShipHullSpecAPI ehm_systemRestore(ShipVariantAPI variant) {
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShipSystemId(lyr_hullSpec.referenceNonDamaged().getShipSystemId());

		return lyr_hullSpec.retrieve();
	}
}