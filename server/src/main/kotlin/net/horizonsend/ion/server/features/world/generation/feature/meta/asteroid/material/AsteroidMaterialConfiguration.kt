package net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.material

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.ConfigurableAsteroidMeta
import net.horizonsend.ion.server.features.world.generation.feature.meta.asteroid.noise.EvaluationConfiguration
import org.bukkit.Material

@Serializable
sealed interface MaterialConfiguration {
	fun build(meta: ConfigurableAsteroidMeta): AsteroidMaterial
}

@Serializable
data class WeightedMaterialConfiguration(val materialConfiguration: MaterialConfiguration, val weight: Double) : MaterialConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): AsteroidMaterial.WeightedMaterial = AsteroidMaterial.WeightedMaterial(materialConfiguration.build(meta), weight)
}

@Serializable
data class NoiseMaterialConfiguration(
	val noiseConfiguration: EvaluationConfiguration,
	val blocks: List<WeightedMaterialConfiguration>,
) : MaterialConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): NoiseAsteroidMaterial = NoiseAsteroidMaterial(meta, noiseConfiguration.build(meta), blocks.map { it.build(meta) })
}

@Serializable
data class SimpleMaterialConfiguration(val material: Material): MaterialConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): AsteroidMaterial = AsteroidMaterial.SimpleMaterial(material)
}

@Serializable
data class WeightedRandomConfiguration(val blocks: List<WeightedMaterialConfiguration>) : MaterialConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): AsteroidMaterial {
		return RamdomWeightedAsteroidMaterial(blocks.map { it.build(meta) })
	}
}

@Serializable
data class RandomConfiguration(val blocks: List<MaterialConfiguration>) : MaterialConfiguration {
	override fun build(meta: ConfigurableAsteroidMeta): AsteroidMaterial {
		return RamdomAsteroidMaterial(blocks.map { it.build(meta) })
	}
}
