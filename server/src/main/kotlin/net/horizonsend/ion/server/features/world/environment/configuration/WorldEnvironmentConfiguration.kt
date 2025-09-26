package net.horizonsend.ion.server.features.world.environment.configuration

import kotlinx.serialization.Serializable

@Serializable
data class WorldEnvironmentConfiguration(
	val atmosphericPressure: Double = 1.013,
	val moduleConfiguration: List<EnvironmentModuleConfiguration> = listOf()
)
