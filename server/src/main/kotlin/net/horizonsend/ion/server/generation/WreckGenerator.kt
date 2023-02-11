package net.horizonsend.ion.server.generation

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk

object WreckGenerator {
	const val wreckGenerationVersion: Byte = 0
	private const val searchRadius = 1.25
	val timing = AsteroidGenerator.timing
	val schematics = Ion.configuration.asteroidConfig.mappedWrecks
	val weightedWrecks = weightWrecks()

	fun generateWreck(
		serverLevel: ServerLevel,
		wreck: Wreck
	) {
		// Find covered chunks
		val coveredChunks = mutableMapOf<ChunkPos, List<Int>>()

		// covered chunks found
		for ((chunkPos, sections) in coveredChunks) {
			// get chunk async
			serverLevel.world.getChunkAtAsync(chunkPos.x, chunkPos.z).thenAcceptAsync asyncChunk@{ bukkitChunk ->
				val nmsChunk = (bukkitChunk as CraftChunk).handle

				// Iterate through covered sections
				for (sectionY in sections) {
					val section = nmsChunk.sections[sectionY]
				}
			}
		}
		// Iterate through covered chunks
	}

	private fun weightWrecks(): List<String> {
		val weightedList = mutableListOf<String>()

		for ((name, weight) in schematics) {
			for (count in weight..0) {
				weightedList.add(name)
			}
		}
		return weightedList
	}

	data class Wreck(
		val x: Int,
		val y: Int,
		val z: Int,
		val wreck: String
	) {
		val schematic = schematics[this.wreck]
	}
}
