package net.horizonsend.ion.server.features.starship.active.ai.spawning

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import java.util.Optional

object AISpawningManager : IonServerComponent(true) {
	val config = IonServer.aiShipConfiguration

	/**
	 * For variety, the spawners are defined in the code, but they get their ship configuration and spawn rates, etc. from configuration files.
	 **/
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
		enableSpawners()
	}

	fun enableSpawners() {
		val averageDelay = (spawners.sumOf { it.config.spawnRate } / spawners.size) / spawners.size

		spawners.forEachIndexed { index: Int, spawner: AISpawner ->
			val delay = averageDelay * index

			Tasks.asyncRepeat(delay, spawner.config.spawnRate) { spawner.handleSpawn() }
		}
	}
}
