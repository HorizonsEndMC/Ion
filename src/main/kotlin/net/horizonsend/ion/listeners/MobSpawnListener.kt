package net.horizonsend.ion.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.RAID
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.REINFORCEMENTS

internal class MobSpawnListener: Listener {
	@EventHandler
	fun onMobSpawn(event: CreatureSpawnEvent) {
		if (event.spawnReason == NATURAL || event.spawnReason == RAID || event.spawnReason == REINFORCEMENTS)
			event.isCancelled = true
	}
}