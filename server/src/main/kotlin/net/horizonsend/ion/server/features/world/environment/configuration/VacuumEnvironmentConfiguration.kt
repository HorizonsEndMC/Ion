package net.horizonsend.ion.server.features.world.environment.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.modules.EnvironmentModule
import net.horizonsend.ion.server.features.world.environment.modules.VacuumModule

@Serializable
object VacuumEnvironmentConfiguration : EnvironmentModuleConfiguration {
	override fun buildModule(manager: WorldEnvironmentManager): EnvironmentModule {
		return VacuumModule(manager)
	}
}
