package com.gmail.fthielisch.chestkits;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestKitsPlugin extends JavaPlugin {

	public static final String LORE_KEY = "Contains a bunch of items!";

	private ChestKitsConfiguration config = null;

	@Override
	public void onEnable() {
		config = new ChestKitsConfiguration(this.getConfig(), this.getLogger());

		this.getServer().getPluginManager().registerEvents(new ChestKitsListener(this), this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player toGive;
		boolean fromConsole = false;
		if (!(sender instanceof Player)) {
			if (args.length == 0) {
				sender.sendMessage("You must specify a target player.");
				sender.sendMessage("Usage: /ckit (PLAYER) (command)");
				return true;
			}
			toGive = sender.getServer().getPlayer(args[0]);
			if (toGive == null || !toGive.isOnline()) {
				sender.sendMessage("The player " + args[0] + " is not online!");
				return true;
			}
			String[] args2 = new String[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				args2[i - 1] = args[i];
			}
			args = args2;
			fromConsole = true;
		} else {
			toGive = (Player) sender;
		}

		if (args.length < 1) {
			sender.sendMessage("Usage: /ckit {kitname}");
			if (sender.hasPermission("ckit.create")) {
				sender.sendMessage("Usage: /ckit create {kitName}");
			}
			if (sender.hasPermission("ckit.delete")) {
				sender.sendMessage("Usage: /ckit delete {kitName}");
			}
			if (sender.hasPermission("ckit.save")) {
				sender.sendMessage("Usage: /ckit save config");
			}
			if (sender.hasPermission("ckit.give")) {
				sender.sendMessage("Usage: /ckit give {playerName} {kitName}");
			}

			return true;
		}

		if (args.length == 1) {
			String kit = args[0];

			List<ItemStack> items = config.getKitContents(kit);
			if (items == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kit + "' does not exist.");
				return true;
			}

			if (!sender.hasPermission("ckit.get." + kit) && !sender.hasPermission("ckit.get.*")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that kit.");
				return true;
			}

			ItemStack is = new ItemStack(Material.CHEST, 1);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName("Kit " + kit);
			meta.setLore(Arrays.asList(LORE_KEY));
			is.setItemMeta(meta);

			if (!toGive.getInventory().addItem(is).isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Your inventory is too full to hold the kit.");
			} else if (fromConsole) {
				toGive.sendMessage(ChatColor.GREEN + "You have been given a " + kit + " kit!");
			}
		}

		if (args[0].equals("create")) {
			if (!sender.hasPermission("ckit.create")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to create new kits.");
				return true;
			}

			String kit = args[1];

			Player p = toGive;

			List<ItemStack> items = new LinkedList<ItemStack>();
			for (ItemStack is : p.getInventory()) {
				if (is == null) {
					continue;
				}
				items.add(new ItemStack(is));
			}

			config.addKit(kit, items);

			sender.sendMessage("Kit '" + kit + "' created. You may want to save the configuration.");
		} else if (args[0].equals("delete")) {
			if (!sender.hasPermission("ckit.delete")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to delete kits.");
				return true;
			}

			String kit = args[1];
			List<ItemStack> items = config.getKitContents(kit);
			if (items == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kit + "' does not exist.");
				return true;
			}

			config.removeKit(kit);

			sender.sendMessage("Kit '" + kit + "' removed. You may want to save the configuration.");
		} else if (args[0].equals("save")) {
			if (!sender.hasPermission("ckit.save")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to this command.");
				return true;
			}

			config.saveConfig(this);

			sender.sendMessage("Kits saved to file.");
		} else if (args[0].equals("give")) {
			
			if (!sender.hasPermission("ckit.give")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to this command.");
				return true;				
			}
			
			toGive = sender.getServer().getPlayer(args[1]);
			if (toGive == null || !toGive.isOnline()) {
				sender.sendMessage("The player " + args[0] + " is not online!");
				return true;				
			}
			
			if (args.length < 3) {
				sender.sendMessage(ChatColor.RED + "You must specify which kit to give the player!");
				return true;
			}
			
			String kit = args[2];

			List<ItemStack> items = config.getKitContents(kit);
			if (items == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kit + "' does not exist.");
				return true;
			}

			if (!sender.hasPermission("ckit.get." + kit) && !sender.hasPermission("ckit.get.*")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that kit.");
				return true;
			}

			ItemStack is = new ItemStack(Material.CHEST, 1);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName("Kit " + kit);
			meta.setLore(Arrays.asList(LORE_KEY));
			is.setItemMeta(meta);

			if (!toGive.getInventory().addItem(is).isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Your inventory is too full to hold the kit.");
			} else {
				toGive.sendMessage(ChatColor.GREEN + "You have been given a " + kit + " kit!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid secondary command. Valid secondary commands: create, delete, save config, give");
			return true;
		}

		return true;
	}

	public void addItemsToChest(Block b1, String kitName) {
		List<ItemStack> items = config.getKitContents(kitName);
		if (items == null) {
			getLogger().severe("Unknown kit was placed: " + kitName);
			return;
		}

		BlockState bs1 = b1.getState();

		if (!(bs1 instanceof Chest)) {
			getLogger().severe("Chest conversion failed? (" + bs1 + ")");
			return;
		}

		Inventory i = ((Chest) bs1).getInventory();

		int maxSlot = i.getSize();
		int slot = 0;
		boolean updated = false;

		Iterator<ItemStack> itms = items.iterator();
		while (itms.hasNext()) {
			ItemStack itm = new ItemStack(itms.next());

			if (slot >= maxSlot) {
				getLogger().severe("Kit " + kitName + " contains too many items! Removing excess...");
				itms.remove();
				updated = true;
				continue;
			}

			i.setItem(slot, itm);

			slot++;
		}

		if (updated) {
			config.saveConfig(this);
		}
	}
}
