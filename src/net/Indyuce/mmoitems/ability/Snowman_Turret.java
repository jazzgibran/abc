package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ParticleEffect;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.PlayerStats;
import net.Indyuce.mmoitems.version.VersionSound;

public class Snowman_Turret extends Ability {
	public Snowman_Turret() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 6);
		addModifier("cooldown", 35);
		addModifier("damage", 2);
		addModifier("radius", 20);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AttackResult whenCast(PlayerStats stats, LivingEntity target, AbilityData data, double damage) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null)
			return new AttackResult(false);

		double duration = Math.min(data.getModifier("duration") * 20, 300);
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);
		double damage1 = data.getModifier("damage");

		loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMEN_TELEPORT.getSound(), 2, 1);
		final Snowman snowman = (Snowman) loc.getWorld().spawnEntity(loc.add(0, 1, 0), EntityType.SNOWMAN);
		snowman.setInvulnerable(true);
		snowman.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 254, true));
		new BukkitRunnable() {
			int ti = 0;
			double j = 0;

			public void run() {
				if (ti++ > duration || stats.getPlayer().isDead() || snowman == null || snowman.isDead()) {
					snowman.remove();
					cancel();
				}

				j += Math.PI / 24 % (2 * Math.PI);
				for (double k = 0; k < 3; k++)
					ParticleEffect.SPELL_INSTANT.display(0, 0, 0, 0, 1, snowman.getLocation().add(Math.cos(j + k / 3 * 2 * Math.PI) * 1.3, 1, Math.sin(j + k / 3 * 2 * Math.PI) * 1.3));
				ParticleEffect.FIREWORKS_SPARK.display(0, 0, 0, .2f, 1, snowman.getLocation().add(0, 1, 0));

				if (ti % 2 == 0)
					for (Entity entity : snowman.getWorld().getEntities())
						if (!entity.equals(snowman) && MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(snowman.getLocation()) < radiusSquared) {
							snowman.getWorld().playSound(snowman.getLocation(), VersionSound.ENTITY_SNOWBALL_THROW.getSound(), 1, 1.3f);
							Snowball snowball = snowman.launchProjectile(Snowball.class);
							snowball.setVelocity(entity.getLocation().add(0, entity.getHeight() / 2, 0).toVector().subtract(snowman.getLocation().add(0, 1, 0).toVector()).normalize().multiply(1.3));
							MMOItems.plugin.getEntities().registerCustomEntity(snowball, damage1);
							break;
						}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);

		return new AttackResult(true);
	}
}
