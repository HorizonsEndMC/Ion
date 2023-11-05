package net.horizonsend.ion.server.features.enviornment.mobs

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.world.WorldInitEvent

object CustomMobSpawning : IonServerComponent(true) {
	private val spawners: MutableMap<World, CustomMobSpawner> = mutableMapOf()

	@EventHandler
	fun onWorldInitialize(event: WorldInitEvent) {
		val config = IonServer.configuration.mobSpawns[event.world.name] ?: return

		spawners[event.world] = CustomMobSpawner(event.world, config.weightedList())
	}

	private fun addWorldMobs(world: World, mobs: List<ServerConfiguration.PlanetSpawnConfig.Mob>) {
		val map = mobs.associateWith { mob -> mob.weight }

		// handle existing spawner
		spawners[world]?.let {
			it.mobs.addMany(map)
			return
		}

		spawners[world] = CustomMobSpawner(world, WeightedRandomList(map))
	}

	override fun onEnable() {
		val allMobs = IonServer.configuration.mobSpawns["all"] ?: return

		IonServer.server.worlds.forEach {
			world -> addWorldMobs(world, allMobs.mobs)
			val spawner = spawners[world]!!
			log.info("Enabled spawner for ${world.name} with mobs ${spawner.mobs}")
		}
	}

	@EventHandler
	fun onMobSpawn(event: CreatureSpawnEvent) {
		spawners[event.location.world]?.handleSpawnEvent(event)
	}
}
