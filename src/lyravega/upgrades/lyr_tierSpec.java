package lyravega.upgrades;

import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.highlightText;
import static lyravega.utilities.lyr_tooltipUtilities.colourizedText.positiveOrNegativeText;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.MutableValue;

import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;
import lyravega.utilities.lyr_tooltipUtilities.colour;

/**
 * A class that is dedicated to house details of a single upgrade tier.
 * @author lyravega
 * @see {@link lyr_upgrade} / {@link lyr_upgradeVault} / {@link lyr_upgradeEffect}
 */
public final class lyr_tierSpec {
	private final int tier; public int getTier() { return this.tier; }
	private final String id; public String getId() { return this.id; }
	private final String name; public String getName() { return this.name; }
	private final Map<String, Integer> commodityCosts; public Map<String, Integer> getCommodityCosts() { return this.commodityCosts; }
	private final Set<String> specialRequirements; public Set<String> getSpecialRequirements() { return this.specialRequirements; }
	private final int storyPointCost; public int getStoryPointCost() { return this.storyPointCost; }
	private final int creditCost; public int getCreditCost() { return this.creditCost; }
	private lyr_tierSpec previousTierSpec; public lyr_tierSpec getPreviousTierSpec() { return this.previousTierSpec; }; public boolean hasPreviousTier() { return this.previousTierSpec != null; }
	private lyr_tierSpec nextTierSpec; public lyr_tierSpec getNextTierSpec() { return this.nextTierSpec; }; public boolean hasNextTier() { return this.nextTierSpec != null; }

	/**
	 * Constructs a layer for an upgrade. Access is restricted to package as these shouldn't be used
	 * on their own, but rather should be constructed through an upgrade container {@link lyr_upgrade}
	 * via its add method to perform properly.
	 * <p> Arrays are preferred over sets or maps as constructing and populating them separately is
	 * tiresome compared to constructing arrays as inline parameters. Insertion order is preserved.
	 * @param commodityCostsArray a map-like two dimensional array for defining commodity costs with {{@link String} commodityId, {@link Integer} amount}. May be {@code null}
	 * @param specialRequirementsArray a set-like single dimensional string array for defining special requirements with {{@link String} specialId}. May be {@code null}
	 * @param storyPointCost an integer for story point cost. May be {@code null}, minimum {@code 0}
	 * @param creditCost an integer for credit cost. May be {@code null}, minimum {@code 0}
	 */
	lyr_tierSpec(lyr_upgrade upgrade, ArrayList<lyr_tierSpec> tierSpecArray, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost, Integer creditCost) {
		Map<String, Integer> commodityCosts = null;
		Set<String> specialRequirements = null;

		if (commodityCostsArray != null) for (Object[] commodityCostArray: commodityCostsArray) {
			if (commodityCosts == null) commodityCosts = new LinkedHashMap<String, Integer>();

			commodityCosts.put(String.class.cast(commodityCostArray[0]), Integer.class.cast(commodityCostArray[1]));
		}

		if (specialRequirementsArray != null) for (String specialId : specialRequirementsArray) {
			if (specialRequirements == null) specialRequirements = new LinkedHashSet<String>();

			specialRequirements.add(specialId);
		}

		this.tier = tierSpecArray.size();
		this.id = upgrade.getUpgradeId()+":"+this.tier;
		this.name = this.tier == 0 ? upgrade.getUpgradeName() : upgrade.getUpgradeName()+" "+lyr_miscUtilities.romanNumerals.toRoman(this.tier);
		this.commodityCosts = commodityCosts;
		this.specialRequirements = specialRequirements;
		this.storyPointCost = storyPointCost != null ? Math.max(0, storyPointCost) : 0;
		this.creditCost = creditCost != null ? Math.max(0, creditCost) : 0;

		this.previousTierSpec = tierSpecArray.isEmpty() ? null : tierSpecArray.get(tierSpecArray.size()-1);
		if (this.previousTierSpec != null) this.previousTierSpec.nextTierSpec = this;

	}

	public boolean canUpgradeTier() {
		if (!this.hasNextTier()) return false;
		if (!this.nextTierSpec.canAfford()) return false;

		return true;
	}

	/**
	 * Raises the tier of the upgrade; alters the tags on the variant, and deducts the costs.
	 * <p> Double-checks if there is a next tier of the upgrade that may be afforded, even though it
	 * should already be done from the {@link #isClickable(FleetMemberAPI, ShipVariantAPI, MarketAPI)}
	 * @param variant that will receive the upgrade; must be the variant passed by the LunaRefitButton as member uses outdated variant
	 */
	void upgradeTier(ShipVariantAPI variant) {
		if (!this.canAfford()) return;

		variant.removeTag(this.getId());
		variant.addTag(this.nextTierSpec.getId());
		this.nextTierSpec.deductCosts();
	}

	/**
	 * Checks if a layer may be afforded by the player. Returns {@code false} immediately if a cost
	 * cannot be afforded, or a requirement is lacking.
	 * @return {@code true} if it may be afforded, {@code false} otherwise
	 */
	public boolean canAfford() {
		if (this.creditCost > 0) {
			final MutableValue playerCredits = Global.getSector().getPlayerFleet().getCargo().getCredits();

			if (playerCredits.get() < this.creditCost) {
				return false;
			}
		}

		if (this.storyPointCost > 0) {
			final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();

			if (playerStats.getStoryPoints() < this.storyPointCost) {
				return false;
			}
		}

		if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
			final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

			for (String commodityCostId : this.commodityCosts.keySet()) {
				int cost = this.commodityCosts.get(commodityCostId);

				if (playerCargo.getCommodityQuantity(commodityCostId) < cost) {
					return false;
				}
			}
		}

		if (this.specialRequirements != null && !this.specialRequirements.isEmpty()) {
			final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
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

	/**
	 * Self-explanatory. Deducts the costs from the player if enough amount is there. Checks if the
	 * player can afford the upgrades beforehand.
	 */
	void deductCosts() {
		if (!this.canAfford()) return;

		if (this.creditCost > 0) {
			final MutableValue playerCredits = Global.getSector().getPlayerFleet().getCargo().getCredits();

			if (playerCredits.get() >= this.creditCost) {	// redundant due to 'canAfford()' but just in case
				playerCredits.subtract(this.creditCost);
			}
		}

		if (this.storyPointCost > 0) {
			final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();

			if (playerStats.getStoryPoints() >= this.storyPointCost) {	// redundant due to 'canAfford()' but just in case
				playerStats.spendStoryPoints(this.storyPointCost, false, null, false, 0f, this.name);
			}
		}

		if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
			final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

			for (String commodityCostId : this.commodityCosts.keySet()) {
				int commodityCost = this.commodityCosts.get(commodityCostId);

				if (playerCargo.getCommodityQuantity(commodityCostId) >= commodityCost) {	// redundant due to 'canAfford()' but just in case
					playerCargo.removeCommodity(commodityCostId, commodityCost);
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
	 * <p> The para will also show how much of something the player has in parantheses if it is not
	 * disabled, since such information is only relevant when needed. Specials will not show any
	 * numbers as they are not consumed.
	 * <p> The text will be generated in such a way that will utilize inline regexable colour HEX
	 * text that'll be formatted into proper values in another method. Don't mind what's being done
	 * here, as this simply is a convenience method to print everything at once.
	 * @param tooltip to be modified
	 * @param isDisabled to gray the text out instead of colorizing the text
	 * @return the para itself if any further modification is needed
	 * @see {@link lyr_tooltipUtilities#addColorizedPara()} where the format is processed
	 * @see {@link lyr_upgrade#addAllRequirementsToTooltip()} where this method is called from for all available layers
	 */
	public LabelAPI addRequirementsToTooltip(TooltipMakerAPI tooltip, float pad, boolean isDisabled) {
		String format = "";

		if (this.creditCost > 0) {
			final MutableValue playerCredits = Global.getSector().getPlayerFleet().getCargo().getCredits();

			if (isDisabled) {
				format = (format.isEmpty() ? "Tier "+(this.tier)+": " : format+" & ")
					+this.creditCost+" Credits";
			} else {
				format = (format.isEmpty() ? highlightText("Tier "+(this.tier))+": " : format+" & ")
					+positiveOrNegativeText(
						playerCredits.get() >= this.creditCost,
						this.creditCost
						+" ("+Math.round(playerCredits.get())+") "
						+"Credits"
					);
			}
		}

		if (this.storyPointCost > 0) {
			final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();

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
			final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

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
			final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
			final Set<String> specials = new HashSet<String>();

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

		return isDisabled ? tooltip.addPara(format, colour.gray, pad) : lyr_tooltipUtilities.addColourizedPara(tooltip, format, pad);
	}
}