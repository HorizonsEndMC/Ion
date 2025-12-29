package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.kyori.adventure.text.format.TextColor
import java.util.concurrent.ConcurrentHashMap

object FrontierNationCache : ManualCache() {
	data class FrontierNationData(
		val id: Oid<FrontierNation>,
		var name: String,
		var leader: SLPlayerId,
		var color: Int,
		var world: String,
		var x: Int,
		var z: Int,
		var radius: Int,
		var invites: Set<SLPlayerId>
	) {
		val textColor: TextColor get() = TextColor.color(color)

		fun setNewLocation(newWorld: String, newX: Int, newZ: Int) {
			FrontierNation.setLocation(id, newWorld, newX, newZ)
		}

		fun setNewRadius(newRadius: Int) {
			FrontierNation.setRadius(id, newRadius)
		}
	}

	private val FRONTIER_NATION_DATA = ConcurrentHashMap<Oid<FrontierNation>, FrontierNationData>()
	private val nameCache = ConcurrentHashMap<String, Oid<FrontierNation>>()

	override fun load() {
		FRONTIER_NATION_DATA.clear()

		fun cache(frontierNation: FrontierNation) {
			val id: Oid<FrontierNation> = frontierNation._id
			val data = FrontierNationData(
				id,
				frontierNation.name,
				frontierNation.leader,
				frontierNation.color,
				frontierNation.world,
				frontierNation.x,
				frontierNation.z,
				frontierNation.radius,
				frontierNation.invites
			)
			FRONTIER_NATION_DATA[id] = data
			nameCache[data.name] = id
		}

		for (frontierNation in FrontierNation.all()) {
			cache(frontierNation)
		}

		FrontierNation.watchInserts { change ->
			change.fullDocument?.let(::cache)
		}

		FrontierNation.watchUpdates { change ->
			val id: Oid<FrontierNation> = change.oid

			val data = FRONTIER_NATION_DATA[id] ?: error("$id wasn't cached")

			change[FrontierNation::name]?.let {
				nameCache.remove(data.name)
				data.name = it.string()
				nameCache[data.name] = id
			}

			change[FrontierNation::leader]?.let {
				data.leader = it.slPlayerId()
			}

			change[FrontierNation::color]?.let {
				data.color = it.int()
			}

			change[FrontierNation::world]?.let {
				data.world = it.string()
			}

			change[FrontierNation::x]?.let {
				data.x = it.int()
			}

			change[FrontierNation::z]?.let {
				data.z = it.int()
			}

			change[FrontierNation::radius]?.let {
				data.radius = it.int()
			}

			change[FrontierNation::invites]?.let {
				data.invites = it.mappedSet { id -> id.slPlayerId() }
			}
		}

		FrontierNation.watchDeletes { change ->
			val id: Oid<FrontierNation> = change.oid

			val data = FRONTIER_NATION_DATA[id] ?: error("$id wasn't cached")

			FRONTIER_NATION_DATA.remove(id)
			nameCache.remove(data.name)
		}
	}

	fun all(): List<FrontierNationData> = FRONTIER_NATION_DATA.values.toList()

	operator fun get(frontierNationId: Oid<FrontierNation>): FrontierNationData = FRONTIER_NATION_DATA[frontierNationId]
		?: error("$frontierNationId wasn't cached")

	fun getByName(name: String): Oid<FrontierNation>? = nameCache[name]
}
