package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.core.registration.keys.WorldGenerationFeatureKeys
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.generator.ChunkGenerator
import kotlin.math.abs

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

					chunkData.setBlock(x, realY, z, blockState.createCraftBlockData())
				}
			}
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
}
