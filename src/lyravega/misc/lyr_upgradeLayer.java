package lyravega.misc;

import static lyravega.utilities.lyr_tooltipUtilities.regexColour.highlightText;
import static lyravega.utilities.lyr_tooltipUtilities.regexColour.positiveOrNegativeText;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lyravega.utilities.lyr_tooltipUtilities;

/**
 * A class that is dedicated to house details of a single upgrade layer, and provide methods to check
 * and/or deduct costs, print cost on a tooltip and whatnot.
 * <p> Even though there is no inheritance between this and the {@link lyr_upgrade} class, they work
 * in conjunction. In essence, an {@link lyr_upgrade} instance is a storage for many of these.
 * @author lyravega
 * @see {@link lyr_upgrade} / {@link lyr_upgradeVault} / {@link _lyr_upgradeEffect}
 */
public class lyr_upgradeLayer {
	private final lyr_upgrade upgrade; public lyr_upgrade getUpgrade() { return this.upgrade; }
	private final int tier; public int getTier() { return this.tier; }
	private final String id; public String getId() { return this.id; }
	private final String name; public String getName() { return this.name; }
	private final Map<String, Integer> commodityCosts; public Map<String, Integer> getCommodityCosts() { return this.commodityCosts; }
	private final Set<String> specialRequirements; public Set<String> getSpecialRequirements() { return this.specialRequirements; }
	private final int storyPointCost; public int getStoryPointCost() { return this.storyPointCost; }

	public lyr_upgradeLayer(lyr_upgrade upgrade, int tier, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost) {
		Map<String, Integer> commodityCosts = null;
		Set<String> specialRequirements = null;

		if (commodityCostsArray != null) for (Object[] commodityCostArray: commodityCostsArray) {
			if (commodityCosts == null) commodityCosts = new LinkedHashMap<String, Integer>();

			commodityCosts.put(String.class.cast(commodityCostArray[0]), Integer.class.cast(commodityCostArray[1]));
		}

		if (specialRequirementsArray != null) for (String specialId : specialRequirementsArray) {
			if (specialRequirements == null) specialRequirements = new HashSet<String>();

			specialRequirements.add(specialId);
		}

		this.upgrade = upgrade;
		this.tier = tier;
		this.id = upgrade.getId()+":"+tier;
		this.name = upgrade.getName()+this.toRoman(tier);
		this.commodityCosts = commodityCosts;
		this.specialRequirements = specialRequirements;
		this.storyPointCost = storyPointCost != null ? Math.max(0, storyPointCost) : 0;
	}

	private String toRoman(int num) {
		switch (num) {
			case 1: return " I";
			case 2: return " II";
			case 3: return " III";
			case 4: return " IV";
			case 5: return " V";
			default: return "";
		}
	}

	public boolean canAfford() {
		final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

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
			Set<String> specials = new HashSet<String>();

			for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
				if (stack.getType() != CargoItemType.SPECIAL) continue;

				switch (stack.getSpecialDataIfSpecial().getId()) {
					case "pristine_nanoforge": specials.add("corrupted_nanoforge");
					default: specials.add(stack.getSpecialDataIfSpecial().getId()); break;
				}
			}

			for (String specialId : this.specialRequirements) {
				if (!specials.contains(specialId)) return false;
			}
		}

		return true;
	}

	public void deductCosts() {
		final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		if (this.storyPointCost > 0) {
			if (playerStats.getStoryPoints() >= this.storyPointCost) {
				playerStats.spendStoryPoints(this.storyPointCost, true, null, true, 0f, this.upgrade.getId()+":"+this.tier);
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

		// specials are not consumed
	}

	public LabelAPI addRequirementsToTooltip(TooltipMakerAPI tooltip, boolean isDisabled) {	// isDisabled is a shitty "shortcut" to paint shit grayed out instead
		final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		String format = "";

		if (this.storyPointCost > 0) {
			if (isDisabled) {
				format = (format.isEmpty() ? "Tier "+(this.tier)+": " : format+" & ")
					+this.storyPointCost+" SP";
			} else {
				format = (format.isEmpty() ? highlightText("Tier "+(this.tier))+": " : format+" & ")
					+positiveOrNegativeText(
						playerStats.getStoryPoints() >= this.storyPointCost,
						this.storyPointCost
						+" ("+playerStats.getStoryPoints()+") "
						+"SP"
					);
			}
		}

		if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
			if (isDisabled) {
				format = (format.isEmpty() ? "Tier "+(this.tier)+": " : format+" & ");

				for (Iterator<String> iterator = this.commodityCosts.keySet().iterator(); iterator.hasNext(); ) {
					String commodityCostId = iterator.next();

					format = format
						+this.commodityCosts.get(commodityCostId)+" "
						+Global.getSettings().getCommoditySpec(commodityCostId).getName();

					if (iterator.hasNext()) format = format+", ";
				}
			} else {
				format = (format.isEmpty() ? highlightText("Tier "+(this.tier))+": " : format+" & ");

				for (Iterator<String> iterator = this.commodityCosts.keySet().iterator(); iterator.hasNext(); ) {
					String commodityCostId = iterator.next();
					int cost = this.commodityCosts.get(commodityCostId);
					int quantity = Math.round(playerCargo.getCommodityQuantity(commodityCostId));

					format = format
						+positiveOrNegativeText(
							quantity >= cost,
							// (quantity < cost ? quantity+"/" : "")
							+this.commodityCosts.get(commodityCostId)
							// +"x"
							+" ("+quantity+") "
							+Global.getSettings().getCommoditySpec(commodityCostId).getName()
						);

					if (iterator.hasNext()) format = format+", ";
				}
			}
		}

		if (this.specialRequirements != null && !this.specialRequirements.isEmpty()) {
			Set<String> specials = new HashSet<String>();

			for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
				if (stack.getType() != CargoItemType.SPECIAL) continue;

				switch (stack.getSpecialDataIfSpecial().getId()) {
					case "pristine_nanoforge": specials.add("corrupted_nanoforge");
					default: specials.add(stack.getSpecialDataIfSpecial().getId()); break;
				}
			}

			if (isDisabled) {
				format = (format.isEmpty() ? "Tier "+(this.tier)+": " : format+" & ");

				for (Iterator<String> iterator = this.specialRequirements.iterator(); iterator.hasNext(); ) {
					String specialId = iterator.next();

					format = format
						+Global.getSettings().getSpecialItemSpec(specialId).getName();

					if (iterator.hasNext()) format = format+", ";
				}
			} else {
				format = (format.isEmpty() ? highlightText("Tier "+(this.tier))+": " : format+" & ");

				for (Iterator<String> iterator = this.specialRequirements.iterator(); iterator.hasNext(); ) {
					String specialId = iterator.next();

					format = format
						+positiveOrNegativeText(
							specials.contains(specialId),
							Global.getSettings().getSpecialItemSpec(specialId).getName()
						);

					if (iterator.hasNext()) format = format+", ";
				}
			}
		}

		if (isDisabled) {
			return tooltip.addPara(format, Misc.getGrayColor(), 2f);
		} else {
			return lyr_tooltipUtilities.addColorizedPara(tooltip, format, 2f);
		}
	}
}