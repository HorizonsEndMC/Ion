package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.string
import net.kyori.adventure.text.format.TextColor
import java.util.concurrent.ConcurrentHashMap

object NationCache : ManualCache() {
	data class NationData(
		val id: Oid<Nation>,
		var name: String,
		var capital: Oid<Settlement>,
		var color: Int,
		var invites: Set<Oid<Settlement>>
	) {
		val leader get() = SettlementCache[capital].leader

		val textColor: TextColor get() = TextColor.color(color)
	}

	private val NATION_DATA = ConcurrentHashMap<Oid<Nation>, NationData>()
	private val nameCache = ConcurrentHashMap<String, Oid<Nation>>()

	override fun load() {
		NATION_DATA.clear()

		fun cache(nation: Nation) {
			val id: Oid<Nation> = nation._id
			val data = NationData(id, nation.name, nation.capital, nation.color, nation.invites)
			NATION_DATA[id] = data
			nameCache[data.name] = id
		}

		for (nation in Nation.all()) {
			cache(nation)
		}

		Nation.watchInserts { change ->
				change.fullDocument?.let(::cache)
		}

		Nation.watchUpdates { change ->
			val id: Oid<Nation> = change.oid

			val data = NATION_DATA[id] ?: error("$id wasn't cached")

			change[Nation::name]?.let {
				nameCache.remove(data.name)
				data.name = it.string()
				nameCache[data.name] = id
			}

			change[Nation::capital]?.let {
				data.capital = it.oid()
			}

			change[Nation::color]?.let {
				data.color = it.int()
			}

			change[Nation::invites]?.let {
				data.invites = it.mappedSet { id -> id.oid() }
			}
		}

		Nation.watchDeletes { change ->
			val id: Oid<Nation> = change.oid

			val data = NATION_DATA[id] ?: error("$id wasn't cached")

			NATION_DATA.remove(id)
			nameCache.remove(data.name)
		}
	}

	fun all(): List<NationData> = NATION_DATA.values.toList()

	operator fun get(nationId: Oid<Nation>): NationData = NATION_DATA[nationId]
		?: error("$nationId is not cached")

	fun getByName(name: String): Oid<Nation>? = nameCache[name]
}
