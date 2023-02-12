package net.horizonsend.ion.server.features.generation

import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator.asteroidGenerationVersion
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator.generateRandomAsteroid
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator.parseDensity
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator.postGenerateAsteroid
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.starlegacy.feature.space.SpaceWorlds
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.util.Random
import kotlin.math.ceil

class GenerationChunkLoadListener : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkLoadEvent) {
		if (!SpaceWorlds.contains(event.world)) return
		if (event.chunk.persistentDataContainer.has(NamespacedKeys.ASTEROIDS_VERSION)) return

		event.chunk.persistentDataContainer.set(
			NamespacedKeys.ASTEROIDS_VERSION,
			PersistentDataType.BYTE,
			asteroidGenerationVersion
		)

		val serverLevel = (event.world as CraftWorld).handle

		val worldX = event.chunk.x * 16
		val worldZ = event.chunk.z * 16

		val chunkDensity = parseDensity(serverLevel, worldX.toDouble(), event.world.maxHeight / 2.0, worldZ.toDouble())

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

			val asteroid = generateRandomAsteroid(asteroidX, asteroidY, asteroidZ, random)

			if (asteroid.size + asteroidY > event.world.maxHeight) continue

			if (asteroidY - asteroid.size < event.world.minHeight) continue

			postGenerateAsteroid(serverLevel, asteroid)
		}
	}
}
