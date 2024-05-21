package net.horizonsend.ion.server.features.ores.generation

import net.horizonsend.ion.server.features.ores.storage.Ore
import net.minecraft.core.BlockPos
import org.bukkit.Chunk
import kotlin.math.pow
import kotlin.random.Random

private const val BUILT_HEIGHT = 384
private const val CHUNK_VOLUME = BUILT_HEIGHT * 16 * 16
private const val ORE_BALANCE = 0.02f

/**
 * Generate all ore blobs for a chunk
 **/
fun generateBlobs(chunk: Chunk, ores: Collection<PlanetOreSettings.OreSetting>): List<OreBlob> {
	val generated = mutableListOf<OreBlob>()

	// Generate blobs for the surrounding chunks as to allow some to cross over into this one
	for (x in chunk.x - 1..chunk.x + 1) for (z in chunk.z - 1..chunk.z + 1) {
		// Guaranteed unique
		val seed = BlockPos.asLong(x, 0, z)

		generated += generateBlobs(seed, ores)
	}

	return generated
}

/**
 * Generate all ore blobs for a chunk, provided a seed
 **/
fun generateBlobs(seed: Long, ores: Collection<PlanetOreSettings.OreSetting>): List<OreBlob> {
	val random = Random(seed)
	return ores.flatMap { generateBlob(random, it) }
}

/**
 * Generate all ore blobs for a single ore in a chunk
 **/
fun generateBlob(random: Random, ore: PlanetOreSettings.OreSetting): List<OreBlob> {
	// Probability that any block is an ore
	val probability = ore.stars * ORE_BALANCE

	// Get the number of blocks by estimated number of ores per chunk split into ore blobs by average volume
	val number = (probability * CHUNK_VOLUME) / ore.getVolume()
	val blobs = mutableListOf<OreBlob>()

	// The same sequence should be generated for every chunk every time, unless the config changes
	for (n in 0..number.toInt()) {
		val x = random.nextFloat() * 15
		val y = random.nextFloat() * 15
		val z = random.nextFloat() * 15
		val size = (random.nextFloat() * (ore.blobSizeMax - ore.blobSizeMin)) + ore.blobSizeMin

		blobs += OreBlob(ore.ore, x, y, z, size)
	}

	return blobs
}

data class OreBlob(
	val ore: Ore,
	val originX: Float,
	val originY: Float,
	val originZ: Float,
	val size: Float
) {
	fun contains(x: Int, y: Int, z: Int): Boolean {
		return (originX - x).pow(2) + (originY - y).pow(2) + (originZ - z).pow(2) <= size
	}
}
