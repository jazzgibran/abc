package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ParticleEffect;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AbilityData;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.PlayerStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.version.VersionSound;

public class HealingAura extends Ability {
    public HealingAura() {
        super(CastingMode.PASSIVE);

        addModifier("range", 5);
        addModifier("heal", 2);
        addModifier("interval", 3);
    }

    @Override
    public void whenPassive(PlayerStats stats, LivingEntity target, AbilityData data) {
        double range = data.getModifier("range");
        double heal = data.getModifier("heal");
        double interval = data.getModifier("interval");

        new BukkitRunnable() {
            public void run() {
                for (Entity entity : stats.getPlayer().getNearbyEntities(range, range, range)) {
                    if (!(entity instanceof LivingEntity))
                        continue;
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (livingEntity == stats.getPlayer())
                        continue;
                    if (!MMOUtils.canTarget(stats.getPlayer(), livingEntity))
                        continue;

                    double healAmount = Math.min(livingEntity.getMaxHealth() - livingEntity.getHealth(), heal);
                    if (healAmount <= 0)
                        continue;

                    livingEntity.setHealth(Math.min(livingEntity.getMaxHealth(), livingEntity.getHealth() + healAmount));
                    livingEntity.getWorld().playSound(livingEntity.getLocation(), VersionSound.ENTITY_EXPERIENCE_ORB_PICKUP.getSound(), 1, 0);
                    ParticleEffect.HEART.display(0, 0, 0, .1f, 6, livingEntity.getLocation());
                }
            }
        }.runTaskTimer(MMOItems.plugin, 0, (long) (20 * interval));
    }

    @Override
    public AttackResult whenAttack(PlayerStats stats, LivingEntity target, AbilityData data, double damage) {
        return new AttackResult(false);
    }

    @Override
    public AttackResult whenDamaged(PlayerStats stats, LivingEntity damager, AbilityData data, double damage) {
        return new AttackResult(false);
    }
}
