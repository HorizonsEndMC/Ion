package net.horizonsend.ion.server.features.world

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.gas.type.WorldGasConfiguration
import net.horizonsend.ion.server.features.world.environment.Environment

@Serializable
data class WorldSettings(
    val flags: MutableSet<WorldFlag> = mutableSetOf(),
    val environments: MutableSet<Environment> = mutableSetOf(),
	val gasses: WorldGasConfiguration = WorldGasConfiguration()
)
