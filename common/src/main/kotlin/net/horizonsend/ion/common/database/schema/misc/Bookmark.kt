package net.horizonsend.ion.common.database.schema.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class Bookmark(
    override val _id: Oid<Bookmark>,
    val name: String,

    val x: Int,
    val y: Int,
    val z: Int,

    val serverName: String = "Survival",
    val worldName: String,
    val owner: SLPlayerId
) : DbObject {
    companion object : OidDbObjectCompanion<Bookmark>(Bookmark::class, setup = {
        ensureIndex(Bookmark::owner)
        ensureIndex(Bookmark::name)
        ensureUniqueIndex(Bookmark::owner, Bookmark::name)
    }) {
        operator fun get(location: DBVec3i, worldName: String) = col.findOne(
            and(Bookmark::x eq location.x, Bookmark::y eq location.y, Bookmark::z eq location.z, Bookmark::worldName eq worldName)
        )

        fun delete(id: Oid<Bookmark>) = trx { sess ->
            col.deleteOneById(sess, id)
        }

        fun create(name: String, owner: SLPlayerId, position: DBVec3i, serverName: String, worldName: String): Oid<Bookmark> = trx { sess ->
            val id = objId<Bookmark>()

            col.insertOne(
                sess,
                Bookmark(id, name, position.x, position.y, position.z, serverName, worldName, owner)
            )

            return@trx id
        }
    }
}