package net.horizonsend.ion.server.features.economy.city

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import java.util.concurrent.ConcurrentHashMap

object TradeCities : IonServerComponent() {
	private val cities: MutableMap<Oid<Territory>, TradeCityData> = ConcurrentHashMap()

	override fun onEnable() {
		val settlementType = TradeCityType.SETTLEMENT
		val settlements = SettlementCache.all()
			.filter { it.cityState == Settlement.CityState.ACTIVE }
			.associate {
				it.territory to TradeCityData(
					it.id,
					settlementType,
					it.territory,
					it.name
				)
			}
		cities.putAll(settlements)
		Settlement.watchUpdates { change ->
			val id: Oid<Settlement> = change.oid

			change[Settlement::cityState]?.also {
				val data: SettlementCache.SettlementData = SettlementCache[id]

				val newState = it.nullable()?.enumValue<Settlement.CityState>()

				if (newState == Settlement.CityState.ACTIVE) {
					cities[data.territory] = TradeCityData(data.id, settlementType, data.territory, data.name)
				} else {
					cities.remove(data.territory)
				}
			}
		}
		Settlement.watchDeletes { change ->
			val oid = change.oid
			cities.filter { it.value.cityOid == oid }.forEach { cities.remove(it.value.territoryId) }
		}

		val npcType = TradeCityType.NPC
		val npcCities = NPCTerritoryOwner.all().filter { it.tradeCity }.associate {
			it.territory to TradeCityData(
				it._id,
				npcType,
				it.territory,
				it.name
			)
		}
		cities.putAll(npcCities)
		NPCTerritoryOwner.watchInserts { change ->
			val fullDocument = change.fullDocument ?: return@watchInserts
			if (!fullDocument.tradeCity) return@watchInserts
			cities[fullDocument.territory] =
				TradeCityData(
					change.oid, npcType, fullDocument.territory, fullDocument.name
				)
		}
		NPCTerritoryOwner.watchDeletes { change ->
			val oid = change.oid
			cities.filter { it.value.cityOid == oid }.forEach { cities.remove(it.value.territoryId) }
		}
	}

	fun getAll(): List<TradeCityData> = cities.values.toList()

	fun isCity(territory: RegionTerritory): Boolean {
		return cities.containsKey(territory.id)
	}

	fun getIfCity(territory: RegionTerritory): TradeCityData? {
		return cities[territory.id]
	}
}
