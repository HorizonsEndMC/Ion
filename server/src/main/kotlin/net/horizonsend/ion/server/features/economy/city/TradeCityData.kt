package net.horizonsend.ion.server.features.economy.city

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.server.features.ai.convoys.AIConvoyTemplate
import net.horizonsend.ion.server.features.ai.convoys.CityContext
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet.Companion.DEFAULT_ITEM_FACTORY

data class TradeCityData(
	val cityOid: Oid<*>,
	val type: TradeCityType,
	val territoryId: Oid<Territory>,
	var displayName: String,
	var scheduledHour: Int? = null,  // 0–23, UTC hour
	var convoyTemplate: AIConvoyTemplate<CityContext>? = null,
	var allowedDestinations: List<TradeCityData>? = null,
	var configEffectiveAfter: Long? = null  // epoch millis when config becomes active
) {
	val settlementId: Oid<Settlement>
		get() {
			require(type == TradeCityType.SETTLEMENT)
			@Suppress("UNCHECKED_CAST")
			return cityOid as Oid<Settlement>
		}

	val tax: Double
		get() = when (type) {
			TradeCityType.NPC -> NATIONS_BALANCE.settlement.maxTaxPercent / 100.0
			TradeCityType.SETTLEMENT -> SettlementCache[settlementId].tradeTax ?: 0.0
		}

	val planetIcon get() = Space.getPlanet(Regions.get<RegionTerritory>(territoryId).world)?.planetIconFactory?.construct() ?: DEFAULT_ITEM_FACTORY.construct()
}
