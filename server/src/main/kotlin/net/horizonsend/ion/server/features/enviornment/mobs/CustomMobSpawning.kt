package net.horizonsend.ion.server.features.enviornment.mobs

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.WorldInitEvent

object CustomMobSpawning : IonServerComponent() {
	private val spawners: MutableMap<World, CustomMobSpawner> = mutableMapOf()

	@EventHandler
	fun onWorldInitialize(event: WorldInitEvent) {
		val config = IonServer.configuration.mobSpawns[event.world.name] ?: return

		spawners[event.world] = CustomMobSpawner(event.world, config.weightedList())
	}

	@EventHandler
	fun onMobSpawn(event: CreatureSpawnEvent) {
		spawners[event.location.world]?.handleSpawnEvent(event)
	}
}
