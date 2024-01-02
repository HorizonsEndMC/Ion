package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.document
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

		val name: String?,

		val aggressor: Oid<Nation>,
		val aggressorGoal: WarGoal,
		val defender: Oid<Nation>,
		var defenderGoal: WarGoal,

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
				data.defenderGoal = it.document()
			}

			change[War::result]?.let {
				data.result	 = it.enumValue<War.Result>()
			}
		}

		War.watchDeletes { change ->
			val id: Oid<War> = change.oid

			warData[id] ?: error("$id wasn't cached")
			warData.remove(id)
		}
	}

	fun findActive() {

	}
}
