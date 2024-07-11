package net.horizonsend.ion.server.features.ai.spawning

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import java.io.File
import java.util.Optional
import java.util.concurrent.TimeUnit

object AISpawningManager : IonServerComponent(true) {
	// The coroutine context in which the heavy spawning work will be handled
	val context = CoroutineScope(Dispatchers.IO + SupervisorJob())

	// General AI configuration
	val config = IonServer.aiSpawningConfiguration

	override fun onEnable() {
		Tasks.syncRepeat(0L, 0L, AISpawningManager::tickSpawners)
		Tasks.syncRepeat(60L, 60L, AISpawningManager::despawnOldAIShips)
	}

	override fun onDisable() {
		for (starship in ActiveStarships.all()) {
			if (starship.controller !is AIController) continue

			StarshipDestruction.vanish(starship)
		}
	}

	// The templates, matched to their identifiers
	val templates: MutableMap<String, AIStarshipTemplate> = mutableMapOf()

	val schematicCache: LoadingCache<File, Optional<Clipboard>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { schematicFile ->
				val clipboard = readSchematic(schematicFile) ?: return@from Optional.empty<Clipboard>()
				return@from Optional.of(clipboard)
			}
		)

	/** Ticks all the spawners, increasing points and maybe triggering an execution */
	private fun tickSpawners() = AISpawners.getAllSpawners().forEach {
		it.tickPoints()
	}

	private fun despawnOldAIShips() {
		for (starship in ActiveStarships.all()) {
			starship as ActiveControlledStarship

			if (meetsDespawnCriteria(starship)) {
				despawn(starship)
			}
		}
	}

	// The AI ship must be at least 30 minutes old
	val timeLivedRequirement get() = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)
	// And not damaged within the last 15 minutes
	val lastDamagedRequirement get() = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(7)

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

	fun allAIStarships(): List<ActiveStarship> = ActiveStarships.all().filter { ship ->
		ship.controller is AIController
	}
}
