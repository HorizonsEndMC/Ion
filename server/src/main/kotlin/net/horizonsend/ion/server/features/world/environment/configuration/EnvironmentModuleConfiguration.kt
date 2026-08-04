package net.horizonsend.ion.server.features.world.environment.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.modules.EnvironmentModule

@Serializable
sealed interface EnvironmentModuleConfiguration {
	fun buildModule(manager: WorldEnvironmentManager): EnvironmentModule
}
