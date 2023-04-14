package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ParticleEffect;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.PlayerStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.version.VersionSound;

public class Earthquake extends Ability {
	public Earthquake() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 3);
		addModifier("duration", 2);
		addModifier("amplifier", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AttackResult whenCast(PlayerStats stats, LivingEntity target, AbilityData data, double damage) {
		if (!stats.getPlayer().isOnGround())
			return new AttackResult(false);

		double damage1 = data.getModifier("damage");
		double slowDuration = data.getModifier("duration");
		double slowAmplifier = data.getModifier("amplifier");

		new BukkitRunnable() {
			Vector vec = getTargetDirection(stats.getPlayer(), target).setY(0);
			Location loc = stats.getPlayer().getLocation().clone();
			int ti = 0;
			List<Integer> hit = new ArrayList<>();

			public void run() {
				ti++;
				if (ti > 20)
					cancel();

				loc.add(vec);
				ParticleEffect.CLOUD.display(.5f, 0, .5f, 0, 5, loc);
				loc.getWorld().playSound(loc, VersionSound.BLOCK_GRAVEL_BREAK.getSound(), 2, 1);
				for (Entity target : MMOUtils.getNearbyChunkEntities(loc))
					if (MMOUtils.canDamage(stats.getPlayer(), target) && loc.distanceSquared(target.getLocation()) < 2 && !hit.contains(target.getEntityId())) {
						hit.add(target.getEntityId());
						MMOItems.plugin.getDamage().damage(stats, (LivingEntity) target, damage1, DamageType.MAGIC);
						((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDuration * 20), (int) slowAmplifier));
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		return new AttackResult(true);
	}
}
