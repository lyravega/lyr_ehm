package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_internals.hullmods.engineCosmetics;
import lyravega.listeners.events.customizableMod;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_engineBuilder;
import lyravega.proxies.lyr_hullSpec;
import lyravega.utilities.lyr_lunaUtilities;

/**
 * This class is used by engine cosmetic hullmods. The changes are permanent; changes do not use
 * {@code advanceInCombat()}.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_ec_base extends _ehm_base implements normalEvents {
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

		if (modGroup.isEmpty()) this.restoreEngines(stats);

		commitVariantChanges(); playDrillSound();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	public _ehm_ec_base() {
		super();

		this.extendedData.groupTag = engineCosmetics.tag;
	}

	protected int engineStyleId;
	protected Object engineStyleSpec;

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
	 * Alters the engine visuals of the ship. Uses the stored internal {@link #engineStyleId} and
	 * {@link #engineStyleSpec}.
	 * @param variant whose hullSpec will be altered
	 */
	protected final void changeEngines(MutableShipStatsAPI stats) {
		stats.getDynamic().getMod(engineCosmetics.tag).modifyFlat(this.hullModSpecId, 1);

		ShipVariantAPI variant = stats.getVariant();
		lyr_hullSpec lyr_hullSpec = new lyr_hullSpec(false, variant.getHullSpec());
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		if (this.engineStyleSpec != null) for (Object temp : lyr_hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyleId(this.engineStyleId);
			engineSlot.setEngineStyleSpec(this.engineStyleSpec);
		} else for (Object temp : lyr_hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyleId(this.engineStyleId);
		}

		variant.setHullSpecAPI(lyr_hullSpec.retrieve());
	}

	/**
	 * Restores the engine visuals of the ship by applying a stock hullSpec on the variant.
	 * <p> This is a lazy method that restores the hull spec instead of unmodifying the changes.
	 * @param variant whose hullSpec will be restored
	 */
	protected final void restoreEngines(MutableShipStatsAPI stats) {
		this.restoreHullSpec(stats.getVariant());	// TODO: convert this to stats as well?
	}

	/**
	 * Creates a new engine data from the current settings set through LunaLib's menu. The data
	 * is converted into an JSON object and passed to the static method in engine style builder
	 * {@link lyravega.proxies.lyr_engineBuilder#newEngineStyleSpec(String, JSONObject)}, which
	 * generates a usable engine data object, stores it in a map while returning it.
	 * <p> LunaLib setting id's are extremely important and must match what this method assumes
	 * and expects, otherwise partial data may be missing at best, or may not work as expected
	 * at worst. The hull modification class names are utilized as the id, and are expected.
	 * @param customEngineStyleSpecName to be used as the id and as a setting prefix
	 * @return engine style spec object
	 */
	protected static Object newCustomEngineStyleSpec(String customEngineStyleSpecName) {
		final Map<String, Object> customEngineSpecData = new HashMap<String, Object>();
		final String modId = ehm_internals.ids.mod;

		customEngineSpecData.put("engineColor", lyr_lunaUtilities.getLunaRGBAColourArray(modId, customEngineStyleSpecName+"_engine"));
		customEngineSpecData.put("contrailColor", lyr_lunaUtilities.getLunaRGBAColourArray(modId, customEngineStyleSpecName+"_contrail"));
		if (lyr_lunaUtilities.getBoolean(modId, customEngineStyleSpecName+"_hasDifferentCampaignEngine")) {
			customEngineSpecData.put("engineCampaignColor", lyr_lunaUtilities.getLunaRGBAColourArray(modId, customEngineStyleSpecName+"_engineCampaign"));
		}
		if (lyr_lunaUtilities.getBoolean(modId, customEngineStyleSpecName+"_hasDifferentCampaignContrail")) {
			customEngineSpecData.put("contrailCampaignColor", lyr_lunaUtilities.getLunaRGBAColourArray(modId, customEngineStyleSpecName+"_contrailCampaign"));
		}
		customEngineSpecData.put("glowSizeMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_glowSizeMult"));
		if (lyr_lunaUtilities.getBoolean(modId, customEngineStyleSpecName+"_hasAlternateGlow")) {
			customEngineSpecData.put("glowAlternateColor", lyr_lunaUtilities.getLunaRGBAColourArray(modId, customEngineStyleSpecName+"_glowAlternate"));
		}
		customEngineSpecData.put("contrailMaxSpeedMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailMaxSpeedMult"));
		customEngineSpecData.put("contrailAngularVelocityMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailAngularVelocityMult"));
		switch (lyr_lunaUtilities.getString(modId, customEngineStyleSpecName+"_mode")) {
			case "Particles": default: {
				customEngineSpecData.put("mode", "PARTICLES");
				customEngineSpecData.put("contrailParticleDuration", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailParticleDuration"));
				customEngineSpecData.put("contrailParticleSizeMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailParticleSizeMult"));
				customEngineSpecData.put("contrailParticleFinalSizeMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailParticleFinalSizeMult"));
			} break;
			case "Plasma": {
				customEngineSpecData.put("mode", "QUAD_STRIP");
				customEngineSpecData.put("contrailDuration", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailDuration"));
				customEngineSpecData.put("contrailMinSeg", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailMinSeg"));
				customEngineSpecData.put("contrailSpawnDistMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailSpawnDistMult"));
				customEngineSpecData.put("contrailWidthMult", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailWidthMult"));
				customEngineSpecData.put("contrailWidthAddedFractionAtEnd", lyr_lunaUtilities.getDouble(modId, customEngineStyleSpecName+"_contrailWidthAddedFractionAtEnd"));
			} break;
			case "Disabled": {
				customEngineSpecData.put("mode", "NONE");
			} break;
		}
		switch (lyr_lunaUtilities.getString(modId, customEngineStyleSpecName+"_type")) {
			case "Additive": customEngineSpecData.put("type", "GLOW"); break;
			case "Regular": customEngineSpecData.put("type", "SMOKE"); break;
		}
		customEngineSpecData.put("omegaMode", lyr_lunaUtilities.getBoolean(modId, customEngineStyleSpecName+"_omegaMode"));
		switch (lyr_lunaUtilities.getString(modId, customEngineStyleSpecName+"_glowSprite")) {
			case "I": customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32.png"); break;
			case "II": customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32b.png"); break;
			case "III": customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32s.png"); break;
			case "Default": default: customEngineSpecData.put("glowSprite", ""); break;
		}
		switch (lyr_lunaUtilities.getString(modId, customEngineStyleSpecName+"_glowOutline")) {
			case "I": customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32.png"); break;
			case "II": customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32b.png"); break;
			case "III": customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32-orig.png"); break;	// causes a NPE, nothing loads this so "ehm_test" does it
			case "Default": default: customEngineSpecData.put("glowOutline", ""); break;
		}

		return lyr_engineBuilder.newEngineStyleSpec(customEngineStyleSpecName, new JSONObject(customEngineSpecData));
	}
}