package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemStat;
import net.Indyuce.mmoitems.api.MMOItem;
import net.Indyuce.mmoitems.api.SpecialChar;
import net.Indyuce.mmoitems.api.Stat;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.TypeSet;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Item_Type_Restriction extends ItemStat {
	public Item_Type_Restriction() {
		super(new ItemStack(Material.EMERALD), "Item Type Restriction", new String[] { "This option defines the item types", "on which your gem can be applied." }, "item-type-restriction", new String[] { "gem_stone" });
	}

	@Override
	public boolean guiClick(PluginInventory inv, InventoryClickEvent event, Player player, Type type, String path, Stat stat) {
		FileConfiguration config = type.getConfigFile();

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, Stat.ITEM_TYPE_RESTRICTION).enable("Write in the chat the item type you want your gem to support.", "Supported formats: WEAPON or BLUNT, PIERCING, SLASHING, OFFHAND, EXTRA.");

		// if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
		// StatEdition.put(player, new StatEdition(type, path,
		// ItemStat.ITEM_TYPE_RESTRICTION, event.getInventory(), 2));
		// player.closeInventory();
		// inv.startChatEdition(player);
		// player.sendMessage(MMOItems.getPrefix() + "Write in the chat
		// the value you want to display in the lore.");
		// }
		//
		// if (event.getAction() == InventoryAction.DROP_ONE_SLOT)
		// if (config.getConfigurationSection(path).contains(getPath()))
		// if (config.getConfigurationSection(path + "." +
		// getPath()).contains("display")) {
		// config.set(path + "." + getPath() + ".display", null);
		// type.saveConfigFile(config, path);
		// new ItemEdition(player, type, path).open();
		// player.sendMessage(MMOItems.getPrefix() + "Successfully reset
		// the type restrictions display.");
		// }

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (config.getConfigurationSection(path).contains(getPath())) {
				List<String> list = config.getStringList(path + "." + getPath());
				if (list.size() < 1)
					return true;

				String last = list.get(list.size() - 1);
				list.remove(last);
				config.set(path + "." + getPath(), list.size() == 0 ? null : list);
				type.saveConfigFile(config, path);
				new ItemEdition(player, type, path).open();
				player.sendMessage(MMOItems.getPrefix() + "Successfully removed " + last + ".");
			}
		return true;
	}

	@Override
	public boolean chatListener(Type type, String path, Player player, FileConfiguration config, String message, Object... info) {
		// if (counter > 1) {
		// config.set(path + "." + getPath() + ".display", message);
		// type.saveConfigFile(config, path);
		// new ItemEdition(player, type, path).open();
		// player.sendMessage(MMOItems.getPrefix() + "Type restrictions
		// display successfully changed to " + message + ChatColor.GRAY + ".");
		// return true;
		// }

		String format = message.toUpperCase().replace(" ", "_").replace("-", "_");
		if (!isValid(format)) {
			player.sendMessage(MMOItems.getPrefix() + ChatColor.RED + format + " is not a valid item type/set.");
			player.sendMessage(MMOItems.getPrefix() + ChatColor.RED + "See all item types here: /mi list type.");
			player.sendMessage(MMOItems.getPrefix() + "You can also enter WEAPON or BLUNT, PIERCING, SLASHING, OFFHAND, EXTRA.");
			return false;
		}

		List<String> list = config.getConfigurationSection(path).contains(getPath()) ? config.getStringList(path + "." + getPath()) : new ArrayList<String>();
		list.add(format);
		config.set(path + "." + getPath(), list);
		type.saveConfigFile(config, path);
		new ItemEdition(player, type, path).open();
		player.sendMessage(MMOItems.getPrefix() + "Your gem now supports " + format + ".");
		return true;
	}

	@Override
	public boolean displayValue(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains(getPath()))
			lore.add(ChatColor.RED + "Your gem supports any item type.");
		else
			for (String s : config.getStringList(path + "." + getPath()))
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + s);
		lore.add("");
		lore.add(ChatColor.YELLOW + SpecialChar.listDash + " Click to add a supported item type/set.");
		lore.add(ChatColor.YELLOW + SpecialChar.listDash + " Right click to remove the last element.");
		return true;
	}

	@Override
	public boolean readStatInfo(MMOItem item, ConfigurationSection config) {
		return apply(item, config.getStringList(getPath()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apply(MMOItem item, Object... values) {
		// List<String> displayedTypes = new ArrayList<String>();
		//
		// for (String typeId : (List<String>) values[0])
		// try {
		// displayedTypes.add(Type.valueOf(typeId).getName());
		// } catch (Exception e) {
		// }
		//
		// String joined = String.join(", ", displayedTypes);
		// item.insertInLore(getPath(), translate().replace("#", joined));
		item.addItemTag(new ItemTag("MMOITEMS_ITEM_TYPE_RESTRICTION", String.join(",", (List<String>) values[0])));
		return true;
	}

	private boolean isValid(String format) {
		if (format.equals("WEAPON"))
			return true;

		for (Type type : MMOItems.plugin.getTypes().getAll())
			if (type.getId().equals(format))
				return true;

		for (TypeSet set : TypeSet.values())
			if (set.name().equals(format))
				return true;

		return false;
	}
}
