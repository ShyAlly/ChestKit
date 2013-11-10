package com.gmail.fthielisch.chestkits;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class ChestKitsKit {
	
	private final String name;
	private List<ItemStack> items;
	private long cooldownTime;
	
	public ChestKitsKit(String name, List<ItemStack> items) {
		this.name = name;
		this.items = items;
		this.cooldownTime = 0;
	}
	
	public String getName() {
		return name;
	}

	public void setCooldown(long cooldown) {
		this.cooldownTime = cooldown;
	}

	public List<ItemStack> getItems() {
		return items;
	}
	
	public long getCooldown() {
		return cooldownTime;
	}
}
