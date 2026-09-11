package net.horizonsend.ion.server.features.world.generation.feature

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.createBlockData
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.PointedDripstone
import org.bukkit.generator.ChunkGenerator
import kotlin.math.abs
import kotlin.random.Random

object ConfigurableAsteroidFeature : GeneratedFeature<ConfigurableAsteroidMeta>(WorldGenerationFeatureKeys.CONFIGURABLE_ASTEROID) {
	override val placementPriority: Int = 0
	override val metaFactory: FeatureMetadataFactory<ConfigurableAsteroidMeta> = ConfigurableAsteroidMeta.Factory

	override fun generateChunk(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		chunkData: ChunkGenerator.ChunkData,
		start: FeatureStart,
		metaData: ConfigurableAsteroidMeta,
		minY: Int,
		maxY: Int
	) {
		val center = Vec3i(start.x, start.y, start.z).toCenterVector()

		val (oreMask, orePlacements) = generateOreMask(metaData, start, chunkPos.x, chunkPos.z)

		// Array of packed x, y to min y levels
		val minColumnY: MutableMap<Long, Int> = Long2ObjectOpenHashMap()
		val maxColumnY: MutableMap<Long, Int> = Long2ObjectOpenHashMap()

		for (x in 0..15) {
			val realX = (chunkPos.x.shl(4) + x).toDouble()
			val xOffset = center.x - realX

			for (realY in minY..maxY) {
				val yOffset = center.y - realY

				for (z in 0..15) {
					val realZ = (chunkPos.z.shl(4) + z).toDouble()
					val zOffset = center.z - realZ

					val centerDistanceSquared = xOffset.squared() + yOffset.squared() + zOffset.squared()

					val blockState = checkBlockPlacement(metaData, start, realX, realY.toDouble(), realZ, centerDistanceSquared) ?: continue

					val key = toBlockKey(realX.toInt(), realY, realZ.toInt())
					if (oreMask.contains(key)) {
						chunkData.setBlock(x, realY, z, orePlacements[key])
						continue
					}

					val packedXZ = ChunkPos.asLong(x, z)
					trackExtents(packedXZ, realY, minColumnY, maxColumnY)

					chunkData.setBlock(x, realY, z, blockState.createCraftBlockData())
				}
			}
		}

		val spikeCount = minColumnY.size / 100

		repeat(spikeCount / 2) {
			val (packed, minY) = minColumnY.entries.random()
			generateTestSpike(chunkData, ChunkPos.getX(packed), minY, ChunkPos.getZ(packed), BlockFace.DOWN, randomInt(1, 8))
		}
		repeat(spikeCount / 2) {
			val (packed, maxY) = maxColumnY.entries.random()
			generateTestSpike(chunkData, ChunkPos.getX(packed), maxY, ChunkPos.getZ(packed), BlockFace.UP, randomInt(1, 8))
		}
	}

	fun generateTestSpike(chunkData: ChunkGenerator.ChunkData, startX: Int, startY: Int, startZ: Int, direction: BlockFace, length: Int) {
		var remaining = length

		var y = startY

		while (remaining > 0) {
			y += direction.modY

			val data = when (remaining) {
				1 -> Material.POINTED_DRIPSTONE.createBlockData<PointedDripstone> { it.thickness = PointedDripstone.Thickness.TIP; it.verticalDirection = direction }
				2 -> Material.POINTED_DRIPSTONE.createBlockData<PointedDripstone> { it.thickness = PointedDripstone.Thickness.FRUSTUM; it.verticalDirection = direction }
				length -> Material.POINTED_DRIPSTONE.createBlockData<PointedDripstone> { it.thickness = PointedDripstone.Thickness.BASE; it.verticalDirection = direction }
				else -> Material.POINTED_DRIPSTONE.createBlockData<PointedDripstone> { it.thickness = PointedDripstone.Thickness.MIDDLE; it.verticalDirection = direction }
			}

			chunkData.setBlock(startX, y, startZ, data)

			remaining--
		}
	}

	fun trackExtents(packedXZ: Long, realY: Int, minColumnY: MutableMap<Long, Int>, maxColumnY: MutableMap<Long, Int>) {
		val minYForColumn = minColumnY[packedXZ]
		if (minYForColumn != null) {
			if (minYForColumn > realY) {
				minColumnY[packedXZ] = realY
			}
		} else {
			minColumnY[packedXZ] = realY
		}

		val maxYForColumn = maxColumnY[packedXZ]
		if (maxYForColumn != null) {
			if (maxYForColumn < realY) {
				maxColumnY[packedXZ] = realY
			}
		} else {
			maxColumnY[packedXZ] = realY
		}
	}

	/**
	 * Places the asteroid block, if inside, and returns the block
	 **/
	private fun checkBlockPlacement(
		metaData: ConfigurableAsteroidMeta,
		start: FeatureStart,
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		distanceSquared: Double
	): BlockState? {
		if (distanceSquared > metaData.sizeSquared) return null

		val fullNoise = metaData.getNoise(worldX, worldY, worldZ, start)

		val noiseSquared = fullNoise * fullNoise
		// Continue if block is not inside any asteroid
		if (distanceSquared > noiseSquared) return null

		val proportionDepth = (abs((distanceSquared / noiseSquared))) * 1.5

		val cave1Noise = abs(metaData.cave1.noise(worldX, worldY, worldZ, 1.0, 1.0) * proportionDepth)
		val cave2Noise = abs(metaData.cave2.noise(worldX, worldY, worldZ, 1.0, 1.0) * proportionDepth)

		val isCave: Boolean = (cave1Noise < 0.1) && (cave2Noise < 0.1)

		if (isCave) return null

		return metaData.paletteBlockPlacer.getValue(worldX, worldY, worldZ, distanceSquared, fullNoise, metaData, start)
	}

	override fun getExtents(metaData: ConfigurableAsteroidMeta): Pair<Vec3i, Vec3i> {
		return Vec3i(
				-metaData.size.toInt(),
				-metaData.size.toInt(),
				-metaData.size.toInt()
			) to Vec3i(
				metaData.size.toInt(),
				metaData.size.toInt(),
				metaData.size.toInt()
			)
	}

	private fun generateOreMask(meta: ConfigurableAsteroidMeta, start: FeatureStart, chunkX: Int, chunkZ: Int): Pair<LongOpenHashSet, Long2ObjectOpenHashMap<BlockData>> {
		val placementMask = LongOpenHashSet()
		val blocks = Long2ObjectOpenHashMap<BlockData>()

		for (chunkX in (chunkX - 1)..(chunkX + 1)) for (chunkZ in (chunkZ - 1)..(chunkZ + 1)) {
			val random = Random(ChunkPos(chunkX, chunkZ).longKey)

			for (def in meta.oreDefinitions) {
				repeat(def.getChunkOreCount(meta)) {
					val placement = def.random(random, start, meta, chunkX, chunkZ)

					for (pos in placement.getOffsetCoordinates()) {
						val key = toBlockKey(pos)
						placementMask.add(key)
						blocks[key] = def.material.toBukkitBlockData()
					}
				}
			}
		}

		return placementMask to blocks
	}
}
