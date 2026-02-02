package net.horizonsend.ion.server.features.world.environment.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.modules.EnvironmentModule
import net.horizonsend.ion.server.features.world.environment.modules.UltracoldModule

@Serializable
object UltracoldEnvironmentModuleConfiguration : EnvironmentModuleConfiguration {
	override fun buildModule(manager: WorldEnvironmentManager): EnvironmentModule {
		return UltracoldModule(manager)
	}
}
