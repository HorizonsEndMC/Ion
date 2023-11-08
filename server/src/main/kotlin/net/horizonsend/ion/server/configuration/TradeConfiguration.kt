package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class TradeConfiguration(
	val ecoStationConfiguration: CollectionConfiguration = CollectionConfiguration()
) {
	@Serializable
	data class CollectionConfiguration(
		val maxProfitPerStationPerDay: Double = 30_000.0
	)
}
