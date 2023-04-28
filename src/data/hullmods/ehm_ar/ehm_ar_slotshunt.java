package data.hullmods.ehm_ar;

import static lyr.tools._lyr_uiTools.commitChanges;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import lyr.misc.lyr_internals;
import lyr.proxies.lyr_hullSpec;
import lyr.proxies.lyr_weaponSlot;

/**@category Adapter Retrofit
 * @author lyravega
 */
public class ehm_ar_slotshunt extends _ehm_ar_base {
	private static final Map<WeaponSize,Float> bonus = new HashMap<WeaponSize,Float>();
	static {
		bonus.put(WeaponSize.LARGE, 0.16f);
		bonus.put(WeaponSize.MEDIUM, 0.08f);
		bonus.put(WeaponSize.SMALL, 0.04f);
	}

	/** 
	 * Provides bonuses based on the number of shunts installed in slots.
	 * Shunts are turned into decorative pieces in the process.
	 * @param stats of the ship whose variant / hullSpec will be altered
	 * @param hullModSpecId to properly accumulate the bonuses under the same id
	 */
	private static final void ehm_slotShunt(MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant(); 
		lyr_hullSpec hullSpec = new lyr_hullSpec(variant.getHullSpec(), false);
		boolean refreshRefit = false;
		float capacitorBonus = 1.0f;
		float heatsinkBonus = 1.0f;

		// slot conversion
		for (String slotId: variant.getFittedWeaponSlots()) {
			if (variant.getSlot(slotId) == null) continue;	// short-circuit to avoid a potential null pointer (may/will happen when vanilla hullSpec is (re)loaded)

			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId);
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSpec.getSize().equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (!lyr_internals.id.shunts.hasMutableBonus.set.contains(weaponId)) continue; // to short-circuit the function if it isn't a shunt

			lyr_weaponSlot slot = hullSpec.getWeaponSlot(slotId); 

			slot.setWeaponType(WeaponType.DECORATIVE);
			hullSpec.addBuiltInWeapon(slotId, weaponId);
			refreshRefit = true; 
		}

		// bonus calculation
		for (WeaponSlotAPI slot: variant.getHullSpec().getAllWeaponSlotsCopy()) {
			if (!slot.getWeaponType().equals(WeaponType.DECORATIVE)) continue;	// since activated shunts become decorative, only need to check them

			String slotId = slot.getId();
			WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slotId); if (weaponSpec == null) continue;	// skip empty slots
			WeaponSize weaponSize = weaponSpec.getSize();
			String weaponId = weaponSpec.getWeaponId();

			if (!weaponSize.equals(variant.getSlot(slotId).getSlotSize())) continue; // requires matching slot size
			if (lyr_internals.id.shunts.capacitors.set.contains(weaponId)) capacitorBonus += bonus.get(weaponSize);
			else if (lyr_internals.id.shunts.dissipators.set.contains(weaponId)) heatsinkBonus += bonus.get(weaponSize);
		}

		stats.getFluxCapacity().modifyMult(hullModSpecId, capacitorBonus);
		stats.getFluxDissipation().modifyMult(hullModSpecId, heatsinkBonus);

		variant.setHullSpecAPI(hullSpec.retrieve());
		if (refreshRefit) { refreshRefit = false; ehm_cleanWeaponGroupsUp(variant); commitChanges(); }
	}

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ehm_slotShunt(stats, hullModSpecId); 
	}
}
