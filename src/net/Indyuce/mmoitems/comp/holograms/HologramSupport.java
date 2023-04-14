package net.Indyuce.mmoitems.comp.holograms;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.metadata.MetadataValue;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;

public abstract class HologramSupport {
	protected static final Random random = new Random();

	protected HologramSupport() {

		if (MMOItems.plugin.getConfig().getBoolean("game-indicators.damage.enabled"))
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority = EventPriority.HIGHEST)
				public void a(EntityDamageEvent event) {
					if (event.isCancelled() || event.getDamage() <= 0)
						return;

					Entity entity = event.getEntity();
					if (!(entity instanceof LivingEntity) || event.getEntity() instanceof ArmorStand)
						return;

					/*
					 * no damage indicator is displayed when the player is
					 * vanished using essentials.
					 */
					if (entity instanceof Player && isVanished((Player) entity))
						return;

					displayIndicator(entity, MMOItems.getLanguage().damageIndicatorFormat.replace("#", MMOItems.getLanguage().damageIndicatorDecimalFormat.format(event.getFinalDamage())));
				}
			}, MMOItems.plugin);

		if (MMOItems.plugin.getConfig().getBoolean("game-indicators.heal.enabled"))
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority = EventPriority.HIGHEST)
				public void a(EntityRegainHealthEvent event) {
					if (event.isCancelled() || event.getAmount() <= 0)
						return;

					Entity entity = event.getEntity();
					if (!(entity instanceof LivingEntity))
						return;

					/*
					 * no damage indicator is displayed when the player is
					 * vanished using essentials.
					 */
					if (entity instanceof Player && isVanished((Player) entity))
						return;

					displayIndicator(entity, MMOItems.getLanguage().healIndicatorFormat.replace("#", MMOItems.getLanguage().healIndicatorDecimalFormat.format(event.getAmount())));
				}
			}, MMOItems.plugin);
	}

	public void displayIndicator(Entity entity, String message) {
		displayIndicator(entity.getLocation().add((random.nextDouble() - .5) * 1.2, MMOUtils.getHeight(entity) * .75, (random.nextDouble() - .5) * 1.2), message, entity instanceof Player ? (Player) entity : null);
	}

	/*
	 * the third argument is the player which the hologram needs to be hidden
	 * from to prevent the indicator from taking too much space on the player
	 * screen
	 */
	public abstract void displayIndicator(Location loc, String message, Player player);

	private boolean isVanished(Player player) {
		for (MetadataValue meta : player.getMetadata("vanished"))
			if (meta.asBoolean())
				return true;
		return false;
	}
}
