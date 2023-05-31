package lyr;

import static data.hullmods.ehm._ehm_basetracker.enhancedEvents;
import static data.hullmods.ehm._ehm_basetracker.normalEvents;
import static data.hullmods.ehm._ehm_basetracker.suppressedEvents;
import static lyr.tools._lyr_uiTools.findUIClasses;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.thoughtworks.xstream.XStream;

import data.hullmods.ehm.events.enhancedEvents;
import data.hullmods.ehm.events.normalEvents;
import data.hullmods.ehm.events.suppressedEvents;
import lyr.misc.lyr_internals;
import lyr.scripts._lyr_fieldRepairsScript;
import lyr.tools._lyr_logger;

public class lyr_plugin extends BaseModPlugin implements _lyr_logger {
	public static final String EHM_ID = "lyr_ehm";
	public static final String LOCALIZATION_JSON = "customization/ehm_localization.json";
	public static final String SETTINGS_JSON = "customization/ehm_settings.json";
	public static JSONObject localizationJSON;
	public static JSONObject settingsJSON;
	
	static {
		try {
			localizationJSON = Global.getSettings().getMergedJSONForMod(LOCALIZATION_JSON, EHM_ID);
			settingsJSON = Global.getSettings().getMergedJSONForMod(SETTINGS_JSON, EHM_ID);
		} catch (IOException | JSONException e) {
			logger.fatal(lyr_internals.logPrefix+"Problem importing configuration JSONs");
		}
	}

	@Override
	public void onGameLoad(boolean newGame) {
		findUIClasses();
		teachAbility();
		teachBlueprints();
		replaceScript();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		registerHullMods();
	}

	@Override
	public void configureXStream(XStream x) {
		x.alias("FieldRepairsScript", _lyr_fieldRepairsScript.class);
	}

	/**
	 * Adds all mod weapons and hull modifications that have the {@link
	 * lyr_internals.tag#experimental "ehm"} tag to the player's faction.
	 * Removes any known with the {@link lyr_internals.tag#restricted
	 * "ehm_restricted"} ones.
	 */
	private static void teachBlueprints() {
		FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();

		playerFaction.addKnownHullMod(lyr_internals.id.baseModification);
		playerFaction.addKnownHullMod(lyr_internals.id.undoModification);

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			String hullModSpecId = hullModSpec.getId();
			if (hullModSpec.hasTag(lyr_internals.tag.experimental) && !playerFaction.knowsHullMod(hullModSpecId)) playerFaction.addKnownHullMod(hullModSpecId);
			else if (hullModSpec.hasTag(lyr_internals.tag.restricted) && playerFaction.knowsHullMod(hullModSpecId)) playerFaction.removeKnownHullMod(hullModSpecId);
		}

		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (weaponSpec.hasTag(lyr_internals.tag.experimental) && !playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.addKnownWeapon(weaponSpec.getWeaponId(), false);
			else if (weaponSpec.hasTag(lyr_internals.tag.restricted) && playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		logger.info(lyr_internals.logPrefix + "Player faction blueprints are updated");
	}

	/**
	 * Searches all hull modification effect classes and their superclasses for any interfaces
	 * they implement. If any one of them has implemented an event interface, adds them to
	 * their respective sets; registers them in essence.
	 * <p> During tracking, if any one of these events are detected, the relevant event methods
	 * will be triggered as long as the hull mod's effect has implemented the interfaces.
	 * @see {@link data.hullmods.ehm.ehm_base ehm_base} base hull modification that enables tracking
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents}
	 */
	private static void registerHullMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(lyr_internals.tag.experimental)) continue;

			Class<?> clazz = hullModSpec.getEffect().getClass();
			Set<Class<?>> interfaces = new HashSet<Class<?>>(Arrays.asList(clazz.getInterfaces()));
			interfaces.addAll(Arrays.asList(clazz.getSuperclass().getInterfaces()));
			
			if (interfaces.contains(normalEvents.class)) {
				normalEvents.put(hullModSpec.getId(), (normalEvents) hullModSpec.getEffect());
			}
			
			if (interfaces.contains(enhancedEvents.class)) {
				enhancedEvents.put(hullModSpec.getId(), (enhancedEvents) hullModSpec.getEffect());
			}
			
			if (interfaces.contains(suppressedEvents.class)) {
				suppressedEvents.put(hullModSpec.getId(), (suppressedEvents) hullModSpec.getEffect());
			}
		}

		logger.info(lyr_internals.logPrefix + "Experimental hull modifications are registered");
	}

	private static void teachAbility() {
		if (!Global.getSector().getCharacterData().getAbilities().contains(lyr_internals.id.ability)) {
			Global.getSector().getCharacterData().addAbility(lyr_internals.id.ability);	// add ability to ongoing games if not present

			logger.info(lyr_internals.logPrefix + "Shunt market control ability taught");
		}
	}

	private static void replaceScript() {
		if (Global.getSector().hasScript(FieldRepairsScript.class)) {
			Global.getSector().removeScriptsOfClass(FieldRepairsScript.class);
		}

		if (!Global.getSector().hasScript(_lyr_fieldRepairsScript.class)) {
			Global.getSector().addScript(new _lyr_fieldRepairsScript());
		}

		logger.info(lyr_internals.logPrefix + "Replaced 'FieldRepairsScript' with modified one");
	}
}
