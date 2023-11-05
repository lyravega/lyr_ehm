package lyravega.misc;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

public class lyr_upgradeLayer {
	private final lyr_upgrade upgrade; public lyr_upgrade getUpgrade() { return this.upgrade; }
	private final int tier; public int getTier() { return this.tier; }
	private final Map<String, Integer> commodityCosts; public Map<String, Integer> getCommodityCosts() { return this.commodityCosts; }
	private final Set<String> specialRequirements; public Set<String> getSpecialRequirements() { return this.specialRequirements; }
	private final int storyPointCost; public int getStoryPointCost() { return this.storyPointCost; }

	public lyr_upgradeLayer(lyr_upgrade upgrade, int level, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost) {
		Map<String, Integer> commodityCosts = null;
		Set<String> specialRequirements = null;

		if (commodityCostsArray != null) for (Object[] commodityCostArray: commodityCostsArray) {
			if (commodityCosts == null) commodityCosts = new HashMap<String, Integer>();

			commodityCosts.put(String.class.cast(commodityCostArray[0]), Integer.class.cast(commodityCostArray[1]));
		}

		if (specialRequirementsArray != null) for (String specialId : specialRequirementsArray) {
			if (specialRequirements == null) specialRequirements = new HashSet<String>();

			specialRequirements.add(specialId);
		}

		this.upgrade = upgrade;
		this.tier = level;
		this.commodityCosts = commodityCosts;
		this.specialRequirements = specialRequirements;
		this.storyPointCost = storyPointCost != null ? Math.max(0, storyPointCost) : 0;
	}

	public boolean canAfford() {
		MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		if (this.storyPointCost > 0) {
			if (playerStats.getStoryPoints() < this.storyPointCost) {
				return false;
			}
		}

		if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
			for (String commodityCostId : this.commodityCosts.keySet()) {
				int cost = this.commodityCosts.get(commodityCostId);

				if (playerCargo.getCommodityQuantity(commodityCostId) < cost) {
					return false;
				}
			}
		}

		if (this.specialRequirements != null && !this.specialRequirements.isEmpty()) {
			// for (String specialRequirementId : this.specialRequirements) {
			// 	if (playerCargo.getCommodityQuantity(specialRequirementId) < 1) {
			// 		return false;
			// 	}
			// }
			// List<CargoStackAPI> specials = playerCargo.getStacksCopy();

			Set<String> specials = new HashSet<String>();

			for (Iterator<CargoStackAPI> iterator = playerCargo.getStacksCopy().iterator(); iterator.hasNext(); ) {
				CargoStackAPI stack = iterator.next();

				if (stack.getType() != CargoItemType.SPECIAL) { iterator.remove(); continue; }

				specials.add(stack.getSpecialDataIfSpecial().getId());
			}

			for (String specialId : this.specialRequirements) {
				if (!specials.contains(specialId)) return false;
			}
		}

		return true;
	}

	public void deductCosts() {
		MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		if (this.storyPointCost > 0) {
			if (playerStats.getStoryPoints() >= this.storyPointCost) {
				playerStats.spendStoryPoints(this.storyPointCost, true, null, true, 0f, this.upgrade.id+":"+this.tier);
			}
		}

		if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
			for (String commodityCostId : this.commodityCosts.keySet()) {
				int cost = this.commodityCosts.get(commodityCostId);

				if (playerCargo.getCommodityQuantity(commodityCostId) >= cost) {
					playerCargo.removeCommodity(commodityCostId, cost);
				}
			}
		}
	}
}