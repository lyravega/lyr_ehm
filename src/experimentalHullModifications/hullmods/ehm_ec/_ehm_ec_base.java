package experimentalHullModifications.hullmods.ehm_ec;

import static lyravega.proxies.lyr_engineBuilder.addEngineData;
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
import experimentalHullModifications.hullmods.ehm.interfaces.normalEvents;
import lunalib.lunaSettings.LunaSettings;
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
	 * @param styleEnum {@link lyravega.proxies.lyr_engineBuilder.engineStyle engineStyle}
	 * @return a hullSpec with the altered engine visuals
	 */
	protected static final ShipHullSpecAPI ehm_pimpMyEngineSlots(ShipVariantAPI variant, int styleEnum) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyle(styleEnum);
		}

		return hullSpec.retrieve();
	}

	/**
	 * Alters the engine visuals of the ship, with custom engine data
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum {@link lyravega.proxies.lyr_engineBuilder.engineStyle engineStyle}
	 * @param engineData {@link #generateEngineData(String, String) generateEngineData}
	 * @return a hullSpec with the altered engine visuals
	 */
	protected static final ShipHullSpecAPI ehm_pimpMyEngineSlots(ShipVariantAPI variant, int styleEnum, Object engineData) {
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		lyr_engineBuilder engineSlot = new lyr_engineBuilder(null, false);

		for (Object temp : hullSpec.getEngineSlots()) {
			engineSlot.recycle(temp).setEngineStyle(styleEnum);
			engineSlot.setEngineData(engineData);
		}

		return hullSpec.retrieve();
	}

	/**
	 * Restores the engine visuals of the ship by applying a stock hullSpec
	 * on the variant.
	 * @param variant whose hullSpec will be altered
	 * @param styleEnum somewhat hardcoded {@link lyravega.proxies.lyr_engineBuilder.engineStyle engineStyle}
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
	 * afterwards {@link lyravega.proxies.lyr_engineBuilder#customEngineData customEngineData}.
	 * <p> Directly returning this object and using is possible, but not done that way.
	 * @param settingIdPrefix class name is used as prefix in LunaLib setting ID's
	 * @param customEngineDataId as the mapId to retrieve it from the map later
	 */
	protected static void generateEngineData(String settingIdPrefix, String customEngineDataId) {
		final String modId = lyr_internals.id.mod;

		Map<String, Object> engineData = new HashMap<String, Object>();

		engineData.put("engineColor", getLunaRGBAColourArray(settingIdPrefix+"engine"));
		engineData.put("contrailColor", getLunaRGBAColourArray(settingIdPrefix+"contrail"));
		if (LunaSettings.getBoolean(modId, settingIdPrefix+"hasDifferentCampaignEngine")) {
			engineData.put("engineCampaignColor", getLunaRGBAColourArray(settingIdPrefix+"engineCampaign"));
		}
		if (LunaSettings.getBoolean(modId, settingIdPrefix+"hasDifferentCampaignContrail")) {
			engineData.put("contrailCampaignColor", getLunaRGBAColourArray(settingIdPrefix+"contrailCampaign"));
		}
		engineData.put("glowSizeMult", LunaSettings.getDouble(modId, settingIdPrefix+"glowSizeMult"));
		if (LunaSettings.getBoolean(modId, settingIdPrefix+"hasAlternateGlow")) {
			engineData.put("glowAlternateColor", getLunaRGBAColourArray(settingIdPrefix+"glowAlternate"));
		}
		engineData.put("contrailMaxSpeedMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailMaxSpeedMult"));
		engineData.put("contrailAngularVelocityMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailAngularVelocityMult"));
		switch (LunaSettings.getString(modId, settingIdPrefix+"mode")) {
			case "Particles": default: {
				engineData.put("mode", "PARTICLES");
				engineData.put("contrailParticleDuration", LunaSettings.getDouble(modId, settingIdPrefix+"contrailParticleDuration"));
				engineData.put("contrailParticleSizeMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailParticleSizeMult"));
				engineData.put("contrailParticleFinalSizeMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailParticleFinalSizeMult"));
			} break;
			case "Plasma": {
				engineData.put("mode", "QUAD_STRIP");
				engineData.put("contrailDuration", LunaSettings.getDouble(modId, settingIdPrefix+"contrailDuration"));
				engineData.put("contrailMinSeg", LunaSettings.getDouble(modId, settingIdPrefix+"contrailMinSeg"));
				engineData.put("contrailSpawnDistMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailSpawnDistMult"));
				engineData.put("contrailWidthMult", LunaSettings.getDouble(modId, settingIdPrefix+"contrailWidthMult"));
				engineData.put("contrailWidthAddedFractionAtEnd", LunaSettings.getDouble(modId, settingIdPrefix+"contrailWidthAddedFractionAtEnd"));
			} break;
			case "Disabled": {
				engineData.put("mode", "NONE");
			} break;
		}
		if (LunaSettings.getString(modId, settingIdPrefix+"type").equals("Additive")) {
			engineData.put("type", "GLOW");
		} else /*if (LunaSettings.getString(modId, settingIdPrefix+"type").equals("Regular"))*/ {
			engineData.put("type", "SMOKE");
		}
		engineData.put("omegaMode", LunaSettings.getBoolean(modId, settingIdPrefix+"omegaMode"));
		switch (LunaSettings.getString(modId, settingIdPrefix+"glowSprite")) {
			case "I": {
				engineData.put("glowSprite", "graphics/fx/engineglow32.png");
			} break;
			case "II": {
				engineData.put("glowSprite", "graphics/fx/engineglow32b.png");
			} break;
			case "III": {
				engineData.put("glowSprite", "graphics/fx/engineglow32s.png");
			} break;
			case "Default": default: {
				engineData.put("glowSprite", "");
			} break;
		}
		switch (LunaSettings.getString(modId, settingIdPrefix+"glowOutline")) {
			case "I": {
				engineData.put("glowOutline", "graphics/fx/engineflame32.png");
			} break;
			case "II": {
				engineData.put("glowOutline", "graphics/fx/engineflame32b.png");
			} break;
			case "III": {
				engineData.put("glowOutline", "graphics/fx/engineflame32-orig.png");	// causes a NPE, nothing loads this so "ehm_test" does it
			} break;
			case "Default": default: {
				engineData.put("glowOutline", "");
			} break;
		}

		addEngineData(new JSONObject(engineData), customEngineDataId);
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