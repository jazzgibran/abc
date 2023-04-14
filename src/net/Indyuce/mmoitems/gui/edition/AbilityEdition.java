package net.Indyuce.mmoitems.gui.edition;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
import net.Indyuce.mmoitems.api.Stat;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.gui.PluginInventory;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class AbilityEdition extends PluginInventory {
	private String configKey;
	private Ability ability;

	private static final DecimalFormat modifierFormat = new DecimalFormat("0.###");

	public AbilityEdition(Player player, Type type, String id, String configKey) {
		super(player, type, id, 1);
		this.configKey = configKey;
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Ability Edition");
		int[] slots = { 23, 24, 25, 32, 33, 34, 41, 42, 43 };
		int n = 0;

		FileConfiguration config = type.getConfigFile();

		String configString = config.getString(id + ".ability." + configKey + ".type");
		String format = configString == null ? "" : configString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z_]", "");
		ability = MMOItems.plugin.getAbilities().hasAbility(format) ? MMOItems.plugin.getAbilities().getAbility(format) : null;

		ItemStack abilityItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta abilityItemMeta = abilityItem.getItemMeta();
		abilityItemMeta.setDisplayName(ChatColor.GREEN + "Ability");
		List<String> abilityItemLore = new ArrayList<String>();
		abilityItemLore.add(ChatColor.GRAY + "Choose what ability your weapon will cast.");
		abilityItemLore.add("");
		abilityItemLore.add(ChatColor.GRAY + "Current Value: " + (ability == null ? ChatColor.RED + "No ability selected." : ChatColor.GOLD + ability.getName()));
		abilityItemLore.add("");
		abilityItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Left click to select.");
		abilityItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Right click to reset.");
		abilityItemMeta.setLore(abilityItemLore);
		abilityItem.setItemMeta(abilityItemMeta);

		if (ability != null) {
			String castModeConfigString = config.getString(id + ".ability." + configKey + ".mode");
			String castModeFormat = castModeConfigString == null ? "" : castModeConfigString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z_]", "");
			CastingMode castMode = CastingMode.safeValueOf(castModeFormat);

			ItemStack castModeItem = new ItemStack(Material.ARMOR_STAND);
			ItemMeta castModeItemMeta = castModeItem.getItemMeta();
			castModeItemMeta.setDisplayName(ChatColor.GREEN + "Casting Mode");
			List<String> castModeItemLore = new ArrayList<String>();
			castModeItemLore.add(ChatColor.GRAY + "Choose what action the player needs to");
			castModeItemLore.add(ChatColor.GRAY + "perform in order to cast your ability.");
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.GRAY + "Current Value: " + (castMode == null ? ChatColor.RED + "No mode selected." : ChatColor.GOLD + castMode.getName()));
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Left click to select.");
			castModeItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Right click to reset.");
			castModeItemMeta.setLore(castModeItemLore);
			castModeItem.setItemMeta(castModeItemMeta);

			inv.setItem(30, castModeItem);
		}

		if (ability != null) {
			ConfigurationSection section = config.getConfigurationSection(id + ".ability." + configKey);
			for (String modifier : ability.getModifiers()) {
				ItemStack modifierItem = new ItemStack(Material.INK_SACK, 1, (short) 8);
				ItemMeta modifierItemMeta = modifierItem.getItemMeta();
				modifierItemMeta.setDisplayName(ChatColor.GREEN + MMOUtils.caseOnWords(modifier.toLowerCase().replace("-", " ")));
				List<String> modifierItemLore = new ArrayList<String>();
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "This is an ability modifier. Changing this");
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "value will slightly customize the ability.");
				modifierItemLore.add("");
				modifierItemLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GOLD + modifierFormat.format(section.contains(modifier) ? section.getDouble(modifier) : ability.getDefaultValue(modifier)));
				modifierItemLore.add(ChatColor.GRAY + "Default Value: " + ChatColor.GOLD + modifierFormat.format(ability.getDefaultValue(modifier)));
				modifierItemLore.add("");
				modifierItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Click to change this value.");
				modifierItemLore.add(ChatColor.YELLOW + SpecialChar.listDash + " Right click to reset.");
				modifierItemMeta.setLore(modifierItemLore);
				modifierItem.setItemMeta(modifierItemMeta);

				modifierItem = MMOItems.plugin.getNMS().addTag(modifierItem, new ItemTag("abilityModifier", modifier));

				inv.setItem(slots[n++], modifierItem);
			}
		}

		ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- No Modifier -");
		glass.setItemMeta(glassMeta);

		ItemStack back = new ItemStack(Material.BARRIER);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(ChatColor.GREEN + SpecialChar.rightArrow + " Ability List");
		back.setItemMeta(backMeta);

		while (n < slots.length)
			inv.setItem(slots[n++], glass);

		addEditionInventoryItems(inv, false);
		inv.setItem(28, abilityItem);
		inv.setItem(6, back);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isPluginItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + SpecialChar.rightArrow + " Ability List")) {
			new AbilityListEdition(player, type, id).open();
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Ability")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, Stat.ABILITIES, configKey, "ability").enable("Write in the chat the ability you want.", "You can access the ability list by typing " + ChatColor.AQUA + "/mi list ability");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				FileConfiguration config = type.getConfigFile();
				if (config.getConfigurationSection(id).contains("ability") && config.getConfigurationSection(id + ".ability").contains(configKey) && config.getConfigurationSection(id + ".ability." + configKey).contains("type")) {
					config.set(id + ".ability." + configKey, null);

					config.set(id + ".ability." + configKey, null);
					if (config.getConfigurationSection(id).contains("ability"))
						if (config.getConfigurationSection(id + ".ability").getKeys(false).size() < 1)
							config.set(id + ".ability", null);

					type.saveConfigFile(config, id);
					open();
					player.sendMessage(MMOItems.getPrefix() + "Successfully reset the ability.");
				}
			}
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Casting Mode")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				new StatEdition(this, Stat.ABILITIES, configKey, "mode").enable("Write in the chat the casting mode you want.");

				player.sendMessage("");
				player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Available Casting Modes");
				for (CastingMode castMode : ability.getSupportedCastingModes())
					player.sendMessage("* " + ChatColor.GREEN + castMode.name());
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				FileConfiguration config = type.getConfigFile();
				if (config.getConfigurationSection(id).contains("ability") && config.getConfigurationSection(id + ".ability").contains(configKey) && config.getConfigurationSection(id + ".ability." + configKey).contains("type")) {
					config.set(id + ".ability." + configKey, null);

					config.set(id + ".ability." + configKey, null);
					if (config.getConfigurationSection(id).contains("ability"))
						if (config.getConfigurationSection(id + ".ability").getKeys(false).size() < 1)
							config.set(id + ".ability", null);

					type.saveConfigFile(config, id);
					open();
					player.sendMessage(MMOItems.getPrefix() + "Successfully reset the ability.");
				}
			}
			return;
		}

		String tag = MMOItems.plugin.getNMS().getStringTag(item, "abilityModifier");
		if (tag.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(this, Stat.ABILITIES, configKey, tag).enable("Write in the chat the value you want.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			FileConfiguration config = type.getConfigFile();
			if (config.getConfigurationSection(id).contains("ability") && config.getConfigurationSection(id + ".ability").contains(configKey) && config.getConfigurationSection(id + ".ability." + configKey).contains(tag)) {
				config.set(id + ".ability." + configKey + "." + tag, null);
				type.saveConfigFile(config, id);
				open();
				player.sendMessage(MMOItems.getPrefix() + "Successfully reset " + ChatColor.GOLD + MMOUtils.caseOnWords(tag.replace("-", " ")) + ChatColor.GRAY + ".");
			}
		}
	}
}