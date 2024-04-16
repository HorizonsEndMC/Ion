package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.chunkKeyX
import net.horizonsend.ion.server.miscellaneous.utils.chunkKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.Chunk
import org.bukkit.World
import org.litote.kmongo.and
import org.litote.kmongo.lte
import org.litote.kmongo.ne
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object UnusedSoldShipPurge : IonServerComponent() {
	override fun onEnable() {
		Tasks.asyncAtHour(8, ::purgeNoobShuttles)
	}

	// Inactive for 7 days
	private val clearBeforeData get() = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

	fun purgeNoobShuttles() = Tasks.async {
		val unused = PlayerStarshipData.find(and(
			PlayerStarshipData::shipDealerInformation ne null, // Sold by ship dealer
			PlayerStarshipData::lastUsed lte clearBeforeData // Last used more than 7 days ago
		))

		val tasks = mutableListOf<CompletableFuture<Boolean>>()

		for (data in unused) {
			val dealerInfo = data.shipDealerInformation!!

			// If they've moved it, don't clear it
			if (dealerInfo.creationBlockKey != data.blockKey) continue

			tasks += clearShip(data)
		}

		CompletableFuture.allOf(*tasks.toTypedArray()).thenAccept {
			val results = tasks.map { it.get() }

			val successes = results.count { it }
			val failures = results.count { !it }

			log.info("Finished clearing sold ships! There were $successes successes, and $failures failures.")
		}
	}

	private fun clearShip(data: PlayerStarshipData): CompletableFuture<Boolean> {
		val world = data.bukkitWorld()

		val toLoad = getLoadTasks(world, data)

		val deferred = CompletableFuture<Boolean>()

		CompletableFuture.allOf(*toLoad.toTypedArray()).thenAccept {
			deferred.complete(deleteShip(data))
		}

		return deferred
	}

	private fun deleteShip(data: PlayerStarshipData): Boolean = try {
		val state = StarshipDetection.detectNewState(data, detector = debugAudience, loadChunks = true)

		DeactivatedPlayerStarships.updateState(data, state)

		Tasks.sync {
			PilotedStarships.activateWithoutPilot(
				debugAudience,
				data,
				createController = {
					GarbageCollectorController(it)
				}
			)
		}

		true
	} catch (e: StarshipDetection.DetectionFailedException) {
		log.warn("Could not delete abandoned sold ship! $data")
		false
	} catch (e: AISpawner.SpawningException) {
		log.warn("Could not delete abandoned sold ship! $data")
		false
	}

	private fun getLoadTasks(world: World, data: PlayerStarshipData): MutableSet<CompletableFuture<Chunk>> {
		val toLoad = mutableSetOf<CompletableFuture<Chunk>>()

		// Undetected ships
		val chunks = data.containedChunks ?: return mutableSetOf()

		for (chunkKey in chunks) {
			val chunkFuture = world.getChunkAtAsync(chunkKeyX(chunkKey), chunkKeyZ(chunkKey))
			toLoad.add(chunkFuture)
		}

		return toLoad
	}

	private class GarbageCollectorController(starship: ActiveStarship): NoOpController(starship, null) {
		override fun tick() {
			StarshipDestruction.vanish(starship)
		}
	}
}
