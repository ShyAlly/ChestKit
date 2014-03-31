package com.gmail.fthielisch.chestkits;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestKitsListener implements Listener {
	
	private ChestKitsPlugin plugin;

	public ChestKitsListener(ChestKitsPlugin chestKitsPlugin) {
		this.plugin = chestKitsPlugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {		
		ItemStack placed = e.getItemInHand();
		if (placed.getType() == Material.CHEST) {
			ItemMeta meta = placed.getItemMeta();
			if (meta.hasDisplayName() && meta.hasLore() && meta.getDisplayName().startsWith("Kit ")) {
				if (meta.getLore().contains(ChestKitsPlugin.LORE_KEY)) {
					// A special type of chest
					plugin.addItemsToChest(e.getBlockPlaced(), placed.getItemMeta().getDisplayName().substring(4));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		// Only looking for anvils
		if (!(e.getInventory() instanceof AnvilInventory)) {
			return;
		}
		
		ItemStack item = e.getCurrentItem();
		
		// Only looking for kits
		if (!item.hasItemMeta()) {
			return;
		}
		
		ItemMeta meta = item.getItemMeta();
				
		if (meta.hasDisplayName() && meta.hasLore() && meta.getDisplayName().startsWith("Kit ")) {
			if (meta.getLore().contains(ChestKitsPlugin.LORE_KEY)) {
				e.setCancelled(true);
			}
		}
	}
}
