package net.Indyuce.mmoitems.ability;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ParticleEffect;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.PlayerStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.version.VersionSound;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Throw_Up extends Ability implements Listener {
	public Throw_Up() {
		super("THROW_UP", "Throw Up", CastingMode.RIGHT_CLICK);

		addModifier("duration", 2.5);
		addModifier("damage", 2);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AttackResult whenCast(PlayerStats stats, LivingEntity target, AbilityData data, double damage) {
		double duration = data.getModifier("duration") * 10;
		double damage1 = data.getModifier("damage") / 2;

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > duration)
					cancel();

				Location loc = stats.getPlayer().getEyeLocation();
				loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * 30));
				loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * 30));

				if (j % 5 == 0)
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (entity.getLocation().distanceSquared(loc) < 40)
							if (stats.getPlayer().getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(stats.getPlayer().getLocation().toVector())) < Math.PI / 6)
								if (MMOUtils.canDamage(stats.getPlayer(), entity))
									MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, damage1, DamageType.MAGIC);

				loc.getWorld().playSound(loc, VersionSound.ENTITY_ZOMBIE_HURT.getSound(), 1, 1);
				Item item = loc.getWorld().dropItem(stats.getPlayer().getLocation().add(0, 1.2, 0), MMOItems.plugin.getNMS().addTag(new ItemStack(Material.ROTTEN_FLESH), new ItemTag("noStack", UUID.randomUUID().toString())));
				item.setVelocity(loc.getDirection().multiply(.8));
				item.setPickupDelay(10000000);
				ParticleEffect.SMOKE_LARGE.display(loc.getDirection(), 1f, stats.getPlayer().getLocation().add(0, 1.2, 0));

				new BukkitRunnable() {
					public void run() {
						item.remove();
					}
				}.runTaskLater(MMOItems.plugin, 40);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
		return new AttackResult(true);
	}
}
