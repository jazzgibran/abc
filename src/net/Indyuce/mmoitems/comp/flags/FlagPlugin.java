package net.Indyuce.mmoitems.comp.flags;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FlagPlugin {
	public boolean isPvpAllowed(Location loc);

	public boolean isFlagAllowed(Player player, CustomFlag customFlag);
	
	public enum CustomFlag {
		MI_ABILITIES,
		MI_WEAPONS,
		MI_COMMANDS,
		MI_CONSUMABLES,
		MI_TOOLS;

		public String getPath() {
			return name().toLowerCase().replace("_", "-");
		}
	}
}
