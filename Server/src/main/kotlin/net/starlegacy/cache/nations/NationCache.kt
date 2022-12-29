package net.starlegacy.cache.nations

import net.starlegacy.cache.ManualCache
import net.starlegacy.database.Oid
import net.starlegacy.database.get
import net.starlegacy.database.int
import net.starlegacy.database.oid
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.string
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.util.Tasks
import java.util.concurrent.ConcurrentHashMap

object NationCache : ManualCache() {
	private fun synced(block: () -> Unit): Unit = Tasks.sync(block)

	data class NationData(
		val id: Oid<Nation>,
		var name: String,
		var capital: Oid<Settlement>,
		var color: Int
	) {
		val leader get() = SettlementCache[capital].leader
	}

	private val NATION_DATA = ConcurrentHashMap<Oid<Nation>, NationData>()
	private val nameCache = ConcurrentHashMap<String, Oid<Nation>>()

	override fun load() {
		NATION_DATA.clear()

		fun cache(nation: Nation) {
			val id: Oid<Nation> = nation._id
			val data = NationData(id, nation.name, nation.capital, nation.color)
			NATION_DATA[id] = data
			nameCache[data.name] = id
		}

		for (nation in Nation.all()) {
			cache(nation)
		}

		Nation.watchInserts { change ->
			synced {
				change.fullDocument?.let(::cache)
			}
		}

		Nation.watchUpdates { change ->
			synced {
				val id: Oid<Nation> = change.oid

				val data = NATION_DATA[id] ?: error("$id wasn't cached")

				change[Nation::name]?.let {
					nameCache.remove(data.name)
					data.name = it.string()
					nameCache[data.name] = id
					NationsMap.updateOwners()
				}

				change[Nation::capital]?.let {
					data.capital = it.oid()
				}

				change[Nation::color]?.let {
					data.color = it.int()
					NationsMap.updateOwners()
				}
			}
		}

		Nation.watchDeletes { change ->
			synced {
				val id: Oid<Nation> = change.oid

				val data = NATION_DATA[id] ?: error("$id wasn't cached")

				NATION_DATA.remove(id)
				nameCache.remove(data.name)
			}
		}
	}

	fun all(): List<NationData> = NATION_DATA.values.toList()

	operator fun get(nationId: Oid<Nation>): NationData = NATION_DATA[nationId]
		?: error("$nationId is not cached")

	fun getByName(name: String): Oid<Nation>? = nameCache[name]
}
