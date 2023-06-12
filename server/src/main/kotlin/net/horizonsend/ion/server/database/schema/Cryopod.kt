package net.horizonsend.ion.server.database.schema

import net.minecraft.core.Vec3i
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.schema.misc.SLPlayer
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

data class Cryopod(
	override val _id: Oid<Cryopod>,
	val x: Int,
	val y: Int,
	val z: Int,
	val owner: SLPlayer,
	val active: Boolean
) : DbObject {

	companion object : OidDbObjectCompanion<Cryopod>(Cryopod::class, setup = {
		ensureIndex(Cryopod::owner)
	}) {
		operator fun get(location: Vec3i) = col.findOne(and(Cryopod::x eq  location.x, Cryopod::y eq location.y, Cryopod::z eq location.z))

		fun delete(id: Oid<Cryopod>) {
			col.deleteOneById(id)
		}

		fun delete(location: Vec3i) = Cryopod[location]?._id?.let { delete(it) }
	}
}
