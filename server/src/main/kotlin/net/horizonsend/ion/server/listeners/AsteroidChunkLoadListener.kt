package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.NamespacedKeys
import net.horizonsend.ion.server.generation.Asteroid
import net.horizonsend.ion.server.generation.Asteroids
import net.horizonsend.ion.server.generation.AsteroidsDataType
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.generateAsteroid
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.parseDensity
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.postGenerateAsteroid
import net.horizonsend.ion.server.generation.generators.OreGenerator.generateOres
import net.starlegacy.feature.space.SpaceWorlds
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.util.Random
import kotlin.math.ceil

class AsteroidChunkLoadListener : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (!SpaceWorlds.contains(event.world)) return

		val container = event.chunk.persistentDataContainer

		if (container.get(NamespacedKeys.ASTEROIDS_CHECK, PersistentDataType.INTEGER) != null) return

		container.set(NamespacedKeys.ASTEROIDS_CHECK, PersistentDataType.INTEGER, 1)

		val worldX = event.chunk.x * 16
		val worldZ = event.chunk.z * 16

		val chunkDensity = parseDensity(event.world, worldX.toDouble(), event.world.maxHeight / 2.0, worldZ.toDouble())

		val asteroids = mutableListOf<Asteroid>()

		// Generate a number of random asteroids in a chunk, proportional to the density in a portion of the chunk. Allows densities of X>1 asteroid per chunk.
		for (count in 0..ceil(chunkDensity).toInt()) {
			val asteroidRandom = Random(System.currentTimeMillis() + event.world.seed)

			// Random coordinate generation.
			val asteroidX = asteroidRandom.nextInt(0, 15) + worldX
			val asteroidZ = asteroidRandom.nextInt(0, 15) + worldZ
			val asteroidY = asteroidRandom.nextInt(event.world.minHeight + 10, event.world.maxHeight - 10)

			// random number out of 100, chance of asteroid's generation. For use in selection.
			val chance = asteroidRandom.nextDouble(100.0)

			// Selects some asteroids that are generated. Allows for densities of 0<X<1 asteroids per chunk.
			if (chance > (chunkDensity * 10)) continue

			val asteroid = generateAsteroid(asteroidX, asteroidY, asteroidZ, asteroidRandom)

			if (asteroid.size + asteroidY > event.world.maxHeight) continue

			if (asteroidY - asteroid.size < event.world.minHeight) continue

			asteroids += asteroid

			postGenerateAsteroid(event.world, event.chunk, asteroid)
		}

		container.set(NamespacedKeys.ASTEROIDS, AsteroidsDataType(), Asteroids(asteroids))
		// Asteroids End

		// Ores begin
		generateOres(event.world, event.chunk)
	}
}
