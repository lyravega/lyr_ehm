package experimentalHullModifications.hullmods.ehm;

import static lyravega.utilities.lyr_interfaceUtilities.commitVariantChanges;
import static lyravega.utilities.lyr_interfaceUtilities.playDrillSound;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.loading.specs.g;

import experimentalHullModifications.proxies.ehm_hullSpec;
import lyravega.listeners.events.normalEvents;
import lyravega.utilities.logger.lyr_logger;

/**
 * Serves as a requirement for all experimental hull modifications, and enables tracking
 * on the ship. Some hull modification effects are also executed from here, and the
 * actual hull modifications only contribute to their tooltips and used for installation
 * checks.
 * @category Base Hull Modification
 * @author lyravega
 */
public final class ehm_module_base extends _ehm_base implements normalEvents {
	//#region CUSTOM EVENTS
	@Override
	public void onInstalled(MutableShipStatsAPI stats) {
		commitVariantChanges(); playDrillSound();
	}

	@Override public void onRemoved(MutableShipStatsAPI stats) {}	// cannot be removed since it becomes a built-in
	//#endregion
	// END OF CUSTOM EVENTS

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();

		ehm_hullSpec hullSpecProxy = new ehm_hullSpec(stats.getVariant().getHullSpec(), false);
		hullSpecProxy.setHullName("Mini Module");
		// hullSpecProxy.addBuiltInMod("axialrotation");
		// hullSpecProxy.getHints().add(ShipTypeHints.INDEPENDENT_ROTATION);
		hullSpecProxy.getHints().add(ShipTypeHints.PLAY_FIGHTER_OVERLOAD_SOUNDS);
		g test = (g) hullSpec;
		test.setDesignation("");
		ShipTypeHints next = hullSpecProxy.getHints().iterator().next();
		Vector2f moduleAnchor = hullSpecProxy.retrieve().getModuleAnchor();

		// if (!lyr_interfaceUtilities.isRefitTab()) {
			// lyr_hullSpec.setHullSize(HullSize.FRIGATE);
		// }

		// for (String tag : variant.getTags()) {
		// 	if (!tag.startsWith("ehm_module_parentShield")) continue;

		// 	Pattern pattern = Pattern.compile(".*:(.*?)/(.*?)/(.*)");
		// 	Matcher matcher = pattern.matcher(tag);

		// 	float shieldCenterX = 0f;
		// 	float shieldCenterY = 0f;
		// 	float shieldRadius = 0f;

		// 	while(matcher.find()) {
		// 		shieldCenterX = Float.parseFloat(matcher.group(1));
		// 		shieldCenterY = Float.parseFloat(matcher.group(2));
		// 		shieldRadius = Float.parseFloat(matcher.group(3));
		// 	};

		// 	lyr_hullSpec.getShieldSpec().setCenterX(shieldCenterX);
		// 	lyr_hullSpec.getShieldSpec().setCenterY(shieldCenterY);
			// hullSpecProxy.getShieldSpec().setRadius(shieldRadius+15);
			// stats.getShieldTurnRateMult().modifyMult(this.hullModSpecId, 10);
			// hullSpecProxy.getShieldSpec().setArc(30f);

		// 	// Object spriteSpec = lyr_hullSpec.getSpriteSpec();
		// 	// MethodHandle testt = null;
		// 	// try {
		// 	// 	testt = lyr_reflectionUtilities.methodReflection.findMethodByClass(spriteSpec, null, float.class).getMethodHandle();
		// 	// 	testt.invoke(spriteSpec, 350f);
		// 	// } catch (Throwable e) {
		// 	// 	e.printStackTrace();
		// 	// }

		// 	break;
		// }

		// _ehm_ar_base.ehm_preProcessShunts(stats);	// at this point, the hull spec should be cloned so proceed and pre-process the shunts
		// lyr_miscUtilities.cleanWeaponGroupsUp(variant);	// when an activator activates shunts on install, so moved this to their 'onInstalled()' method
		variant.setHullSpecAPI(hullSpecProxy.retrieve());
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI module, String hullModSpecId) {
		this.dronify(module);
	}

	@Override
	public void advanceInCombat(ShipAPI module, float amount) {
		this.dronify(module);

		// boolean drone = ship.isDrone();
		// ShipAPI parentShip = ship.getParentStation();
		// ShipAPI parentShipTarget = parentShip.getShipTarget();
		// ShipAPI shipTarget = ship.getShipTarget();
		// Vector2f shieldTarget = ship.getShieldTarget();
		// // if (ship.getShipTarget() == null)
		// // else
		// List<WeaponGroupAPI> weaponGroupsCopy = ship.getWeaponGroupsCopy();
		// Vector2f target = weaponGroupsCopy.iterator().next().getAIPlugins().iterator().next().getTarget();
		// MissileAPI targetMissile = weaponGroupsCopy.iterator().next().getAIPlugins().iterator().next().getTargetMissile();
		// ShipAPI targetShip = weaponGroupsCopy.iterator().next().getAIPlugins().iterator().next().getTargetShip();

		// if (targetShip != null) {
		// 	WeaponSlotAPI stationSlot = ship.getStationSlot();
		// 	Vector2f slotPosition = ship.getStationSlot().computePosition(ship);
		// 	float angle = Math.round(Math.toRadians(Vector2f.angle(slotPosition, targetShip.getLocation())));
		// 	ship.setFacing(angle);
		// 	// ship.setFacing(ship.getStationSlot().computeMidArcAngle(targetShip));
		// }
		// else
		// 	ship.setFacing(parentShip.getFacing());
	}

	/**
	 * Changes some of the module parameters to fool the AI into thinking these modules are not a big
	 * threat. If not done so, the AI will look at the size of the hulls and whatnot, and will not
	 * engage properly.
	 * <p> For redundancy, this method should be called from both the {@code advanceInCombat()}
	 * and {@code applyEffectsAfterShipCreation()}. Simulation and combat have different behaviours,
	 * and the first caller may be different for each.
	 * <p> In both cases, the module's collision class is set to 'SHIP', hull size is set to
	 * 'FIGHTER' and 'isDrone' boolean is set to {@code true}. Latter is also used for checks.
	 * Modifications are done once, then the checks will fail.
	 * <p> The method also includes a block that is used for redundancy; in case the above changes
	 * persist outside combat, the block will reset the changes back to their originals. Hull size
	 * especially have issues in refit tab if it's left as 'FIGHTER'.
	 * <p> The redundancy block is effectively a dead code block as different ships will be recreated
	 * for combat, and refit will retain the non-modified versions since the changes are applied in
	 * combat. But it's there just in case.
	 * @param module
	 */
	protected void dronify(ShipAPI module) {
		if (module.isDrone() && Global.getCurrentState() != GameState.COMBAT) {	// this block is for redundancy; it covers just-in-case scenarios but is effectively a dead code block
			module.setHullSize(HullSize.FRIGATE);	// reset hullsize; in combat, is set to FIGHTER
			module.setCollisionClass(CollisionClass.SHIP);	// reset collision; redundant as it does not change, but just in case
			module.setDrone(false);	// reset boolean; used as a control for the combat check
			lyr_logger.debug("Changing module parameters to 'NON-COMBAT'; 'HullSize."+module.getHullSize().name()+"', 'CollisionClass."+module.getCollisionClass().name()+"', 'isDrone = "+module.isDrone()+"'");
		} else if (!module.isDrone() && Global.getCurrentState() == GameState.COMBAT) {	// in simulation 'advanceInCombat()' will fall here first, in combat 'applyEffectsAfterShipCreation()' will
			module.setHullSize(HullSize.FIGHTER);	// this messes with the refit UI; needs to get reset
			module.setCollisionClass(CollisionClass.SHIP);	// required alongside FIGHTER to fix their collisions
			module.setDrone(true);	// used in the check, primarily here to mess with AI
			lyr_logger.debug("Changing module parameters to 'COMBAT'; 'HullSize."+module.getHullSize().name()+"', 'CollisionClass."+module.getCollisionClass().name()+"', 'isDrone = "+module.isDrone()+"'");
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		return true;
	}
}
