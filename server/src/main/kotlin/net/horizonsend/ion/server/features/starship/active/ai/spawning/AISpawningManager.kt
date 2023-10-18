package net.horizonsend.ion.server.features.starship.active.ai.spawning

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.common.utils.text.isVowel
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ai.AIManager
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import java.util.Optional

object AISpawningManager : IonServerComponent(true) {
	val config = IonServer.aiShipConfiguration
	val spawners = listOf<AISpawner>(
		BasicCargoMissionSpawner()
	)

	val templates: MutableMap<String, AIStarshipTemplate> = mutableMapOf()

	val schematicCache: LoadingCache<String, Optional<Clipboard>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { identifier ->
				val template = templates[identifier] ?: return@from Optional.empty<Clipboard>()
				val clipboard = readSchematic(template.schematicFile) ?: return@from Optional.empty<Clipboard>()
				return@from Optional.of(clipboard)
			}
		)

	operator fun get(identifier: String) = spawners.firstOrNull { it.identifier == identifier }


	override fun onEnable() {
		Tasks.asyncRepeat(config.spawnRate, config.spawnRate) { handleSpawn() }
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	fun handleSpawn() {
		val worldSettings = config.spawnerWeightedRandomList().randomOrNull()?.identifier ?: return
		val spawner = AISpawningManager[worldSettings] ?: return

		val loc = spawner.findLocation()

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

				if (IonServer.configuration.serverName == "Survival")
					Notify.online(spawnMessage)
				else
					IonServer.server.sendMessage(spawnMessage)
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
