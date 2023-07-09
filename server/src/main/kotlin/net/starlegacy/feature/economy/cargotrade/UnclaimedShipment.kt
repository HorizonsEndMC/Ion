package net.starlegacy.feature.economy.cargotrade

import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.CargoCrate
import net.starlegacy.feature.economy.city.TradeCityData

/**
 * Used for shipments that haven't been claimed yet.
 */
data class UnclaimedShipment(
	val crate: Oid<CargoCrate>,
	val from: TradeCityData,
	val to: TradeCityData,
	val crateCost: Double,
	val crateRevenue: Double,
	var expiryDays: Int,
	var isAvailable: Boolean = true,
	private val fromPlanet: String,
	private val toPlanet: String
) {
	val routeValue = CargoCrates[crate].getValue(fromPlanet) - CargoCrates[crate].getValue(toPlanet)
}
