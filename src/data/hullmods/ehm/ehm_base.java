package data.hullmods.ehm;

import static data.hullmods.ehm_ar._ehm_ar_base.ehm_processShunts;
import static lyravega.misc.lyr_lunaSettings.showFluff;
import static lyravega.tools._lyr_uiTools.commitChanges;
import static lyravega.tools._lyr_uiTools.playSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lyravega.misc.lyr_internals;
import lyravega.misc.lyr_tooltip.header;
import lyravega.misc.lyr_tooltip.text;

/**
 * Serves as a requirement for all experimental hull modifications, and enables tracking
 * on the ship. Some hull modification effects are also executed from here, and the
 * actual hull modifications only contribute to their tooltips and used for installation
 * checks.
 * @category Base Hull Modification 
 * @author lyravega
 */
public class ehm_base extends _ehm_basetracker {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();
		
		boolean isBasePerma = variant.getPermaMods().contains(lyr_internals.id.baseModification);
		boolean isBaseBuiltIn = ehm_hasRetrofitBaseBuiltIn(variant);
		boolean isGettingRestored = !(isBasePerma && isBaseBuiltIn);	// when the ship is getting restored, hull spec won't have the base, but variant will

		// if (!isBaseBuiltIn) {
		if (!isBaseBuiltIn || !Misc.getDHullId(hullSpec).equals(hullSpec.getHullId())) {
			variant.setHullSpecAPI(ehm_hullSpecClone(variant)); 
			
			if (!isBasePerma) {	// to make this a one-time commit, and to avoid re-committing if/when the ship is getting restored
				variant.addPermaMod(lyr_internals.id.baseModification, false);
				commitChanges(); playSound();
			}
		}

		ehm_processShunts(stats, isGettingRestored);
		ehm_cleanWeaponGroupsUp(variant);
	}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		ehm_trackShip(ship);
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		ShipVariantAPI variant = ship.getVariant();

		if (!variant.hasHullMod(hullModSpecId)) {
			tooltip.addSectionHeading(header.severeWarning, header.severeWarning_textColour, header.severeWarning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(text.baseRetrofitWarning[0], text.padding).setHighlight(text.baseRetrofitWarning[1]);

			if (variant.getHullSpec().isRestoreToBase()) {
				tooltip.addSectionHeading(header.restoreWarning, header.warning_textColour, header.warning_bgColour, Alignment.MID, header.padding).flash(1.0f, 1.0f);
				tooltip.addPara(text.restoreWarning[0], text.padding).setHighlight(text.restoreWarning[1]);
			}

			super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		} else if (showFluff) {
			tooltip.addSectionHeading("FLUFF", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
			String playerRank = Global.getSector().getPlayerPerson().getRank();
			switch ((int) Math.round(Math.random() * 10)) {
				case 0: 
					tooltip.addPara("If you need slot shunts " + playerRank + ", we need to dock in a colony or a spaceport so that I can get to work!", text.padding);
					break;
				case 1: 
					tooltip.addPara(playerRank + ", if you are unhappy with what I am offering you, I can get rid of the base hull modifications that I've made. Let me know!", text.padding);
					break;
				case 2: 
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.weaponRetrofit))
						tooltip.addPara(playerRank + ", if you'd like me to alter weapon type of the every weapon slot on the ship all together, I can do so with retrofits!", text.padding);
					else tooltip.addPara("The weapon retrofits come at a cost, but their main purpose is to allow flexibility, and of course, let you use your favourite weapons.", text.padding);
					break;
				case 3: 
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.systemRetrofit))
						tooltip.addPara(playerRank + ", the ships are designed along with their systems, but I can change them anytime you want. The effectiveness of the said systems may not be as good as on their original ships, however!", text.padding);
					else tooltip.addPara("Some system & ship combinations may be powerful. Some may not. No refunds! Just joking...", text.padding);
					break;
				case 4: 
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.engineCosmetic))
						tooltip.addPara(playerRank + ", let me know if you'd like to have this ship's engine exhaust colour get changed. The selection is very limited and they're purely cosmetic.", text.padding);
					else tooltip.addPara("Flying in style!", text.padding);
					break;
				case 5:
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.shieldCosmetic))
						tooltip.addPara("If you'd like, I can modify the shield emitters to have a different colour, " + playerRank, text.padding);
					else tooltip.addPara("Flying in style!", text.padding);
					break;
				case 6: 
					if (!variant.hasHullMod(lyr_internals.id.hullmods.diverterandconverter))
						tooltip.addPara("I can divert power from a weapon slot using a diverter to power a converter on another slot, " + playerRank + "! Extra firepower at your fingertips!", text.padding);
					else tooltip.addPara("Converters use the power diverted by diverters. If I cannot activate a converter, that means we lack enough diverters!", text.padding);
					break;
				case 7: 
					if (!variant.hasHullMod(lyr_internals.id.hullmods.mutableshunt))
						tooltip.addPara(playerRank + ", I can alter the weapon slots completely. I can install shunts that'll provide bonuses to flux capacity or dissipation, can even turn a large one into a fully-working fighter bay!", text.padding);
					else tooltip.addPara("The capacitors and dissipators are designed to improve the built-in ones and also support other on-board systems indirectly. An additional fighter bay on the other hand...", text.padding);
					break;
				case 8: 
					if (!variant.hasHullMod(lyr_internals.id.hullmods.stepdownadapter))
						tooltip.addPara(playerRank + ", if you need more weapon slots of smaller sizes for any reason, I can adapt bigger slots into smaller ones!", text.padding);
					else tooltip.addPara("I will activate any adapter you install on the ship, " + playerRank + ". The additional slots might be smaller, but sometimes having more of something is the answer.", text.padding);
					break;
				case 9: 
					if (!variant.getSMods().contains(lyr_internals.id.hullmods.overengineered))
						tooltip.addPara(playerRank + ", have you thought about letting me over-engineer the ship? It will cost you a story point, but I think you might like the benefits!", text.padding);
					else tooltip.addPara("This over-engineered ship is a beast, " + playerRank + "! Pretty much replaced everything internal, while keeping the structural integrity intact! A mir... *cough* masterpiece!", text.padding);
					break;
				default: tooltip.addPara("All systems operational, " + playerRank, text.padding); break;
			}
		}
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();

		return (ehm_hasRetrofitBaseBuiltIn(variant)) ? false : true;
	}
}
