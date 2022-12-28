package net.horizonsend.ion.server.generation

import java.util.Random
import net.horizonsend.ion.server.generation.populators.AsteroidPopulator
import net.horizonsend.ion.server.generation.populators.OrePopulator
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo

class SpaceChunkGenerator : ChunkGenerator() {
	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
	}

	override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> {
		return mutableListOf(AsteroidPopulator(), OrePopulator())
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