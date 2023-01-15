package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.NamespacedKeys
import net.horizonsend.ion.server.generation.Asteroid
import net.horizonsend.ion.server.generation.Asteroids
import net.horizonsend.ion.server.generation.AsteroidsDataType
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.configuration
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.generateAsteroid
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.parseDensity
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.postGenerateAsteroid
import net.horizonsend.ion.server.generation.generators.WreckGenerator.generateWreck
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.util.Tasks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.Random
import kotlin.math.ceil

class AsteroidChunkLoadListener : Listener {
	private val dataType = AsteroidsDataType() // Save some class construction
	private val wrecks_folder: File = IonServer.Ion.dataFolder.resolve("asteroids/wrecks")

	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (!SpaceWorlds.contains(event.world)) return
		if (event.chunk.inhabitedTime != 0L) return

		val container = event.chunk.persistentDataContainer

		if (container.get(NamespacedKeys.ASTEROIDS_VERSION, PersistentDataType.INTEGER) != null) return

		container.set(NamespacedKeys.ASTEROIDS_VERSION, PersistentDataType.INTEGER, 1)

		val worldX = event.chunk.x * 16
		val worldZ = event.chunk.z * 16

		val chunkDensity = parseDensity(event.world, worldX.toDouble(), event.world.maxHeight / 2.0, worldZ.toDouble())

		val asteroids = mutableListOf<Asteroid>()

		val random = Random(System.currentTimeMillis() + event.world.seed + event.chunk.hashCode())

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.
		for (count in 0..ceil(chunkDensity).toInt()) {
			// random number out of 100, chance of asteroid's generation. For use in selection.
			val chance = random.nextDouble(100.0)

			// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
			if (chance > (chunkDensity * 10)) continue

			// Random coordinate generation.
			val asteroidX = random.nextInt(0, 15) + worldX
			val asteroidZ = random.nextInt(0, 15) + worldZ
			val asteroidY = random.nextInt(event.world.minHeight + 10, event.world.maxHeight - 10)

			val asteroid = generateAsteroid(asteroidX, asteroidY, asteroidZ, random)

			if (asteroid.size + asteroidY > event.world.maxHeight) continue

			if (asteroidY - asteroid.size < event.world.minHeight) continue

			asteroids += asteroid

			postGenerateAsteroid(event.world, event.chunk, asteroid)
		}

		wrecks_folder.mkdir()
		val wreck = wrecks_folder.listFiles()?.random()

		wreck?.let {
			val chance = random.nextDouble(100.0 * configuration.wreckRatio)

			// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
			if (chance < (chunkDensity * 10)) {
				val x = random.nextInt(0, 15) + worldX
				val z = random.nextInt(0, 15) + worldZ
				val y = random.nextInt(event.world.minHeight + 10, event.world.maxHeight - 10)

				Tasks.async { generateWreck(event.chunk, x, y, z, it) }
			}
		}

		container.set(NamespacedKeys.ASTEROIDS, dataType, Asteroids(asteroids))
	}
}
