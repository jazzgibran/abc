package net.Indyuce.mmoitems.ability.onhit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ParticleEffect;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ArrayCast;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.PlayerStats;
import net.Indyuce.mmoitems.version.VersionSound;

public class Weaken_Target extends Ability implements Listener {
	public Map<UUID, WeakenedInfo> marked = new HashMap<>();

	public Weaken_Target() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT);

		addModifier("duration", 4);
		addModifier("extra-damage", 40);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AttackResult whenCast(PlayerStats stats, LivingEntity initialTarget, AbilityData data, double damage) {
		LivingEntity target = initialTarget == null ? new ArrayCast(stats.getPlayer(), 50).getHitEntity() : initialTarget;
		if (target == null)
			return new AttackResult(false);

		if (marked.containsKey(target.getUniqueId()))
			return new AttackResult(false);

		marked.put(target.getUniqueId(), new WeakenedInfo(data.getModifier("extra-damage")));
		effect(target.getLocation());
		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ENDERMEN_HURT.getSound(), 2, 1.5f);

		/*
		 * display particles until the entity is hit again and eventually remove
		 * the mark from the entity
		 */
		new BukkitRunnable() {
			long duration = (long) (data.getModifier("duration") * 1000);

			public void run() {
				if (!marked.containsKey(target.getUniqueId()) || marked.get(target.getUniqueId()).date + duration < System.currentTimeMillis()) {
					cancel();
					return;
				}

				for (double j = 0; j < Math.PI * 2; j += Math.PI / 18)
					ParticleEffect.SMOKE_NORMAL.display(0, 0, 0, 0, 1, target.getLocation().clone().add(Math.cos(j) * .7, .1, Math.sin(j) * .7));
			}
		}.runTaskTimer(MMOItems.plugin, 0, 20);
		return new AttackResult(true);
	}

	@EventHandler
	public void a(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.ENTITY_EXPLOSION)
			return;

		Entity entity = event.getEntity();
		if (marked.containsKey(entity.getUniqueId())) {
			event.setDamage(event.getDamage() * (1 + marked.get(entity.getUniqueId()).extraDamage));
			effect(entity.getLocation());
			marked.remove(entity.getUniqueId());
			entity.getWorld().playSound(entity.getLocation(), VersionSound.ENTITY_ENDERMEN_DEATH.getSound(), 2, 2);
		}
	}

	@EventHandler
	public void b(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if (item.getType() == Material.MILK_BUCKET && marked.containsKey(player.getUniqueId())) {
			marked.remove(player.getUniqueId());
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDERMEN_DEATH.getSound(), 2, 2);
		}
	}

	private void effect(Location loc) {
		new BukkitRunnable() {
			double y = 0;

			public void run() {
				for (int j = 0; j < 3; j++) {
					y += .07;
					for (int k = 0; k < 3; k++)
						ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(Color.BLACK), loc.clone().add(Math.cos(y * Math.PI + (k * Math.PI * 2 / 3)) * (3 - y) / 2.5, y, Math.sin(y * Math.PI + (k * Math.PI * 2 / 3)) * (3 - y) / 2.5));
				}
				if (y > 3)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	public class WeakenedInfo {
		public long date = System.currentTimeMillis();
		public double extraDamage;

		public WeakenedInfo(double extraDamage) {
			this.extraDamage = extraDamage / 100;
		}
	}
}
