package lyr.misc;

import java.util.HashSet;
import java.util.Set;

public class lyr_internals {
	public static final String logName = "EHM";
	public static final String logPrefix = "(Experimental Hull Modifications) - ";
	public static class id {
		public static final String
			drillSound = "drill", // must match .json
			baseRetrofit = "ehm_base"; // must match hullmod id in .csv
		public static class hullmods {
			public static final String
				overengineered = "ehm_mr_overengineered",
				stepdownadapter = "ehm_ar_stepdownadapter",
				diverterandconverter = "ehm_ar_diverterandconverter",
				mutableshunt = "ehm_ar_mutableshunt";
		}
		public static class shunts {
			public static class adapters {
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
			public static class capacitors {
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
			public static class dissipators {
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
			public static class converters {
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
			public static class diverters {
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
			public static class launchTube {
				public static final String
					large = "ehm_bay_large"; // must match weapon id in .csv and .wpn
				public static final Set<String> set = new HashSet<String>();
				static {
					set.add(large);
				}
			}
			public static class hasMutableBonus {
				public static final Set<String> set = new HashSet<String>();
				static {
					set.addAll(capacitors.set);
					set.addAll(dissipators.set);
					set.addAll(launchTube.set);
				}
			}
			public static final Set<String> set = new HashSet<String>();
			static {
				set.addAll(adapters.set);
				set.addAll(capacitors.set);
				set.addAll(dissipators.set);
				set.addAll(converters.set);
				set.addAll(diverters.set);
			}
		}
	}

	public static class tag {
		public static final String
			base = "ehm_base", // must match hullmod tag in .csv

			experimental = "ehm", // must match hullmod/weapon tag in .csv
			restricted = "ehm_restricted", // must match hullmod/weapon tag in .csv
			extended = "ehm_extended", // must match hullmod/weapon tag in .csv

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
			mutableShunt = "ehm_mutable"; // must match weapon tag in .csv
	}

	public static class affix {
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

	public static class event {
		public static final String onInstall = "onInstall";
		public static final String onRemove = "onRemove";
		public static final String sModCleanUp = "sModCleanUp";
	}

	public static class eventMethod {
		public static final String onInstall = "onInstall";
		public static final String onRemove = "onRemove";
		public static final String sModCleanUp = "sModCleanUp";
	}
}