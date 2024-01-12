package experimentalHullModifications.misc;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import experimentalHullModifications.misc.ehm_tooltip.text;
import lyravega.upgrades.lyr_upgradeVault;
import lyravega.utilities.lyr_miscUtilities;
import lyravega.utilities.lyr_tooltipUtilities;

public final class ehm_internals {
	public static final class ids {
		public static final String
			hullModFilePath = "data/hullmods/hull_mods.csv",
			drillSound = "drill",
			submarket = "ehm_submarket",
			ability = "ehm_ability",
			faction = "experimental",
			manufacturer = "Experimental",
			mod = "lyr_ehm",
			experimental = "ehm";
	}

	public static final class shunts {
		public static final EnumMap<WeaponSize, Integer> slotValues = new EnumMap<WeaponSize, Integer>(WeaponSize.class);
		static {
			slotValues.put(WeaponSize.SMALL, 1);
			slotValues.put(WeaponSize.MEDIUM, 2);
			slotValues.put(WeaponSize.LARGE, 4);
		}

		public static final class adapters {
			public static final class ids {
				public static final String
					mediumDual = "ehm_adapter_mediumDual",
					largeDual = "ehm_adapter_largeDual",
					largeTriple = "ehm_adapter_largeTriple",
					largeQuad = "ehm_adapter_largeQuad";
			}
			public static final String activatorId = hullmods.activatorRetrofits.adapterShuntActivator;
			public static final String groupTag = "ehm_adapter";
			public static final String tag = groupTag;
		}

		public static final class converters {
			public static final class ids {
				public static final String
					mediumToLarge = "ehm_converter_mediumToLarge",
					smallToLarge = "ehm_converter_smallToLarge",
					smallToMedium = "ehm_converter_smallToMedium";
			}
			public static final String activatorId = hullmods.activatorRetrofits.diverterConverterActivator;
			public static final String groupTag = "ehm_converter";
			public static final String tag = groupTag;
		}

		public static final class diverters {
			public static final class ids {
				public static final String
					large = "ehm_diverter_large",
					medium = "ehm_diverter_medium",
					small = "ehm_diverter_small";
			}
			public static final String activatorId = hullmods.activatorRetrofits.diverterConverterActivator;
			public static final String groupTag = "ehm_diverter";
			public static final String tag = groupTag;
		}

		public static final class capacitors {
			public static final class ids {
				public static final String
					large = "ehm_capacitor_large",
					medium = "ehm_capacitor_medium",
					small = "ehm_capacitor_small";
			}
			public static final String activatorId = hullmods.activatorRetrofits.mutableShuntActivator;
			public static final String groupTag = "ehm_capacitor";
			public static final String tag = groupTag;
		}

		public static final class dissipators {
			public static final class ids {
				public static final String
					large = "ehm_dissipator_large",
					medium = "ehm_dissipator_medium",
					small = "ehm_dissipator_small";
			}
			public static final String activatorId = hullmods.activatorRetrofits.mutableShuntActivator;
			public static final String groupTag = "ehm_dissipator";
			public static final String tag = groupTag;
		}

		public static final class hangars {
			public static final class ids {
				public static final String
					large = "ehm_tube_large";
			}
			public static final String activatorId = hullmods.activatorRetrofits.hangarShuntActivator;
			public static final String groupTag = "ehm_hangar";
			public static final String tag = groupTag;
		}

		public static final class modules {
			public static final class ids {
				public static final String
					prototype = "ehm_module_prototype";
			}
			public static final String activatorId = hullmods.activatorRetrofits.miniModuleActivator;
			public static final String groupTag = "ehm_module";
			public static final String tag = groupTag;
		}

		public static final String tag = ids.experimental;
	}

	public static final class hullmods {
		public static final class main {
			public static final String
				base = "ehm_base",
				undo = "ehm_undo",
				test = "ehm_test";
		}

		public static final class activatorRetrofits {
			public static final String
				diverterConverterActivator = "ehm_ar_diverterandconverter",
				hangarShuntActivator = "ehm_ar_launchtube",
				mutableShuntActivator = "ehm_ar_mutableshunt",
				miniModuleActivator = "ehm_ar_minimodule",
				adapterShuntActivator = "ehm_ar_stepdownadapter";
			public static final String tag = "ehm_ar";
		}

		public static final class systemRetrofits {
			public static final String	// all of the system retrofit ids are based on system ids; they just get a prefix
				acausaldisruptor = "ehm_sr_acausaldisruptor",
				ammofeed = "ehm_sr_ammofeed",
				burndrive = "ehm_sr_burndrive",
				canister_flak = "ehm_sr_canister_flak",
				chiral_figment = "ehm_sr_chiral_figment",
				cryoflux = "ehm_sr_cryoflux",
				damper = "ehm_sr_damper",
				damper_omega = "ehm_sr_damper_omega",
				displacer = "ehm_sr_displacer",
				displacer_degraded = "ehm_sr_displacer_degraded",
				drone_borer = "ehm_sr_drone_borer",
				drone_pd = "ehm_sr_drone_pd",
				drone_pd_x2 = "ehm_sr_drone_pd_x2",
				drone_sensor = "ehm_sr_drone_sensor",
				drone_station_high = "ehm_sr_drone_station_high",
				drone_station_lt = "ehm_sr_drone_station_lt",
				drone_station_mid = "ehm_sr_drone_station_mid",
				drone_strike = "ehm_sr_drone_strike",
				drone_terminator = "ehm_sr_drone_terminator",
				dynamic_stabilizer = "ehm_sr_dynamic_stabilizer",
				emp = "ehm_sr_emp",
				energy_conversion = "ehm_sr_energy_conversion",
				entropyamplifier = "ehm_sr_entropyamplifier",
				fastmissileracks = "ehm_sr_fastmissileracks",
				flarelauncher = "ehm_sr_flarelauncher",
				flarelauncher_active = "ehm_sr_flarelauncher_active",
				flarelauncher_fighter = "ehm_sr_flarelauncher_fighter",
				flarelauncher_single = "ehm_sr_flarelauncher_single",
				forgevats = "ehm_sr_forgevats",
				forgevats_station = "ehm_sr_forgevats_station",
				fortressshield = "ehm_sr_fortressshield",
				highenergyfocus = "ehm_sr_highenergyfocus",
				inferniuminjector = "ehm_sr_inferniuminjector",
				interdictor = "ehm_sr_interdictor",
				lidararray = "ehm_sr_lidararray",
				maneuveringjets = "ehm_sr_maneuveringjets",
				microburn = "ehm_sr_microburn",
				microburn_omega = "ehm_sr_microburn_omega",
				mine_strike = "ehm_sr_mine_strike",
				mine_strike_station = "ehm_sr_mine_strike_station",
				mote_control = "ehm_sr_mote_control",
				nova_burst = "ehm_sr_nova_burst",
				orion_device = "ehm_sr_orion_device",
				phasecloak = "ehm_sr_phasecloak",
				phaseteleporter = "ehm_sr_phaseteleporter",
				plasmajets = "ehm_sr_plasmajets",
				recalldevice = "ehm_sr_recalldevice",
				reservewing = "ehm_sr_reservewing",
				skimmer_drone = "ehm_sr_skimmer_drone",
				targetingfeed = "ehm_sr_targetingfeed",
				temporalshell = "ehm_sr_temporalshell",
				traveldrive = "ehm_sr_traveldrive";
			public static final String tag = "ehm_sr";
		}

		public static final class weaponRetrofits {
			public static final String
				ballisticslotretrofit = "ehm_wr_ballisticslotretrofit",
				energyslotretrofit = "ehm_wr_energyslotretrofit",
				missileslotretrofit = "ehm_wr_missileslotretrofit",
				universalslotretrofit = "ehm_wr_universalslotretrofit",
				universalsmallslotretrofit = "ehm_wr_universalsmallslotretrofit";
			public static final String tag = "ehm_wr";
		}

		public static final class engineCosmetics {
			public static final String
				blueEngines = "ehm_ec_blueEngines",
				greenEngines = "ehm_ec_greenEngines",
				redEngines = "ehm_ec_redEngines",
				crimsonEngines = "ehm_ec_crimsonEngines",
				highTechEngines = "ehm_ec_highTechEngines",
				lowTechEngines = "ehm_ec_lowTechEngines",
				midlineEngines = "ehm_ec_midlineEngines";
			public static final String tag = "ehm_ec";
		}

		public static final class shieldCosmetics {
			public static final String
				blueShields = "ehm_sc_blueShields",
				greenShields = "ehm_sc_greenShields",
				redShields = "ehm_sc_redShields",
				crimsonShields = "ehm_sc_crimsonShields",
				highTechShields = "ehm_sc_highTechShields",
				lowTechShields = "ehm_sc_lowTechShields",
				midlineShields = "ehm_sc_midlineShields";
			public static final String tag = "ehm_sc";
		}

		public static final class misc {
			public static final String
				aiswitch = "ehm_mr_aiswitch",
				auxilarygenerators = "ehm_mr_auxilarygenerators",
				logisticsoverhaul = "ehm_mr_logisticsoverhaul",
				overengineered = "ehm_mr_overengineered";
			public static final String tag = "ehm_mr";
		}

		public static final class extensions {
			public static final String
				heavyenergyintegration = "ehm_mr_heavyenergyintegration",
				expensivemissiles = "ehm_mr_expensivemissiles";
		}

		public static final class tags {
			public static final String
				experimental = ids.experimental;
		}

		public enum checks {
			reqBase {
				@Override public boolean check(ShipAPI ship) {
					return lyr_miscUtilities.hasBuiltInHullMod(ship, hullmods.main.base);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.lacksBase, text.colourized.padding);
				}
			},
			reqNoLogistics {
				@Override public boolean check(ShipAPI ship) {
					return !ship.getVariant().hasHullMod(hullmods.misc.logisticsoverhaul);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasLogisticsOverhaul, text.colourized.padding);
				}
			},
			reqShield {
				@Override public boolean check(ShipAPI ship) {
					return ship.getShield() != null;
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.noShields, text.padding);
				}
			},
			reqEngine {
				@Override public boolean check(ShipAPI ship) {
					return lyr_miscUtilities.hasEngines(ship);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.noEngines, text.padding);
				}
			},
			reqNoPhase {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasPhaseCloak(ship);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasPhase, text.padding);
				}
			},
			reqWingBays {
				@Override public boolean check(ShipAPI ship) {
					return ship.getNumFighterBays() != 0;
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.noWings, text.padding);
				}
			},
			reqNotChild {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.isModule(ship);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.isModule, text.padding);
				}
			},
			reqDiverterAndConverter {
				@Override public boolean check(ShipAPI ship) {
					return !ship.getVariant().hasHullMod(hullmods.activatorRetrofits.diverterConverterActivator);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeaponsOnConvertedSlots, text.padding);
				}
			},
			hasWeaponsOnConvertedSlots {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasWeapons(ship, ehm_internals.affixes.convertedSlot);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeaponsOnConvertedSlots, text.padding);
				}
			},
			hasWeaponsOnAdaptedSlots {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasWeapons(ship, ehm_internals.affixes.adaptedSlot);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeaponsOnAdaptedSlots, text.padding);
				}
			},
			hasExtraWings {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasExtraWings(ship, hullmods.main.base);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasExtraWings, text.padding);
				}
			},
			hasWeapons {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasWeapons(ship);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWeapons, text.colourized.padding);
				}
			},
			hasMiniModules {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasModulesWithPrefix(ship, "ehm_module");
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasMiniModules, text.padding);
				}
			},
			hasAnyFittedWings {
				@Override public boolean check(ShipAPI ship) {
					return !lyr_miscUtilities.hasAnyFittedWings(ship);
				}

				@Override public void print(TooltipMakerAPI tooltip, ShipAPI ship) {
					if (this.check(ship)) return; lyr_tooltipUtilities.addColourizedPara(tooltip, text.colourized.hasWings, text.colourized.padding);
				}
			};

			public abstract boolean check(ShipAPI ship);
			public abstract void print(TooltipMakerAPI tooltip, ShipAPI ship);
		}

		public static final class uiTags {
			public static final Set<String> set = new HashSet<String>();
			public static final String
				activators = "Activators",
				cosmetics = "Cosmetics",
				retrofits = "Retrofits",
				systems = "Systems";
			static {
				set.add(activators);
				set.add(cosmetics);
				set.add(retrofits);
				set.add(systems);
			}
		}
	}

	public static final class upgrades {
		public static final String
			prefix = lyr_upgradeVault.ids.prefix,
			overdrive = prefix+"_overdrive";
	}

	public static final class statIds {
		public static final String
			engineCosmetics = hullmods.engineCosmetics.tag,
			shieldCosmetics = hullmods.shieldCosmetics.tag,
			weaponRetrofits = hullmods.weaponRetrofits.tag,
			upgrade = lyr_upgradeVault.ids.statId,
			adapters = shunts.adapters.groupTag,
			converters = shunts.converters.groupTag,
			diverters = shunts.diverters.groupTag,
			capacitors = shunts.capacitors.groupTag,
			dissipators = shunts.dissipators.groupTag,
			hangars = shunts.hangars.groupTag,
			ordnancePoints = "ehm_ordnancePoints",
			slotPoints = "ehm_slotPoints",
			slotPointsNeeded = "ehm_slotPointsNeeded",
			slotPointsUsed = "ehm_slotPointsUsed",
			slotPointsFromMods = "ehm_slotPointsFromMods",
			slotPointsFromDiverters = diverters,	// having a separate stat for this is not necessary
			slotPointsToConverters = converters;	// having a separate stat for this is not necessary
	}

	public static final class affixes {
		public static final String	// these should not be altered in any update if possible
			normalSlot = "WS",
			adaptedSlot = "AS_",
			convertedSlot = "CS_",
			launchSlot = "LS_";
	}
}