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
		public static class utility {
			public static class adapter {
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
			public static class shunt {
				public static class capacitor {
					public static final String
						largeCapacitor = "ehm_shunt_largeCapacitor", // must match weapon id in .csv and .wpn
						mediumCapacitor = "ehm_shunt_mediumCapacitor", // must match weapon id in .csv and .wpn
						smallCapacitor = "ehm_shunt_smallCapacitor"; // must match weapon id in .csv and .wpn
					public static final Set<String> set = new HashSet<String>();
					static {
						set.add(largeCapacitor);
						set.add(mediumCapacitor);
						set.add(smallCapacitor);
					}
				}
				public static class heatsink {
					public static final String
						largeHeatsink = "ehm_shunt_largeHeatsink", // must match weapon id in .csv and .wpn
						mediumHeatsink = "ehm_shunt_mediumHeatsink", // must match weapon id in .csv and .wpn
						smallHeatsink = "ehm_shunt_smallHeatsink"; // must match weapon id in .csv and .wpn
					public static final Set<String> set = new HashSet<String>();
					static {
						set.add(largeHeatsink);
						set.add(mediumHeatsink);
						set.add(smallHeatsink);
					}
				}
				public static final Set<String> set = new HashSet<String>();
				static {
					set.addAll(heatsink.set);
					set.addAll(capacitor.set);
				}
			}
			public static final Set<String> set = new HashSet<String>();
			static {
				set.addAll(shunt.set);
				set.addAll(adapter.set);
			}
		}
	}

	public static class tag {
		public static final String
			baseRetrofit = "ehm_base", // must match hullmod tag in .csv
			allRetrofit = "ehm", // must match hullmod tag in .csv
			externalAccess = "ehm_externalAccess", // must match hullmod tag in .csv
			restricted = "ehm_restricted", // must match hullmod tag in .csv
			systemRetrofit = "ehm_sr", // must match hullmod tag in .csv
			weaponRetrofit = "ehm_wr", // must match hullmod tag in .csv
			adapterRetrofit = "ehm_ar", // must match hullmod tag in .csv
			shieldCosmetic = "ehm_sc", // must match hullmod tag in .csv
			engineCosmetic = "ehm_ec", // must match hullmod tag in .csv
			adapterUtility = "ehm_adapter", // must match weapon tag in .csv
			shuntUtility = "ehm_shunt", // must match weapon tag in .csv
			reqShields = "ehm_sr_require_shields", // must match hullmod tag in .csv
			reqNoPhase = "ehm_sr_require_no_phase", // must match hullmod tag in .csv
			reqWings = "ehm_sr_require_wings"; // must match hullmod tag in .csv
	}

	public static class affix {
		public static final String
			adaptedSlot = "AS_", // should NOT be altered in any update
			allRetrofit = "ehm_", // must match hullmod id in .csv
			systemRetrofit = "ehm_sr_", // must match hullmod id in .csv
			weaponRetrofit = "ehm_wr_", // must match hullmod id in .csv
			adapterRetrofit = "ehm_ar_", // must match hullmod id in .csv
			shieldCosmetic = "ehm_sc_", // must match hullmod id in .csv
			engineCosmetic = "ehm_ec_"; // must match hullmod id in .csv
	}
}