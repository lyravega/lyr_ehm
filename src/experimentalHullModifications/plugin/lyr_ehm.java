package experimentalHullModifications.plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.thoughtworks.xstream.XStream;

import experimentalHullModifications.abilities.ehm_ability;
import experimentalHullModifications.abilities.listeners.ehm_shuntInjector;
import experimentalHullModifications.abilities.listeners.ehm_submarketInjector;
import experimentalHullModifications.hullmods.ehm._ehm_base;
import experimentalHullModifications.hullmods.ehm._ehm_base.extendedData;
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_lostAndFound;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.scripts.ehm_fieldRepairsScript;
import experimentalHullModifications.upgrades._ehmu_test;
import experimentalHullModifications.upgrades.ehmu_overdrive;
import lunalib.lunaRefit.LunaRefitManager;
import lyravega.listeners.lyr_eventDispatcher;
import lyravega.listeners.lyr_fleetTracker;
import lyravega.misc.lyr_upgradeVault;
import lyravega.utilities.logger.lyr_logger;

public final class lyr_ehm extends BaseModPlugin {
	public static final class friend { private friend() {} }; private static final friend friend = new friend();
	public static final ehm_settings lunaSettings = new ehm_settings();

	@Override
	public void onGameLoad(boolean newGame) {
		teachAbility(ehm_internals.ids.ability);
		updateBlueprints();
		replaceFieldRepairsScript();
		attachShuntAccessListener();
		lyr_fleetTracker.attach();
		if (lunaSettings.getClearUnknownSlots()) ehm_lostAndFound.returnStuff();

		// TODO: clean this shit up
		// if (!Global.getSettings().isDevMode()) return;
		// LunaRefitManager.addRefitButton(new _ehmu_test());
		processExtendedData();
		Global.getSettings().getHullModSpec("lyr_tracker").setHidden(false);	// TODO: remove these debug shit
		Global.getSettings().getHullModSpec("lyr_tracker").setHiddenEverywhere(false);
	}

	@Override
	public void onApplicationLoad() throws Exception {
		lunaSettings.attach();
		updateHullMods();
		processExtendedData();
		lyr_eventDispatcher.registerModsWithEvents("data/hullmods/hull_mods.csv", ehm_internals.ids.mod);
		lyr_upgradeVault.registerUpgrade(new ehmu_overdrive());

		// TODO: clean this shit up
		// if (!Global.getSettings().isDevMode()) return;
		LunaRefitManager.addRefitButton(new _ehmu_test());
	}

	@Override
	public void configureXStream(XStream x) {
		x.alias("FieldRepairsScript", ehm_fieldRepairsScript.class);
		x.alias("data.abilities.ehm_ability", ehm_ability.class);	// remember to use this for serialized shit
		x.alias("ehm_shuntInjector", ehm_shuntInjector.class);	// added transient, but just in case
		x.alias("ehm_submarketInjector", ehm_submarketInjector.class);	// added transient, but just in case
	}

	/**
	 * Purges all of the experimental stuff from all factions' known lists,
	 * then adds valid experimental hull modifications to player's faction
	 */
	public static void updateBlueprints() {
		// FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();
		CharacterDataAPI playerData = Global.getSector().getCharacterData();

		// purge experimental weapon blueprints
		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (!ehm_internals.ids.manufacturer.equals(weaponSpec.getManufacturer())) continue;
			if (!weaponSpec.hasTag(ehm_internals.hullmods.tags.experimental)) continue;

			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		// purge experimental hullmod blueprints
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!ehm_internals.ids.manufacturer.equals(hullModSpec.getManufacturer())) continue;
			if (!hullModSpec.hasTag(ehm_internals.hullmods.tags.experimental)) continue;

			playerData.removeHullMod(hullModSpec.getId());
			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownHullMod(hullModSpec.getId());
		}

		// final String targetTag = ehm_settings.getCosmeticsOnly() ? ehm_internals.tag.cosmetic : ehm_internals.tag.experimental;
		final boolean cosmeticsOnly = lunaSettings.getCosmeticsOnly();

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!_ehm_base.class.isInstance(hullModSpec.getEffect())) continue;
			final extendedData extendedData = _ehm_base.class.cast(hullModSpec.getEffect()).getExtendedData(friend);

			if (extendedData.isRestricted) continue;
			if (!cosmeticsOnly || cosmeticsOnly && extendedData.isCosmetic) playerData.addHullMod(hullModSpec.getId());
		}

		lyr_logger.info("Faction blueprints are updated");
	}

	public static void updateHullMods() {
		final SettingsAPI settingsAPI = Global.getSettings();
		Set<String> uiTags;

		if (lunaSettings.getCosmeticsOnly()) {
			uiTags = settingsAPI.getHullModSpec(ehm_internals.hullmods.main.base).getUITags();
			uiTags.clear(); uiTags.add(ehm_internals.hullmods.uiTags.cosmetics);

			uiTags = settingsAPI.getHullModSpec(ehm_internals.hullmods.main.undo).getUITags();
			uiTags.clear(); uiTags.add(ehm_internals.hullmods.uiTags.cosmetics);
		} else {
			uiTags = settingsAPI.getHullModSpec(ehm_internals.hullmods.main.base).getUITags();
			uiTags.clear(); uiTags.addAll(ehm_internals.hullmods.uiTags.set);

			uiTags = settingsAPI.getHullModSpec(ehm_internals.hullmods.main.undo).getUITags();
			uiTags.clear(); uiTags.addAll(ehm_internals.hullmods.uiTags.set);
		}
	}

	private static void teachAbility(String abilityId) {	// add ability to ongoing games if not present
		final CharacterDataAPI characterData = Global.getSector().getCharacterData();
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		if (!characterData.getAbilities().contains(abilityId)
		 || !playerFleet.hasAbility(abilityId)) {
			characterData.addAbility(abilityId);
			playerFleet.addAbility(abilityId);

			lyr_logger.info("Ability with the id '"+abilityId+"' taught");
		} else lyr_logger.info("Ability with the id '"+abilityId+"' was already known");
	}

	private static void replaceFieldRepairsScript() {
		final SectorAPI sector = Global.getSector();

		if (Global.getSettings().getModManager().isModEnabled("QualityCaptains")) {
			if (sector.hasScript(ehm_fieldRepairsScript.class)) {
				sector.removeScriptsOfClass(ehm_fieldRepairsScript.class);
				lyr_logger.warn("Removing modified 'FieldRepairsScript' replacement from this mod");
			}

			lyr_logger.info("Skipping 'FieldRepairsScript' replacement as 'Quality Captains' is detected");
			return;
		}

		if (sector.hasScript(FieldRepairsScript.class)) sector.removeScriptsOfClass(FieldRepairsScript.class);
		if (!sector.hasScript(ehm_fieldRepairsScript.class)) sector.addScript(new ehm_fieldRepairsScript());
		lyr_logger.info("Replaced 'FieldRepairsScript' with modified one");
	}

	public static void attachShuntAccessListener() {
		if (!Global.getSector().getPlayerFleet().getAbility(ehm_internals.ids.ability).isActive()) return;

		switch (lunaSettings.getShuntAvailability()) {
			case "Always": ehm_submarketInjector.nullify(friend); ehm_shuntInjector.attach(); break;
			case "Submarket": ehm_shuntInjector.nullify(friend); ehm_submarketInjector.attach(); break;
			default: break;
		}
	}

	// TODO: clean this shit up, decide on what csv columns to use
	private static void processExtendedData() {
		try {
			JSONArray loadCSV = Global.getSettings().loadCSV("data/hullmods/hull_mods.csv", ehm_internals.ids.mod);

			for (int i = 0; i < loadCSV.length(); i++) {
				JSONObject hullModEntry = loadCSV.getJSONObject(i);
				HullModSpecAPI hullModSpec = Global.getSettings().getHullModSpec(hullModEntry.getString("id"));

				if (hullModSpec == null || !_ehm_base.class.isInstance(hullModSpec.getEffect())) continue;

				final extendedData extendedData = _ehm_base.class.cast(hullModSpec.getEffect()).getExtendedData(friend);

				if ("true".equalsIgnoreCase(hullModEntry.getString("ecsv_isCosmetic"))) extendedData.isCosmetic = true;
				if ("true".equalsIgnoreCase(hullModEntry.getString("ecsv_isRestricted"))) extendedData.isRestricted = true;
				if ("true".equalsIgnoreCase(hullModEntry.getString("ecsv_isCustomizable"))) extendedData.isCustomizable = true;

				String[] applicableChecks = hullModEntry.getString("ecsv_applicableChecks").split("[\\s,]+");
				if (!applicableChecks[0].isEmpty()) {
					if (extendedData.applicableChecks == null) extendedData.applicableChecks = new HashSet<String>();
					extendedData.applicableChecks.clear(); extendedData.applicableChecks.addAll(Arrays.asList(applicableChecks));
				} else if (extendedData.applicableChecks != null) { extendedData.applicableChecks.clear(); extendedData.applicableChecks = null; }

				String[] lockedInChecks = hullModEntry.getString("ecsv_lockedInChecks").split("[\\s,]+");
				if (!lockedInChecks[0].isEmpty()) {
					if (extendedData.lockedInChecks == null) extendedData.lockedInChecks = new HashSet<String>();
					extendedData.lockedInChecks.clear(); extendedData.lockedInChecks.addAll(Arrays.asList(lockedInChecks));
				} else if (extendedData.lockedInChecks != null) { extendedData.lockedInChecks.clear(); extendedData.lockedInChecks = null; }

				String[] lockedOutChecks = hullModEntry.getString("ecsv_lockedOutChecks").split("[\\s,]+");
				if (!lockedOutChecks[0].isEmpty()) {
					if (extendedData.lockedOutChecks == null) extendedData.lockedOutChecks = new HashSet<String>();
					extendedData.lockedOutChecks.clear(); extendedData.lockedOutChecks.addAll(Arrays.asList(lockedOutChecks));
				} else if (extendedData.lockedOutChecks != null) { extendedData.lockedOutChecks.clear(); extendedData.lockedOutChecks = null; }

				lyr_logger.debug("Processed extended comma separated values for '"+hullModSpec.getId()+"'");
			}

			lyr_logger.info("Extended CSV data are processed");
		} catch (Throwable t) {
			lyr_logger.error("Problem occured during processing extended CSV data", t);
		}
	}
}
