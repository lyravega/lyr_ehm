package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.listeners.lyr_fleetTracker.refreshFleetView;
import static lyravega.proxies.lyr_engineBuilder.addEngineStyleSpec;
import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.misc.lyr_internals;
import experimentalHullModifications.misc.lyr_tooltip.header;
import experimentalHullModifications.misc.lyr_tooltip.text;
import lyravega.listeners.events.normalEvents;
import lyravega.proxies.lyr_engineBuilder;
import lyravega.proxies.lyr_hullSpec;
import lyravega.tools._ehm_helpers;
import lyravega.tools.lyr_lunaAccessors;

/**
 * This class is used by engine cosmetic hullmods. The changes are 
 * permanent, and does not use {@code advanceInCombat()}.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public abstract class _ehm_ec_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		if (_ehm_helpers.removeHullModsWithSameTag(variant, lyr_internals.tag.engineCosmetic, this.hullModSpecId)) return;	// removes other engine cosmetics and short-circuits if there was any
		commitVariantChanges(); playDrillSound(); refreshFleetView();	// short-circuit is due to onRemove() below, to avoid doing same things multiple times 
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		if (!_ehm_helpers.hasHullModWithTag(variant, lyr_internals.tag.engineCosmetic, this.hullModSpecId))
			variant.setHullSpecAPI(ehm_restoreEngineSlots_lazy(variant));
		commitVariantChanges(); playDrillSound(); refreshFleetView();
	}
	//#endregion
	// END OF CUSTOM EVENTS

	/**
	 * Alters the engine visuals of the ship. Uses the vanilla engine styles
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum {@link lyravega.proxies.lyr_engineBuilder.engineStyleIds engineStyle}
	 * @return a hullSpec with the altered engine visuals
	 */
	protected static final ShipHullSpecAPI ehm_applyEngineCosmetics(ShipVariantAPI variant, int styleEnum) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyleId(styleEnum);
		}

		return hullSpec.retrieve();
	}

	/**
	 * Alters the engine visuals of the ship, with custom engine data
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum {@link lyravega.proxies.lyr_engineBuilder.engineStyleIds engineStyle}
	 * @param engineData {@link #newCustomEngineSpec(String, String) generateEngineData}
	 * @return a hullSpec with the altered engine visuals
	 */
	protected static final ShipHullSpecAPI ehm_applyEngineCosmetics(ShipVariantAPI variant, int styleEnum, Object engineData) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyleId(styleEnum);
			engineSlot.setEngineStyleSpec(engineData);
		}

		return hullSpec.retrieve();
	}

	/**
	 * Restores the engine visuals of the ship by applying a stock hullSpec
	 * on the variant.
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum somewhat hardcoded {@link lyravega.proxies.lyr_engineBuilder.engineStyleIds engineStyle}
	 * @return a hullSpec with restored engine visuals
	 */
	public static final ShipHullSpecAPI ehm_restoreEngineSlots_lazy(ShipVariantAPI variant) {
		ShipHullSpecAPI hullSpec = ehm_hullSpecRefresh(variant);

		return hullSpec;
	}


	/**
	 * Creates a new engine data from the current settings set through LunaLib's menu. The
	 * data is converted into an JSON object and passed to the static method {@link
	 * lyravega.proxies.lyr_engineBuilder#addEngineData addEngineData()}, which generates
	 * a game-usable engine data object and stores it in a map there which can be used
	 * afterwards {@link lyravega.proxies.lyr_engineBuilder#customEngineStyleSpecs customEngineData}.
	 * <p> Directly returning this object and using is possible, but not done that way.
	 * @param settingIdPrefix class name is used as prefix in LunaLib setting ID's
	 * @param customEngineSpecName as the mapId to retrieve it from the map later
	 */
	protected static void newCustomEngineSpec(String settingIdPrefix, String customEngineSpecName) {
		Map<String, Object> customEngineSpecData = new HashMap<String, Object>();

		customEngineSpecData.put("engineColor", lyr_lunaAccessors.getLunaRGBAColourArray(settingIdPrefix+"engine"));
		customEngineSpecData.put("contrailColor", lyr_lunaAccessors.getLunaRGBAColourArray(settingIdPrefix+"contrail"));
		if (lyr_lunaAccessors.getBoolean(settingIdPrefix+"hasDifferentCampaignEngine")) {
			customEngineSpecData.put("engineCampaignColor", lyr_lunaAccessors.getLunaRGBAColourArray(settingIdPrefix+"engineCampaign"));
		}
		if (lyr_lunaAccessors.getBoolean(settingIdPrefix+"hasDifferentCampaignContrail")) {
			customEngineSpecData.put("contrailCampaignColor", lyr_lunaAccessors.getLunaRGBAColourArray(settingIdPrefix+"contrailCampaign"));
		}
		customEngineSpecData.put("glowSizeMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"glowSizeMult"));
		if (lyr_lunaAccessors.getBoolean(settingIdPrefix+"hasAlternateGlow")) {
			customEngineSpecData.put("glowAlternateColor", lyr_lunaAccessors.getLunaRGBAColourArray(settingIdPrefix+"glowAlternate"));
		}
		customEngineSpecData.put("contrailMaxSpeedMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailMaxSpeedMult"));
		customEngineSpecData.put("contrailAngularVelocityMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailAngularVelocityMult"));
		switch (lyr_lunaAccessors.getString(settingIdPrefix+"mode")) {
			case "Particles": default: {
				customEngineSpecData.put("mode", "PARTICLES");
				customEngineSpecData.put("contrailParticleDuration", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailParticleDuration"));
				customEngineSpecData.put("contrailParticleSizeMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailParticleSizeMult"));
				customEngineSpecData.put("contrailParticleFinalSizeMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailParticleFinalSizeMult"));
			} break;
			case "Plasma": {
				customEngineSpecData.put("mode", "QUAD_STRIP");
				customEngineSpecData.put("contrailDuration", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailDuration"));
				customEngineSpecData.put("contrailMinSeg", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailMinSeg"));
				customEngineSpecData.put("contrailSpawnDistMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailSpawnDistMult"));
				customEngineSpecData.put("contrailWidthMult", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailWidthMult"));
				customEngineSpecData.put("contrailWidthAddedFractionAtEnd", lyr_lunaAccessors.getDouble(settingIdPrefix+"contrailWidthAddedFractionAtEnd"));
			} break;
			case "Disabled": {
				customEngineSpecData.put("mode", "NONE");
			} break;
		}
		if (lyr_lunaAccessors.getString(settingIdPrefix+"type").equals("Additive")) {
			customEngineSpecData.put("type", "GLOW");
		} else /*if (lyr_lunaAccessors.getString(settingIdPrefix+"type").equals("Regular"))*/ {
			customEngineSpecData.put("type", "SMOKE");
		}
		customEngineSpecData.put("omegaMode", lyr_lunaAccessors.getBoolean(settingIdPrefix+"omegaMode"));
		switch (lyr_lunaAccessors.getString(settingIdPrefix+"glowSprite")) {
			case "I": {
				customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32.png");
			} break;
			case "II": {
				customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32b.png");
			} break;
			case "III": {
				customEngineSpecData.put("glowSprite", "graphics/fx/engineglow32s.png");
			} break;
			case "Default": default: {
				customEngineSpecData.put("glowSprite", "");
			} break;
		}
		switch (lyr_lunaAccessors.getString(settingIdPrefix+"glowOutline")) {
			case "I": {
				customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32.png");
			} break;
			case "II": {
				customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32b.png");
			} break;
			case "III": {
				customEngineSpecData.put("glowOutline", "graphics/fx/engineflame32-orig.png");	// causes a NPE, nothing loads this so "ehm_test" does it
			} break;
			case "Default": default: {
				customEngineSpecData.put("glowOutline", "");
			} break;
		}

		addEngineStyleSpec(new JSONObject(customEngineSpecData), customEngineSpecName);
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (this.hullModSpec.hasTag(lyr_internals.tag.customizable)) {
			tooltip.addSectionHeading(header.customizable, header.customizable_textColour, header.customizable_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.customizable[0], text.padding).setHighlight(text.customizable[1]);
			// tooltip.addPara(text.customizableEngine[0], text.padding).setHighlight(text.customizableEngine[1]);
		}

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			// if (_ehm_helpers.ehm_hasHullModWithTag(ship, lyr_internals.tag.engineCosmetic, id)) tooltip.addPara(text.hasEngineCosmetic[0], text.padding).setHighlight(text.hasEngineCosmetic[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!_ehm_helpers.hasRetrofitBaseBuiltIn(ship)) return false; 
		// if (_ehm_helpers.ehm_hasHullModWithTag(ship, lyr_internals.tag.engineCosmetic, id)) return false;

		return true; 
	}
	//#endregion
}