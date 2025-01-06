package net.horizonsend.ion.server.features.world.generation

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.configuration.GenerationConfiguration
import org.bukkit.Chunk

abstract class IonWorldGenerator<T: GenerationConfiguration>(val world: IonWorld, val configuration: T) {
	val seed = world.world.seed

	abstract suspend fun generateChunk(chunk: Chunk)
}
