package data.hullmods;

import com.fs.starfarer.api.combat.ShipVariantAPI;

public interface _ehm_hullmodevents {
	public boolean onRemove(ShipVariantAPI variant);
	public boolean onInstall(ShipVariantAPI variant);
}
