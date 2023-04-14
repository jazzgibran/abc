package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ParticleEffect;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.PlayerStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.version.VersionSound;

public class Freezing_Curse extends Ability {
	public Freezing_Curse() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 7);
		addModifier("duration", 3);
		addModifier("damage", 3);
		addModifier("radius", 3);
		addModifier("amplifier", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AttackResult whenCast(PlayerStats stats, LivingEntity target, AbilityData data, double damage) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null)
			return new AttackResult(false);

		new BukkitRunnable() {
			final double rads = Math.toRadians(stats.getPlayer().getEyeLocation().getYaw() - 90);
			double ti = rads;
			int j = 0;

			public void run() {

				if (j++ % 2 == 0)
					loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_PLING.getSound(), 2, (float) (.5 + ((ti - rads) / (Math.PI * 2) * 1.5)));
				for (int j = 0; j < 2; j++) {
					ti += Math.PI / 32;
					ParticleEffect.SPELL_INSTANT.display(0, 0, 0, 0, 1, loc.clone().add(Math.cos(ti) * 3, .1, Math.sin(ti) * 3));
				}

				if (ti > Math.PI * 2 + rads) {
					loc.getWorld().playSound(loc, VersionSound.BLOCK_GLASS_BREAK.getSound(), 3, .5f);

					for (double j = 0; j < Math.PI * 2; j += Math.PI / 32)
						ParticleEffect.CLOUD.display(0, 0, 0, 0, 1, loc.clone().add(Math.cos(j) * 3, .1, Math.sin(j) * 3));

					double radius = data.getModifier("radius");
					double amplifier = data.getModifier("amplifier");
					double duration = data.getModifier("duration");
					double damage = data.getModifier("damage");
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (entity.getLocation().distanceSquared(loc) < radius * radius && MMOUtils.canDamage(stats.getPlayer(), entity)) {
							MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, damage, DamageType.MAGIC);
							((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
							((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration * 20), (int) amplifier));
						}
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		return new AttackResult(true);
	}
}
