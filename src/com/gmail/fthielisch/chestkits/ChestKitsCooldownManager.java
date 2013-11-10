package com.gmail.fthielisch.chestkits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class ChestKitsCooldownManager {

	/**
	 * A mapping of every player to a mapping of every kit to the last point in
	 * time that kit was gotten by the player.
	 */
	private Map<String, Map<String, Long>> cooldownLog;

	public ChestKitsCooldownManager() {
		cooldownLog = new HashMap<String, Map<String, Long>>();
	}
	
	private Map<String, Long> getRecord(String playerName) {
		Map<String, Long> ret = cooldownLog.get(playerName);
		if (ret != null) {
			return ret;
		}
		ret = new HashMap<String, Long>();
		cooldownLog.put(playerName, ret);
		return ret;
	}

	public long getCooldownPeriod(Player p, ChestKitsKit kit) {
		if (kit.getCooldown() <= 0) {
			return 0;
		}
		Map<String, Long> record = getRecord(p.getName());
		
		Long lastAccess = record.get(kit.getName());
		
		if (lastAccess == null) {
			return 0;
		}
		
		// The time that's elapsed since the player last got the kit
		long timeDifference = System.currentTimeMillis() - lastAccess.longValue();
		
		// Return the time until the cooldown expires (if cooldown > time elapsed) or 0 if already expired
		return Math.max(kit.getCooldown() - timeDifference, 0);
	}
	
	public void setAccessTime(Player p, ChestKitsKit kit) {
		Map<String, Long> record = getRecord(p.getName());		
		record.put(kit.getName(), Long.valueOf(System.currentTimeMillis()));
	}
}
