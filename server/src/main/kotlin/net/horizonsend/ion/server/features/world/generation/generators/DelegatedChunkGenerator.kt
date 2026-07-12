package net.horizonsend.ion.server.features.world.generation.generators

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.bukkit.EmptyChunkGenerator
import org.bukkit.Bukkit
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

class DelegatedChunkGenerator(val worldName: String) : ChunkGenerator() {
	private var cachedGenerator: ChunkGenerator? = null

	private fun getIonGenerator(): ChunkGenerator {
		cachedGenerator?.let { return it }

		val world = Bukkit.getWorld(worldName) ?: return EmptyChunkGenerator
		val ionWorld = IonWorld.getIfLoaded(world) ?: return EmptyChunkGenerator
		return ionWorld.terrainGenerator ?: return EmptyChunkGenerator
	}

	override fun generateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		getIonGenerator().generateNoise(worldInfo, random, chunkX, chunkZ, chunkData)
	}

	override fun generateSurface(
		worldInfo: WorldInfo,
		random: Random,
		chunkX: Int,
		chunkZ: Int,
		chunkData: ChunkData
	) {
		getIonGenerator().generateSurface(worldInfo, random, chunkX, chunkZ, chunkData)
	}

	override fun generateBedrock(
		worldInfo: WorldInfo,
		random: Random,
		chunkX: Int,
		chunkZ: Int,
		chunkData: ChunkData
	) {
		getIonGenerator().generateBedrock(worldInfo, random, chunkX, chunkZ, chunkData)
	}

	override fun generateCaves(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		getIonGenerator().generateCaves(worldInfo, random, chunkX, chunkZ, chunkData)
	}

	override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider? {
		return getIonGenerator().getDefaultBiomeProvider(worldInfo)
	}

	override fun getBaseHeight(worldInfo: WorldInfo, random: Random, x: Int, z: Int, heightMap: HeightMap): Int {
		return getIonGenerator().getBaseHeight(worldInfo, random, x, z, heightMap)
	}

	override fun generateChunkData(
		world: World,
		random: Random,
		x: Int,
		z: Int,
		biome: BiomeGrid
	): ChunkData {
		return getIonGenerator().generateChunkData(world, random, x, z, biome)
	}

	override fun canSpawn(world: World, x: Int, z: Int): Boolean {
		return getIonGenerator().canSpawn(world, x, z)
	}

	override fun getDefaultPopulators(world: World): List<BlockPopulator?> {
		return getIonGenerator().getDefaultPopulators(world)
	}

	override fun getFixedSpawnLocation(world: World, random: Random): Location? {
		return getIonGenerator().getFixedSpawnLocation(world, random)
	}

	override fun isParallelCapable(): Boolean {
		return getIonGenerator().isParallelCapable()
	}

	override fun shouldGenerateNoise(): Boolean {
		return getIonGenerator().shouldGenerateNoise()
	}

	override fun shouldGenerateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
		return getIonGenerator().shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)
	}

	override fun shouldGenerateSurface(): Boolean {
		return getIonGenerator().shouldGenerateSurface()
	}

	override fun shouldGenerateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
		return getIonGenerator().shouldGenerateSurface(worldInfo, random, chunkX, chunkZ)
	}

	override fun shouldGenerateBedrock(): Boolean {
		return getIonGenerator().shouldGenerateBedrock()
	}

	override fun shouldGenerateCaves(): Boolean {
		return getIonGenerator().shouldGenerateCaves()
	}

	override fun shouldGenerateCaves(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
		return getIonGenerator().shouldGenerateCaves(worldInfo, random, chunkX, chunkZ)
	}

	override fun shouldGenerateDecorations(): Boolean {
		return getIonGenerator().shouldGenerateDecorations()
	}

	override fun shouldGenerateDecorations(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
		return getIonGenerator().shouldGenerateDecorations(worldInfo, random, chunkX, chunkZ)
	}

	override fun shouldGenerateMobs(): Boolean {
		return getIonGenerator().shouldGenerateMobs()
	}

	override fun shouldGenerateMobs(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
		return getIonGenerator().shouldGenerateMobs(worldInfo, random, chunkX, chunkZ)
	}

	override fun shouldGenerateStructures(): Boolean {
		return getIonGenerator().shouldGenerateStructures()
	}

	override fun shouldGenerateStructures(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
		return getIonGenerator().shouldGenerateStructures(worldInfo, random, chunkX, chunkZ)
	}
}
