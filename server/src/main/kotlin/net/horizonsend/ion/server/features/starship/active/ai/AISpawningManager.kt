package net.horizonsend.ion.server.features.starship.active.ai

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

object AISpawningManager : IonServerComponent(true) {
	val config = IonServer.aiShipConfiguration
	val spawners = listOf<AISpawner>(
		BasicCargoMissionSpawner()
	)

	operator fun get(identifier: String) = spawners.firstOrNull { it.identifier == identifier }


	override fun onEnable() {
		Tasks.asyncRepeat(20 * 15, config.spawnRate) { handleSpawn() }
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun handleSpawn() {
		val world = Bukkit.getWorld(config.worldWeightedList().random()) ?: return
		val configuration = config.getWorld(world) ?: return

		val spawnerIdentifier = configuration.templateWeightedList().random()
		val spawner = AISpawningManager[spawnerIdentifier] ?: return

		val loc = spawner.findLocation(world, configuration)

		if (loc == null) {
			IonServer.logger.info("Aborted spawning AI ship. Could not find location after 15 attempts.")
			return
		}

		val deferred = spawner.spawn(loc)

		deferred.invokeOnCompletion { throwable ->
			val ship = deferred.getCompleted()

			AIManager.activeShips.add(ship)

			throwable?.let { IonServer.server.debug("AI Starship at could not be spawned: ${throwable.message}!") }
			Notify.online(Component.text("Spawning AI ship at ${Vec3i(loc)}"))
		}
	}
}
