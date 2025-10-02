package net.horizonsend.ion.server.features.world.generation.generators.configuration.generator

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.FeatureGenerator
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator
import net.horizonsend.ion.server.features.world.generation.generators.configuration.feature.FeaturePlacementConfiguration

@Serializable
data class FeatureGeneratorConfiguration(
	val features: Set<FeaturePlacementConfiguration<*>>
) : GenerationConfiguration {
	override fun buildGenerator(world: IonWorld): IonWorldGenerator<*> {
		return FeatureGenerator(world, this)
	}
}
