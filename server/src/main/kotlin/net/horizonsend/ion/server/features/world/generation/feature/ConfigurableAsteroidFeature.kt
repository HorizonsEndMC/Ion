package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import kotlin.math.abs
import kotlin.random.Random

object ConfigurableAsteroidFeature : GeneratedFeature<ConfigurableAsteroidMeta>(NamespacedKeys.key("asteroid_normal"), AsteroidPlacementConfiguration()) {
	override val metaFactory: FeatureMetadataFactory<ConfigurableAsteroidMeta> = ConfigurableAsteroidMeta.Factory

	override suspend fun generateSection(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		start: FeatureStart,
		metaData: ConfigurableAsteroidMeta,
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

					val blockState = checkBlockPlacement(metaData, start, realX, realY, realZ, centerDistanceSquared) ?: continue

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
		metaData: ConfigurableAsteroidMeta,
		start: FeatureStart,
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		distanceSquared: Double
	): BlockState? {
		if (distanceSquared > metaData.sizeSquared) return null
		// Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.
		val paletteSample = (metaData.materialNoise.noise(worldX, worldY, worldZ, 0.0, 0.0, true) + 1) / 2

		val fullNoise = metaData.getNoise(worldX, worldY, worldZ, start)

		val noiseSquared = fullNoise * fullNoise
		// Continue if block is not inside any asteroid
		if (distanceSquared > noiseSquared) return null

		val proportionDepth = (abs((distanceSquared / noiseSquared))) * 1.5

		val cave1Noise = abs(metaData.cave1.noise(worldX, worldY, worldZ, 1.0, 1.0) * proportionDepth)
		val cave2Noise = abs(metaData.cave2.noise(worldX, worldY, worldZ, 1.0, 1.0) * proportionDepth)

		val isCave: Boolean = (cave1Noise < 0.1) && (cave2Noise < 0.1)

		if (isCave) return null

		return metaData.blockPlacer.getValue(worldX, worldY, worldZ, distanceSquared, fullNoise, metaData, start)
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

	override fun generateMetaData(chunkRandom: Random): ConfigurableAsteroidMeta {
//		val material = Material.entries.filter { material -> material.isBlock }.random(chunkRandom)
		// chunkRandom.nextDouble(5.0, 40.0)
		return ConfigurableAsteroidMeta(chunkRandom.nextLong(), 150.0, Material.STONE)
	}
}
