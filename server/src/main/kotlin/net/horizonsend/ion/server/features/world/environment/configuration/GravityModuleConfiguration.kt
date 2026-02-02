package net.horizonsend.ion.server.features.world.environment.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.modules.EnvironmentModule
import net.horizonsend.ion.server.features.world.environment.modules.GravityModule

@Serializable
data class GravityModuleConfiguration(val strength: Double) : EnvironmentModuleConfiguration {
	override fun buildModule(manager: WorldEnvironmentManager): EnvironmentModule {
		return GravityModule(manager, strength)
	}
}
