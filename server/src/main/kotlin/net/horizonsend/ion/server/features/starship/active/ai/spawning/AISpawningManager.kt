package net.horizonsend.ion.server.features.starship.active.ai.spawning

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.active.ai.spawning.test.BasicCargoMissionSpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import java.util.Optional
import java.util.concurrent.TimeUnit

object AISpawningManager : IonServerComponent(true) {
	val context = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

		Tasks.syncRepeat(60L, 60L, ::despawnAIShips)
	}

	override fun onDisable() {
		for (starship in ActiveStarships.all()) {
			if (starship.controller !is AIController) continue

			StarshipDestruction.vanish(starship)
		}
	}

	private fun enableSpawners() {
		val averageDelay = (spawners.sumOf { it.configuration.spawnRate } / spawners.size) / spawners.size

		spawners.forEachIndexed { index: Int, spawner: AISpawner ->
			val delay = averageDelay * index

			Tasks.asyncRepeat(delay, spawner.configuration.spawnRate) { spawner.trigger(context) }
		}
	}

	// The AI ship must be at least 30 minutes old
	val timeLivedRequirement get() = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)
	// And not damaged within the last 15 minutes
	val lastDamagedRequirement get() = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(7)

	fun despawnAIShips() {
		for (starship in ActiveStarships.all()) {
			starship as ActiveControlledStarship

			if (meetsDespawnCriteria(starship)) {
				despawn(starship)
			}
		}
	}

	private fun meetsDespawnCriteria(starship: ActiveControlledStarship): Boolean {
		val controller = starship.controller

		if (controller !is AIController) return false

		val mostRecentDamager = starship.damagers.entries.sortedBy { it.value.lastDamaged }.firstOrNull()

		if (mostRecentDamager != null && mostRecentDamager.value.lastDamaged > lastDamagedRequirement) return false

		return starship.creationTime <= timeLivedRequirement
	}

	private fun despawn(starship: ActiveControlledStarship) {
		StarshipDestruction.vanish(starship)
	}
}
