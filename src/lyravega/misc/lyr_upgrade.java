package lyravega.misc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class lyr_upgrade {
	final String id;
	private final EnumMap<HullSize, ArrayList<lyr_upgradeLayer>> upgradeLayers;

	public lyr_upgrade(String id) {
		this.id = id;
		this.upgradeLayers = new EnumMap<HullSize, ArrayList<lyr_upgradeLayer>>(HullSize.class);
	}

	public ArrayList<lyr_upgradeLayer> getUpgradeLayers(HullSize hullSize) { return this.upgradeLayers.get(hullSize); }

	public lyr_upgradeLayer getUpgradeLayer(HullSize hullSize, int i) { return this.upgradeLayers.get(hullSize).get(i); }

	public void addUpgradeTier(HullSize hullSize, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost) {
		if (hullSize == null) hullSize = HullSize.DEFAULT;

		if (this.upgradeLayers.get(hullSize) == null) {
			this.upgradeLayers.put(hullSize, new ArrayList<lyr_upgradeLayer>());
		}

		this.getUpgradeLayers(hullSize).add(new lyr_upgradeLayer(this, this.getMaxTier(hullSize)+1, commodityCostsArray, specialRequirementsArray, storyPointCost));
	}

	public lyr_upgradeLayer getCurrentLayer(ShipVariantAPI variant) {
		return this.getUpgradeLayer(variant.getHullSize(), this.getCurrentTier(variant)-1);
	}

	@Deprecated
	public lyr_upgradeLayer getNextLayer(ShipVariantAPI variant) {
		return this.getUpgradeLayer(variant.getHullSize(), this.getCurrentTier(variant));
	}

	public int getMaxTier(HullSize hullSize) {
		return this.getUpgradeLayers(hullSize).size();
	}

	public int getCurrentTier(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (!tag.startsWith(this.id+":")) continue;

			return Integer.valueOf(tag.replace(this.id+":", ""));
		};	return 0;	// first layer is 0 due to array, no tier is -1 because of that
	}

	public boolean canUpgradeTier(ShipVariantAPI variant) {
		int currentLevel = this.getCurrentTier(variant)-1;
		HullSize hullSize = variant.getHullSize();

		return currentLevel < this.getMaxTier(hullSize) && this.getUpgradeLayer(hullSize, currentLevel+1).canAfford();
	}

	public void upgradeTier(ShipVariantAPI variant, boolean upgrade) {
		int level = 0;	// first layer is 0 due to array, no tier is -1 because of that

		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); ) {
			String tag = iterator.next();
			if (!tag.startsWith(this.id+":")) continue;

			level = Integer.valueOf(tag.replace(this.id+":", ""));
			iterator.remove(); break;
		};	level += 1;	// set to next level here

		variant.addTag(this.id+":"+(level));
		this.getUpgradeLayer(variant.getHullSize(), level).deductCosts();
	}
}