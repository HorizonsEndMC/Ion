package net.horizonsend.ion.server.features.gas.type

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.gas.collection.CollectedGas

@Serializable
data class WorldGasConfiguration(
	val gasses: List<CollectedGas> = listOf()
)

