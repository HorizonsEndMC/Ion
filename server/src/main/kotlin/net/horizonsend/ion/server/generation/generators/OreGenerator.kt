package net.horizonsend.ion.server.generation.generators

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.ServerConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.LevelChunkSection
import net.starlegacy.util.nms
import net.starlegacy.util.toBlockPos
import org.bukkit.Bukkit.createBlockData
import org.bukkit.block.data.BlockData

object OreGenerator {
	val weightedOres = oreWeights()
	val asteroidBlocks: MutableSet<BlockState> = mutableSetOf()
	private val oreMap: Map<String, BlockData> = IonServer.Ion.configuration.ores.associate { it.material to createBlockData(it.material) }

	private data class PlacedOre(
		val materialString: String,
		val size: Int,
		val originX: Int,
		val originY: Int,
		val originZ: Int
	)

	fun generateOres(world: ServerLevel, chunk: LevelChunk): Map<Long, BlockState> {
		val worldX = chunk.pos.x * 16
		val worldZ = chunk.pos.z * 16

		if (weightedOres.isEmpty()) return mapOf()

		val random: RandomSource = RandomSource.create(world.seed)

		val ores = mutableMapOf<Long, BlockState>()

		for (count in (IonServer.Ion.configuration.oreRatio * 10000).toInt() downTo 0) {
			val originX = random.nextInt(worldX, worldX + 16)
			val originY = random.nextInt(world.minBuildHeight + 10, world.maxBuildHeight - 10)
			val originZ = random.nextInt(worldZ, worldZ + 16)

			if (!asteroidBlocks.contains(
					world.getBlockIfLoaded(
							BlockPos(originX, originY, originZ)
						)?.defaultBlockState()
				)
			) {
				continue
			}
			// Quickly move on if it's not in an asteroid

			val ore = weightedOres[random.nextInt(0, weightedOres.size - 1)]

			val blobSize = random.nextInt(ore.maxBlobSize).coerceAtLeast(1)

			ores += generateOre(world, PlacedOre(ore.material, blobSize, originX, originY, originZ))
		}

		return ores
	}

	private fun generateOre(world: ServerLevel, ore: PlacedOre): Map<Long, BlockState> {
		val oreBlocks = getSphereBlocks(ore.size, Triple(ore.originX, ore.originY, ore.originZ))
		val mappedOre = oreMap[ore.materialString]?.nms

		var nmsChunk = world.getChunk(ore.originX.shr(4), ore.originZ.shr(4))
		var section: LevelChunkSection

		val returnBlockStates = mutableMapOf<Long, BlockState>() // Ore blocks to return

		for (block in oreBlocks) {
			val blockPos = block.toBlockPos()
			val (x, y, z) = block
			val chunkX = x.shr(4)
			val chunkZ = z.shr(4)

			if (nmsChunk.locX != chunkX || nmsChunk.locZ != chunkZ) {
				nmsChunk = world.getChunk(chunkX, chunkZ)
			}

			// shouldn't go negative with this scheme
			section = try {
				nmsChunk.sections[
					(y + world.minBuildHeight)
						.coerceAtLeast(0)
						.coerceAtMost(world.maxBuildHeight - 1)
						.shr(4)
				]
			} catch (e: java.lang.Exception) {
				e.printStackTrace(); continue
			}

			if (!asteroidBlocks.contains(world.getBlockState(block.toBlockPos()))) continue

			mappedOre?.let {
				section.setBlockState(
					x - chunkX.shl(4),
					y - section.bottomBlockY(),
					z - chunkZ.shl(4),
					it
				)

				returnBlockStates[blockPos.asLong()] = it
			}
			nmsChunk.playerChunk?.blockChanged(blockPos)
		}
		nmsChunk.playerChunk?.broadcastChanges(nmsChunk)

		return returnBlockStates
	}

	private fun getSphereBlocks(radius: Int, origin: Triple<Int, Int, Int>): List<Triple<Int, Int, Int>> {
		if (radius == 1) return listOf(origin) // bypass the rest of this if it's useless

		val (originX, originY, originZ) = origin

		val circleBlocks = mutableListOf<Triple<Int, Int, Int>>()
		val upperBoundSquared = radius * radius

		for (x in originX - radius..originX + radius) {
			for (z in originZ - radius..originZ + radius) {
				for (y in originY - radius..originY + radius) {
					val distance =
						((x - originX) * (x - originX) + (y - originY) * (y - originY) + (z - originZ) * (z - originZ))

					if (distance < upperBoundSquared) {
						circleBlocks.add(Triple(x, y, z))
					}
				}
			}
		}

		return circleBlocks
	}

	private fun oreWeights(): List<ServerConfiguration.Ore> {
		val weightedList = mutableListOf<ServerConfiguration.Ore>()

		for (ore in IonServer.Ion.configuration.ores) {
			for (occurrence in ore.rolls downTo 0) {
				weightedList.add(ore)
			}
		}

		return weightedList
	}
}
