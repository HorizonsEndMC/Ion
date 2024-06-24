package net.horizonsend.ion.server.features.transport.configuration

import kotlinx.serialization.Serializable

@Serializable
data class TransportConfiguration(
	val extractorTickIntervalMS: Long,
	val maxPowerRemovedPerExtractorTick: Int
) {

}
