package lyravega.misc;

import java.util.HashSet;
import java.util.Set;

public final class lyr_internals {
	public static final class id {
		public static final String
			submarket = "ehm_submarket", // must match submarket id in .csv
			ability = "ehm_ability", // must match ability id in .csv
			faction = "experimental", // must match faction id in .csv and .faction
			drillSound = "drill", // must match .json
			mod = "lyr_ehm"; // must match .json
		public static final class hullmods {
			public static final String
				base = "ehm_base",
				undo = "ehm_undo",
				test = "ehm_test",
				diverterandconverter = "ehm_ar_diverterandconverter",
				mutableshunt = "ehm_ar_mutableshunt",
				stepdownadapter = "ehm_ar_stepdownadapter",
				blueEngines = "ehm_cec_blueEngines",
				greenEngines = "ehm_cec_greenEngines",
				redEngines = "ehm_cec_redEngines",
				crimsonEngines = "ehm_ec_torpedoEngines",	// id & class mismatch
				highTechEngines = "ehm_ec_highTechEngines",
				lowTechEngines = "ehm_ec_lowTechEngines",
				midlineEngines = "ehm_ec_midlineEngines",
				aiswitch = "ehm_mr_aiswitch",
				auxilarygenerators = "ehm_mr_auxilarygenerators",
				overengineered = "ehm_mr_overengineered",
				hei = "ehm_mr_heavyenergyintegration",
				blueShields = "ehm_sc_blueShields",	// id & class mismatch
				greenShields = "ehm_sc_greenShields",	// id & class mismatch
				redShields = "ehm_sc_redShields",	// id & class mismatch
				crimsonShields = "ehm_sc_yellowShields",	// id & class mismatch
				highTechShields = "ehm_sc_cyanShields",	// id & class mismatch
				lowTechShields = "ehm_sc_magentaShields",	// id & class mismatch
				midlineShields = "ehm_sc_purpleShields",	// id & class mismatch
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
				traveldrive = "ehm_sr_traveldrive",
				ballisticslotretrofit = "ehm_wr_ballisticslotretrofit",
				energyslotretrofit = "ehm_wr_energyslotretrofit",
				missileslotretrofit = "ehm_wr_missileslotretrofit",
				universalslotretrofit = "ehm_wr_universalslotretrofit",
				universalsmallslotretrofit = "ehm_wr_universalsmallslotretrofit";
		}
		public static final class shunts {
			public static final class adapters {
				public static final String
					mediumDual = "ehm_adapter_mediumDual", // must match weapon id in .csv and .wpn
					largeDual = "ehm_adapter_largeDual", // must match weapon id in .csv and .wpn
					largeTriple = "ehm_adapter_largeTriple", // must match weapon id in .csv and .wpn
					largeQuad = "ehm_adapter_largeQuad"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(mediumDual);
					set.add(largeDual);
					set.add(largeTriple);
					set.add(largeQuad);
				}
			}
			public static final class capacitors {
				public static final String
					large = "ehm_capacitor_large", // must match weapon id in .csv and .wpn
					medium = "ehm_capacitor_medium", // must match weapon id in .csv and .wpn
					small = "ehm_capacitor_small"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(large);
					set.add(medium);
					set.add(small);
				}
			}
			public static final class dissipators {
				public static final String
					large = "ehm_dissipator_large", // must match weapon id in .csv and .wpn
					medium = "ehm_dissipator_medium", // must match weapon id in .csv and .wpn
					small = "ehm_dissipator_small"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(large);
					set.add(medium);
					set.add(small);
				}
			}
			public static final class converters {
				public static final String
					mediumToLarge = "ehm_converter_mediumToLarge", // must match weapon id in .csv and .wpn
					smallToLarge = "ehm_converter_smallToLarge", // must match weapon id in .csv and .wpn
					smallToMedium = "ehm_converter_smallToMedium"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(mediumToLarge);
					set.add(smallToLarge);
					set.add(smallToMedium);
				}
			}
			public static final class diverters {
				public static final String
					large = "ehm_diverter_large", // must match weapon id in .csv and .wpn
					medium = "ehm_diverter_medium", // must match weapon id in .csv and .wpn
					small = "ehm_diverter_small"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(large);
					set.add(medium);
					set.add(small);
				}
			}
			public static final class launchTubes {
				public static final String
					large = "ehm_tube_large"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(large);
				}
			}
			public static final Set<String> set = new HashSet<String>();
			static {
				set.addAll(adapters.set);
				set.addAll(capacitors.set);
				set.addAll(dissipators.set);
				set.addAll(converters.set);
				set.addAll(diverters.set);
				set.addAll(launchTubes.set);
			}
		}
	}

	public static final class tag {
		public static final String
			base = "ehm_base", // must match hullmod tag in .csv

			experimental = "ehm", // must match hullmod/weapon tag in .csv
			restricted = "ehm_restricted", // must match hullmod/weapon tag in .csv
			extended = "ehm_extended", // must match hullmod/weapon tag in .csv
			customizable = "ehm_customizable", // must match hullmod/weapon tag in .csv

			systemRetrofit = "ehm_sr", // must match hullmod tag in .csv
			weaponRetrofit = "ehm_wr", // must match hullmod tag in .csv
			adapterRetrofit = "ehm_ar", // must match hullmod tag in .csv
			shieldCosmetic = "ehm_sc", // must match hullmod tag in .csv
			engineCosmetic = "ehm_ec", // must match hullmod tag in .csv

			reqShields = "ehm_sr_require_shields", // must match hullmod tag in .csv
			reqNoPhase = "ehm_sr_require_no_phase", // must match hullmod tag in .csv
			reqWings = "ehm_sr_require_wings", // must match hullmod tag in .csv

			externalAccess = "ehm_externalAccess", // must match hullmod tag in .csv

			adapterShunt = "ehm_adapter", // must match weapon tag in .csv
			capacitorShunt = "ehm_capacitor", // must match weapon tag in .csv
			dissipatorShunt = "ehm_dissipator", // must match weapon tag in .csv
			converterShunt = "ehm_converter", // must match weapon tag in .csv
			diverterShunt = "ehm_diverter", // must match weapon tag in .csv
			tubeShunt = "ehm_tube"; // must match weapon tag in .csv
	}

	public static final class affix {
		public static final String
			normalSlot = "WS", // should NOT be altered in any update
			adaptedSlot = "AS_", // should NOT be altered in any update
			convertedSlot = "CS_", // should NOT be altered in any update
			allRetrofit = "ehm_", // must match hullmod id in .csv
			systemRetrofit = "ehm_sr_", // must match hullmod id in .csv
			weaponRetrofit = "ehm_wr_", // must match hullmod id in .csv
			adapterRetrofit = "ehm_ar_", // must match hullmod id in .csv
			shieldCosmetic = "ehm_sc_", // must match hullmod id in .csv
			engineCosmetic = "ehm_ec_"; // must match hullmod id in .csv
	}

	public static final class events {
		public static final String 
			onInstall = "onInstall",
			onRemove = "onRemove",
			onEnhance = "onEnhance",
			onNormalize = "onNormalize",
			onSuppress = "onSuppress",
			onRestore = "onRestore";
	}
}