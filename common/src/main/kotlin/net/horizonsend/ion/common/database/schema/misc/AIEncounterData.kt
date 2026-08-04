package net.horizonsend.ion.common.database.schema.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.combine
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.time.Duration

class AIEncounterData(
	override val _id: Oid<AIEncounterData>,
	val name: String,

	var lastActiveTime : Long,
	var lastDuration: Long,
	var lastSeparation: Long
) : DbObject{
	companion object : OidDbObjectCompanion<AIEncounterData>(AIEncounterData::class, setup = {
		ensureIndex(AIEncounterData::name)
	}) {

		fun create(name: String,lastActiveTime : Long,lastDuration: Long, lastSeparation: Long): Oid<AIEncounterData> = trx { sess ->
			val id = objId<AIEncounterData>()

			AIEncounterData.col.insertOne(
				sess,
				AIEncounterData(id, name, lastActiveTime, lastDuration, lastSeparation)
			)

			return@trx id
		}

		fun saveData(
			id : Oid<AIEncounterData>,
			lastActiveTime: Long,
			lastDuration: Long,
			lastSeparation: Long) {
			col.updateOneById(id, combine(
				setValue(AIEncounterData::lastActiveTime, lastActiveTime),
				setValue(AIEncounterData::lastDuration, lastDuration),
				setValue(AIEncounterData::lastSeparation, lastSeparation)
			))
		}
	}
}
