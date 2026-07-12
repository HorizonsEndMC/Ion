package net.horizonsend.ion.server.features.world.generation.generators.configuration.generator

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.IonWorldGenerator

@Serializable
sealed interface GenerationConfiguration {
	fun buildGenerator(world: IonWorld): IonWorldGenerator<*>
}
