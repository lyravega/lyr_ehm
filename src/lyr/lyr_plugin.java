package lyr;

import static data.hullmods.ehm._ehm_basetracker.enhancedEvents;
import static data.hullmods.ehm._ehm_basetracker.normalEvents;
import static data.hullmods.ehm._ehm_basetracker.suppressedEvents;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import data.hullmods.ehm.events.enhancedEvents;
import data.hullmods.ehm.events.normalEvents;
import data.hullmods.ehm.events.suppressedEvents;
import lyr.misc.lyr_internals;
import lyr.tools._lyr_uiTools._lyr_delayedFinder;

public class lyr_plugin extends BaseModPlugin {
	private static final Logger logger = Logger.getLogger(lyr_internals.logName);
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

	private static class interactionListener implements ColonyInteractionListener {
		@Override
		public void reportPlayerOpenedMarket(MarketAPI market) {
			market.addSubmarket(lyr_internals.id.submarket);

			logger.info(lyr_internals.logPrefix + "Attached experimental submarket");
		}

		@Override
		public void reportPlayerClosedMarket(MarketAPI market) {
			market.removeSubmarket(lyr_internals.id.submarket);

			logger.info(lyr_internals.logPrefix + "Detached experimental submarket");
		}

		@Override
		public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {}

		@Override
		public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {}
	}

	@Override
	public void onGameLoad(boolean newGame) {
		findUIClasses();
		attachInteractionListener();
		updateBlueprints();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		registerHullMods();
	}

	private static void updateBlueprints() {
		FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();

		playerFaction.addKnownHullMod(lyr_internals.id.baseRetrofit);
		playerFaction.addKnownHullMod(lyr_internals.id.undoRetrofit);

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

	private static void registerHullMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
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

		logger.info(lyr_internals.logPrefix + "Hull modifications are registered");
	}

	private static void attachInteractionListener() {
		Global.getSector().getListenerManager().addListener(new interactionListener(), true);

		logger.info(lyr_internals.logPrefix + "Attached colony interaction listener");
	}

	private static void findUIClasses() {
		logger.info(lyr_internals.logPrefix + "Initializing UI class finder");

		new _lyr_delayedFinder();
	}
}
