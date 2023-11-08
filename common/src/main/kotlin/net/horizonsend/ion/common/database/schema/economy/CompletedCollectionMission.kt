package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * A collected item that has been claimed
 *
 **/
class CompletedCollectionMission(
	override val _id: Oid<CompletedCollectionMission>,

	val player: SLPlayerId,
	val claimTime: Date,

	val profit: Double,
	val station: Oid<EcoStation>,
	val item: Oid<CollectedItem>
) : DbObject {
	companion object : OidDbObjectCompanion<CompletedCollectionMission>(CompletedCollectionMission::class, setup = {
		ensureIndex(CompletedCollectionMission::player)
		ensureIndex(CompletedCollectionMission::station)
	}) {
		fun create(
			player: SLPlayerId,
			claimed: Date,
			profit: Double,
			station: Oid<EcoStation>,
			item: Oid<CollectedItem>
		): Oid<CompletedCollectionMission> {
			val id = objId<CompletedCollectionMission>()
			col.insertOne(
				CompletedCollectionMission(id, player, claimed, profit, station, item)
			)
			return id
		}

		fun hasSoldTo(player: SLPlayerId, ecoStation: Oid<EcoStation>, timeLimitHours: Long): Boolean {
			val cutoffTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(timeLimitHours))
			return !none(
				and(
					CompletedCollectionMission::player eq player,
					CompletedCollectionMission::station eq ecoStation,
					CompletedCollectionMission::claimTime gte cutoffTime
				)
			)
		}

		fun profitIn(player: SLPlayerId, ecoStation: Oid<EcoStation>, timeLimitHours: Long): Double {
			val cutoffTime = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(timeLimitHours))

			val all = find(
				and(
					CompletedCollectionMission::player eq player,
					CompletedCollectionMission::station eq ecoStation,
					CompletedCollectionMission::claimTime gte cutoffTime
				)
			)

			return all.sumByDouble { it.profit }
		}
	}
}
