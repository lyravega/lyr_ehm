package data.hullmods.ehm;

import static data.hullmods.ehm_ar._ehm_ar_base.ehm_adapterActivator;
import static data.hullmods.ehm_ar._ehm_ar_base.ehm_diverterAndConverterActivator;
import static data.hullmods.ehm_ar._ehm_ar_base.ehm_mutableShuntActivator;
import static data.hullmods.ehm_mr.ehm_mr_overengineered.slotPointBonus;
import static lyr.tools._lyr_uiTools.commitChanges;
import static lyr.tools._lyr_uiTools.playSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import lyr.misc.lyr_internals;
import lyr.misc.lyr_tooltip;

/**
 * Serves as a requirement for all experimental hull modifications, and enables tracking
 * on the ship. Main methods for tracking is located in the class that this one extends.
 * @category Base Hull Modification 
 * @author lyravega
 */
public class ehm_base extends _ehm_basetracker {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();

		if (!ehm_hasRetrofitBaseBuiltIn(variant)) {
			variant.setHullSpecAPI(ehm_hullSpecClone(variant)); commitChanges(); playSound();
		} else {
			final int slotPoints = variant.getSMods().contains(lyr_internals.id.hullmods.overengineered) ? slotPointBonus.get(hullSize) : 0;

			if (variant.hasHullMod(lyr_internals.id.hullmods.stepdownadapter)) ehm_adapterActivator(stats);
			if (variant.hasHullMod(lyr_internals.id.hullmods.diverterandconverter)) ehm_diverterAndConverterActivator(stats, slotPoints);
			if (variant.hasHullMod(lyr_internals.id.hullmods.mutableshunt)) ehm_mutableShuntActivator(stats, lyr_internals.id.hullmods.mutableshunt);
		}
	}

	@Override 
	public void applyEffectsAfterShipCreation(ShipAPI ship, String hullModSpecId) {
		ehm_trackShip(ship);
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		if (ship == null) return;

		if (!ship.getVariant().hasHullMod(hullModSpecId)) {
			tooltip.addSectionHeading(lyr_tooltip.header.severeWarning, lyr_tooltip.header.severeWarning_textColour, lyr_tooltip.header.severeWarning_bgColour, Alignment.MID, lyr_tooltip.header.padding).flash(1.0f, 1.0f);
			tooltip.addPara(lyr_tooltip.text.baseRetrofitWarning, lyr_tooltip.text.padding);

			super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec);
		} else {
			tooltip.addSectionHeading("FLUFF", lyr_tooltip.header.info_textColour, lyr_tooltip.header.info_bgColour, Alignment.MID, lyr_tooltip.header.padding);
			String playerRank = Global.getSector().getPlayerPerson().getRank();
			switch ((int) Math.round(Math.random() * 10)) {
				case 0: 
					tooltip.addPara("01001001 00100000 01100001 01101101 00100000 01110100 01101001 01110010 01100101 01100100... *cough* All systems operational, " + playerRank, lyr_tooltip.text.padding);
					break;
				case 1: 
					tooltip.addPara(playerRank + ", if you are unhappy with what I am offering you, I can get rid of the base hull modifications that I've made. Let me know!", lyr_tooltip.text.padding);
					break;
				case 2: 
					if (!ehm_hasExperimentalModWithTag(ship.getVariant(), lyr_internals.tag.weaponRetrofit))
						tooltip.addPara(playerRank + ", if you'd like me to alter weapon type of the every weapon slot on the ship all together, let me know!", lyr_tooltip.text.padding);
					else tooltip.addPara("The weapon retrofits come at a cost, but their main purpose is to allow flexibility, and of course, let you use your favourite weapons.", lyr_tooltip.text.padding);
					break;
				case 3: 
					if (!ehm_hasExperimentalModWithTag(ship.getVariant(), lyr_internals.tag.systemRetrofit))
						tooltip.addPara(playerRank + ", even though the ship systems are specifically designed for certain ships, I can change them anytime you want. The effectiveness of the said systems may not be as good as on their original ships, however!", lyr_tooltip.text.padding);
					else tooltip.addPara("Some system & ship combinations may be powerful. Some may not. No refunds! Just joking...", lyr_tooltip.text.padding);
					break;
				case 4: 
					if (!ehm_hasExperimentalModWithTag(ship.getVariant(), lyr_internals.tag.engineCosmetic))
						tooltip.addPara(playerRank + ", let me know if you'd like to have this ship's engine exhaust colour get changed. The selection is very limited but they're purely cosmetic.", lyr_tooltip.text.padding);
					else tooltip.addPara("Flying in style!", lyr_tooltip.text.padding);
					break;
				case 5:
					if (!ehm_hasExperimentalModWithTag(ship.getVariant(), lyr_internals.tag.shieldCosmetic))
						tooltip.addPara("If you'd like, I can modify the shield emitters to have a different colour, " + playerRank, lyr_tooltip.text.padding);
					else tooltip.addPara("Flying in style!", lyr_tooltip.text.padding);
					break;
				case 6: 
					if (!ship.getVariant().hasHullMod(lyr_internals.id.hullmods.diverterandconverter))
						tooltip.addPara("I can divert power from a weapon slot using a diverter to power a converter on another slot, " + playerRank + "! Extra firepower at your fingertips!", lyr_tooltip.text.padding);
					else tooltip.addPara("Converters use the power diverted by diverters. If I cannot activate a converter, that means we lack enough diverters!", lyr_tooltip.text.padding);
					break;
				case 7: 
					if (!ship.getVariant().hasHullMod(lyr_internals.id.hullmods.mutableshunt))
						tooltip.addPara(playerRank + ", I can alter the weapon slots completely. I can install shunts that'll provide bonuses to flux capacity or dissipation, can even turn a large one into a fully-working fighter bay!", lyr_tooltip.text.padding);
					else tooltip.addPara("The capacitors and dissipators are designed to improve the built-in ones and also support other on-board systems indirectly. An additional fighter bay on the other hand...", lyr_tooltip.text.padding);
					break;
				case 8: 
					if (!ship.getVariant().hasHullMod(lyr_internals.id.hullmods.stepdownadapter))
						tooltip.addPara(playerRank + ", if you need more weapon slots of smaller sizes for any reason, I can adapt bigger slots into smaller ones!", lyr_tooltip.text.padding);
					else tooltip.addPara("I will activate any adapter you install on the ship, " + playerRank + ". The additional slots might be smaller, but sometimes having more of something is the answer.", lyr_tooltip.text.padding);
					break;
				case 9: 
					if (!ship.getVariant().getSMods().contains(lyr_internals.id.hullmods.overengineered))
						tooltip.addPara(playerRank + ", have you thought about letting me over-engineer the ship? It will cost you a story point, but I think you might like the benefits!", lyr_tooltip.text.padding);
					else tooltip.addPara("This over-engineered ship is a beast, " + playerRank + "! Pretty much replaced everything internal, while keeping the structural integrity intact! A mir... *cough* masterpiece!", lyr_tooltip.text.padding);
					break;
				default: tooltip.addPara("All systems operational, " + playerRank, lyr_tooltip.text.padding); break;
			}
		}
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();

		return (ehm_hasRetrofitBaseBuiltIn(variant)) ? false : true;
	}
}
