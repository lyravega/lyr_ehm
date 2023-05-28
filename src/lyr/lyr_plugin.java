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
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import data.hullmods.ehm.events.enhancedEvents;
import data.hullmods.ehm.events.normalEvents;
import data.hullmods.ehm.events.suppressedEvents;
import data.submarkets.ehm_submarket;
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

	/**
	 * An inner listener class whose sole purpose is to attach/detach the
	 * {@link data.submarkets.ehm_submarket experimental submarket}
	 */
	private static class ehm_interactionListener implements ColonyInteractionListener {
		private ehm_interactionListener() {
			if (!Global.getSector().getCharacterData().getAbilities().contains(lyr_internals.id.ability)) {
				Global.getSector().getCharacterData().addAbility(lyr_internals.id.ability);	// add ability to ongoing games if not present
			}
		}

		@Override
		public void reportPlayerOpenedMarket(MarketAPI market) {
			if (market == null) return;
			if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;	// show submarket only if this ability is active
			if (market.hasSubmarket(lyr_internals.id.submarket)) return;

			market.addSubmarket(lyr_internals.id.submarket);

			logger.info(lyr_internals.logPrefix + "Attached experimental submarket");
		}

		@Override
		public void reportPlayerClosedMarket(MarketAPI market) {
			if (market == null) return;
			if (!Global.getSector().getPlayerFleet().getAbility(lyr_internals.id.ability).isActive()) return;
			if (!market.hasSubmarket(lyr_internals.id.submarket)) return;

			market.removeSubmarket(lyr_internals.id.submarket);

			CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoItemQuantity<String> weaponCargo : playerCargo.getWeapons()) {
				if (ehm_submarket.shunts.contains(weaponCargo.getItem())) playerCargo.removeWeapons(weaponCargo.getItem(), weaponCargo.getCount());
			}

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

	/**
	 * Adds all mod weapons and hull modifications that have the {@link
	 * lyr_internals.tag#experimental "ehm"} tag to the player's faction.
	 * Removes any known with the {@link lyr_internals.tag#restricted
	 * "ehm_restricted"} ones.
	 */
	private static void updateBlueprints() {
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

	/**
	 * A simple method that attaches a transient interaction listener
	 */
	private static void attachInteractionListener() {
		Global.getSector().getListenerManager().addListener(new ehm_interactionListener(), true);

		logger.info(lyr_internals.logPrefix + "Attached colony interaction listener");
	}

	/**
	 * A simple method that initializes an every frame script that waits
	 * till the relevant UI parts are available and can be fished for classes
	 */
	private static void findUIClasses() {
		logger.info(lyr_internals.logPrefix + "Initializing UI class finder");

		new _lyr_delayedFinder();
	}
}
