package lyravega.plugin;

import static lyravega.listeners.lyr_shipTracker.allModEvents;
import static lyravega.listeners.lyr_shipTracker.enhancedEvents;
import static lyravega.listeners.lyr_shipTracker.normalEvents;
import static lyravega.listeners.lyr_shipTracker.suppressedEvents;
import static lyravega.listeners.lyr_shipTracker.weaponEvents;

import java.util.Set;

import org.apache.log4j.Level;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.thoughtworks.xstream.XStream;

import experimentalHullModifications.abilities.ehm_ability;
import lyravega.listeners.lyr_colonyInteractionListener;
import lyravega.listeners.lyr_tabListener;
import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.listeners.events.suppressedEvents;
import lyravega.listeners.events.weaponEvents;
import lyravega.misc.lyr_internals;
import lyravega.scripts.lyr_fieldRepairsScript;
import lyravega.tools.lyr_logger;

public class lyr_ehm extends BaseModPlugin implements lyr_logger {
	public static final lyr_settings settings = new lyr_settings();
	static {
		logger.setLevel(Level.ALL);
	}

	@Override
	public void onGameLoad(boolean newGame) {
		teachAbility(lyr_internals.id.ability);
		updateBlueprints();
		replaceFieldRepairsScript();
		attachShuntAccessListener();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		lyr_settings.attach();
		updateHullMods();
		registerModsWithEvents();
	}

	@Override
	public void configureXStream(XStream x) {
		x.alias("FieldRepairsScript", lyr_fieldRepairsScript.class);
		x.alias("data.abilities.ehm_ability", ehm_ability.class);	// remember to use this for serialized shit
		x.alias("lyr_tabListener", lyr_tabListener.class);	// added transient, but just in case
		x.alias("lyr_colonyInteractionListener", lyr_colonyInteractionListener.class);	// added transient, but just in case
	}

	/**
	 * Purges all of the experimental stuff from all factions' known lists,
	 * then adds valid experimental hull modifications to player's faction
	 */
	static void updateBlueprints() {
		// FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();
		CharacterDataAPI playerData = Global.getSector().getCharacterData();

		// purge experimental weapon blueprints
		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (!isExperimental(weaponSpec, false)) continue;

			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		// purge experimental hullmod blueprints
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!isExperimental(hullModSpec, false)) continue;

			playerData.removeHullMod(hullModSpec.getId());
			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownHullMod(hullModSpec.getId());
		}

		final String targetTag = settings.getCosmeticsOnly() ? lyr_internals.tag.cosmetic : lyr_internals.tag.experimental;

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!isExperimental(hullModSpec, true)) continue;

			if (hullModSpec.hasTag(targetTag)) playerData.addHullMod(hullModSpec.getId());
		}

		logger.info(logPrefix + "Faction blueprints are updated");
	}

	static void updateHullMods() {
		final SettingsAPI settingsAPI = Global.getSettings();
		Set<String> uiTags;

		if (lyr_ehm.settings.getCosmeticsOnly()) {
			uiTags = settingsAPI.getHullModSpec(lyr_internals.id.hullmods.base).getUITags();
			uiTags.clear(); uiTags.add(lyr_internals.tag.ui.cosmetics);

			uiTags = settingsAPI.getHullModSpec(lyr_internals.id.hullmods.undo).getUITags();
			uiTags.clear(); uiTags.add(lyr_internals.tag.ui.cosmetics);
		} else {
			uiTags = settingsAPI.getHullModSpec(lyr_internals.id.hullmods.base).getUITags();
			uiTags.clear(); uiTags.addAll(lyr_internals.tag.ui.all);

			uiTags = settingsAPI.getHullModSpec(lyr_internals.id.hullmods.undo).getUITags();
			uiTags.clear(); uiTags.addAll(lyr_internals.tag.ui.all);
		}
	}

	/**
	 * Checks all of the hullmod effects and if they have implemented any events, registers them
	 * in their map. During tracking, if any one of these events are detected, the relevant event
	 * methods will be called
	 * @see {@link lyr_ehm.hullmods.ehm.ehm_base ehm_base} base hull modification that enables tracking
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents}
	 */
	private static void registerModsWithEvents() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!isExperimental(hullModSpec, true)) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (weaponEvents.class.isInstance(hullModEffect)) weaponEvents.put(hullModSpec.getId(), (weaponEvents) hullModEffect);
			if (normalEvents.class.isInstance(hullModEffect)) normalEvents.put(hullModSpec.getId(), (normalEvents) hullModEffect);
			if (enhancedEvents.class.isInstance(hullModEffect)) enhancedEvents.put(hullModSpec.getId(), (enhancedEvents) hullModEffect);
			if (suppressedEvents.class.isInstance(hullModEffect)) suppressedEvents.put(hullModSpec.getId(), (suppressedEvents) hullModEffect);
		}

		allModEvents.addAll(normalEvents.keySet());
		allModEvents.addAll(enhancedEvents.keySet());
		allModEvents.addAll(suppressedEvents.keySet());

		logger.info(logPrefix + "Experimental hull modifications are registered");
	}

	private static void teachAbility(String abilityId) {	// add ability to ongoing games if not present
		final CharacterDataAPI characterData = Global.getSector().getCharacterData();
		final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

		if (!characterData.getAbilities().contains(abilityId)
		 || !playerFleet.hasAbility(abilityId)) {
			characterData.addAbility(abilityId);	
			playerFleet.addAbility(abilityId);

			logger.info(logPrefix + "Ability with the id '"+abilityId+"' taught");
		} else logger.info(logPrefix + "Ability with the id '"+abilityId+"' was already known");
	}

	private static void replaceFieldRepairsScript() {
		final SectorAPI sector = Global.getSector();

		if (Global.getSettings().getModManager().isModEnabled("QualityCaptains")) {
			if (sector.hasScript(lyr_fieldRepairsScript.class)) {
				sector.removeScriptsOfClass(lyr_fieldRepairsScript.class);
				logger.info(logPrefix + "Removing modified 'FieldRepairsScript' replacement from this mod");
			}

			logger.info(logPrefix + "Skipping 'FieldRepairsScript' replacement as 'Quality Captains' is detected");
			return;
		}

		if (sector.hasScript(FieldRepairsScript.class)) sector.removeScriptsOfClass(FieldRepairsScript.class);
		if (!sector.hasScript(lyr_fieldRepairsScript.class)) sector.addScript(new lyr_fieldRepairsScript());
		logger.info(logPrefix + "Replaced 'FieldRepairsScript' with modified one");
	}

	static void attachShuntAccessListener() {
		lyr_tabListener.detach();
		lyr_colonyInteractionListener.detach();

		switch (settings.getShuntAvailability()) {
			case "Always": lyr_tabListener.attach(true); break;
			case "Submarket": lyr_colonyInteractionListener.attach(true); break;
			default: break;
		}
	}

	private static boolean isExperimental(HullModSpecAPI spec, boolean excludeRestricted) {
		if (!spec.getManufacturer().equals(lyr_internals.id.manufacturer)) return false;
		if (!spec.hasTag(lyr_internals.tag.experimental)) return false;
		if (excludeRestricted && spec.hasTag(lyr_internals.tag.restricted)) return false;
		return true;
	}

	private static boolean isExperimental(WeaponSpecAPI spec, boolean excludeRestricted) {
		if (!spec.getManufacturer().equals(lyr_internals.id.manufacturer)) return false;
		if (!spec.hasTag(lyr_internals.tag.experimental)) return false;
		if (excludeRestricted && spec.hasTag(lyr_internals.tag.restricted)) return false;
		return true;
	}
}
