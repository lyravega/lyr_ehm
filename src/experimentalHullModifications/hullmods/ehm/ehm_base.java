package experimentalHullModifications.hullmods.ehm;

import static experimentalHullModifications.hullmods.ehm_ar._ehm_ar_base.ehm_processShunts;
import static lyravega.listeners.lyr_lunaSettingsListener.showFluff;
import static lyravega.tools.lyr_uiTools.commitVariantChanges;
import static lyravega.tools.lyr_uiTools.playDrillSound;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName.Gender;
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
public class ehm_base extends _ehm_tracker {
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String hullModSpecId) {
		ShipVariantAPI variant = stats.getVariant();
		ShipHullSpecAPI hullSpec = variant.getHullSpec();
		
		boolean isBasePerma = variant.getPermaMods().contains(lyr_internals.id.hullmods.base);
		boolean isBaseBuiltIn = hullSpec.isBuiltInMod(lyr_internals.id.hullmods.base);
		boolean isGettingRestored = !(isBasePerma && isBaseBuiltIn);	// when the ship is getting restored, hull spec won't have the base, but variant will

		// if (!isBaseBuiltIn) {
		if (!isBaseBuiltIn || !Misc.getDHullId(hullSpec).equals(hullSpec.getHullId())) {
			variant.setHullSpecAPI(ehm_hullSpecClone(variant)); 
			
			if (!isBasePerma) {	// to make this a one-time commit, and to avoid re-committing if/when the ship is getting restored
				variant.addPermaMod(lyr_internals.id.hullmods.base, false);
				commitVariantChanges(); playDrillSound();
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
			String playerSalutation = Global.getSector().getPlayerPerson().getGender().equals(Gender.MALE) ? Misc.SIR : Misc.MAAM;

			tooltip.addSectionHeading("FLUFF", header.info_textColour, header.info_bgColour, Alignment.MID, header.padding);
			switch ((int) Math.round(Math.random() * 10)) {
				case 0: 
					tooltip.addPara("For slot shunts, we may need to dock in a colony or a spaceport " + playerSalutation, text.padding);
					break;
				case 1: 
					tooltip.addPara(playerSalutation + ", if you are unhappy with what I am offering you, I can get rid of the base hull modifications that I've made. Let me know!", text.padding);
					break;
				case 2: 
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.weaponRetrofit))
						tooltip.addPara(playerSalutation + ", with slot retrofits every weapon slot may be altered all together to make them compatible with other weapon types.", text.padding);
					else tooltip.addPara("The slot retrofits come at a cost, but their main purpose is to allow flexibility, and of course letting you use your favourite weapons, "+ playerSalutation, text.padding);
					break;
				case 3: 
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.systemRetrofit))
						tooltip.addPara("The ships are designed along with their systems, however with system retrofits, I can change them anytime you want, "+ playerSalutation +".", text.padding);
					else tooltip.addPara("Some system & ship combinations may be powerful. Some may not. No refunds! Just joking...", text.padding);
					break;
				case 4: 
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.engineCosmetic))
						tooltip.addPara(playerSalutation + ", let me know if you'd like to have this ship's engine exhaust colour get changed. I can even fully customize them to your exact specifications!", text.padding);
					else tooltip.addPara("The engine exhaust cosmetics are looking great, " + playerSalutation, text.padding);
					break;
				case 5:
					if (!ehm_hasExperimentalModWithTag(variant, lyr_internals.tag.shieldCosmetic))
						tooltip.addPara("If you'd like, I can modify the shield emitters to project a shield with different colours, " + playerSalutation, text.padding);
					else tooltip.addPara("The shield emitters are modified to project colours of your choice, " + playerSalutation, text.padding);
					break;
				case 6: 
					if (!variant.hasHullMod(lyr_internals.id.hullmods.diverterandconverter))
						tooltip.addPara("I can divert power from a weapon slot using a diverter to power a converter on another slot, " + playerSalutation + "! The trade-off is necessary to make such modifications.", text.padding);
					else tooltip.addPara("Converters use the power diverted by diverters. If I cannot activate a converter, that means we lack enough diverters!", text.padding);
					break;
				case 7: 
					if (!variant.hasHullMod(lyr_internals.id.hullmods.mutableshunt))
						tooltip.addPara(playerSalutation + ", slot housings may be replaced with extra flux capacitors or dissipators, or a fighter bay may be fit into a large slot with select slot shunts!", text.padding);
					else tooltip.addPara("The capacitors and dissipators are designed to improve the built-in ones and also support other on-board systems indirectly. An additional fighter bay on the other hand...", text.padding);
					break;
				case 8: 
					if (!variant.hasHullMod(lyr_internals.id.hullmods.stepdownadapter))
						tooltip.addPara(playerSalutation + ", if you need more weapon slots of smaller sizes for any reason, bigger slots may be adapted into multiple smaller ones!", text.padding);
					else tooltip.addPara("Any adapters will be activated, " + playerSalutation + ". The additional slots might be smaller, but sometimes having more of something is the answer.", text.padding);
					break;
				case 9: 
					if (!variant.getSMods().contains(lyr_internals.id.hullmods.overengineered))
						tooltip.addPara(playerSalutation + ", have you thought about letting me over-engineer the ship? You might find the benefits interesting!", text.padding);
					else tooltip.addPara("This over-engineered ship is a beast, " + playerSalutation + "! Every internal system, even the bulkheads were replaced, while keeping the structural integrity intact! A mir... *cough* masterpiece!", text.padding);
					break;
				default: tooltip.addPara("All systems operational, " + playerSalutation, text.padding); break;
			}
		}
	}

	@Override
	public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
		ShipVariantAPI variant = ship.getVariant();

		return (ehm_hasRetrofitBaseBuiltIn(variant)) ? false : true;
	}
}
