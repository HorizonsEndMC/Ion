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
import org.litote.kmongo.or
import java.util.Date

/**
 * @param name The name of this war. Can be renamed via staff command.
 *
 * @param startTime The time the war was initiated
 *
 * @param points The current score of the war. Defensive victories decrease the score, offensive victories increase the score.
 *
 * @param result The end result of the war. Null if it is active.
 **/
data class War(
	override val _id: Oid<War>,

	val name: String,

	val aggressor: Oid<Nation>,
	val aggressorGoal: WarGoal,

	val defender: Oid<Nation>,
	val defenderGoal: WarGoal = Humiliate,
	val defenderHasSetGoal: Boolean = false,

	val startTime: Date = Date(System.currentTimeMillis()),

	val points: Int = 0,

	val result: Result? = null
) : DbObject {
	companion object : OidDbObjectCompanion<War>(War::class, setup = {
		ensureIndex(War::aggressor)
		ensureIndex(War::defender)
		ensureIndex(War::startTime)
	}) {
		fun strictQuery(aggressor: Oid<Nation>, defender: Oid<Nation>) = and(War::aggressor eq aggressor, War::defender eq defender)
		fun participantQuery(nationOne: Oid<Nation>, nationTwo: Oid<Nation>) = or(strictQuery(nationOne, nationTwo), strictQuery(nationTwo, nationOne))

		fun activeQuery(aggressor: Oid<Nation>, defender: Oid<Nation>) = and(participantQuery(aggressor, defender), War::result eq null)

		fun create(aggressor: Oid<Nation>, defender: Oid<Nation>, goal: WarGoal, name: String): Oid<War> = trx { session ->
			require(none(session, activeQuery(aggressor, defender))) // Require that there is not an existing active war between the two nations
//			require(!isTruce(aggressor, defender)) // Cannot create a war if there is an enforced peace TODO

			val id = objId<War>()

			col.insertOne(
				session,
				War(
					_id = id,
					name = name,
					aggressor = aggressor,
					defender = defender,
					aggressorGoal= goal
				)
			)

			return@trx id
		}

		/**
		 * Ends the specified war
		 **/
		fun end(id: Oid<War>, aggressorVictory: Boolean) = trx {

		}

		fun addPoints(war: Oid<War>, points: Int) {
			require(points > 0)

			updateById(war, inc(War::points, points))
		}

		fun subtractPoints(war: Oid<War>, points: Int) {
			require(points > 0)

			updateById(war, inc(War::points, -points))
		}
	}

	enum class Result(val displayName: String) {
		AGGRESSOR_VICTORY("Aggressor Victory"),
		DEFENDER_VICTORY("Defender Victory"),
		WHITE_PEACE("White Peace"),
	}
}
