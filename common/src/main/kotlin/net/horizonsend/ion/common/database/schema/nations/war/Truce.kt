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
import org.litote.kmongo.or
import java.util.Date

class Truce(
	override val _id: Oid<Truce>,

	val war: Oid<War>,

	val victor: Oid<Nation>,
	val defeated: Oid<Nation>,

	val enforcedGoal: WarGoal,
	val startTime: Date,
) : DbObject {
	companion object : OidDbObjectCompanion<Truce>(Truce::class, setup = {
		ensureIndex(Truce::victor)
		ensureIndex(Truce::defeated)
		ensureIndex(Truce::startTime)
	}) {
		fun participantQuery(victor: Oid<Nation>, defeated: Oid<Nation>) = and(Truce::victor eq victor, Truce::defeated eq defeated)
		fun nationQuery(nationOne: Oid<Nation>, nationTwo: Oid<Nation>) = or(participantQuery(nationOne, nationTwo), participantQuery(nationTwo, nationOne))

		fun create(victor: Oid<Nation>, defeated: Oid<Nation>, war: Oid<War>, goal: WarGoal): Oid<Truce> = trx { session ->
			//TODO checks

			val id = objId<Truce>()

			col.insertOne(session,
				Truce(
					_id = id,
					war = war,
					victor = victor,
					defeated = defeated,
					enforcedGoal = goal,
					startTime = Date(System.currentTimeMillis())
				)
			)

			return@trx id
		}
	}

	fun getTruceEndDate(): Date = enforcedGoal.getTruceEndDate(startTime)
}
