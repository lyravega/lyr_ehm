package lyravega.upgrades;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.header;
import lunalib.lunaRefit.BaseRefitButton;
import lyravega.utilities.lyr_interfaceUtilities;
import lyravega.utilities.lyr_tooltipUtilities.colour;
import lyravega.utilities.logger.lyr_logger;

/**
 * @author lyravega
 * @see {@link lyr_tierSpec} / {@link lyr_upgradeVault} / {@link lyr_upgradeEffect}
 */
public abstract class lyr_upgrade extends BaseRefitButton implements lyr_upgradeEffect {
	protected final String id;
	protected final String name;
	private final EnumMap<HullSize, ArrayList<lyr_tierSpec>> tierSpecArrays;
	private final EnumMap<HullSize, Map<String, lyr_tierSpec>> tierSpecMaps;

	/**
	 * Constructs an upgrade container which holds the upgrade tiers. Only simple assignments are
	 * done here, the tiers needs to be added via methods.
	 * @param id of the upgrade, also used as a tag prefix that will be applied on variants
	 * @param name a friendly name of the upgrade, usually used/saved for tooltips and whatnot
	 */
	public lyr_upgrade(String id, String name) {
		this.id = id;
		this.name = name;
		this.tierSpecArrays = new EnumMap<HullSize, ArrayList<lyr_tierSpec>>(HullSize.class);
		this.tierSpecMaps = new EnumMap<HullSize, Map<String, lyr_tierSpec>>(HullSize.class);
	}

	@Override public final String getUpgradeId() { return this.id; }

	@Override public final String getUpgradeName() { return this.name; }

	/**
	 * Constructs a tier for this upgrade and stores it in separate list per hull size. If no hull
	 * size is given, then default hull size will be utilized.
	 * <p> Arrays are preferred over sets or maps as constructing and populating them separately is
	 * tiresome compared to constructing arrays as inline parameters. Insertion order is preserved.
	 * @param hullSize determine which list category the tier goes to. May be {@code null}; {@code HullSize.DEFAULT} will be utilized in that case
	 * @param commodityCostsArray a map-like two dimensional array for defining commodity costs with {{@link String} commodityId, {@link Integer} amount}. May be {@code null}
	 * @param specialRequirementsArray a set-like single dimensional string array for defining special requirements with {{@link String} specialId}. May be {@code null}
	 * @param storyPointCost an integer for story point cost. May be {@code null}, minimum {@code 0}
	 * @param creditCost an integer for credit cost. May be {@code null}, minimum {@code 0}
	 */
	protected final void addTierSpec(HullSize hullSize, Object[][] commodityCostsArray, String[] specialRequirementsArray, Integer storyPointCost, Integer creditCost) {
		if (hullSize == null) hullSize = HullSize.DEFAULT;
		ArrayList<lyr_tierSpec> tierSpecArray = this.tierSpecArrays.get(hullSize);

		if (tierSpecArray == null) {
			this.tierSpecArrays.put(hullSize, new ArrayList<lyr_tierSpec>());
			tierSpecArray = this.tierSpecArrays.get(hullSize);
			// tierSpecArray.add(null);	// adding null entry to make tiers start from 1 instead of 0
			tierSpecArray.add(new lyr_tierSpec(this, tierSpecArray, null, null, null, null));	// adding empty entry to make tiers start from 1 instead of 0
		}

		lyr_tierSpec tierSpec = new lyr_tierSpec(this, tierSpecArray, commodityCostsArray, specialRequirementsArray, storyPointCost, creditCost);
		tierSpecArray.add(tierSpec);	// the constructor isn't passed during 'add()' as the array size is utilized during construction; doing it separately keeps things clear
	}

	/**
	 * Grabs the upgrade tier list associated with the passed hull size, if any. If there is no such
	 * list or the passed {@code hullSize} is null, redirects to {@link HullSize#DEFAULT} instead.
	 * @param hullSize category
	 * @return an upgrade tier list
	 */
	private final ArrayList<lyr_tierSpec> getTierSpecs(HullSize hullSize) {
		if (!this.tierSpecArrays.containsKey(hullSize)) hullSize = HullSize.DEFAULT;	// redirect to default if hull size null or not found

		return this.tierSpecArrays.get(hullSize);
	}

	/**
	 * Grabs an upgrade tier from a hull size list with the passed index. If there is no such list
	 * list or the passed {@code hullSize} is null, redirects to {@link HullSize#DEFAULT} instead.
	 * @param hullSize category
	 * @param tier index of the tier; tier indices start from 0 but first tier counts as one so adjust accordingly
	 * @return an upgrade tier
	 */
	private final lyr_tierSpec getTierSpec(HullSize hullSize, int tier) {
		try {
			return this.getTierSpecs(hullSize).get(tier);
		} catch (ArrayIndexOutOfBoundsException e) {
			lyr_logger.debug("Upgrade tier spec for 'HullSize."+hullSize.name()+"' not found at index '"+tier+"'", e); return null;
		}
	}

	@Override
	public final lyr_tierSpec getTierSpec(MutableShipStatsAPI stats) {
		StatMod tierStat = stats.getDynamic().getMod(lyr_upgradeVault.ids.statId).getFlatBonuses().get(this.id);

		if (tierStat != null) return this.getTierSpec(stats.getVariant().getHullSize(), Math.round(tierStat.getValue()));

		return this.getTierSpec(stats.getVariant().getHullSize(), 0);	// return the dummy tier for checking next tier data and avoid null shit
	}

	@Override
	public final lyr_tierSpec getTierSpec(ShipVariantAPI variant) {
		for (String tag : variant.getTags()) {
			if (!tag.startsWith(this.id)) continue;

			return this.getTierSpec(variant.getHullSize(), Integer.valueOf(tag.replaceFirst(this.id+":", "")));
		};	return this.getTierSpec(variant.getHullSize(), 0);
	}

	/**
	 * Convenience method to print all tier requirements of this upgrade for the passed variant on
	 * the passed tooltip, along with some headers and extra information where applicable.
	 * <p> Checks the variant's current tier (if any) and prints the relevant information on the
	 * tooltip. Purchased tiers will not be shown, and only the next tier's requirements will be
	 * colourized while the rest will be grayed out.
	 * <p> Depending on the situation, the headers may differ; for example on a variant that is at
	 * max tier, no requirement will be shown and instead just a header stating such will be displayed.
	 * @param tooltip to be modified
	 * @param textPad
	 * @param headerPad
	 * @param variant to check and show the tooltip for
	 * @see {@link lyr_tierSpec#addRequirementsToTooltip()} where the individual tier requirements are printed
	 */
	@Override
	public final void addUpgradeRequirementsToTooltip(FleetMemberAPI member, ShipVariantAPI variant, TooltipMakerAPI tooltip, float textPad, float headerPad) {
		final lyr_tierSpec currentTierSpec = this.getTierSpec(member.getStats());
		final int currentTier = currentTierSpec.getTier();

		if (currentTierSpec.getNextTierSpec() == null) {
			tooltip.addSectionHeading("MAX TIER", colour.button, header.invisible_bgColour, Alignment.MID, headerPad);
			return;
		}

		if (currentTierSpec.canUpgradeTier()) {
			tooltip.addSectionHeading("UPGRADE REQUIREMENTS", colour.highlight, header.invisible_bgColour, Alignment.MID, headerPad);
		} else if (currentTierSpec.hasNextTier()) {
			tooltip.addSectionHeading("UPGRADE REQUIREMENTS UNMET", colour.negative, header.invisible_bgColour, Alignment.MID, headerPad);
		}

		for (lyr_tierSpec tierSpec : this.getTierSpecs(variant.getHullSize())) {
			if (tierSpec.getTier() > currentTier) tierSpec.addRequirementsToTooltip(tooltip, textPad, currentTierSpec.getNextTierSpec() != tierSpec);	// skip purchased tiers, colourize next tier, desaturate rest
		}

		if (currentTierSpec.canUpgradeTier()) {
			tooltip.addSectionHeading("HOLD SHIFT & CLICK TO UPGRADE", colour.positive, header.invisible_bgColour, Alignment.MID, headerPad);
			tooltip.addPara("Any special item requirements will not be consumed, while the rest will be", textPad);
		}
	}

	//#region LunaRefitButton
	@Override
	public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
		return this.getTierSpec(member.getStats()).getName();
	}

	@Override
	public boolean isClickable(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return this.getTierSpec(member.getStats()).canUpgradeTier();
	}

	@Override
	public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {
		if (!event.isShiftDown()) return;
		this.getTierSpec(member.getStats()).upgradeTier(variant);
		variant.addPermaMod(lyr_upgradeVault.ids.modId, false);

		this.refreshVariant();
		this.refreshButtonList();
		lyr_interfaceUtilities.playDrillSound();
	}

	@Override
	public boolean hasTooltip(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return true;
	}

	@Override
	public float getToolipWidth(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		return 400.0f;
	}

	@Override
	public void addTooltip(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
		final int currentTier = this.getTierSpec(member.getStats()).getTier();

		tooltip.addSectionHeading(this.getUpgradeName().toUpperCase(), colour.button, header.invisible_bgColour, Alignment.MID, 2f);
		if (currentTier > 0) tooltip.addSectionHeading("CURRENT TIER: "+currentTier, colour.button, header.invisible_bgColour, Alignment.MID, 2f).flash(1f, 1f);
		this.addUpgradeRequirementsToTooltip(member, variant, tooltip, 2f, 2f);
	}
	//#endregion
}