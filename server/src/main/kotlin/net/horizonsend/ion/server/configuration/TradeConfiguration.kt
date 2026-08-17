package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class TradeConfiguration(
	val ecoStationConfiguration: CollectionConfiguration = CollectionConfiguration()
) {
	@Serializable
	data class CollectionConfiguration(
		//Default for newly created ones, the real config is not here
		val profitCapPerLevel: Double = 2_000.0,
		val maxProfitPerStationPerDay: Double = 100_000.0,
		val stationTypeByEcoStationName: Map<String, String> = emptyMap(),
		val buyMultiplierByEcoStationName: Map<String, Double> = emptyMap(),
		val sellMultiplierByEcoStationName: Map<String, Double> = emptyMap(),
		val stationTypeConfigurations: Map<String, StationTypeConfiguration> = emptyMap()
	)

	@Serializable
	data class StationTypeConfiguration(
		val profitCapPerLevel: Double = 2_000.0,
		val maxProfitPerStationPerDay: Double = 100_000.0
	)
}
