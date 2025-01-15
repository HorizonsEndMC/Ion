package net.horizonsend.ion.server.features.transport

import kotlinx.serialization.Serializable

@Serializable
data class TransportConfiguration(
	val extractorConfiguration: ExtractorConfiguration = ExtractorConfiguration(),
	val powerConfiguration: PowerTransportConfiguration = PowerTransportConfiguration()
) {
	@Serializable
	data class ExtractorConfiguration(
		val extractorTickIntervalMS: Long = 2000,
		val maxFluidRemovedPerExtractorTick: Int = 1000,
	)

	@Serializable
	data class PowerTransportConfiguration(
		val maxPowerRemovedPerExtractorTick: Int = 1000,
		val solarPanelTickPower: Int = 100,
		val maxExtractorDestinations: Int = 100,
		val maxSolarDestinations: Int = 100,
		val maxPathfindDepth: Int = 600
	)
}
