package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.war.Truce
import net.horizonsend.ion.common.database.schema.nations.war.War
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

object TruceCache : ManualCache() {
	data class TruceData(
		val id: Oid<Truce>,

		val war: Oid<War>,

		val victor: Oid<Nation>,
		val defeated: Oid<Nation>,

		val endDate: Date
	)

	override fun load() {
		truceData.clear()

		fun cache(truce: Truce) {
			val id: Oid<Truce> = truce._id

			val data = TruceData(
				id = id,
				war = truce.war,
				victor = truce.victor,
				defeated = truce.defeated,
				endDate = truce.getTruceEndDate()
			)

			truceData[id] = data
		}

		for (nation in Truce.all()) {
			cache(nation)
		}

		Truce.watchInserts { change ->
			change.fullDocument?.let(::cache)
		}
	}

	private val truceData = ConcurrentHashMap<Oid<Truce>, TruceData>()
}
