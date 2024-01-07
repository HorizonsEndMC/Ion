package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
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

		val endDate: Date,

		val partyPlayers: Set<SLPlayerId>,
		val partySettlements: Set<Oid<Settlement>>
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
				endDate = truce.getTruceEndDate(),
				partyPlayers = truce.partyPlayers,
				partySettlements = truce.partySettlements
			)

			truceData[id] = data
		}

		for (nation in Truce.all()) {
			cache(nation)
		}

		Truce.watchInserts { change ->
			change.fullDocument?.let(::cache)
		}

		Truce.watchDeletes { change ->
			val truce = change.fullDocument ?: return@watchDeletes

			truceData.remove(truce._id)
		}
	}

	private val truceData = ConcurrentHashMap<Oid<Truce>, TruceData>()

	operator fun get(id: Oid<Truce>): TruceData? = truceData[id]

	fun getByNation(id: Oid<Nation>): Iterable<TruceData> = truceData.values.filter { (it.defeated == id || it.victor == id) && it.endDate > Date(System.currentTimeMillis()) }

	// Measures against abuse
	fun getBySettlement(id: Oid<Settlement>): Iterable<TruceData> = truceData.values.filter { (it.partySettlements.contains(id)) && it.endDate > Date(System.currentTimeMillis()) }
	fun getByPlayer(id: SLPlayerId): Iterable<TruceData> = truceData.values.filter { (it.partyPlayers.contains(id)) && it.endDate > Date(System.currentTimeMillis()) }
}
