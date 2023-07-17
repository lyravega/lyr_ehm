package lyravega.plugin;

import static lyravega.listeners.lyr_shipTracker.enhancedEvents;
import static lyravega.listeners.lyr_shipTracker.normalEvents;
import static lyravega.listeners.lyr_shipTracker.suppressedEvents;

import org.apache.log4j.Level;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
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
import lyravega.tools.lyr_logger;

public class lyr_ehm extends BaseModPlugin implements lyr_logger {
	static {
		logger.setLevel(Level.ALL);
	}

	@Override
	public void onGameLoad(boolean newGame) {
		teachAbility();
		teachBlueprints();
		replaceFieldRepairsScript(false);
		if (!newGame) attachShuntAccessListener();
	}

	@Override
	public void onApplicationLoad() throws Exception {
		registerHullMods();
		lyr_lunaSettingsListener.attach();
	}

	@Override
	public void configureXStream(XStream x) {
		x.alias("FieldRepairsScript", lyr_fieldRepairsScript.class);
		// remember to use this for serialized shit
		x.alias("data.abilities.ehm_ability", experimentalHullModifications.abilities.ehm_ability.class);
		x.alias("lyr_tabListener", lyravega.listeners.lyr_tabListener.class);	// added transient, but just in case
		x.alias("lyr_colonyInteractionListener", lyravega.listeners.lyr_colonyInteractionListener.class);	// added transient, but just in case
	}

	/**
	 * Adds all mod weapons and hull modifications that have the {@link
	 * lyr_internals.tag#experimental "ehm"} tag to the player's faction.
	 * Removes any known with the {@link lyr_internals.tag#restricted
	 * "ehm_restricted"} ones.
	 */
	private static void teachBlueprints() {
		FactionAPI playerFaction = Global.getSector().getPlayerPerson().getFaction();

		playerFaction.addKnownHullMod(lyr_internals.id.hullmods.base);
		playerFaction.addKnownHullMod(lyr_internals.id.hullmods.undo);

		for (HullModSpecAPI hullModSpec : Global.getSettings().getAllHullModSpecs()) {
			String hullModSpecId = hullModSpec.getId();
			if (hullModSpec.hasTag(lyr_internals.tag.experimental) && !playerFaction.knowsHullMod(hullModSpecId)) playerFaction.addKnownHullMod(hullModSpecId);
			else if (hullModSpec.hasTag(lyr_internals.tag.restricted) && playerFaction.knowsHullMod(hullModSpecId)) playerFaction.removeKnownHullMod(hullModSpecId);
		}

		for (WeaponSpecAPI weaponSpec : Global.getSettings().getAllWeaponSpecs()) {
			if (weaponSpec.hasTag(lyr_internals.tag.experimental) && !playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.addKnownWeapon(weaponSpec.getWeaponId(), false);
			else if (weaponSpec.hasTag(lyr_internals.tag.restricted) && playerFaction.knowsWeapon(weaponSpec.getWeaponId())) playerFaction.removeKnownWeapon(weaponSpec.getWeaponId());
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

		logger.info(logPrefix + "Experimental hull modifications are registered");
	}

	private static void teachAbility() {
		if (!Global.getSector().getCharacterData().getAbilities().contains(lyr_internals.id.ability)) {
			Global.getSector().getCharacterData().addAbility(lyr_internals.id.ability);	// add ability to ongoing games if not present

			logger.info(logPrefix + "Shunt market control ability taught");
		} else logger.info(logPrefix + "Shunt market control was already known");
	}

	private static void replaceFieldRepairsScript(boolean isTransient) {
		if (Global.getSector().hasScript(FieldRepairsScript.class)) {
			Global.getSector().removeScriptsOfClass(FieldRepairsScript.class);
		}

		if (!Global.getSector().hasScript(lyr_fieldRepairsScript.class)) {
			if (isTransient) Global.getSector().addTransientScript(new lyr_fieldRepairsScript());
			else Global.getSector().addScript(new lyr_fieldRepairsScript());
		}

		logger.info(logPrefix + "Replaced 'FieldRepairsScript' with modified one");
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
