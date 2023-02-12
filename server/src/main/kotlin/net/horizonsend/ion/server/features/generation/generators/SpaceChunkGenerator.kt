package net.horizonsend.ion.server.features.generation.generators

import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

class SpaceChunkGenerator : ChunkGenerator() {
	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
	}

	override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> {
		return mutableListOf()
	}

	override fun shouldGenerateSurface(): Boolean {
		return false
	}

	override fun shouldGenerateCaves(): Boolean {
		return true
	}

	override fun shouldGenerateMobs(): Boolean {
		return false
	}

	override fun shouldGenerateDecorations(): Boolean {
		return false
	}

	override fun shouldGenerateStructures(): Boolean {
		return false
	}

	override fun shouldGenerateNoise(): Boolean {
		return false
	}
}
