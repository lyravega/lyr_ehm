package lyravega.misc;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyravega.utilities.lyr_miscUtilities;

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
			if (commodityCosts == null) commodityCosts = new LinkedHashMap<String, Integer>();

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

		// specials are not consumed
	}

	public LabelAPI addRequirementsToTooltip(TooltipMakerAPI tooltip, boolean isDisabled) {	// isDisabled is a shitty shortcut to repaint shit
		final MutableCharacterStatsAPI playerStats = Global.getSector().getPlayerStats();
		final CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();

		String format = "";

		// String enabledColourHex = "(0x"+Integer.toHexString(Color.WHITE.getRGB()).substring(2)+"|";
		String disabledColourHex = "(0x"+Integer.toHexString(Color.GRAY.getRGB()).substring(2)+"|";
		String tierColourHex = "(0x"+Integer.toHexString(Color.YELLOW.getRGB()).substring(2)+"|";
		String availableColourHex = "(0x"+Integer.toHexString(Color.GREEN.getRGB()).substring(2)+"|";
		String unavailableColourHex = "(0x"+Integer.toHexString(Color.RED.getRGB()).substring(2)+"|";

		if (isDisabled) {
			tierColourHex = disabledColourHex;
			availableColourHex = disabledColourHex;
			unavailableColourHex = disabledColourHex;
		}

		if (this.storyPointCost > 0) {
			format = format.isEmpty() ? tierColourHex+"Level "+(this.getTier())+"):" : format+" &";

			format = format+" "
				+(playerStats.getStoryPoints() >= this.storyPointCost ? availableColourHex : unavailableColourHex)
				+this.storyPointCost+" SP)";
		}

		if (this.commodityCosts != null && !this.commodityCosts.isEmpty()) {
			format = format.isEmpty() ? tierColourHex+"Level "+(this.getTier())+"):" : format+" &";

			for (Iterator<String> iterator = this.commodityCosts.keySet().iterator(); iterator.hasNext(); ) {
				String commodityCostId = iterator.next();
				int cost = this.commodityCosts.get(commodityCostId);

				format = format+" "
					+(playerCargo.getCommodityQuantity(commodityCostId) >= cost ? availableColourHex : unavailableColourHex)
					+this.commodityCosts.get(commodityCostId)+" "
					+Global.getSettings().getCommoditySpec(commodityCostId).getName()+")";

				if (iterator.hasNext()) format = format+",";
			}
		}

		if (this.specialRequirements != null && !this.specialRequirements.isEmpty()) {
			format = format.isEmpty() ? tierColourHex+"Level "+(this.getTier())+"):" : format+" &";

			Set<String> specials = new HashSet<String>();

			for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
				if (stack.getType() != CargoItemType.SPECIAL) continue;

				switch (stack.getSpecialDataIfSpecial().getId()) {
					case "pristine_nanoforge": specials.add("corrupted_nanoforge");
					default: specials.add(stack.getSpecialDataIfSpecial().getId()); break;
				}
			}

			for (Iterator<String> iterator = this.specialRequirements.iterator(); iterator.hasNext(); ) {
				String specialId = iterator.next();

				format = format+" "
					+(specials.contains(specialId) ? availableColourHex : unavailableColourHex)
					+Global.getSettings().getSpecialItemSpec(specialId).getName()+")";

				if (iterator.hasNext()) format = format+", ";
			}
		}

		LabelAPI para = lyr_miscUtilities.addColorizedPara(tooltip, format, 0f);
		if (isDisabled) para.setColor(Color.GRAY);	// hacky way to make all of them grayed out because I got lazy
		return para;
	}
}