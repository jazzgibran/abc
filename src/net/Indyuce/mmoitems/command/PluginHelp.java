package net.Indyuce.mmoitems.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.SpecialChar;

public class PluginHelp {
	private static final int commandsPerPage = 8;

	private CommandSender sender;

	public PluginHelp(CommandSender sender) {
		this.sender = sender;
	}

	public void open(int page) {
		if (page < 1 || (page - 1) * commandsPerPage >= PluginCommand.values().length)
			return;

		for (int j = 0; j < 10; j++)
			sender.sendMessage("");
		sender.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "--------------------" + ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + " MMOItems Help " + ChatColor.DARK_GRAY + "]" + ChatColor.STRIKETHROUGH + "-------------------");

		if (sender instanceof Player) {
			int min = (page - 1) * commandsPerPage;
			int max = page * commandsPerPage;
			int n = 0;

			for (; min + n < max && min + n < PluginCommand.values().length && min + n > -1; n++)
				PluginCommand.values()[min + n].sendAsJson((Player) sender);

			while (n++ < commandsPerPage)
				sender.sendMessage("");

			MMOItems.plugin.getNMS().sendJson((Player) sender, "[{\"text\":\"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.DARK_GRAY + "[\"},{\"text\":\"" + ChatColor.RED + "��\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mi help " + (page - 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Previous Page\"}}},{\"text\":\"" + ChatColor.DARK_GRAY + "]" + ChatColor.STRIKETHROUGH + "---" + ChatColor.DARK_GRAY + "(" + ChatColor.GREEN + page + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + getMaxPage() + ChatColor.DARK_GRAY + ")" + ChatColor.STRIKETHROUGH + "---" + ChatColor.DARK_GRAY + "[\"},{\"text\":\"" + ChatColor.GREEN + "��\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mi help " + (page + 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Next Page\"}}},{\"text\":\"" + ChatColor.DARK_GRAY + "]" + ChatColor.STRIKETHROUGH + "-----------------\"}]");
		} else {
			for (PluginCommand command : PluginCommand.values())
				command.sendAsMessage(sender);
		}
	}

	public static int getMaxPage() {
		return (int) Math.max(0, Math.ceil((double) PluginCommand.values().length / (double) commandsPerPage));
	}

	public enum PluginCommand {
		OPTIONAL("()" + ChatColor.WHITE + " = Optional"),
		REQUIRED("<>" + ChatColor.WHITE + " = Required"),
		MULTIPLE_ARGS("..." + ChatColor.WHITE + " = Multiple Args"),
		HOVER_COMMAND(ChatColor.WHITE + "Hover a command to see its description."),
		TAB_INFO(ChatColor.WHITE + "Press [tab] while typing a command to auto-complete."),
		SPACE(""),

		HELP("mi help <page>", "Shows the help page."),
		GIVE("mi <type> <item> (player) (min-max) (unident-chance)...", "&a/mi <type> <item> (player) (min-max) (unidentification-chance) (drop-chance)\\n&fGives an item to a player.\\nSupports drop chance, unidentification chance & random amounts."),
		BROWSE("mi browse", "Allows you to browse through all the items you created."),
		ABILITY("mi ability <ability> (player) (mod1) (val1) (mod2) (val2)...", "Forces a player to cast an ability."),
		DROP("mi drop <type> <item-id> <world-name> <x> <y> <z>...", "&a/mi drop <type> <item-id> <world-name> <x> <y> <z> <drop-chance> <[min]-[max]> <unidentified-chance>\\n&fDrops an item at the target location."),

		HEAL("mi heal", "Heals you & remove negative potion effects."),
		IDENTIFY("mi identifify", "Identifies the item you are holding."),
		UNIDENTIFY("mi unidentifify", "Unidentifies the item you are holding."),
		INFO("mi info (player)", "Displays information about the specified player."),
		LIST("mi list", "Shows the plugin documentation help page."),
		SUDOCONSOLE("mi sudoconsole <command>", "Forces the console to run a command (for item commands)."),
		RELOAD("mi reload (adv-recipes/drops)", "Reloads a specific/all plugin system(s)."),

		CREATE("mi create <type> <id>", "Creates a new item."),
		LOAD("mi load <type> <id>", "Loads in the plugin the item you are holding."),
		COPY("mi copy <type> <target-id> <new-item-id>", "Duplicates an existing item."),
		DELETE("mi delete <type> <id>", "Removes an item from the plugin."),
		EDIT("mi edit <type> <id>", "Brings up the item edition menu."),
		ITEMLIST("mi itemlist <type>", "Lists all items from a specific item type."),
		ALLITEMS("mi allitems", "Lists items from every type."),

		MIMSG("mimsg <message>", "Use this message if you are having issues with the chat edition feature."),
		ADVANCEDWORKBENCH("advancedworkbench", "Opens the advanced workbench."),
		UPDATEITEM("updateitem", "Updates the item you are holding."),
		UPDATEITEM_ITEM("updateitem <type> <id>", "Enables the item updater for a specific item."),
		GEMSTONES("gemstones", "Lists gem stones from the item you are holding."),

		;

		private String usage, help;

		private PluginCommand(String line) {
			this(line, null);
		}

		private PluginCommand(String usage, String help) {
			this.usage = usage;
			this.help = help == null ? null : ChatColor.translateAlternateColorCodes('&', help);
		}

		private boolean isCommand() {
			return help != null;
		}

		private void sendAsJson(Player player) {
			if (isCommand())
				MMOItems.plugin.getNMS().sendJson(player, "{\"text\":\"" + ChatColor.LIGHT_PURPLE + SpecialChar.listDash + ChatColor.GRAY + " /" + usage + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + help + "\"}}}");
			else
				player.sendMessage(ChatColor.LIGHT_PURPLE + usage);
		}

		private void sendAsMessage(CommandSender sender) {
			if (isCommand())
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "- /" + usage + ChatColor.WHITE + " | " + help);
			else
				sender.sendMessage(ChatColor.LIGHT_PURPLE + usage);
		}
	}
}
