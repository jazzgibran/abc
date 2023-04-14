package net.Indyuce.mmoitems.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.SpecialChar;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.version.VersionSound;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class ItemBrowser extends PluginInventory {
	private Type type;
	private List<String> itemIDs;
	private Map<String, ItemStack> cached = new HashMap<String, ItemStack>();

	private static final int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };

	public ItemBrowser(Player player) {
		this(player, null, 1);
	}

	public ItemBrowser(Player player, Type type, int page) {
		super(player, page);
		this.type = type;

		if (type != null)
			itemIDs = new ArrayList<String>(type.getConfigFile().getKeys(false));
	}

	@Override
	public Inventory getInventory() {
		int min = (page - 1) * slots.length;
		int max = page * slots.length;
		int n = 0;

		/*
		 * displays all possible item types if no type was previously selected
		 * by the player
		 */
		if (type == null) {
			Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Explorer");
			List<Type> types = new ArrayList<>(MMOItems.plugin.getTypes().getAll());
			for (int j = min; j < Math.min(max, types.size()); j++) {
				Type type = types.get(j);
				int items = type.getConfigFile().getKeys(false).size();

				ItemStack item = type.getItem();
				item.setAmount(Math.max(1, Math.min(64, items)));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + type.getName() + ChatColor.DARK_GRAY + " (Click to browse)");
				meta.addItemFlags(ItemFlag.values());
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "There " + (items > 1 || items < 1 ? "are" : "is") + " " + (items < 1 ? "" + ChatColor.RED + ChatColor.ITALIC + "no" : "" + ChatColor.GOLD + ChatColor.ITALIC + items) + ChatColor.GRAY + ChatColor.ITALIC + " item" + (items > 1 ? "s" : "") + " in that type.");
				meta.setLore(lore);
				item.setItemMeta(meta);

				item = MMOItems.plugin.getNMS().addTag(item, new ItemTag("typeId", type.getId()));

				inv.setItem(slots[n++], item);
			}

			ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
			ItemMeta glassMeta = glass.getItemMeta();
			glassMeta.setDisplayName(ChatColor.RED + "- No type -");
			glass.setItemMeta(glassMeta);

			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta nextMeta = next.getItemMeta();
			nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
			next.setItemMeta(nextMeta);

			ItemStack previous = new ItemStack(Material.ARROW);
			ItemMeta previousMeta = previous.getItemMeta();
			previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
			previous.setItemMeta(previousMeta);

			while (n < slots.length)
				inv.setItem(slots[n++], glass);
			inv.setItem(18, page > 1 ? previous : null);
			inv.setItem(26, inv.getItem(34).equals(glass) ? null : next);

			return inv;
		}

		ItemStack error = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		ItemMeta errorMeta = error.getItemMeta();
		errorMeta.setDisplayName(ChatColor.RED + "- Error -");
		List<String> errorLore = new ArrayList<String>();
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "An error occured while");
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "trying to generate that item.");
		errorMeta.setLore(errorLore);
		error.setItemMeta(errorMeta);

		/*
		 * displays every item in a specific type. items are cached inside the
		 * map at the top to reduce performance impact and are directly rendered
		 */
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Explorer: " + type.getName());
		for (int j = min; j < Math.min(max, itemIDs.size()); j++) {
			String id = itemIDs.get(j);
			if (!cached.containsKey(id)) {
				ItemStack item = MMOItems.getItem(type, id);
				if (item == null || item.getType() == Material.AIR) {
					cached.put(id, error);
					inv.setItem(slots[n++], error);
					continue;
				}

				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<String>();
				lore.add("");
				lore.add(ChatColor.YELLOW + SpecialChar.smallListDash + " Left click to obtain this item.");
				lore.add(ChatColor.YELLOW + SpecialChar.smallListDash + " Right click to edit this item.");
				meta.setLore(lore);
				item.setItemMeta(meta);

				cached.put(id, item);
			}

			inv.setItem(slots[n++], cached.get(id));
		}

		ItemStack noItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemMeta noItemMeta = noItem.getItemMeta();
		noItemMeta.setDisplayName(ChatColor.RED + "- No item -");
		noItem.setItemMeta(noItemMeta);

		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta nextMeta = next.getItemMeta();
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		next.setItemMeta(nextMeta);

		ItemStack back = new ItemStack(Material.ARROW);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(ChatColor.GREEN + SpecialChar.rightArrow + " Back");
		back.setItemMeta(backMeta);

		ItemStack previous = new ItemStack(Material.ARROW);
		ItemMeta previousMeta = previous.getItemMeta();
		previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
		previous.setItemMeta(previousMeta);

		while (n < slots.length)
			inv.setItem(slots[n++], noItem);
		inv.setItem(49, back);
		inv.setItem(18, page > 1 ? previous : null);
		inv.setItem(26, max >= itemIDs.size() ? null : next);
		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory())
			return;

		ItemStack item = event.getCurrentItem();
		if (MMOUtils.isPluginItem(item, false)) {
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
				page++;
				open();
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
				page--;
				open();
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + SpecialChar.rightArrow + " Back"))
				new ItemBrowser(player).open();

			if (type == null && !item.getItemMeta().getDisplayName().equals(ChatColor.RED + "- No type -")) {
				Type type = MMOItems.plugin.getTypes().get(MMOItems.plugin.getNMS().getStringTag(item, "typeId"));
				new ItemBrowser(player, type, 1).open();
			}
		}

		String id = MMOItems.plugin.getNMS().getStringTag(item, "MMOITEMS_ITEM_ID");
		if (id.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			player.getInventory().addItem(removeLastLoreLines(item, 3));
			player.playSound(player.getLocation(), VersionSound.ENTITY_EXPERIENCE_ORB_PICKUP.getSound(), 1, 2);
		}

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			new ItemEdition(player, type, id, 1, removeLastLoreLines(item, 3)).open();
	}

	private ItemStack removeLastLoreLines(ItemStack item, int amount) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		meta.setLore(lore.subList(0, lore.size() - amount));

		ItemStack item1 = item.clone();
		item1.setItemMeta(meta);
		return item1;
	}
}
