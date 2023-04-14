package net.Indyuce.mmoitems.api.edition;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Stat;
import net.Indyuce.mmoitems.gui.PluginInventory;

public class StatEdition {

	/*
	 * saves the last inventory opened. it saves the item data, and the last
	 * opened page. allows for a much easier access to this data
	 */
	private PluginInventory inv;

	/*
	 * saves the data about the edited data so the plugin can edit the
	 * corresponding stat. some stats have complex chat formats, so the object
	 * array allow to save more complex edition info
	 */
	private Stat stat;
	private Object[] info;

	public StatEdition(PluginInventory inv, Stat stat, Object... info) {
		this.inv = inv;
		this.stat = stat;
		this.info = info;
	}

	public PluginInventory getEditionInventory() {
		return inv;
	}

	public Stat getStat() {
		return stat;
	}

	public Object[] getData() {
		return info;
	}

	public Player getPlayer() {
		return inv.getPlayer();
	}

	public void enable(String... messages) {
		getPlayer().closeInventory();

		getPlayer().sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
		for (String message : messages)
			getPlayer().sendMessage(MMOItems.getPrefix() + message);
		getPlayer().sendMessage(MMOItems.getPrefix() + "Type 'cancel' to abort editing.");

		/*
		 * anvil text input feature. enables players to use an anvil to input
		 * text if they are having conflicts with their chat management plugins.
		 */
		if (MMOItems.plugin.getConfig().getBoolean("anvil-text-input") && MMOItems.plugin.getVersion().isBelowOrEqual(1, 13)) {
			new AnvilGUI().open(this);
			return;
		}

		/*
		 * default chat edition feature
		 */
		new ChatEdition().open(this);
		MMOItems.plugin.getNMS().sendTitle(getPlayer(), ChatColor.GOLD + "" + ChatColor.BOLD + "Item Edition", "See chat.", 10, 40, 10);
	}

	public void output(String output) {
		if (output.equals("cancel"))
			inv.open();
		else
			stat.c().chatListener(inv.getItemType(), inv.getItemID(), getPlayer(), inv.getItemType().getConfigFile(), output, info);
	}

	public interface StatEditionProcess {
		void open(StatEdition edition);

		void close();
	}
}
