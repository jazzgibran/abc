package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.ConfigData;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.DropTable;
import net.Indyuce.mmoitems.api.event.ItemDropEvent;

public class DropTableManager implements Listener {
	private Map<EntityType, DropTable> monsters = new HashMap<>();
	private Map<Material, DropTable> blocks = new HashMap<>();

	public DropTableManager() {
		reload();
	}

	public void reload() {
		FileConfiguration config = ConfigData.getCD(MMOItems.plugin, "", "drops");

		monsters.clear();
		blocks.clear();

		if (config.contains("monsters"))
			for (String key : config.getConfigurationSection("monsters").getKeys(false)) {
				EntityType type = null;
				try {
					type = EntityType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
				} catch (Exception e) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't read the drop table mob type " + key);
					continue;
				}

				monsters.put(type, new DropTable(config.getConfigurationSection("monsters." + key)));
			}

		if (config.contains("blocks"))
			for (String key : config.getConfigurationSection("blocks").getKeys(false)) {
				Material material = null;
				try {
					material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
				} catch (Exception e) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't read the drop table material " + key);
					continue;
				}

				blocks.put(material, new DropTable(config.getConfigurationSection("blocks." + key)));
			}
	}

	@EventHandler
	public void a(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();

		if (monsters.containsKey(entity.getType())) {
			List<ItemStack> drops = monsters.get(entity.getType()).read(false);

			ItemDropEvent dropEvent = new ItemDropEvent(entity.getKiller(), drops, ItemDropEvent.DropCause.MONSTER, entity);
			Bukkit.getPluginManager().callEvent(dropEvent);
			if (dropEvent.isCancelled())
				return;

			event.getDrops().addAll(drops);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void b(BlockBreakEvent event) {
		if (event.isCancelled())
			return;

		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (player == null || player.getGameMode() == GameMode.CREATIVE)
			return;

		if (blocks.containsKey(block.getType())) {
			List<ItemStack> drops = blocks.get(block.getType()).read(hasSilkTouchTool(player));

			ItemDropEvent dropEvent = new ItemDropEvent(player, drops, ItemDropEvent.DropCause.BLOCK, block);
			Bukkit.getPluginManager().callEvent(dropEvent);
			if (dropEvent.isCancelled())
				return;

			for (ItemStack drop : drops)
				block.getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), drop);
		}
	}

	private boolean hasSilkTouchTool(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		return item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
	}
}