package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetaData
import net.horizonsend.ion.server.features.world.generation.feature.meta.FeatureMetadataFactory
import net.horizonsend.ion.server.features.world.generation.feature.meta.OreBlob
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material.MaterialConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.EvaluationConfiguration
import net.horizonsend.ion.server.features.world.generation.feature.start.FeatureStart
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.nbt.CompoundTag
import org.bukkit.util.noise.PerlinOctaveGenerator
import kotlin.math.sqrt
import kotlin.random.Random

data class ConfigurableAsteroidMeta(
	override val seed: Long,
	val size: Double,
	val oreBlobs: MutableList<OreBlob> = mutableListOf(),

	private val aliasedStructureNoiseLayers: Pair<String, EvaluationConfiguration>,
	private val aliasedPaletteConfiguration: Pair<String, MaterialConfiguration>
) : FeatureMetaData {
	val random = Random(seed)
	val paletteBlockPlacer = aliasedPaletteConfiguration.second.build(this)
	private val structureNoiseLayers = aliasedStructureNoiseLayers.second.build(this)

	override val factory: FeatureMetadataFactory<ConfigurableAsteroidMeta> = Factory

	val sizeSquared = size.squared()

	val cave1 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }
	val cave2 = PerlinOctaveGenerator(random.nextLong(), 3).apply { this.setScale(sqrt(0.05 * (1 / size))) }

	val totalDisplacement = getMaxDisplacement()
	private val normalizingFactor = size / totalDisplacement

	fun getNoise(x: Double, y: Double, z: Double, start: FeatureStart): Double {
		return structureNoiseLayers.getValue(x, y, z, Vec3i(start.x, start.y, start.z)) * normalizingFactor
	}

	private fun getMaxDisplacement(): Double {
		return structureNoiseLayers.getFallbackValue()
	}

	object Factory : FeatureMetadataFactory<ConfigurableAsteroidMeta>() {
		override fun load(data: CompoundTag): ConfigurableAsteroidMeta {
			val structureAlias = data.getString("structureAlias")
			val structure = ConfigurationFiles.globalAsteroidConfiguration().structureTemplates[structureAlias]!!
			val paletteAlias = data.getString("paletteAlias")
			val palette = ConfigurationFiles.globalAsteroidConfiguration().paletteTemplates[paletteAlias]!!

			return ConfigurableAsteroidMeta(
				data.getLong("seed"),
				data.getDouble("size"),
				mutableListOf(),
				structureAlias to structure,
				paletteAlias to palette
			)
		}

		override fun saveData(featureData: ConfigurableAsteroidMeta): CompoundTag {
			val tag = CompoundTag()
			tag.putLong("seed", featureData.seed)
			tag.putDouble("size", featureData.size)
			tag.putString("structureAlias", featureData.aliasedStructureNoiseLayers.first)
			tag.putString("paletteAlias", featureData.aliasedPaletteConfiguration.first)
			return tag
		}
	}
}
