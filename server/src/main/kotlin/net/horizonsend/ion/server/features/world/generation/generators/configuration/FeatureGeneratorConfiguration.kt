package net.horizonsend.ion.server.features.world.generation.generators.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.FeatureGenerator
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator

@Serializable
data class FeatureGeneratorConfiguration(
	val features: Set<FeatureConfiguration>
) : GenerationConfiguration {
	override fun buildGenerator(world: IonWorld): IonWorldGenerator<*> {
		return FeatureGenerator(world, this)
	}
}
