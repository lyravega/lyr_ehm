package lyravega.plugin;

import static lyravega.listeners.lyr_shipTracker.allRegistered;
import static lyravega.listeners.lyr_shipTracker.enhancedEvents;
import static lyravega.listeners.lyr_shipTracker.normalEvents;
import static lyravega.listeners.lyr_shipTracker.suppressedEvents;

import org.apache.log4j.Level;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
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
		teachBlueprints();
		replaceFieldRepairsScript();
		attachShuntAccessListener();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		registerHullMods();
		lyr_settings.attach();
	}

	@Override
	public void configureXStream(XStream x) {
		x.alias("FieldRepairsScript", lyr_fieldRepairsScript.class);
		x.alias("data.abilities.ehm_ability", ehm_ability.class);	// remember to use this for serialized shit
		x.alias("lyr_tabListener", lyr_tabListener.class);	// added transient, but just in case
		x.alias("lyr_colonyInteractionListener", lyr_colonyInteractionListener.class);	// added transient, but just in case
	}

	/**
	 * Adds all mod weapons and hull modifications that have the passed
	 * tagToLearn, and removes any known ones with the tagToForget
	 * @param tagToLearn
	 * @param tagToForget
	 */
	static void teachBlueprints() {
		final String targetTag = settings.getCosmeticsOnly() ? lyr_internals.tag.cosmetic : lyr_internals.tag.experimental;
		// FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();
		CharacterDataAPI playerData = Global.getSector().getCharacterData();

		// purge weapon blueprints
		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (!weaponSpec.getManufacturer().equals(lyr_internals.id.manufacturer)) continue;

			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		// purge hullmod blueprints
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.getManufacturer().equals(lyr_internals.id.manufacturer)) continue;

			playerData.removeHullMod(hullModSpec.getId());
			for (FactionAPI faction : Global.getSector().getAllFactions())
				faction.removeKnownHullMod(hullModSpec.getId());
		}

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.getManufacturer().equals(lyr_internals.id.manufacturer)) continue;

			if (hullModSpec.hasTag(targetTag)) playerData.addHullMod(hullModSpec.getId());
		}

		logger.info(logPrefix + "Faction blueprints are updated");
	}

	/**
	 * Searches all hull modification effect classes and their superclasses for any interfaces
	 * they implement. If any one of them has implemented an event interface, adds them to
	 * their respective sets; registers them in essence.
	 * <p> During tracking, if any one of these events are detected, the relevant event methods
	 * will be triggered as long as the hull mod's effect has implemented the interfaces.
	 * @see {@link lyr_ehm.hullmods.ehm.ehm_base ehm_base} base hull modification that enables tracking
	 * @see {@link normalEvents} / {@link enhancedEvents} / {@link suppressedEvents}
	 */
	private static void registerHullMods() {
		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			if (!hullModSpec.hasTag(lyr_internals.tag.experimental)) continue;
			// if (cosmeticsOnly && !hullModSpec.hasUITag("Cosmetics")) continue;

			HullModEffect hullModEffect = hullModSpec.getEffect();

			if (normalEvents.class.isInstance(hullModEffect)) normalEvents.put(hullModSpec.getId(), (normalEvents) hullModEffect);
			if (enhancedEvents.class.isInstance(hullModEffect)) enhancedEvents.put(hullModSpec.getId(), (enhancedEvents) hullModEffect);
			if (suppressedEvents.class.isInstance(hullModEffect)) suppressedEvents.put(hullModSpec.getId(), (suppressedEvents) hullModEffect);
		}

		allRegistered.addAll(normalEvents.keySet());
		allRegistered.addAll(enhancedEvents.keySet());
		allRegistered.addAll(suppressedEvents.keySet());

		logger.info(logPrefix + "Experimental hull modifications are registered");
	}

	private static void teachAbility(String abilityId) {	// add ability to ongoing games if not present
		if (!Global.getSector().getCharacterData().getAbilities().contains(abilityId)
		 || !Global.getSector().getPlayerFleet().hasAbility(abilityId)) {
			Global.getSector().getCharacterData().addAbility(abilityId);	
			Global.getSector().getPlayerFleet().addAbility(abilityId);

			logger.info(logPrefix + "Ability with the id '"+abilityId+"' taught");
		} else logger.info(logPrefix + "Ability with the id '"+abilityId+"' was already known");
	}

	private static void replaceFieldRepairsScript() {
		if (Global.getSettings().getModManager().isModEnabled("QualityCaptains")) {
			if (Global.getSector().hasScript(lyr_fieldRepairsScript.class)) {
				Global.getSector().removeScriptsOfClass(lyr_fieldRepairsScript.class);
				logger.info(logPrefix + "Removing modified 'FieldRepairsScript' replacement from this mod");
			}

			logger.info(logPrefix + "Skipping 'FieldRepairsScript' replacement as 'Quality Captains' is detected");
			return;
		}

		if (Global.getSector().hasScript(FieldRepairsScript.class)) Global.getSector().removeScriptsOfClass(FieldRepairsScript.class);
		if (!Global.getSector().hasScript(lyr_fieldRepairsScript.class)) Global.getSector().addScript(new lyr_fieldRepairsScript());
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
}
