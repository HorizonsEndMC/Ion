package net.horizonsend.ion.server.features.world.generation.feature

import net.horizonsend.ion.server.features.space.data.BlockData
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.StandardAsteroidMetaData
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.AsteroidPlacementConfiguration
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.util.noise.PerlinOctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

object StandardAsteroidFeature : GeneratedFeature<StandardAsteroidMetaData>(NamespacedKeys.key("asteroid_normal"), AsteroidPlacementConfiguration()) {
	override val metaFactory: FeatureMetadataFactory<StandardAsteroidMetaData> = StandardAsteroidMetaData.Factory

	override suspend fun generateSection(
		generator: IonWorldGenerator<*>,
		chunkPos: ChunkPos,
		start: FeatureStart,
		metadata: StandardAsteroidMetaData,
		sectionY: Int,
		sectionMin: Int,
		sectionMax: Int,
	): CompletedSection {
		val sizeFactor = metadata.size / 15
		val section = CompletedSection.empty(sectionY)
		val center = Vec3i(start.x, start.y, start.z)

		val random = Random(start.seed + sectionY + chunkPos.x + chunkPos.z)

		val cave1 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / metadata.size))) }
		val cave2 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / metadata.size))) }

		val shapingNoise = SimplexOctaveGenerator(start.seed, 1)
		val materialNoise = SimplexOctaveGenerator(start.seed, 1).apply {
			this.setScale(0.15 / sqrt(sizeFactor))
		}

		for (x in 0..15) for (y in 0..15) for (z in 0..15) {
			val realX = chunkPos.x.shl(4) + x
			val realZ = chunkPos.z.shl(4) + z
			val realY = sectionMin + y

			val isCave: Boolean = (abs(cave1.noise(realX.toDouble(), realY.toDouble(), realZ.toDouble(), 1.0, 1.0)) < 0.1)
					&& (abs(cave2.noise(realX.toDouble(), realY.toDouble(), realZ.toDouble(), 1.0, 1.0)) < 0.1)

			if (isCave) continue

			var block: BlockState = checkBlockPlacement(
				metadata,
				materialNoise,
				shapingNoise,
				realX.toDouble(),
				realY.toDouble(),
				realZ.toDouble(),
				distanceSquared(start.x, start.y, start.z, realX, realY, realZ),
				sizeFactor
			) ?: continue

//			section.setBlock(x, y, z, BlockData(metadata.block.createBlockData().nms, null))

			val blockData = BlockData(block, null)

			section.setBlock(x, y, z, blockData)
		}

		return section
	}

	/**
	 * Places the asteroid block, if inside, and returns the block
	 **/
	private fun checkBlockPlacement(
		metadata: StandardAsteroidMetaData,
		materialNoise: SimplexOctaveGenerator,
		shapingNoise: SimplexOctaveGenerator,
		worldX: Double,
		worldY: Double,
		worldZ: Double,
		distanceSquared: Long,
		sizeFactor: Double
	): BlockState? {
		// Calculate a noise pattern with a minimum at zero, and a max peak of the size of the materials list.
		val paletteSample = (materialNoise.noise(worldX, worldY, worldZ, 0.0, 0.0, true) + 1) / 2

		// Full noise is used as the radius of the asteroid, and it is offset by the noise of each block pos.
		var fullNoise = 0.0
		val initialScale = 0.015 / sizeFactor.coerceAtLeast(1.0)

		for (octave in 0..metadata.octaves) {
			shapingNoise.setScale(initialScale * (octave + 1.0).pow(2.25 + (sizeFactor / 2.25).coerceAtMost(0.5)))

			val offset = abs(shapingNoise.noise(worldX, worldY, worldZ, 0.0, 1.0, false)) * (metadata.size / (octave + 1.0).pow(2.25))

			fullNoise += offset
		}

		fullNoise *= fullNoise
		// Continue if block is not inside any asteroid
		if (distanceSquared >= fullNoise) return null

//		val difference = (abs(1 - (distanceSquared.toDouble() / fullNoise)) * metadata.paletteBlocks.lastIndex).roundToInt().coerceIn(0, metadata.paletteBlocks.lastIndex)

		val index = (paletteSample * metadata.paletteBlocks.size).toInt()
		return metadata.paletteBlocks[index]
	}

	override fun getExtents(metaData: StandardAsteroidMetaData): Pair<Vec3i, Vec3i> {
		return Vec3i(-metaData.size.toInt(), -metaData.size.toInt(), -metaData.size.toInt()) to Vec3i(metaData.size.toInt(), metaData.size.toInt(), metaData.size.toInt())
	}

	override fun generateMetaData(chunkRandom: Random): StandardAsteroidMetaData {
//		val material = Material.entries.filter { material -> material.isBlock }.random(chunkRandom)
		// chunkRandom.nextDouble(5.0, 40.0)
		return StandardAsteroidMetaData(150.0, Material.STONE)
	}
}
