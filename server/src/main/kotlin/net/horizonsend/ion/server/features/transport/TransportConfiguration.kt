package net.horizonsend.ion.server.features.transport

import kotlinx.serialization.Serializable

@Serializable
data class TransportConfiguration(
	val extractorConfiguration: ExtractorConfiguration = ExtractorConfiguration()
) {
	@Serializable
	data class ExtractorConfiguration(
		val extractorTickIntervalMS: Long = 2000,
		val maxPowerRemovedPerExtractorTick: Int = 1000,
		val maxFluidRemovedPerExtractorTick: Int = 1000,
	)
}
