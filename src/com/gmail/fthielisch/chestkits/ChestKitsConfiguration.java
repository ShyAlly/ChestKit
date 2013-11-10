package com.gmail.fthielisch.chestkits;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ChestKitsConfiguration {

	private HashMap<String, ChestKitsKit> kits;

	public ChestKitsConfiguration(Configuration config, Logger log) {
		kits = new HashMap<String, ChestKitsKit>();

		List<String> listOfKits = config.getStringList("kits");
		if (listOfKits != null) {
			for (String kitName : listOfKits) {
				log.info("Loading kit " + kitName);
				List<?> items = config.getList("kititems." + kitName);
				if (items == null) {
					log.severe("Kit " + kitName + " has no items defined! Skipping...");
					continue;
				}
	
				List<ItemStack> itemStacks = new LinkedList<ItemStack>();
				for (Object o : items) {
					if (o instanceof ItemStack) {
						itemStacks.add((ItemStack)o);
					} else {
						log.severe("Invalid object found in configuration for kit " + kitName + ": " + o);
					}
				}
				
				long cooldown = config.getLong("kitcooldown." + kitName, 0L);
				
				ChestKitsKit kit = new ChestKitsKit(kitName, itemStacks);
				kit.setCooldown(cooldown);
				
				addKit(kitName, kit);
			}
		}
		
		log.info("Successfully loaded " + kits.size() + " kits.");
	}
	
	public ChestKitsKit getKit(String kit) {
		return kits.get(kit);
	}

	public void saveConfig(ChestKitsPlugin plugin) {
		List<String> kitNames = new LinkedList<String>();
		for (String s : kits.keySet()) {
			kitNames.add(s);
		}
		
		FileConfiguration fc = plugin.getConfig();
		fc.set("kits", kitNames);
		
		for (Entry<String, ChestKitsKit> entry : kits.entrySet()) {
			ChestKitsKit kit = entry.getValue();
			fc.set("kititems." + entry.getKey(), kit.getItems());
			fc.set("kitcooldown." + entry.getKey(), kit.getCooldown());
		}
		
		plugin.saveConfig();
	}

	public void removeKit(String kit) {
		kits.remove(kit);
	}
	
	public void addKit(String kitName, ChestKitsKit kit) {
		kits.put(kitName, kit);
	}
}
