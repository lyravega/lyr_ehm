package lyr.settings;

import java.util.HashSet;
import java.util.Set;

public class lyr_internals {
	//#region INTERNALS
	public static class id {
		public static final String drillSound = "drill"; // must match .json
		public static final String baseRetrofit = "ehm_base"; // must match hullmod id in .csv
		public static class adapter {
			public static final String mediumDual = "ehm_adapter_mediumDual"; // must match weapon id in .csv and .wpn
			public static final String largeDual = "ehm_adapter_largeDual"; // must match weapon id in .csv and .wpn
			public static final String largeTriple = "ehm_adapter_largeTriple"; // must match weapon id in .csv and .wpn
			public static final String largeQuad = "ehm_adapter_largeQuad"; // must match weapon id in .csv and .wpn
			public static final Set<String> set = new HashSet<String>();
			static {
				set.add(mediumDual);
				set.add(largeDual);
				set.add(largeTriple);
				set.add(largeQuad);
			}
		}
	}

	public static class tag {
		public static final String baseRetrofit = "ehm_base"; // must match hullmod tag in .csv
		public static final String allRetrofit = "ehm"; // must match hullmod tag in .csv
		public static final String externalAccess = "ehm_externalAccess"; // must match hullmod tag in .csv
		public static final String systemRetrofit = "ehm_sr"; // must match hullmod tag in .csv
		public static final String weaponRetrofit = "ehm_wr"; // must match hullmod tag in .csv
		public static final String adapterRetrofit = "ehm_ar"; // must match hullmod tag in .csv
		public static final String shieldCosmetic = "ehm_sc"; // must match hullmod tag in .csv
		public static final String engineCosmetic = "ehm_ec"; // must match hullmod tag in .csv
		public static final String adapterWeapon = "ehm_adapter"; // must match weapon tag in .csv
		public static final String reqShields = "ehm_sr_require_shields"; // must match hullmod tag in .csv
		public static final String reqNoPhase = "ehm_sr_require_no_phase"; // must match hullmod tag in .csv
		public static final String reqWings = "ehm_sr_require_wings"; // must match hullmod tag in .csv
	}

	public static class affix {
		public static final String adaptedSlot = "AS_"; // should NOT be altered in any update
		public static final String allRetrofit = "ehm_"; // must match hullmod id in .csv
		public static final String systemRetrofit = "ehm_sr_"; // must match hullmod id in .csv
		public static final String weaponRetrofit = "ehm_wr_"; // must match hullmod id in .csv
		public static final String adapterRetrofit = "ehm_ar_"; // must match hullmod id in .csv
		public static final String shieldCosmetic = "ehm_sc_"; // must match hullmod id in .csv
		public static final String engineCosmetic = "ehm_ec_"; // must match hullmod id in .csv
	}
	//#endregion 
	// END OF INTERNALS
}