package net.horizonsend.ion.server.features.starship.active.ai

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.common.utils.text.isVowel
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AggressivenessLevel
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.World

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

		val spawnerIdentifier = configuration.spawnerWeightedRandomList().random()
		val spawner = AISpawningManager[spawnerIdentifier] ?: return

		val loc = spawner.findLocation(world, configuration)

		if (loc == null) {
			log.warn("Aborted spawning AI ship. Could not find location after 15 attempts.")
			return
		}
		val deferred = spawner.spawn(loc)

		deferred.invokeOnCompletion { throwable ->
			throwable?.let {
				IonServer.server.debug("AI Starship at could not be spawned: ${throwable.message}!")
				return@invokeOnCompletion
			}

			val ship = deferred.getCompleted()

			// Wait 1 tick for the controller to update
			Tasks.sync {
				val controller = ship.controller as AIController

				AIManager.activeShips.add(ship)

				val spawnMessage = createSpawnMessage(
					controller.aggressivenessLevel,
					ship.getDisplayNameComponent(),
					Vec3i(loc),
					loc.world
				)

				Notify.online(spawnMessage)
			}
		}
	}

	private fun createSpawnMessage(
		aggressivenessLevel: AggressivenessLevel,
		shipName: Component,
		location: Vec3i,
		world: World
	): Component {
		val aAn = if (aggressivenessLevel.name[0].isVowel()) "An " else "A "

		val (x, y, z) = location

		return text()
			.color(NamedTextColor.GRAY)
			.append(text(aAn))
			.append(aggressivenessLevel.displayName)
			.append(text(" "))
			.append(shipName)
			.append(text(" has spawned in "))
			.append(text(world.name, NamedTextColor.WHITE))
			.append(text(" at "))
			.append(text(x, NamedTextColor.WHITE))
			.append(text(", "))
			.append(text(y, NamedTextColor.WHITE))
			.append(text(", "))
			.append(text(z, NamedTextColor.WHITE))
			.build()
	}
}
