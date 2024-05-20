package net.horizonsend.ion.server.features.starship.ai.spawning

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.ai.spawning.alien.AlienSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.ExplorerSingleSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp.MiningCorpSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.PirateSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PrivateerSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.tsaii.TsaiiSpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import java.util.Optional
import java.util.concurrent.TimeUnit

object AISpawningManager : IonServerComponent(true) {
	// The coroutine context in which the heavy spawning work will be handled
	val context = CoroutineScope(Dispatchers.IO + SupervisorJob())

	// General AI configuration
	val config = IonServer.aiSpawningConfiguration

	/**
	 * For variety, the spawners are defined in the code, but they get their ship configuration and spawn rates, etc. from configuration files.
	 **/
	val spawners = mutableListOf<AISpawner>()

	// The templates, matched to their identifiers
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
		registerSpawners()

		Tasks.syncRepeat(0L, 0L, ::tickSpawners)
		Tasks.syncRepeat(60L, 60L, ::despawnAIShips)
	}

	override fun onDisable() {
		for (starship in ActiveStarships.all()) {
			if (starship.controller !is AIController) continue

			StarshipDestruction.vanish(starship)
		}
	}

	/** Register all the spawners after the server has been initialized */
	private fun registerSpawners() {
		// Register spawners
		spawners += ExplorerSingleSpawner()
		spawners += PrivateerSpawner()
		spawners += MiningCorpSpawner()
		spawners += PirateSpawner()
		spawners += TsaiiSpawner()
		spawners += AlienSpawner()
	}

	/** Ticks all the spawners, increasing points and maybe triggering an execution */
	private fun tickSpawners() = spawners.forEach(AISpawner::tickPoints)

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
