package net.horizonsend.ion.server.features.world.generation.generators.interfaces

import org.bukkit.Chunk

interface WorldGenerator {
	suspend fun generateChunk(chunk: Chunk)
}
