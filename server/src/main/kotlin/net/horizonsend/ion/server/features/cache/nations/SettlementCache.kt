package net.horizonsend.ion.server.features.cache.nations

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import net.horizonsend.ion.server.features.cache.ManualCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.containsUpdated
import net.horizonsend.ion.common.database.double
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.util.Tasks

object SettlementCache : ManualCache() {
	private fun synced(block: () -> Unit): Unit = Tasks.sync(block)

	data class SettlementData(
		val id: Oid<Settlement>,
		var territory: Oid<Territory>,
		var name: String,
		var leader: SLPlayerId,
		var nation: Oid<Nation>?,
		var cityState: Settlement.CityState?,
		var minBuildAccess: Settlement.ForeignRelation?,
		var tradeTax: Double? = null
	)

	private val SETTLEMENT_DATA = ConcurrentHashMap<Oid<Settlement>, SettlementData>()
	private val nameCache = ConcurrentHashMap<String, Oid<Settlement>>()

	override fun load() {
		SETTLEMENT_DATA.clear()

		fun cache(settlement: Settlement) {
			val id: Oid<Settlement> = settlement._id
			val territory = settlement.territory
			val name = settlement.name
			val leader = settlement.leader
			val nation = settlement.nation
			val cityState = settlement.cityState
			val minBuild = settlement.minimumBuildAccess
			val tax = settlement.tradeTax
			val data = SettlementData(id, territory, name, leader, nation, cityState, minBuild, tax)
			SETTLEMENT_DATA[id] = data
			nameCache[data.name.lowercase(Locale.getDefault())] = id
		}

		for (settlement in Settlement.all()) {
			cache(settlement)
		}

		Settlement.watchInserts { change ->
			synced {
				cache(change.fullDocument!!)
			}
		}

		Settlement.watchUpdates { change ->
			val id: Oid<Settlement> = change.oid

			synced {
				val data = SETTLEMENT_DATA[id] ?: error("$id wasn't cached")

				change[Settlement::territory]?.let {
					data.territory = it.oid()
				}

				change[Settlement::name]?.let {
					nameCache.remove(data.name.lowercase(Locale.getDefault()))
					data.name = it.string()
					nameCache[data.name.lowercase(Locale.getDefault())] = id
				}

				change[Settlement::leader]?.let {
					data.leader = it.slPlayerId()
				}

				change[Settlement::nation]?.let {
					data.nation = it.nullable()?.oid()
				}

				change[Settlement::cityState]?.let {
					data.cityState = it.nullable()?.enumValue<Settlement.CityState>()
				}

				change[Settlement::tradeTax]?.let {
					data.tradeTax = it.nullable()?.double()
				}

				change[Settlement::minimumBuildAccess]?.let {
					data.minBuildAccess = it.nullable()?.enumValue<Settlement.ForeignRelation>()
				}

				if (change.containsUpdated(Settlement::leader) ||
					change.containsUpdated(Settlement::minimumBuildAccess) ||
					change.containsUpdated(Settlement::cityState) ||
					change.containsUpdated(Settlement::name) ||
					change.containsUpdated(Settlement::nation)
				) {
					updateRegionsAsync(id)
				}
			}
		}

		Settlement.watchDeletes { change ->
			synced {
				val id: Oid<Settlement> = change.oid

				val name =
					get(id).name.lowercase(Locale.getDefault()) // get the name first since it's about to be removed
				SETTLEMENT_DATA.remove(id)
				nameCache.remove(name.lowercase(Locale.getDefault()))
			}
		}
	}

	private fun updateRegionsAsync(id: Oid<Settlement>) {
		Tasks.async {
			val data = SETTLEMENT_DATA[id] ?: return@async

			Regions.refreshSettlementTerritoryLocally(id)
			Regions.refreshSettlementMembersLocally(id)
			NationsMap.updateTerritory(Regions[data.territory])
		}
	}

	fun allIds(): List<Oid<Settlement>> = SETTLEMENT_DATA.keys.toList()

	fun all(): List<SettlementData> = SETTLEMENT_DATA.values.toList()

	operator fun get(settlementId: Oid<Settlement>): SettlementData = SETTLEMENT_DATA[settlementId]
		?: error("$settlementId is not cached")

	fun getByName(name: String): Oid<Settlement>? = nameCache[name.lowercase(Locale.getDefault())]
}
