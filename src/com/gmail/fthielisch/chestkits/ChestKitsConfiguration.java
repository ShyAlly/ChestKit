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

	private HashMap<String, List<ItemStack>> kits;

	public ChestKitsConfiguration(Configuration config, Logger log) {
		kits = new HashMap<String, List<ItemStack>>();

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
				
				addKit(kitName, itemStacks);
			}
		}
		
		log.info("Successfully loaded " + kits.size() + " kits.");
	}
	
	public List<ItemStack> getKitContents(String kit) {
		return kits.get(kit);
	}

	public void saveConfig(ChestKitsPlugin plugin) {
		List<String> kitNames = new LinkedList<String>();
		for (String s : kits.keySet()) {
			kitNames.add(s);
		}
		
		FileConfiguration fc = plugin.getConfig();
		fc.set("kits", kitNames);
		
		for (Entry<String, List<ItemStack>> entry : kits.entrySet()) {
			fc.set("kititems." + entry.getKey(), entry.getValue());
		}
		
		plugin.saveConfig();
	}

	public void removeKit(String kit) {
		kits.remove(kit);
	}
	
	public void addKit(String kit, List<ItemStack> items) {
		kits.put(kit, items);
	}
}
