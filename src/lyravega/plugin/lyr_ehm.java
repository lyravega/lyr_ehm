package lyravega.plugin;

import static lyravega.listeners.lyr_shipTracker.enhancedEvents;
import static lyravega.listeners.lyr_shipTracker.normalEvents;
import static lyravega.listeners.lyr_shipTracker.suppressedEvents;
import static lyravega.listeners.lyr_shipTracker.allRegistered;

import org.apache.log4j.Level;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.VersionInfoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.impl.campaign.skills.FieldRepairsScript;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.thoughtworks.xstream.XStream;

import lyravega.listeners.lyr_colonyInteractionListener;
import lyravega.listeners.lyr_lunaSettingsListener;
import lyravega.listeners.lyr_tabListener;
import lyravega.listeners.events.enhancedEvents;
import lyravega.listeners.events.normalEvents;
import lyravega.listeners.events.suppressedEvents;
import lyravega.misc.lyr_internals;
import lyravega.scripts.lyr_fieldRepairsScript;
import lyravega.scripts.lyr_qualityCaptainsTempFix;
import lyravega.tools.lyr_logger;

public class lyr_ehm extends BaseModPlugin implements lyr_logger {
	static {
		logger.setLevel(Level.ALL);
	}

	@Override
	public void onGameLoad(boolean newGame) {
		teachAbility(lyr_internals.id.ability);
		teachBlueprints(lyr_internals.tag.experimental, lyr_internals.tag.restricted);
		replaceFieldRepairsScript();
		attachShuntAccessListener();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		registerHullMods();
		lyr_lunaSettingsListener.attach();
	}

	@Override
	public void configureXStream(XStream x) {
		x.alias("FieldRepairsScript", lyr_fieldRepairsScript.class);
		x.alias("data.abilities.ehm_ability", experimentalHullModifications.abilities.ehm_ability.class);	// remember to use this for serialized shit
		x.alias("lyr_tabListener", lyravega.listeners.lyr_tabListener.class);	// added transient, but just in case
		x.alias("lyr_colonyInteractionListener", lyravega.listeners.lyr_colonyInteractionListener.class);	// added transient, but just in case
	}

	/**
	 * Adds all mod weapons and hull modifications that have the passed
	 * tagToLearn, and removes any known ones with the tagToForget
	 * @param tagToLearn
	 * @param tagToForget
	 */
	private static void teachBlueprints(String tagToLearn, String tagToForget) {
		FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			String hullModSpecId = hullModSpec.getId();
			if (hullModSpec.hasTag(tagToLearn) && !playerFaction.knowsHullMod(hullModSpecId)) playerFaction.addKnownHullMod(hullModSpecId);
			else if (hullModSpec.hasTag(tagToForget) && playerFaction.knowsHullMod(hullModSpecId)) playerFaction.removeKnownHullMod(hullModSpecId);
		}

		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (weaponSpec.hasTag(tagToLearn) && !playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.addKnownWeapon(weaponSpec.getWeaponId(), false);
			else if (weaponSpec.hasTag(tagToForget) && playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.removeKnownWeapon(weaponSpec.getWeaponId());
		}

		logger.info(logPrefix + "Player faction blueprints are updated");
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
			VersionInfoAPI version = Global.getSettings().getModManager().getModSpec("QualityCaptains").getVersionInfo();

			boolean disableFix = lyr_lunaSettingsListener.disableQualityCaptainsTempFix;
			boolean requiresFix;

			try {
				requiresFix = ((Integer.parseInt(version.getMajor()) << 16) + (Integer.parseInt(version.getMinor()) << 8) + Integer.parseInt(version.getPatch()) - 66818) <= 0;	// 66818 is shifted 1.5.2
			} catch (Exception e) {	// will throw an exception if the version info has any letters in it
				requiresFix = true;
			}

			if (!disableFix && requiresFix) {
				if (!Global.getSector().hasTransientScript(lyr_qualityCaptainsTempFix.class)) {
					Global.getSector().addTransientScript(new lyr_qualityCaptainsTempFix());
					logger.warn(logPrefix + "Suppressing 'FieldRepairScript' replacement from 'Quality Captains v"+version.getString()+"'. Version threshold is 'v1.5.2'");
				}
			} else {
				if (Global.getSector().hasScript(lyr_fieldRepairsScript.class)) {
					Global.getSector().removeScriptsOfClass(lyr_fieldRepairsScript.class);
					logger.info(logPrefix + "Removing modified 'FieldRepairsScript' replacement from this mod");
				}

				logger.info(logPrefix + "Skipping 'FieldRepairsScript' replacement as 'Quality Captains v"+version.getString()+"' is detected" + (disableFix ? ". Skip is forced" : ""));
				return;
			}
		}

		if (Global.getSector().hasScript(FieldRepairsScript.class)) {
			Global.getSector().removeScriptsOfClass(FieldRepairsScript.class);

			if (!Global.getSector().hasScript(lyr_fieldRepairsScript.class)) {
				Global.getSector().addScript(new lyr_fieldRepairsScript());
				
				logger.info(logPrefix + "Replaced 'FieldRepairsScript' with modified one");
			}
		}
	}

	public static void attachShuntAccessListener() {
		lyr_tabListener.detach();
		lyr_colonyInteractionListener.detach();

		switch (lyr_lunaSettingsListener.shuntAvailability) {
			case "Always": lyr_tabListener.attach(true); break;
			case "Submarket": lyr_colonyInteractionListener.attach(true); break;
			default: break;
		}
	}
}
