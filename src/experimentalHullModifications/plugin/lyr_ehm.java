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
import experimentalHullModifications.hullmods.ehm._ehm_base.ecsv;
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

	@Override
	public void onGameLoad(boolean newGame) {
		teachAbility(ehm_internals.ids.ability);
		updateBlueprints();
		replaceFieldRepairsScript();
		attachShuntAccessListener();
		lyr_fleetTracker.attach();
		if (ehm_settings.getClearUnknownSlots()) ehm_lostAndFound.returnStuff();

		// if (!Global.getSettings().isDevMode()) return;
		// LunaRefitManager.addRefitButton(new _ehmu_test());
		processECSV();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		ehm_settings.attach();
		updateHullMods();
		processECSV();
		lyr_eventDispatcher.registerModsWithEvents("data/hullmods/hull_mods.csv", ehm_internals.ids.mod);
		lyr_upgradeVault.registerUpgrade(new ehmu_overdrive());

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
			if (!weaponSpec.hasTag(ehm_internals.tags.experimental)) continue;

			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		// purge experimental hullmod blueprints
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!ehm_internals.ids.manufacturer.equals(hullModSpec.getManufacturer())) continue;
			if (!hullModSpec.hasTag(ehm_internals.tags.experimental)) continue;

			playerData.removeHullMod(hullModSpec.getId());
			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownHullMod(hullModSpec.getId());
		}

		// final String targetTag = ehm_settings.getCosmeticsOnly() ? ehm_internals.tag.cosmetic : ehm_internals.tag.experimental;
		final boolean cosmeticsOnly = ehm_settings.getCosmeticsOnly();

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!_ehm_base.class.isInstance(hullModSpec.getEffect())) continue;
			final ecsv ecsv = _ehm_base.class.cast(hullModSpec.getEffect()).ecsv(friend);

			if (ecsv.isRestricted) continue;
			if (!cosmeticsOnly || cosmeticsOnly && ecsv.isCosmetic) playerData.addHullMod(hullModSpec.getId());
		}

		lyr_logger.info("Faction blueprints are updated");
	}

	public static void updateHullMods() {
		final SettingsAPI settingsAPI = Global.getSettings();
		Set<String> uiTags;

		if (ehm_settings.getCosmeticsOnly()) {
			uiTags = settingsAPI.getHullModSpec(ehm_internals.ids.hullmods.base).getUITags();
			uiTags.clear(); uiTags.add(ehm_internals.tags.uiTags.cosmetics);

			uiTags = settingsAPI.getHullModSpec(ehm_internals.ids.hullmods.undo).getUITags();
			uiTags.clear(); uiTags.add(ehm_internals.tags.uiTags.cosmetics);
		} else {
			uiTags = settingsAPI.getHullModSpec(ehm_internals.ids.hullmods.base).getUITags();
			uiTags.clear(); uiTags.addAll(ehm_internals.tags.uiTags.all);

			uiTags = settingsAPI.getHullModSpec(ehm_internals.ids.hullmods.undo).getUITags();
			uiTags.clear(); uiTags.addAll(ehm_internals.tags.uiTags.all);
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

		switch (ehm_settings.getShuntAvailability()) {
			case "Always": ehm_submarketInjector.nullify(friend); ehm_shuntInjector.attach(); break;
			case "Submarket": ehm_shuntInjector.nullify(friend); ehm_submarketInjector.attach(); break;
			default: break;
		}
	}

	private static void processECSV() {
		try {
			JSONArray loadCSV = Global.getSettings().loadCSV("data/hullmods/hull_mods.csv", ehm_internals.ids.mod);

			for (int i = 0; i < loadCSV.length(); i++) {
				JSONObject hullModEntry = loadCSV.getJSONObject(i);
				HullModSpecAPI hullModSpec = Global.getSettings().getHullModSpec(hullModEntry.getString("id"));

				if (hullModSpec == null || !_ehm_base.class.isInstance(hullModSpec.getEffect())) continue;

				final ecsv ecsv = _ehm_base.class.cast(hullModSpec.getEffect()).ecsv(friend);

				if ("true".equalsIgnoreCase(hullModEntry.getString("ecsv_isCosmetic"))) ecsv.isCosmetic = true;
				if ("true".equalsIgnoreCase(hullModEntry.getString("ecsv_isRestricted"))) ecsv.isRestricted = true;
				if ("true".equalsIgnoreCase(hullModEntry.getString("ecsv_isCustomizable"))) ecsv.isCustomizable = true;

				String[] applicableChecks = hullModEntry.getString("ecsv_applicableChecks").split("[\\s,]+");
				if (!applicableChecks[0].isEmpty()) {
					if (ecsv.applicableChecks == null) ecsv.applicableChecks = new HashSet<String>(4, 0.75f);
					ecsv.applicableChecks.clear(); ecsv.applicableChecks.addAll(Arrays.asList(applicableChecks));
				} else if (ecsv.applicableChecks != null) { ecsv.applicableChecks.clear(); ecsv.applicableChecks = null; }

				String[] lockedInChecks = hullModEntry.getString("ecsv_lockedInChecks").split("[\\s,]+");
				if (!lockedInChecks[0].isEmpty()) {
					if (ecsv.lockedInChecks == null) ecsv.lockedInChecks = new HashSet<String>(4, 0.75f);
					ecsv.lockedInChecks.clear(); ecsv.lockedInChecks.addAll(Arrays.asList(lockedInChecks));
				} else if (ecsv.lockedInChecks != null) { ecsv.lockedInChecks.clear(); ecsv.lockedInChecks = null; }

				String[] lockedOutChecks = hullModEntry.getString("ecsv_lockedOutChecks").split("[\\s,]+");
				if (!lockedOutChecks[0].isEmpty()) {
					if (ecsv.lockedOutChecks == null) ecsv.lockedOutChecks = new HashSet<String>(4, 0.75f);
					ecsv.lockedOutChecks.clear(); ecsv.lockedOutChecks.addAll(Arrays.asList(lockedOutChecks));
				} else if (ecsv.lockedOutChecks != null) { ecsv.lockedOutChecks.clear(); ecsv.lockedOutChecks = null; }

				lyr_logger.debug("Processed extended comma separated values for '"+hullModSpec.getId()+"'");
			}

			// lyr_logger.info("Hull modifications from the mod '"+modId+"' are processed");
		} catch (Throwable t) {
			lyr_logger.error("WUT", t);
		}
	}
}
