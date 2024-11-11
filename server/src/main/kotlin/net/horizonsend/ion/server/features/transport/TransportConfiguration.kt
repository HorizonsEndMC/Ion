package net.horizonsend.ion.server.features.transport

import kotlinx.serialization.Serializable

@Serializable
data class TransportConfiguration(
	val extractorTickIntervalMS: Long = 2000,
	val maxPowerRemovedPerExtractorTick: Int = 1000
)
