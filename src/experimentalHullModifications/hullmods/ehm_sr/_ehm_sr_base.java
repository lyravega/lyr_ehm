package experimentalHullModifications.hullmods.ehm_sr;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals.hullmods.systemRetrofits;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_reflectionUtilities;
import lyravega.utilities.logger.lyr_logger;

/**
 * This class is used by system retrofit hullmods. They are pretty straightforward in their operation;
 * change the system of a hullSpec.
 * <p> The class name is important as the system id is extracted from the simple class name.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_ec._ehm_ec_base _ehm_ec_base} for engine cosmetic base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_sr_base extends _ehm_base implements normalEvents {
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

		if (modGroup.isEmpty()) this.restoreSystem(stats);

		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public _ehm_sr_base() {
		super();

		this.extendedData.groupTag = systemRetrofits.tag;
	}

	protected String systemId;

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.systemId = this.getClass().getSimpleName().replace(systemRetrofits.tag+"_", "");

		if (Global.getSettings().getShipSystemSpec(this.systemId) == null) {
			hullModSpec.setHidden(true);
			hullModSpec.setHiddenEverywhere(true);

			lyr_logger.warn("Ship system with systemId '"+this.systemId+"' not found, hiding '"+hullModSpec.getId()+"'"); return;
		}

		Description shipSystemDescription = Global.getSettings().getDescription(this.systemId, Description.Type.SHIP_SYSTEM);

		hullModSpec.setDescriptionFormat(shipSystemDescription.getText1());
		// hullModSpec.setShortDesc(shipSystemDescription.getText3());	// this is not on the API
		try {
			lyr_reflectionUtilities.methodReflection.invokeDirect(hullModSpec, "setShortDesc", shipSystemDescription.getText3());
		} catch (Throwable t) {
			lyr_logger.error("Could not set the short description of hull modification spec", t);
		}
	}

	/**
	 * Alters the system on a hullSpec and applies it on the variant. Uses the stored {@link
	 * #systemId} that is derived from the class name; system retrofit class names are based on
	 * system ids, they simply have a prefix.
	 * @param stats of the ship/member that will have its system replaced
	 */
	protected final void changeSystem(MutableShipStatsAPI stats) {
		this.registerModInGroup(stats);

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShipSystemId(this.systemId);

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	/**
	 * Restores a system of a hullSpec to its stock one, and applies it on the variant.
	 * @param variant that will have its system reset to factory defaults
	 */
	protected final void restoreSystem(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShipSystemId(lyr_hullSpec.referenceNonDamaged().getShipSystemId());

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	/**
	 * Alters the defense system on a hullSpec and applies it on the variant. Uses the stored {@link
	 * #systemId} that is derived from the class name; system retrofit class names are based on
	 * system ids, they simply have a prefix.
	 * @param stats of the ship/member that will have its system replaced
	 * @deprecated The AI will not understand how to use these things
	 */
	@Deprecated
	protected final void changeDefense(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShipDefenseId(this.systemId);
		lyr_hullSpec.getShieldSpec().setType(ShieldType.PHASE);

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	/**
	 * Restores a defense system of a hullSpec to its stock one, and applies it on the variant.
	 * @param variant that will have its system reset to factory defaults
	 * @deprecated The AI will not understand how to use these things
	 */
	@Deprecated
	protected final void restoreDefense(MutableShipStatsAPI stats) {
		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());

		lyr_hullSpec.setShipDefenseId(lyr_hullSpec.referenceNonDamaged().getShipDefenseId());
		lyr_hullSpec.getShieldSpec().setType(lyr_hullSpec.referenceNonDamaged().getShieldType());

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}
}