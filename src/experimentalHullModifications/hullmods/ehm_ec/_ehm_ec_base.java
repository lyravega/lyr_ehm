package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;
import static lyravega.utilities.lyr_interfaceUtilities.refreshPlayerFleetView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.ehm_internals.hullmods.engineCosmetics;
import experimentalHullModifications.plugin.lyr_ehm;
import experimentalHullModifications.proxies.ehm_hullSpec;
import lyravega.listeners.events.customizableMod;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_engineBuilder;

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

		commitVariantChanges(); playDrillSound(); refreshPlayerFleetView(false);
	}

	@Override
	public void onRemoved(MutableShipStatsAPI stats) {
		Set<String> modGroup = this.getModsFromSameGroup(stats);

		if (modGroup.isEmpty()) this.restoreEngines(stats);

		commitVariantChanges(); playDrillSound(); refreshPlayerFleetView(false);
	}
	//#endregion
	// END OF CUSTOM EVENTS

	protected int engineStyleId;
	protected Object engineStyleSpec;

	public _ehm_ec_base() {
		super();

		this.extendedData.groupTag = engineCosmetics.tag;
	}

	/**
	 * Called during initialization. Sets the relevant fields of the non-customizable mods.
	 * <p> The customizable ones actually implement the {@link customizableMod} interface, but both
	 * use the same method signature for ease of use.
	 */
	public abstract void updateData();

	@Override
	public void init(HullModSpecAPI hullModSpec) {
		super.init(hullModSpec);

		this.updateData();
	}

	/**
	 * Alters the engine visuals of the ship. Uses the stored internal {@link #engineStyleId} and
	 * {@link #engineStyleSpec}.
	 * @param variant whose hullSpec will be altered
	 */
	protected final void changeEngines(MutableShipStatsAPI stats) {
		this.registerModInGroup(stats);

		ShipVariantAPI variant = stats.getVariant();
		ehm_hullSpec hullSpec = new ehm_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		if (this.engineStyleSpec != null) for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyleId(this.engineStyleId);
			engineSlot.setEngineStyleSpec(this.engineStyleSpec);
		} else for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyleId(this.engineStyleId);
		}

		variant.setHullSpecAPI(hullSpec.retrieve());
	}

	/**
	 * Restores the engine visuals of the ship by applying a stock hullSpec on the variant.
	 * <p> This is a lazy method that restores the hull spec instead of unmodifying the changes.
	 * @param stats of the ship/member whose hullSpec will be restored
	 */
	protected final void restoreEngines(MutableShipStatsAPI stats) {
		this.refreshHullSpec(stats);
	}

	/**
	 * Creates a new engine data from the current settings set through LunaLib's menu. The data
	 * is converted into an JSON object and passed to the static method in engine style builder
	 * {@link lyravega.proxies.lyr_engineBuilder#newEngineStyleSpec(String, JSONObject)}, which
	 * generates a usable engine data object, stores it in a map while returning it.
	 * <p> Hardcoded to utilize the hull modification spec id as a prefix to query the LunaLib
	 * settings. If the ids change, then the setting ids must also match the changes.
	 * @return engine style spec object
	 */
	protected Object newCustomEngineStyleSpec() {
		final Map<String, Object> customEngineSpecData = new HashMap<String, Object>();

		customEngineSpecData.put("engineColor", lyr_ehm.lunaSettings.getLunaRGBAColourArray(this.hullModSpecId+"_engine"));
		customEngineSpecData.put("contrailColor", lyr_ehm.lunaSettings.getLunaRGBAColourArray(this.hullModSpecId+"_contrail"));
		if (lyr_ehm.lunaSettings.getBoolean(this.hullModSpecId+"_hasDifferentCampaignEngine")) {
			customEngineSpecData.put("engineCampaignColor", lyr_ehm.lunaSettings.getLunaRGBAColourArray(this.hullModSpecId+"_engineCampaign"));
		}
		if (lyr_ehm.lunaSettings.getBoolean(this.hullModSpecId+"_hasDifferentCampaignContrail")) {
			customEngineSpecData.put("contrailCampaignColor", lyr_ehm.lunaSettings.getLunaRGBAColourArray(this.hullModSpecId+"_contrailCampaign"));
		}
		customEngineSpecData.put("glowSizeMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_glowSizeMult"));
		if (lyr_ehm.lunaSettings.getBoolean(this.hullModSpecId+"_hasAlternateGlow")) {
			customEngineSpecData.put("glowAlternateColor", lyr_ehm.lunaSettings.getLunaRGBAColourArray(this.hullModSpecId+"_glowAlternate"));
		}
		customEngineSpecData.put("contrailMaxSpeedMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailMaxSpeedMult"));
		customEngineSpecData.put("contrailAngularVelocityMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailAngularVelocityMult"));
		switch (lyr_ehm.lunaSettings.getString(this.hullModSpecId+"_mode")) {
			case "Particles": default: {
				customEngineSpecData.put("mode", "PARTICLES");
				customEngineSpecData.put("contrailParticleDuration", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailParticleDuration"));
				customEngineSpecData.put("contrailParticleSizeMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailParticleSizeMult"));
				customEngineSpecData.put("contrailParticleFinalSizeMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailParticleFinalSizeMult"));
			} break;
			case "Plasma": {
				customEngineSpecData.put("mode", "QUAD_STRIP");
				customEngineSpecData.put("contrailDuration", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailDuration"));
				customEngineSpecData.put("contrailMinSeg", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailMinSeg"));
				customEngineSpecData.put("contrailSpawnDistMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailSpawnDistMult"));
				customEngineSpecData.put("contrailWidthMult", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailWidthMult"));
				customEngineSpecData.put("contrailWidthAddedFractionAtEnd", lyr_ehm.lunaSettings.getDouble(this.hullModSpecId+"_contrailWidthAddedFractionAtEnd"));
			} break;
			case "Disabled": {
				customEngineSpecData.put("mode", "NONE");
			} break;
		}
		switch (lyr_ehm.lunaSettings.getString(this.hullModSpecId+"_type")) {
			case "Additive": customEngineSpecData.put("type", "GLOW"); break;
			case "Regular": customEngineSpecData.put("type", "SMOKE"); break;
		}
		customEngineSpecData.put("omegaMode", lyr_ehm.lunaSettings.getBoolean(this.hullModSpecId+"_omegaMode"));
		switch (lyr_ehm.lunaSettings.getString(this.hullModSpecId+"_glowSprite")) {
			case "I": customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32.png"); break;
			case "II": customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32b.png"); break;
			case "III": customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32s.png"); break;
			case "Default": default: customEngineSpecData.put("glowSprite", ""); break;
		}
		switch (lyr_ehm.lunaSettings.getString(this.hullModSpecId+"_glowOutline")) {
			case "I": customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32.png"); break;
			case "II": customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32b.png"); break;
			case "III": customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32-orig.png"); break;	// causes a NPE, nothing loads this so "ehm_test" does it
			case "Default": default: customEngineSpecData.put("glowOutline", ""); break;
		}

		return lyr_engineBuilder.newEngineStyleSpec(this.hullModSpecId, new JSONObject(customEngineSpecData));
	}
}