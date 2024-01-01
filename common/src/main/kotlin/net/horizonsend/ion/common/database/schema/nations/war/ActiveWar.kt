package net.horizonsend.ion.common.database.schema.nations.war

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.and
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import java.util.Date

/**
 *
 *
 * @param result The end result of the war. Null if it is active.
 **/
data class ActiveWar(
	override val _id: Oid<ActiveWar>,

	val name: String? = null,

	val aggressor: Oid<Nation>,
	val defender: Oid<Nation>,

	val startTime: Date = Date(System.currentTimeMillis()),

	val goal: WarGoal,
	val points: Int = 0,

	val result: Result? = null
) : DbObject {
	companion object : OidDbObjectCompanion<ActiveWar>(ActiveWar::class, setup = {
		ensureIndex(ActiveWar::aggressor)
		ensureIndex(ActiveWar::defender)
		ensureIndex(ActiveWar::startTime)
	}) {
		fun participantQuery(aggressor: Oid<Nation>, defender: Oid<Nation>) = and(ActiveWar::aggressor eq aggressor, ActiveWar::defender eq defender)
		fun activeQuery(aggressor: Oid<Nation>, defender: Oid<Nation>) = and(participantQuery(aggressor, defender), ActiveWar::result eq null)

		fun create(aggressor: Oid<Nation>, defender: Oid<Nation>, goal: WarGoal): Oid<ActiveWar> = trx { session ->
			require(none(session, activeQuery(aggressor, defender))) // Require that there is not an existing active war between the two nations
//			require(!isTruce(aggressor, defender)) // Cannot create a war if there is an enforced peace TODO

			val id = objId<ActiveWar>()

			col.insertOne(
				session,
				ActiveWar(
					_id = id,
					aggressor = aggressor,
					defender = defender,
					goal= goal
				)
			)

			return@trx id
		}

		/**
		 * Ends the specified war
		 **/
		fun end(id: Oid<ActiveWar>, aggressorVictory: Boolean) = trx {

		}

		fun addPoints(activeWar: Oid<ActiveWar>, points: Int) {
			require(points > 0)

			updateById(activeWar, inc(ActiveWar::points, points))
		}

		fun subtractPoints(activeWar: Oid<ActiveWar>, points: Int) {
			require(points > 0)

			updateById(activeWar, inc(ActiveWar::points, -points))
		}
	}

	enum class Result(val displayName: String) {
		AGGRESSOR_VICTORY("Aggressor Victory"),
		DEFENDER_VICTORY("Defender Victory"),
		WHITE_PEACE("White Peace"),
	}
}
