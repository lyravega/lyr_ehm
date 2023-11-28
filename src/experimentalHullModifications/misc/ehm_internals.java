package experimentalHullModifications.misc;

import java.util.*;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public final class ehm_internals {
	public static final class ids {
		public static final String
			hullModFilePath = "data/hullmods/hull_mods.csv",	// must be hullmod .csv file path
			drillSound = "drill",	// must match .json
			submarket = "ehm_submarket",	// must match submarket id in .csv
			ability = "ehm_ability",	// must match ability id in .csv
			faction = "experimental",	// must match faction id in .csv and .faction
			manufacturer = "Experimental",	// must match the 'designTypeColors' in settings.json
			mod = "lyr_ehm",	// must match .json
			experimental = "ehm";	// must match hullmod/weapon tag in .csv
	}

	public static final class shunts {
		private static final EnumMap<WeaponSize, Integer> slotValues = new EnumMap<WeaponSize, Integer>(WeaponSize.class);
		static {
			slotValues.put(WeaponSize.SMALL, 1);
			slotValues.put(WeaponSize.MEDIUM, 1);
			slotValues.put(WeaponSize.LARGE, 1);
		}

		public static final class adapters {
			public static final class ids {
				public static final String
					mediumDual = "ehm_adapter_mediumDual",	// must match weapon id in .csv and .wpn
					largeDual = "ehm_adapter_largeDual",	// must match weapon id in .csv and .wpn
					largeTriple = "ehm_adapter_largeTriple",	// must match weapon id in .csv and .wpn
					largeQuad = "ehm_adapter_largeQuad";	// must match weapon id in .csv and .wpn
			}
			public static final String activatorId = hullmods.activatorRetrofits.adapterShuntActivator;
			public static final String tag = "ehm_adapter";
			public static final String groupTag = tag;
			public static final Map<String, adapterParameters> dataMap = new HashMap<String, adapterParameters>();
			public static final Set<String> idSet = dataMap.keySet();
			private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.adaptedSlot, affixes.convertedSlot});

			public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
				return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
			}

			static {
				final adapterParameters mediumDual = new adapterParameters();
				mediumDual.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
				mediumDual.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
				dataMap.put(ids.mediumDual, mediumDual);

				final adapterParameters largeDual = new adapterParameters();
				largeDual.addChild("L", WeaponSize.MEDIUM, new Vector2f(0.0f, 12.0f)); // left
				largeDual.addChild("R", WeaponSize.MEDIUM, new Vector2f(0.0f, -12.0f)); // right
				dataMap.put(ids.largeDual, largeDual);

				final adapterParameters largeTriple = new adapterParameters();
				largeTriple.addChild("L", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // left
				largeTriple.addChild("R", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // right
				largeTriple.addChild("C", WeaponSize.MEDIUM, new Vector2f(0.0f, 0.0f)); // center
				dataMap.put(ids.largeTriple, largeTriple);

				final adapterParameters largeQuad = new adapterParameters();
				largeQuad.addChild("L", WeaponSize.SMALL, new Vector2f(0.0f, 6.0f)); // left
				largeQuad.addChild("R", WeaponSize.SMALL, new Vector2f(0.0f, -6.0f)); // right
				largeQuad.addChild("FL", WeaponSize.SMALL, new Vector2f(-4.0f, 18.0f)); // far left
				largeQuad.addChild("FR", WeaponSize.SMALL, new Vector2f(-4.0f, -18.0f)); // far right
				dataMap.put(ids.largeQuad, largeQuad);
			}

			public static class adapterParameters {
				private final Set<String> children; public Set<String> getChildren() { return this.children; }
				private final Map<String, Vector2f> childrenOffsets; public Vector2f getChildOffset(String childPrefix) { return this.childrenOffsets.get(childPrefix); }
				private final Map<String, WeaponSize> childrenSizes; public WeaponSize getChildSize(String childPrefix) { return this.childrenSizes.get(childPrefix); }

				private adapterParameters() {
					this.children = new HashSet<String>();
					this.childrenOffsets = new HashMap<String, Vector2f>();
					this.childrenSizes = new HashMap<String, WeaponSize>();
				}

				private void addChild(String childId, WeaponSize childSize, Vector2f childOffset) {
					this.children.add(childId);
					this.childrenOffsets.put(childId, childOffset);
					this.childrenSizes.put(childId, childSize);
				}
			}
		}

		public static final class converters {
			public static final class ids {
				public static final String
					mediumToLarge = "ehm_converter_mediumToLarge",	// must match weapon id in .csv and .wpn
					smallToLarge = "ehm_converter_smallToLarge",	// must match weapon id in .csv and .wpn
					smallToMedium = "ehm_converter_smallToMedium";	// must match weapon id in .csv and .wpn
			}
			public static final String activatorId = hullmods.activatorRetrofits.diverterConverterActivator;
			public static final String tag = "ehm_converter";
			public static final String groupTag = tag;
			public static final Map<String, converterParameters> dataMap = new HashMap<String, converterParameters>();
			public static final Set<String> idSet = dataMap.keySet();
			private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.adaptedSlot, affixes.convertedSlot});

			public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
				return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
			}

			static {
				dataMap.put(ids.mediumToLarge, new converterParameters("ML", WeaponSize.LARGE, slotValues.get(WeaponSize.LARGE) - slotValues.get(WeaponSize.MEDIUM)));
				dataMap.put(ids.smallToLarge, new converterParameters("SL", WeaponSize.LARGE, slotValues.get(WeaponSize.LARGE) - slotValues.get(WeaponSize.SMALL)));
				dataMap.put(ids.smallToMedium, new converterParameters("SM", WeaponSize.MEDIUM, slotValues.get(WeaponSize.MEDIUM) - slotValues.get(WeaponSize.SMALL)));
			}

			public static final class converterParameters {
				private final String childSuffix; public String getChildSuffix() { return this.childSuffix; }
				private final int childCost; public int getChildCost() { return this.childCost; }
				private final WeaponSize childSize; public WeaponSize getChildSize() { return this.childSize; }

				private converterParameters(String childSuffix, WeaponSize childSize, int childCost) {
					this.childSuffix = childSuffix;
					this.childCost = childCost;
					this.childSize = childSize;
				}
			}
		}

		public static final class diverters {
			public static final class ids {
				public static final String
					large = "ehm_diverter_large",	// must match weapon id in .csv and .wpn
					medium = "ehm_diverter_medium",	// must match weapon id in .csv and .wpn
					small = "ehm_diverter_small";	// must match weapon id in .csv and .wpn
			}
			public static final String activatorId = hullmods.activatorRetrofits.diverterConverterActivator;
			public static final String tag = "ehm_diverter";
			public static final String groupTag = tag;
			public static final Map<String, Integer> dataMap = new HashMap<String, Integer>();
			public static final Set<String> idSet = dataMap.keySet();
			private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.convertedSlot});

			public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
				return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
			}

			static {
				dataMap.put(ids.large, slotValues.get(WeaponSize.LARGE));
				dataMap.put(ids.medium, slotValues.get(WeaponSize.MEDIUM));
				dataMap.put(ids.small, slotValues.get(WeaponSize.SMALL));
			}
		}

		public static final class capacitors {
			public static final class ids {
				public static final String
					large = "ehm_capacitor_large",	// must match weapon id in .csv and .wpn
					medium = "ehm_capacitor_medium",	// must match weapon id in .csv and .wpn
					small = "ehm_capacitor_small";	// must match weapon id in .csv and .wpn
			}
			public static final String activatorId = hullmods.activatorRetrofits.mutableShuntActivator;
			public static final String tag = "ehm_capacitor";
			public static final String groupTag = tag;
			public static final Map<String, Integer> dataMap = new HashMap<String, Integer>();
			public static final Set<String> idSet = dataMap.keySet();
			private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.convertedSlot});

			public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
				return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
			}

			static {
				dataMap.put(ids.large, slotValues.get(WeaponSize.LARGE));
				dataMap.put(ids.medium, slotValues.get(WeaponSize.MEDIUM));
				dataMap.put(ids.small, slotValues.get(WeaponSize.SMALL));
			}
		}

		public static final class dissipators {
			public static final class ids {
				public static final String
					large = "ehm_dissipator_large",	// must match weapon id in .csv and .wpn
					medium = "ehm_dissipator_medium",	// must match weapon id in .csv and .wpn
					small = "ehm_dissipator_small";	// must match weapon id in .csv and .wpn
			}
			public static final String activatorId = hullmods.activatorRetrofits.mutableShuntActivator;
			public static final String tag = "ehm_dissipator";
			public static final String groupTag = tag;
			public static final Map<String, Integer> dataMap = new HashMap<String, Integer>();
			public static final Set<String> idSet = dataMap.keySet();
			private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.convertedSlot});

			public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
				return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
			}

			static {
				dataMap.put(ids.large, slotValues.get(WeaponSize.LARGE));
				dataMap.put(ids.medium, slotValues.get(WeaponSize.MEDIUM));
				dataMap.put(ids.small, slotValues.get(WeaponSize.SMALL));
			}
		}

		public static final class hangars {
			public static final class ids {
				public static final String
					large = "ehm_tube_large";	// must match weapon id in .csv and .wpn
			}
			public static final String activatorId = hullmods.activatorRetrofits.hangarShuntActivator;
			public static final String tag = "ehm_hangar";
			public static final String groupTag = tag;
			public static final Map<String, Integer> dataMap = new HashMap<String, Integer>();
			public static final Set<String> idSet = dataMap.keySet();
			private static final List<String> invalidSlotPrefixes = Arrays.asList(new String[]{affixes.convertedSlot});

			public static final boolean isValidSlot(WeaponSlotAPI slot, WeaponSpecAPI shuntSpec) {
				return !invalidSlotPrefixes.contains(slot.getId().substring(0,3));
			}

			static {
				dataMap.put(ids.large, 1);
			}
		}

		public static final String tag = ids.experimental;
		public static final Set<String> idSet = new HashSet<String>();
		static {
			idSet.addAll(adapters.idSet);
			idSet.addAll(capacitors.idSet);
			idSet.addAll(dissipators.idSet);
			idSet.addAll(converters.idSet);
			idSet.addAll(diverters.idSet);
			idSet.addAll(hangars.idSet);
		}
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
				adapterShuntActivator = "ehm_ar_stepdownadapter";
			public static final String tag = "ehm_ar";	// must match hullmod tag in .csv
		}

		public static final class systemRetrofits {
			public static final String
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
			public static final String tag = "ehm_sr";	// must match hullmod tag in .csv
		}

		public static final class weaponRetrofits {
			public static final String
				ballisticslotretrofit = "ehm_wr_ballisticslotretrofit",
				energyslotretrofit = "ehm_wr_energyslotretrofit",
				missileslotretrofit = "ehm_wr_missileslotretrofit",
				universalslotretrofit = "ehm_wr_universalslotretrofit",
				universalsmallslotretrofit = "ehm_wr_universalsmallslotretrofit";
			public static final String tag = "ehm_wr";	// must match hullmod tag in .csv
		}

		public static final class engineCosmetics {
			public static final String
				blueEngines = "ehm_cec_blueEngines",
				greenEngines = "ehm_cec_greenEngines",
				redEngines = "ehm_cec_redEngines",
				crimsonEngines = "ehm_ec_torpedoEngines",	// id & class mismatch
				highTechEngines = "ehm_ec_highTechEngines",
				lowTechEngines = "ehm_ec_lowTechEngines",
				midlineEngines = "ehm_ec_midlineEngines";
			public static final String tag = "ehm_ec";	// must match hullmod tag in .csv
		}

		public static final class shieldCosmetics {
			public static final String
				blueShields = "ehm_sc_blueShields",	// id & class mismatch
				greenShields = "ehm_sc_greenShields",	// id & class mismatch
				redShields = "ehm_sc_redShields",	// id & class mismatch
				crimsonShields = "ehm_sc_yellowShields",	// id & class mismatch
				highTechShields = "ehm_sc_cyanShields",	// id & class mismatch
				lowTechShields = "ehm_sc_magentaShields",	// id & class mismatch
				midlineShields = "ehm_sc_purpleShields";	// id & class mismatch
			public static final String tag = "ehm_sc";	// must match hullmod tag in .csv
		}

		public static final class misc {
			public static final String
				aiswitch = "ehm_mr_aiswitch",
				auxilarygenerators = "ehm_mr_auxilarygenerators",
				logisticsoverhaul = "ehm_mr_logisticsoverhaul",
				overengineered = "ehm_mr_overengineered";
			public static final String tag = "ehm_mr";	// must match hullmod tag in .csv
		}

		public static final class extensions {
			public static final String
				heavyenergyintegration = "ehm_mr_heavyenergyintegration",
				expensivemissiles = "ehm_mr_expensivemissiles";
		}

		public static final class tags {
			public static final String
				reqBase = "reqBase",
				reqNoLogistics = "reqNoLogistics",
				reqShield = "reqShield",
				reqNoPhase = "reqNoPhase",
				reqWingBays = "reqWingBays",
				reqNotChild = "reqNotChild",
				reqDiverterAndConverter = "reqDiverterAndConverter",
				hasWeaponsOnConvertedSlots = "hasWeaponsOnConvertedSlots",
				hasWeaponsOnAdaptedSlots = "hasWeaponsOnAdaptedSlots",
				hasExtraWings = "hasExtraWings",
				hasWeapons = "hasWeapons",
				hasMiniModules = "hasMiniModules",
				hasAnyFittedWings = "hasAnyFittedWings",
				experimental = ids.experimental;	// must match hullmod/weapon tag in .csv
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
			prefix = "ehmu",
			overdrive = prefix+"_overdrive";
	}

	public static final class stats {
		public static final String
			overdrive = upgrades.overdrive,
			adapters = shunts.adapters.groupTag,	// must match .wpn group tag; also used as a stat id
			converters = shunts.converters.groupTag,	// must match .wpn group tag; also used as a stat id
			diverters = shunts.diverters.groupTag,	// must match .wpn group tag; also used as a stat id
			capacitors = shunts.capacitors.groupTag,	// must match .wpn group tag; also used as a stat id
			dissipators = shunts.dissipators.groupTag,	// must match .wpn group tag; also used as a stat id
			hangars = shunts.hangars.groupTag,	// must match .wpn group tag; also used as a stat id
			ordnancePoints = "ehm_ordnancePoints",	// TODO: implement/use this
			slotPoints = "ehm_slotPoints",
			slotPointsNeeded = "ehm_slotPointsNeeded",
			slotPointsUsed = "ehm_slotPointsUsed",
			slotPointsFromMods = "ehm_slotPointsFromMods",
			slotPointsFromDiverters = diverters,	// having a separate stat for this is not necessary
			slotPointsToConverters = converters;	// having a separate stat for this is not necessary
	}

	public static final class affixes {
		public static final String
			normalSlot = "WS",	// should NOT be altered in any update
			adaptedSlot = "AS_",	// should NOT be altered in any update
			convertedSlot = "CS_",	// should NOT be altered in any update
			launchSlot = "LS_";	// should NOT be altered in any update
	}
}