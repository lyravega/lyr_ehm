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

	/**
	 * Constructs a layer for an upgrade. Access is restricted to package as these shouldn't be used
	 * on their own, but rather should be constructed through an upgrade container {@link lyr_upgrade}
	 * to perform properly.
	 * @param hullSize determine which list category the layer goes to. May be {@code null}; {@code HullSize.DEFAULT} will be utilized in that case
	 * @param commodityCostsArray a two dimensional array for defining commodity costs, where the expected objects are {{@link String}, {@link Integer}}. May be {@code null}
	 * @param specialRequirementsArray a single dimensional string array for defining special requirements. May be {@code null}
	 * @param storyPointCost an integer for story point cost. May be {@code null}, minimum {@code 0}
	 */
	lyr_upgradeLayer(lyr_upgrade upgrade, int tier, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost) {
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

	/**
	 * Checks if a layer may be afforded by the player. All costs are checked one by one till a
	 * @return {@code true} if it may be afforded, {@code false} otherwise
	 */
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
			// SpecialItemData testData = new SpecialItemData("pristine_nanoforge", null);	// not using these even though a special check may be done this way
			// boolean test = playerCargo.getQuantity(CargoItemType.SPECIAL, testData) > 0;	// because need to check what the player has first due to specials that may count as each other

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

	/**
	 * A tooltip modification method that automatically generates this tier's requirements and puts
	 * them on the tooltip. The used colours are all drawn from game, and uses gray, highlight,
	 * positive and negative ones.
	 * <p> The boolean governs if the para will be grayed out, should be set to {@code false} for
	 * the tiers that is beyond the next one. Otherwise, tier, affordable and unaffordable texts will
	 * utilize highlight, positive and negative colours in that order.
	 * <p> The text will be generated in such a way that will utilize inline regexable colour HEX
	 * text that'll be formatted into proper values in another method. Don't mind what's being done
	 * here, as this simply is a convenience method to print everything at once.
	 * @param tooltip to be modified
	 * @param isDisabled to gray the text out instead of colorizing the text
	 * @return the para itself if any further modification is needed
	 * @see {@link lyr_tooltipUtilities#addColorizedPara(TooltipMakerAPI, String, float)}
	 */
	public LabelAPI addRequirementsToTooltip(TooltipMakerAPI tooltip, float pad, boolean isDisabled) {
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
			return tooltip.addPara(format, Misc.getGrayColor(), pad);
		} else {
			return lyr_tooltipUtilities.addColorizedPara(tooltip, format, pad);
		}
	}
}