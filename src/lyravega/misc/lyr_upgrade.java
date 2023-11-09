package lyravega.misc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import lyravega.utilities.lyr_tooltipUtilities.colour;

/**
 * A class that is dedicated to house multiple upgrade layers in one place while providing accessors
 * for easier navigation and information retrieval. Variant tag alterations are also done from here.
 * <p> Upgrades store a tag on a variant, and relevant effects are applied by extracting the tier
 * information from it. The effects are applied through an interface that abstracts the upgrade.
 * <p> In these upgrade classes, layer refers to the base definitions, and tier refers to the current
 * layer being in use. An upgrade may have multiple layers, but only one tier in use. Engrish :s
 * <p> In addition, layer indices start from 0, while tier indices start from 1. This is to avoid
 * having no tier set to {@code -1}. When querying a layer, tier needs to be adjusted accordingly.
 * @author lyravega
 * @see {@link lyr_upgradeLayer} / {@link lyr_upgradeVault} / {@link _lyr_upgradeEffect}
 */
public class lyr_upgrade {
	private final String id;
	private final String name;
	private final EnumMap<HullSize, ArrayList<lyr_upgradeLayer>> upgradeLayers;

	/**
	 * Constructs an upgrade container which holds the upgrade layers. Only simple assignments are
	 * done here, the layers needs to be added via methods.
	 * @param id of the upgrade, also used as a tag prefix that will be applied on variants
	 * @param name a friendly name of the upgrade, usually used/saved for tooltips and whatnot
	 */
	public lyr_upgrade(String id, String name) {
		this.id = id;
		this.name = name;
		this.upgradeLayers = new EnumMap<HullSize, ArrayList<lyr_upgradeLayer>>(HullSize.class);
	}

	public String getId() { return this.id; }

	public String getName() { return this.name; }

	/**
	 * Grabs the upgrade layer list associated with the passed hull size, if any. If there is no such
	 * list or the passed {@code hullSize} is null, redirects to {@link HullSize#DEFAULT} instead.
	 * @param hullSize category
	 * @return an upgrade layer list
	 */
	public ArrayList<lyr_upgradeLayer> getUpgradeLayers(HullSize hullSize) {
		if (hullSize == null || !this.upgradeLayers.containsKey(hullSize)) hullSize = HullSize.DEFAULT;	// redirect to default if hull size null or not found

		return this.upgradeLayers.get(hullSize);
	}

	/**
	 * Grabs an upgrade layer from a hull size list with the passed index. If there is no such list
	 * list or the passed {@code hullSize} is null, redirects to {@link HullSize#DEFAULT} instead.
	 * @param hullSize category
	 * @param i index of the layer; layer indices start from 0 but first tier counts as one so adjust accordingly
	 * @return an upgrade layer
	 */
	public lyr_upgradeLayer getUpgradeLayer(HullSize hullSize, int i) {
		return this.getUpgradeLayers(hullSize).get(i);	// redirection is done in other method, no null checks here
	}

	public lyr_upgradeLayer getCurrentLayer(ShipVariantAPI variant) {
		return this.getUpgradeLayer(variant.getHullSize(), this.getCurrentTier(variant)-1);
	}

	@Deprecated
	public lyr_upgradeLayer getNextLayer(ShipVariantAPI variant) {
		return this.getUpgradeLayer(variant.getHullSize(), this.getCurrentTier(variant));
	}

	/**
	 * Constructs a layer for this upgrade and stores it in separate list per hull size. If no hull
	 * size is given, then default hull size will be utilized.
	 * @param hullSize determine which list category the layer goes to. May be {@code null}; {@code HullSize.DEFAULT} will be utilized in that case
	 * @param commodityCostsArray a two dimensional array for defining commodity costs, where the expected objects are {{@link String} commodityId, {@link Integer} amount}. May be {@code null}
	 * @param specialRequirementsArray a single dimensional string array for defining special requirements. May be {@code null}
	 * @param storyPointCost an integer for story point cost. May be {@code null}, minimum {@code 0}
	 * @param creditCost an integer for credit cost. May be {@code null}, minimum {@code 0}
	 */
	public void addUpgradeLayer(HullSize hullSize, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost, Integer creditCost) {
		if (hullSize == null) hullSize = HullSize.DEFAULT;

		if (this.upgradeLayers.get(hullSize) == null) {
			this.upgradeLayers.put(hullSize, new ArrayList<lyr_upgradeLayer>());
		}

		this.getUpgradeLayers(hullSize).add(new lyr_upgradeLayer(this, this.getMaxTier(hullSize)+1, commodityCostsArray, specialRequirementsArray, storyPointCost, creditCost));
	}

	public String getCurrentLayerName(ShipVariantAPI variant) {
		final int currentTier = this.getCurrentTier(variant);

		if (currentTier == 0) return this.name;	// tiers are +1 than layers; tiers start from 1, layers start from 0

		return this.getUpgradeLayer(variant.getHullSize(), currentTier-1).getName();
	}

	public int getCurrentTier(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (!tag.startsWith(this.id)) continue;

			return Integer.valueOf(tag.replace(this.id+":", ""));
		};	return 0;
	}

	public int getMaxTier(HullSize hullSize) {
		return this.getUpgradeLayers(hullSize).size();
	}

	/**
	 * Checks if the next tier of the upgrade may be applied on the variant by first checking if it
	 * is at max tier already first, then if it may be afforded. Also used before upgrading the tier.
	 * @param variant that will be checked
	 * @return {@code true} if it may be, {@code false} otherwise
	 */
	public boolean canUpgradeTier(ShipVariantAPI variant) {
		int currentTier = this.getCurrentTier(variant);
		HullSize hullSize = variant.getHullSize();

		return currentTier < this.getMaxTier(hullSize) && this.getUpgradeLayer(hullSize, currentTier).canAfford();
	}

	private boolean canUpgradeTier(HullSize hullSize, Integer currentTier) {
		return currentTier < this.getMaxTier(hullSize) && this.getUpgradeLayer(hullSize, currentTier).canAfford();
	}

	/**
	 * Advances the tier of the upgrade to the next layer. Variant will have its upgrade tag (if any)
	 * replaced with a newer one, and any consumable costs required for upgrading will be deducted.
	 * <p> Checks if it can be done so beforehand just in case, even though the check should be done
	 * from the UI elements already.
	 * @param variant that will receive the upgrade
	 */
	public void upgradeTier(ShipVariantAPI variant) {
		if (!this.canUpgradeTier(variant)) return;

		int tier = 0;

		for (Iterator<String> iterator = variant.getTags().iterator(); iterator.hasNext(); ) {
			String tag = iterator.next();
			if (!tag.startsWith(this.id)) continue;

			tier = Integer.valueOf(tag.replace(this.id+":", ""));
			iterator.remove(); break;
		}

		variant.addTag(this.id+":"+(tier+1));
		this.getUpgradeLayer(variant.getHullSize(), tier).deductCosts();
	}

	/**
	 * Convenience method to print all layer requirements of this upgrade for the passed variant on
	 * the passed tooltip, along with some headers and extra information where applicable.
	 * <p> Checks the variant's current tier (if any) and prints the relevant information on the
	 * tooltip. Purchased tiers will not be shown, and only the next layer's requirements will be
	 * colourized while the rest will be grayed out.
	 * <p> Depending on the situation, the headers may differ; for example on a variant that is at
	 * max tier, no requirement will be shown and instead just a header stating such will be displayed.
	 * @param tooltip to be modified
	 * @param textPad
	 * @param headerPad
	 * @param variant to check and show the tooltip for
	 * @see {@link lyr_upgradeLayer#addRequirementsToTooltip()} where the individual layer requirements are printed
	 */
	public void addAllRequirementsToTooltip(ShipVariantAPI variant, TooltipMakerAPI tooltip, float textPad, float headerPad) {
		final int currentTier = this.getCurrentTier(variant);
		final HullSize hullSize = variant.getHullSize();
		final boolean canUpgradeTier = this.canUpgradeTier(hullSize, currentTier);

		if (!(currentTier < this.getMaxTier(hullSize))) {
			tooltip.addSectionHeading("MAX TIER", colour.button, header.invisible_bgColour, Alignment.MID, headerPad);
			return;
		}

		if (canUpgradeTier) {
			tooltip.addSectionHeading("UPGRADE REQUIREMENTS", colour.highlight, header.invisible_bgColour, Alignment.MID, headerPad);
		} else {
			tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", colour.negative, header.invisible_bgColour, Alignment.MID, headerPad);
		}

		for (lyr_upgradeLayer upgradeLayer : this.getUpgradeLayers(hullSize)) {
			int upgradeTier = upgradeLayer.getTier();

			if (upgradeTier > currentTier) upgradeLayer.addRequirementsToTooltip(tooltip, textPad, upgradeTier != currentTier+1);	// skip purchased tiers, colourize next tier, desaturate rest
		}

		if (canUpgradeTier) {
			tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", colour.positive, header.invisible_bgColour, Alignment.MID, headerPad);
			tooltip.addPara("Any special item requirements will not be consumed, while the rest will be", textPad);
		}
	}
}