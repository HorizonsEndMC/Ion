package net.starlegacy.feature.economy.city

import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.nations.NATIONS_BALANCE

data class TradeCityData(
	val cityOid: Oid<*>,
	val type: TradeCityType,
	val territoryId: Oid<Territory>,
	var displayName: String
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
}
