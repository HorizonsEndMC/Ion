package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class TradeConfiguration(
	val ecoStationConfiguration: CollectionConfiguration = CollectionConfiguration()
) {
	@Serializable
	data class CollectionConfiguration(
		val profitCapPerLevel: Double = 1_000.0,
		val maxProfitPerStationPerDay: Double = 100_000.0
	)
}
