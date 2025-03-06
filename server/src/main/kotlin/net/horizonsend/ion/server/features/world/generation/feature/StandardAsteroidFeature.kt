package net.horizonsend.ion.server.features.world.generation.feature

import com.github.auburn.FastNoiseLite
import com.github.auburn.FastNoiseLite.FractalType
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.StandardAsteroidMetaData
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

object StandardAsteroidFeature : GeneratedFeature<StandardAsteroidMetaData>(NamespacedKeys.key("asteroid_normal"), AsteroidPlacementConfiguration()) {
	override val metaFactory: FeatureMetadataFactory<StandardAsteroidMetaData> = StandardAsteroidMetaData.Factory

	override suspend fun generateSection(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		start: FeatureStart,
		metaData: StandardAsteroidMetaData,
		sectionY: Int,
		sectionMin: Int,
		sectionMax: Int,
	): CompletedSection {
		val section = CompletedSection.empty(sectionY)
		val center = Vec3i(start.x, start.y, start.z).toCenterVector()

		for (x in 0..15) {
			val realX = (chunkPos.x.shl(4) + x).toDouble()
			val xOffset = center.x - realX

			for (y in 0..15) {
				val realY = (sectionMin + y).toDouble()
				val yOffset = center.y - realY

				for (z in 0..15) {
					val realZ = (chunkPos.z.shl(4) + z).toDouble()
					val zOffset = center.z - realZ

					val centerDistanceSquared = xOffset.squared() + yOffset.squared() + zOffset.squared()

					val blockState = checkBlockPlacement(metaData, realX, realY, realZ, centerDistanceSquared) ?: continue

					section.setBlock(x, y, z, BlockData(blockState, null))
				}
			}
		}

		return section
	}

	/**
	 * Places the asteroid block, if inside, and returns the block
	 **/
	private fun checkBlockPlacement(
		metaData: StandardAsteroidMetaData,
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		distanceSquared: Double
	): BlockState? {
		if (distanceSquared > metaData.sizeSquared) return null
		// Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.
		val paletteSample = (metaData.materialNoise.noise(worldX, worldY, worldZ, 0.0, 0.0, true) + 1) / 2

		val xScale = 1.0f
		val yScale = 1.0f
		val zScale = 1.0f

		// Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.
		var fullNoise = metaData.shapingNoise.withIndex().sumOf { (octave, generator) ->
			val generatedValue = (generator.noise(worldX * xScale, worldY * yScale, worldZ * zScale, 1.0, 1.0, true) + 1.0) / 2.0
			generatedValue * metaData.scaleNoiseFactor(octave) * metaData.normalizingFactor
		}

		val noise = FastNoiseLite()
		noise.SetNoiseType(FastNoiseLite.NoiseType.Cellular)
		noise.SetSeed(metaData.seed.toInt())
		noise.SetFrequency(0.015f)
		noise.SetFractalType(FractalType.None)
		noise.SetCellularReturnType(FastNoiseLite.CellularReturnType.Distance2Div)
//		noise.SetFractalLacunarity(2f)
//		noise.SetFractalPingPongStrength(2f)
//		noise.SetFractalOctaves(3)
		noise.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.EuclideanSq)

		fullNoise += (noise.GetNoise(worldX.toFloat() * xScale, worldY.toFloat() * yScale, worldZ.toFloat() * zScale) * (metaData.size * 0.35) * -1)

		val noiseSquared = fullNoise * fullNoise
		// Continue if block is not inside any asteroid
		if (distanceSquared > noiseSquared) return null

//		val sqrtDistanceSquared = sqrt(distanceSquared)
//		val sizeRatio = fullNoise / sqrtDistanceSquared

		val proportionDepth = (abs((distanceSquared / noiseSquared))) * 1.5

		val cave1Noise = abs(metaData.cave1.noise(worldX, worldY, worldZ, 1.0, 1.0) * proportionDepth)
		val cave2Noise = abs(metaData.cave2.noise(worldX, worldY, worldZ, 1.0, 1.0) * proportionDepth)

		val isCave: Boolean = (cave1Noise < 0.1) && (cave2Noise < 0.1)

		if (isCave) return null

//		Depth based
		val difference = (abs(1 - (distanceSquared / noiseSquared)))
//		return metadata.paletteBlocks[difference.roundToInt().coerceIn(0, metadata.paletteBlocks.lastIndex)]

//		return getSurfaceNoise(difference, paletteSample, metaData, cave1Noise, cave2Noise)
//		return getSurfaceDepth(distanceSquared, metaData)

//		Surface detection
//		val surfaceDetectionDifference = fullNoise - sqrt(distanceSquared)
//		if (abs(surfaceDetectionDifference) < 1) {
//			check(sizeRatio)
////			return Material.NETHERITE_BLOCK.createBlockData().nms
//		}

//		// Normal noise
//		val index = (paletteSample * metadata.paletteBlocks.size).toInt()
//		return metadata.paletteBlocks[index]

		return if (distanceSquared < (metaData.size.pow(2))) return Material.GREEN_CONCRETE.createBlockData().nms else Material.RED_CONCRETE.createBlockData().nms

		// On the surface, do normal noise
//		val index = (paletteSample * metadata.paletteBlocks.size).toInt()
//		return metadata.paletteBlocks[index]
	}

	private fun getSurfaceNoise(difference: Double, paletteSample: Double, metaData: StandardAsteroidMetaData, cave1Noise: Double, cave2Noise: Double): BlockState {
		if (difference < 0.1) {
			val index = (paletteSample * metaData.paletteBlocks.size).toInt()
			return metaData.paletteBlocks[index]
		}
		if ((cave1Noise < 0.125) && (cave2Noise < 0.125)) {
			return Material.BLACKSTONE.createBlockData().nms
		}
		if (difference < 0.5) {
			return Material.STONE.createBlockData().nms
		}
		if (difference < 0.75) {
			return Material.DEEPSLATE.createBlockData().nms
		}
		if (difference < 0.85) {
			return Material.BLACKSTONE.createBlockData().nms
		}
		return Material.MAGMA_BLOCK.createBlockData().nms
	}

	private fun getSurfaceDepth(distanceSquared: Double, metaData: StandardAsteroidMetaData): BlockState {
		val radius = metaData.totalDisplacement
		// Concentrate into the outer ratio
		val concentration = radius * 0.55
		val sqrtDistance = sqrt(distanceSquared)
		if (sqrtDistance < concentration) return metaData.paletteBlocks.first()

		val ratio = ((sqrtDistance - concentration) / ((radius - concentration) - 1))
		val index = (metaData.paletteBlocks.size * ratio).roundToInt().coerceIn(0 ..< metaData.paletteBlocks.size)
		return metaData.paletteBlocks[index]
	}

	private val LIMIT_EXTENSION_RANGE = 1.2

	override fun getExtents(metaData: StandardAsteroidMetaData): Pair<Vec3i, Vec3i> {
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

	override fun generateMetaData(chunkRandom: Random): StandardAsteroidMetaData {
//		val material = Material.entries.filter { material -> material.isBlock }.random(chunkRandom)
		// chunkRandom.nextDouble(5.0, 40.0)
		return StandardAsteroidMetaData(chunkRandom.nextLong(), 150.0, Material.STONE)
	}
}
