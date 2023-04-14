package net.Indyuce.mmoitems.gui.edition;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.SpecialChar;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class AbilityListEdition extends PluginInventory {
	private static final DecimalFormat modifierFormat = new DecimalFormat("0.###");

	public AbilityListEdition(Player player, Type type, String id) {
		super(player, type, id, 1);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Ability List");
		int[] slots = { 19, 20, 21, 22, 23, 24, 25 };
		int n = 0;

		FileConfiguration config = type.getConfigFile();

		if (config.getConfigurationSection(id).contains("ability"))
			for (String key : config.getConfigurationSection(id + ".ability").getKeys(false)) {
				String abilityFormat = config.getString(id + ".ability." + key + ".type");
				Ability ability = abilityFormat != null && MMOItems.plugin.getAbilities().hasAbility(abilityFormat = abilityFormat.toUpperCase().replace(" ", "_").replace("-", "_")) ? MMOItems.plugin.getAbilities().getAbility(abilityFormat) : null;

				CastingMode castMode = CastingMode.safeValueOf(config.getString(id + ".ability." + key + ".mode"));

				ItemStack abilityItem = new ItemStack(Material.BLAZE_POWDER);
				ItemMeta abilityItemMeta = abilityItem.getItemMeta();
				abilityItemMeta.setDisplayName(ability != null ? ChatColor.GREEN + ability.getName() : ChatColor.RED + "! No Ability Selected !");
				List<String> abilityItemLore = new ArrayList<>();
				abilityItemLore.add("");
				abilityItemLore.add(ChatColor.GRAY + "Cast Mode: " + (castMode != null ? ChatColor.GOLD + castMode.getName() : ChatColor.RED + "Not Selected"));
				abilityItemLore.add("");

				boolean check = false;
				if (ability != null)
					for (String modifier : config.getConfigurationSection(id + ".ability." + key).getKeys(false))
						if (!modifier.equals("type") && !modifier.equals("mode") && ability.getModifiers().contains(modifier)) {
							abilityItemLore.add(ChatColor.GRAY + "* " + MMOUtils.caseOnWords(modifier.toLowerCase().replace("-", " ")) + ": " + ChatColor.GOLD + modifierFormat.format(config.getDouble(id + ".ability." + key + "." + modifier)));
							check = true;
						}
				if (check)
					abilityItemLore.add("");

				abilityItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Left click to edit.");
				abilityItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Right click to remove.");
				abilityItemMeta.setLore(abilityItemLore);
				abilityItem.setItemMeta(abilityItemMeta);

				abilityItem = MMOItems.plugin.getNMS().addTag(abilityItem, new ItemTag("configKey", key));

				inv.setItem(slots[n++], abilityItem);
			}

		ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- No Ability -");
		glass.setItemMeta(glassMeta);

		ItemStack add = new ItemStack(Material.BOOK_AND_QUILL);
		ItemMeta addMeta = add.getItemMeta();
		addMeta.setDisplayName(ChatColor.GREEN + "Add an ability...");
		add.setItemMeta(addMeta);

		inv.setItem(40, add);
		while (n < slots.length)
			inv.setItem(slots[n++], glass);
		addEditionInventoryItems(inv, true);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isPluginItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Add an ability...")) {
			FileConfiguration config = type.getConfigFile();
			if (!config.getConfigurationSection(id).contains("ability")) {
				config.createSection(id + ".ability.ability1");
				type.saveConfigFile(config, id);
				new AbilityEdition(player, type, id, "ability1").open();
				return;
			}

			if (config.getConfigurationSection(id + ".ability").getKeys(false).size() > 6) {
				player.sendMessage(MMOItems.getPrefix() + ChatColor.RED + "You've hit the 7 abilities/item limit.");
				return;
			}

			for (int j = 1; j < 8; j++)
				if (!config.getConfigurationSection(id + ".ability").contains("ability" + j)) {
					config.createSection(id + ".ability.ability" + j);
					type.saveConfigFile(config, id);
					new AbilityEdition(player, type, id, "ability" + j).open();
					break;
				}
		}

		String tag = MMOItems.plugin.getNMS().getStringTag(item, "configKey");
		if (tag.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new AbilityEdition(player, type, id, tag).open();

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			FileConfiguration config = type.getConfigFile();
			if (config.getConfigurationSection(id).contains("ability") && config.getConfigurationSection(id + ".ability").contains(tag)) {
				config.set(id + ".ability." + tag, null);
				type.saveConfigFile(config, id);
				open();
				player.sendMessage(MMOItems.getPrefix() + "Successfully removed " + ChatColor.GOLD + tag + ChatColor.DARK_GRAY + " (Internal ID)" + ChatColor.GRAY + ".");
			}
		}
	}
}