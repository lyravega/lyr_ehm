package experimentalHullModifications.plugin;

import java.util.Set;

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
import experimentalHullModifications.misc.ehm_internals;
import experimentalHullModifications.misc.ehm_settings;
import experimentalHullModifications.scripts.ehm_fieldRepairsScript;
import lunalib.lunaRefit.LunaRefitManager;
import lyravega.listeners.lyr_eventDispatcher;
import lyravega.listeners.lyr_fleetTracker;
import lyravega.misc.test;
import lyravega.utilities.logger.lyr_logger;

public final class lyr_ehm extends BaseModPlugin {
	public static final class friend {
		private friend() {}
	};	private static friend friend = new friend();

	@Override
	public void onGameLoad(boolean newGame) {
		teachAbility(ehm_internals.id.ability);
		updateBlueprints();
		replaceFieldRepairsScript();
		attachShuntAccessListener();
		lyr_fleetTracker.attach();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		ehm_settings.attach();
		updateHullMods();
		lyr_eventDispatcher.registerModsWithEvents("data/hullmods/hull_mods.csv", ehm_internals.id.mod);
		LunaRefitManager.addRefitButton(new test());

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
			if (!ehm_internals.id.manufacturer.equals(weaponSpec.getManufacturer())) continue;
			if (!weaponSpec.hasTag(ehm_internals.tag.experimental)) continue;

			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		// purge experimental hullmod blueprints
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!ehm_internals.id.manufacturer.equals(hullModSpec.getManufacturer())) continue;
			if (!hullModSpec.hasTag(ehm_internals.tag.experimental)) continue;

			playerData.removeHullMod(hullModSpec.getId());
			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownHullMod(hullModSpec.getId());
		}

		final String targetTag = ehm_settings.getCosmeticsOnly() ? ehm_internals.tag.cosmetic : ehm_internals.tag.experimental;

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!ehm_internals.id.manufacturer.equals(hullModSpec.getManufacturer())) continue;
			if (!hullModSpec.hasTag(ehm_internals.tag.experimental)) continue;
			if (hullModSpec.hasTag(ehm_internals.tag.restricted)) continue;

			if (hullModSpec.hasTag(targetTag)) playerData.addHullMod(hullModSpec.getId());
		}

		lyr_logger.info("Faction blueprints are updated");
	}

	public static void updateHullMods() {
		final SettingsAPI settingsAPI = Global.getSettings();
		Set<String> uiTags;

		if (ehm_settings.getCosmeticsOnly()) {
			uiTags = settingsAPI.getHullModSpec(ehm_internals.id.hullmods.base).getUITags();
			uiTags.clear(); uiTags.add(ehm_internals.tag.ui.cosmetics);

			uiTags = settingsAPI.getHullModSpec(ehm_internals.id.hullmods.undo).getUITags();
			uiTags.clear(); uiTags.add(ehm_internals.tag.ui.cosmetics);
		} else {
			uiTags = settingsAPI.getHullModSpec(ehm_internals.id.hullmods.base).getUITags();
			uiTags.clear(); uiTags.addAll(ehm_internals.tag.ui.all);

			uiTags = settingsAPI.getHullModSpec(ehm_internals.id.hullmods.undo).getUITags();
			uiTags.clear(); uiTags.addAll(ehm_internals.tag.ui.all);
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
		if (!Global.getSector().getPlayerFleet().getAbility(ehm_internals.id.ability).isActive()) return;

		switch (ehm_settings.getShuntAvailability()) {
			case "Always": ehm_submarketInjector.nullify(friend); ehm_shuntInjector.attach(); break;
			case "Submarket": ehm_shuntInjector.nullify(friend); ehm_submarketInjector.attach(); break;
			default: break;
		}
	}
}
