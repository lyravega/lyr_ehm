package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.addEngineStyleSpec;
import static lyravega.tools.lyr_uiTools.commitChanges;
import static lyravega.tools.lyr_uiTools.playSound;

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
import lunalib.lunaSettings.LunaSettings;
import lyravega.listeners.events.normalEvents;
import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;
import lyravega.proxies.lyr_engineBuilder;
import lyravega.proxies.lyr_hullSpec;

/**
 * This class is used by engine cosmetic hullmods. The changes are 
 * permanent, and does not use {@code advanceInCombat()}.
 * @see {@link experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base _ehm_ar_base} for slot adapter base
 * @see {@link experimentalHullModifications.hullmods.ehm_sr._ehm_sr_base _ehm_sr_base} for system retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_wr._ehm_wr_base _ehm_wr_base} for weapon retrofit base
 * @see {@link experimentalHullModifications.hullmods.ehm_sc._ehm_sc_base _ehm_sc_base} for shield cosmetic base
 * @author lyravega
 */
public class _ehm_ec_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstall(ShipVariantAPI variant) {
		commitChanges(); playSound();
	}

	@Override
	public void onRemove(ShipVariantAPI variant) {
		variant.setHullSpecAPI(ehm_restoreEngineSlots_lazy(variant));
		commitChanges(); playSound();
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
	protected static final ShipHullSpecAPI ehm_pimpMyEngineSlots(ShipVariantAPI variant, int styleEnum, Object engineData) {
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
		final String modId = lyr_internals.id.mod;

		Map<String, Object> customEngineSpecData = new HashMap<String, Object>();

		customEngineSpecData.put("engineColor", getLunaRGBAColourArray(settingIdPrefix+"engine"));
		customEngineSpecData.put("contrailColor", getLunaRGBAColourArray(settingIdPrefix+"contrail"));
		if (LunaSettings.getBoolean(modId, settingIdPrefix+"hasDifferentCampaignEngine")) {
			customEngineSpecData.put("engineCampaignColor", getLunaRGBAColourArray(settingIdPrefix+"engineCampaign"));
		}
		if (LunaSettings.getBoolean(modId, settingIdPrefix+"hasDifferentCampaignContrail")) {
			customEngineSpecData.put("contrailCampaignColor", getLunaRGBAColourArray(settingIdPrefix+"contrailCampaign"));
		}
		customEngineSpecData.put("glowSizeMult", LunaSettings.getDouble(modId, settingIdPrefix+"glowSizeMult"));
		if (LunaSettings.getBoolean(modId, settingIdPrefix+"hasAlternateGlow")) {
			customEngineSpecData.put("glowAlternateColor", getLunaRGBAColourArray(settingIdPrefix+"glowAlternate"));
		}
		customEngineSpecData.put("contrailMaxSpeedMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailMaxSpeedMult"));
		customEngineSpecData.put("contrailAngularVelocityMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailAngularVelocityMult"));
		switch (LunaSettings.getString(modId, settingIdPrefix+"mode")) {
			case "Particles": default: {
				customEngineSpecData.put("mode", "PARTICLES");
				customEngineSpecData.put("contrailParticleDuration", LunaSettings.getDouble(modId, settingIdPrefix+"contrailParticleDuration"));
				customEngineSpecData.put("contrailParticleSizeMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailParticleSizeMult"));
				customEngineSpecData.put("contrailParticleFinalSizeMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailParticleFinalSizeMult"));
			} break;
			case "Plasma": {
				customEngineSpecData.put("mode", "QUAD_STRIP");
				customEngineSpecData.put("contrailDuration", LunaSettings.getDouble(modId, settingIdPrefix+"contrailDuration"));
				customEngineSpecData.put("contrailMinSeg", LunaSettings.getDouble(modId, settingIdPrefix+"contrailMinSeg"));
				customEngineSpecData.put("contrailSpawnDistMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailSpawnDistMult"));
				customEngineSpecData.put("contrailWidthMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailWidthMult"));
				customEngineSpecData.put("contrailWidthAddedFractionAtEnd", LunaSettings.getDouble(modId, settingIdPrefix+"contrailWidthAddedFractionAtEnd"));
			} break;
			case "Disabled": {
				customEngineSpecData.put("mode", "NONE");
			} break;
		}
		if (LunaSettings.getString(modId, settingIdPrefix+"type").equals("Additive")) {
			customEngineSpecData.put("type", "GLOW");
		} else /*if (LunaSettings.getString(modId, settingIdPrefix+"type").equals("Regular"))*/ {
			customEngineSpecData.put("type", "SMOKE");
		}
		customEngineSpecData.put("omegaMode", LunaSettings.getBoolean(modId, settingIdPrefix+"omegaMode"));
		switch (LunaSettings.getString(modId, settingIdPrefix+"glowSprite")) {
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
		switch (LunaSettings.getString(modId, settingIdPrefix+"glowOutline")) {
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

	protected static int[] getLunaRGBAColourArray(String settingIdPrefix) {
		String colourString = LunaSettings.getString(lyr_internals.id.mod, settingIdPrefix+"Colour");
		int[] rgba = {0,0,0,0};
		rgba[0] = Integer.parseInt(colourString.substring(1, 3), 16);
		rgba[1] = Integer.parseInt(colourString.substring(3, 5), 16);
		rgba[2] = Integer.parseInt(colourString.substring(5, 7), 16);
		rgba[3] = LunaSettings.getInt(lyr_internals.id.mod, settingIdPrefix+"Alpha");

		return rgba;
	}

	protected static String getLunaName(String settingIdPrefix) {
		return LunaSettings.getString(lyr_internals.id.mod, settingIdPrefix+"name");
	}

	//#region INSTALLATION CHECKS
	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (this.hullModSpec.hasTag(lyr_internals.tag.customizable)) {
			tooltip.addSectionHeading(header.customizable, header.customizable_textColour, header.customizable_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.customizable[0], text.padding).setHighlight(text.customizable[1]);
			tooltip.addPara(text.customizableEngine[0], text.padding).setHighlight(text.customizableEngine[1]);
		}

		if (!isApplicableToShip(ship)) {
			tooltip.addSectionHeading(header.notApplicable, header.notApplicable_textColour, header.notApplicable_bgColour, Alignment.MID, header.padding);

			if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) tooltip.addPara(text.lacksBase[0], text.padding).setHighlight(text.lacksBase[1]);
			if (ehm_hasRetrofitTag(ship, lyr_internals.tag.engineCosmetic, hullModSpecId)) tooltip.addPara(text.hasEngineCosmetic[0], text.padding).setHighlight(text.hasEngineCosmetic[1]);
		}

		super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship == null) return false; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship.getVariant())) return false; 
		if (ehm_hasRetrofitTag(ship, lyr_internals.tag.engineCosmetic, hullModSpecId)) return false;

		return true; 
	}
	//#endregion
}