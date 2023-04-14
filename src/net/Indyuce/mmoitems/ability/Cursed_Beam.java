package net.Indyuce.mmoitems.ability;

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

public class Cursed_Beam extends Ability {
	public Cursed_Beam() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 8);
		addModifier("cooldown", 10);
		addModifier("duration", 5);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AttackResult whenCast(PlayerStats stats, LivingEntity target, AbilityData data, double damage) {
		double damage1 = data.getModifier("damage");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_WITHER_SHOOT.getSound(), 2, 2);
		new BukkitRunnable() {
			Vector dir = getTargetDirection(stats.getPlayer(), target).multiply(.3);
			Location loc = stats.getPlayer().getEyeLocation().clone();
			double r = 0.4;
			int ti = 0;

			public void run() {
				ti++;
				if (ti > 50)
					cancel();

				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				for (double j = 0; j < 4; j++) {
					loc.add(dir);
					for (double i = 0; i < Math.PI * 2; i += Math.PI / 6) {
						Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(i), r * Math.sin(i), 0), loc);
						loc.add(vec);
						ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1, loc);
						loc.add(vec.multiply(-1));
					}

					for (Entity target : entities)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							effect(target);
							loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMEN_TELEPORT.getSound(), 2, .7f);

							double duration = data.getModifier("duration");
							for (Entity aoeTarget : entities)
								if (MMOUtils.canDamage(stats.getPlayer(), target) && loc.distanceSquared(aoeTarget.getLocation().add(0, 1, 0)) < 9) {
									MMOItems.plugin.getDamage().damage(stats, (LivingEntity) aoeTarget, damage1, DamageType.MAGIC);
									((LivingEntity) aoeTarget).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (duration * 20), 0));
								}
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		return new AttackResult(true);
	}

	private void effect(Entity ent) {
		new BukkitRunnable() {
			final Location loc2 = ent.getLocation();
			double y = 0;

			public void run() {
				for (int i = 0; i < 3; i++) {
					y += .05;
					for (int j = 0; j < 2; j++) {
						double xz = y * Math.PI * .8 + (j * Math.PI);
						ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1, loc2.clone().add(Math.cos(xz) * 2.5, y, Math.sin(xz) * 2.5));
					}
				}
				if (y >= 3)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
