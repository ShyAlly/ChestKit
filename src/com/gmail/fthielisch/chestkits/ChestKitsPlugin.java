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
	private ChestKitsCooldownManager cooldownManager = new ChestKitsCooldownManager();
	
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
			String kitName = args[0];

			ChestKitsKit kit = config.getKit(kitName);
			if (kit == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kitName + "' does not exist.");
				return true;
			}

			if (!sender.hasPermission("ckit.get." + kitName) && !sender.hasPermission("ckit.get.*")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that kit.");
				return true;
			}
			
			// 
			long cooldownPeriod;
			if (fromConsole) {
				cooldownPeriod = 0;
			} else {
				cooldownPeriod = cooldownManager.getCooldownPeriod(toGive, kit);
			}
			
			if (cooldownPeriod > 0) {
				sender.sendMessage(ChatColor.RED + "This kit is currently on cooldown! Please wait another " + (cooldownPeriod / 1000) + " seconds.");
				return true;
			}

			ItemStack is = new ItemStack(Material.CHEST, 1);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName("Kit " + kitName);
			meta.setLore(Arrays.asList(LORE_KEY));
			is.setItemMeta(meta);

			if (!toGive.getInventory().addItem(is).isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Your inventory is too full to hold the kit.");
			} else if (fromConsole) {
				toGive.sendMessage(ChatColor.GREEN + "You have been given a " + kitName + " kit!");
			}
			return true;
		}

		if (args[0].equals("create")) {
			if (!sender.hasPermission("ckit.create")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to create new kits.");
				return true;
			}

			String kitName = args[1];

			Player p = toGive;

			List<ItemStack> items = new LinkedList<ItemStack>();
			for (ItemStack is : p.getInventory()) {
				if (is == null) {
					continue;
				}
				items.add(new ItemStack(is));
			}
			
			ChestKitsKit kit = new ChestKitsKit(kitName, items);

			config.addKit(kitName, kit);

			sender.sendMessage("Kit '" + kitName + "' created. You may want to save the configuration.");
		} else if (args[0].equals("cooldown")) {
			if (!sender.hasPermission("ckit.cooldown")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to give kits cooldowns.");
				return true;
			}
			if (args.length < 3) {
				sender.sendMessage(ChatColor.RED + "You must specify how long to set the cooldown to.");
				sender.sendMessage(ChatColor.RED + "You may set the cooldown to 0 to remove it.");
				return true;
			}

			String kitName = args[1];
			ChestKitsKit kit = config.getKit(kitName);
			if (kit == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kitName + "' does not exist.");
				return true;
			}
			
			long cooldown;
			try {
				cooldown = Long.parseLong(args[2]);
			} catch (Exception e) {
				sender.sendMessage("Invalid number provided for cooldown.");
				return true;
			}
			
			kit.setCooldown(cooldown);

			sender.sendMessage("Kit '" + kitName + "' removed. You may want to save the configuration.");
		} else if (args[0].equals("delete")) {
			if (!sender.hasPermission("ckit.delete")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to delete kits.");
				return true;
			}

			String kitName = args[1];
			ChestKitsKit kit = config.getKit(kitName);
			if (kit == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kitName + "' does not exist.");
				return true;
			}

			config.removeKit(kitName);

			sender.sendMessage("Kit '" + kitName + "' removed. You may want to save the configuration.");
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
			
			String kitName = args[2];

			ChestKitsKit kit = config.getKit(kitName);
			if (kit == null) {
				sender.sendMessage(ChatColor.RED + "The kit '" + kitName + "' does not exist.");
				return true;
			}

			if (!sender.hasPermission("ckit.get." + kitName) && !sender.hasPermission("ckit.get.*")) {
				sender.sendMessage(ChatColor.RED + "You do not have access to that kit.");
				return true;
			}

			ItemStack is = new ItemStack(Material.CHEST, 1);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName("Kit " + kitName);
			meta.setLore(Arrays.asList(LORE_KEY));
			is.setItemMeta(meta);

			if (!toGive.getInventory().addItem(is).isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Your inventory is too full to hold the kit.");
			} else {
				toGive.sendMessage(ChatColor.GREEN + "You have been given a " + kitName + " kit!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Invalid secondary command. Valid secondary commands: create, delete, save config, give");
			return true;
		}

		return true;
	}

	public void addItemsToChest(Block b1, String kitName) {
		ChestKitsKit kit = config.getKit(kitName);
		if (kit == null) {
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

		Iterator<ItemStack> itms = kit.getItems().iterator();
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
