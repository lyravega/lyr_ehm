package data.hullmods.ehm_wr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.loading.specs.HullVariantSpec;
import com.fs.starfarer.loading.specs.g;
import com.fs.starfarer.loading.specs.oOoo;

import data.hullmods._ehm_base;
import data.hullmods.ehm_ar._ehm_ar_base;
import data.hullmods.ehm_sr._ehm_sr_base;

/**
 * This class is used by weapon retrofit hullmods. They are pretty 
 * straightforward in their operation; change all of the weapon slots 
 * on a ship to a different type. 
 * </p>
 * Reason to split this as another base was primarily maintenance.
 * @see {@link _ehm_ar_base} for slot adapter base
 * @see {@link _ehm_sr_base} for system retrofit base
 * @author lyravega
 * @version 0.5
 * @since 0.3
 */
public class _ehm_wr_base extends _ehm_base {
	/**
	 * Alters the weapon slots on the passed hullSpec, and returns it. The returned 
	 * hullSpec needs to be installed on the variant.
	 * @param variant that will have its weapon slots altered
	 * @param conversions is a map that pairs slot types
	 * @return a hullSpec to be installed on the variant
	 * @see {@link #ehm_weaponSlotRestore()} reverses this process one slot at a time
	 */
	protected static final g ehm_weaponSlotRetrofit(HullVariantSpec variant, Map<WeaponType, WeaponType> conversions) {
		g hullSpec = variant.getHullSpec();

		for (oOoo slot: hullSpec.getAllWeaponSlots()) {
			WeaponType convertFrom = slot.getWeaponType();
			
			if (conversions.containsKey(convertFrom)) {
				WeaponType convertTo = (WeaponType) conversions.get(convertFrom); // Why is the typecast necessary here? Doesn't '.get()' return a 'WeaponType'?!?
				slot.setWeaponType(convertTo);
			}
		}

		return hullSpec;
	}
	@Deprecated // without obfuscated stuff
	protected static final ShipHullSpecAPI ehm_weaponSlotRetrofit(ShipVariantAPI variantAPI, Map<WeaponType, WeaponType> conversions) {
		HullVariantSpec tempVariant = new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(variantAPI).getHullSpec());

		for (WeaponSlotAPI slotAPI: variantAPI.getHullSpec().getAllWeaponSlotsCopy()) {
			String slotId = slotAPI.getId();
			WeaponType convertFrom = slotAPI.getWeaponType();
			
			if (conversions.containsKey(convertFrom)) {
				WeaponType convertTo = (WeaponType) conversions.get(convertFrom); // Why is the typecast necessary here? Doesn't '.get()' return a 'WeaponType'?!?
				tempVariant.getHullSpec().getWeaponSlot(slotId).setWeaponType(convertTo);
			}
		}

		return tempVariant.getHullSpec();
	}

	/**
	 * Compares the weapon slots of a stock hullSpec to the variant's hullspec. Restores 
	 * altered slots to the originals, ignoring the decorative slots that an adapter 
	 * might have altered. As there might be other things that alter these slots, 
	 * restoring only the necessary ones is preferable.
	 * @param variant with the altered weapon slots
	 * @return the restored hullSpec in near-mint condition
	 * @see {@link data.scripts.shipTrackerScript} only called externally by this script
	 */
	public static final g ehm_weaponSlotRestore(HullVariantSpec variant) {
		g stockHullSpec = ehm_hullSpecClone(variant, true);
		g hullSpec = variant.getHullSpec();

		for (oOoo stockSlot: stockHullSpec.getAllWeaponSlots()) {
			if (!stockSlot.isWeaponSlot()) continue;
			String slotId = stockSlot.getId();
			WeaponType stockSlotWeaponType = stockSlot.getWeaponType();

			if (hullSpec.getWeaponSlot(slotId).isDecorative()) {
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"L").setWeaponType(stockSlotWeaponType);
				hullSpec.getWeaponSlot(ehm.affix.adaptedSlot+slotId+"R").setWeaponType(stockSlotWeaponType);
			} else {
				hullSpec.getWeaponSlot(slotId).setWeaponType(stockSlotWeaponType);
			}
		}

		return hullSpec;
	}
	@Deprecated // without obfuscated stuff
	public static final ShipHullSpecAPI ehm_weaponSlotRestore(ShipVariantAPI variantAPI) {
		ShipHullSpecAPI stockHullSpecAPI = ehm_hullSpecClone(variantAPI, true);
		HullVariantSpec tempVariant = new HullVariantSpec("ehm_tempVariant", HullVariantSpec.class.cast(variantAPI).getHullSpec());
		// ShipHullSpecAPI hullSpecAPI = tempVariant.getHullSpec();

		for (WeaponSlotAPI stockSlotAPI: stockHullSpecAPI.getAllWeaponSlotsCopy()) {
			String slotId = stockSlotAPI.getId();

			if (!weaponTypes.contains(stockSlotAPI.getWeaponType())) continue;
			WeaponType stockSlotWeaponType = stockSlotAPI.getWeaponType();

			if (tempVariant.getHullSpec().getWeaponSlot(slotId).isDecorative()) {
				tempVariant.getHullSpec().getWeaponSlot(ehm.affix.adaptedSlot+slotId+"L").setWeaponType(stockSlotWeaponType);
				tempVariant.getHullSpec().getWeaponSlot(ehm.affix.adaptedSlot+slotId+"R").setWeaponType(stockSlotWeaponType);
			} else {
				tempVariant.getHullSpec().getWeaponSlot(slotId).setWeaponType(stockSlotWeaponType);
			}
		}

		return tempVariant.getHullSpec();
	}

	private static final Set<WeaponType> weaponTypes = new HashSet<WeaponType>();
	static {
		weaponTypes.add(WeaponType.BALLISTIC); 
		weaponTypes.add(WeaponType.ENERGY);
		weaponTypes.add(WeaponType.MISSILE);
		weaponTypes.add(WeaponType.HYBRID);
		weaponTypes.add(WeaponType.COMPOSITE);
		weaponTypes.add(WeaponType.SYNERGY);
		weaponTypes.add(WeaponType.UNIVERSAL);
	}

	//#region INSTALLATION CHECKS
	@Override
	protected String unapplicableReason(ShipAPI ship) {
		if (ship == null) return ehm.excuses.noShip; 

		if (!ehm_hasRetrofitBaseBuiltIn(ship)) return ehm.excuses.lacksBase; 
		if (ehm_hasRetrofitTag(ship, ehm.tag.weaponRetrofit, hullModSpecId)) return ehm.excuses.hasWeaponRetrofit; 

		return null; 
	}

	@Override
	protected String cannotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		HullVariantSpec variant = HullVariantSpec.class.cast(ship.getVariant());
		Collection<String> fittedWeapons = variant.getFittedWeaponSlots();
		fittedWeapons.retainAll(variant.getNonBuiltInWeaponSlots());

		if (!fittedWeapons.isEmpty()) return ehm.excuses.hasWeapons;

		return null;
	}
	//#endregion
}