package net.horizonsend.ion.common.database.schema

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.time.Instant
import java.util.Date

/**
 * Represents a cryopod multiblock
 *
 *
 *
 * @param lastSelectedAt The time this cryopod was last selected.
 * 	Cryopods will be accessed in descending order of selection.
 **/
data class Cryopod(
    override val _id: Oid<Cryopod>,

    val x: Int,
    val y: Int,
    val z: Int,

	val serverName: String = "Survival",
    val worldName: String,
    val owner: SLPlayerId,

	val lastSelectedAt: Date = Date.from(Instant.now())
) : DbObject {
	companion object : OidDbObjectCompanion<Cryopod>(Cryopod::class, setup = {
		ensureIndex(Cryopod::owner)
		ensureIndex(Cryopod::lastSelectedAt)
	}) {
		operator fun get(location: DBVec3i, worldName: String) = col.findOne(
			and(Cryopod::x eq  location.x, Cryopod::y eq location.y, Cryopod::z eq location.z, Cryopod::worldName eq worldName)
		)

		fun delete(id: Oid<Cryopod>) = trx { session ->
			col.deleteOneById(session, id)
		}

		fun create(owner: SLPlayerId, position: DBVec3i, serverName: String, worldName: String): Oid<Cryopod> = trx { session ->
			if (!none(session, and(Cryopod::x eq  position.x, Cryopod::y eq position.y, Cryopod::z eq position.z, Cryopod::worldName eq worldName))) {
				Cryopod[position, worldName]?._id?.let { col.deleteOneById(it) }
			}

			val id = objId<Cryopod>()

			col.insertOne(
				session,
				Cryopod(id, position.x, position.y, position.z, serverName, worldName, owner)
			)

			return@trx id
		}

		fun delete(location: DBVec3i, worldName: String) = Cryopod[location, worldName]?._id?.let { delete(it) }
	}

	fun vec3i(): DBVec3i = DBVec3i(x, y, z)
}
