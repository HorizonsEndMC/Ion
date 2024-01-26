package net.horizonsend.ion.server.features.world.generation.generators.bukkit

import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

object EmptyChunkGenerator : ChunkGenerator() {
	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {}
	override fun getDefaultPopulators(world: World) = mutableListOf<BlockPopulator>()
	override fun shouldGenerateSurface() = false
	override fun shouldGenerateCaves() = true
	override fun shouldGenerateMobs() = false
	override fun shouldGenerateDecorations() = false
	override fun shouldGenerateStructures() = false
	override fun shouldGenerateNoise() = false
}
