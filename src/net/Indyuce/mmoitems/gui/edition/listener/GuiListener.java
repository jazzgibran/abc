package net.Indyuce.mmoitems.gui.edition.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.SpecialChar;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.AdvancedRecipeEdition;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;

public class GuiListener implements Listener {
	@EventHandler
	public void a(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();

		if (event.getInventory().getHolder() instanceof PluginInventory) {
			PluginInventory inventory = (PluginInventory) event.getInventory().getHolder();
			inventory.whenClicked(event);

			// if inventory is edition inventory
			// then the player can click specific items
			if (!inventory.isEditionInventory())
				return;

			if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isPluginItem(item, false))
				return;

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + SpecialChar.fourEdgedClub + " Get the Item! " + SpecialChar.fourEdgedClub))
				for (ItemStack drop : player.getInventory().addItem(event.getInventory().getItem(4)).values())
					player.getWorld().dropItemNaturally(player.getLocation(), drop);

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + SpecialChar.rightArrow + " Back"))
				new ItemEdition(player, inventory.getItemType(), inventory.getItemID()).open();
		}
	}

	@EventHandler
	public void b(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (AdvancedRecipeEdition.noDrop.contains(player.getUniqueId())) {
			AdvancedRecipeEdition.noDrop.remove(player.getUniqueId());
			event.setCancelled(true);
		}
	}
}
