package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.war.War
import net.horizonsend.ion.common.database.schema.nations.war.WarGoal
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

object WarCache : ManualCache() {
	data class WarData(
		val id: Oid<War>,

		val name: String,

		val aggressor: Oid<Nation>,
		val aggressorGoal: WarGoal,

		val defender: Oid<Nation>,
		var defenderGoal: WarGoal,
		var defenderHasSetGoal: Boolean = false,

		val startDate: Date,

		var points: Int,

		var result: War.Result?,
	)

	private val warData = ConcurrentHashMap<Oid<War>, WarData>()

	override fun load() {
		warData.clear()

		fun cache(war: War) {
			val id: Oid<War> = war._id

			val data = WarData(
				id = id,
				name = war.name,
				aggressor = war.aggressor,
				aggressorGoal = war.aggressorGoal,
				defender = war.defender,
				defenderGoal = war.defenderGoal,
				startDate = war.startTime,
				points = war.points,
				result = war.result
			)

			warData[id] = data
		}

		for (nation in War.all()) {
			cache(nation)
		}

		War.watchInserts { change ->
			change.fullDocument?.let(::cache)
		}

		War.watchUpdates { change ->
			val id: Oid<War> = change.oid

			val data = warData[id] ?: error("$id wasn't cached")

			change[War::points]?.let {
				data.points = it.int()
			}

			change[War::defenderGoal]?.let {
				data.defenderGoal = it.enumValue()
			}

			change[War::defenderHasSetGoal]?.let {
				data.defenderHasSetGoal = it.boolean()
			}

			change[War::result]?.let {
				data.result	= it.enumValue<War.Result>()
			}
		}

		War.watchDeletes { change ->
			val id: Oid<War> = change.oid

			warData[id] ?: error("$id wasn't cached")
			warData.remove(id)
		}
	}

	fun allActive(): Collection<WarData> = warData.values.filter { it.result == null }

	fun all(): Collection<WarData> = warData.values

	operator fun get(id: Oid<War>): WarData = warData[id] ?: error("War $id wasn't cached!")
	operator fun get(id: Oid<Nation>): Collection<WarData> = all().filter { it.defender == id || it.aggressor == id }
	fun getActive(id: Oid<Nation>): Collection<WarData> = allActive().filter { it.defender == id || it.aggressor == id }
	fun getActiveDefending(id: Oid<Nation>): Collection<WarData> = allActive().filter { it.defender == id }
	fun getActiveAggressor(id: Oid<Nation>): Collection<WarData> = allActive().filter { it.aggressor == id }
}
