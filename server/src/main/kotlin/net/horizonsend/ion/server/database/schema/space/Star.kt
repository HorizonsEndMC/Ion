package net.horizonsend.ion.server.database.schema.space

import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.objId
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.set
import org.litote.kmongo.setTo

data class Star(
    override val _id: Oid<Star> = objId(),
    var name: String,
    var spaceWorld: String,
    var x: Int,
    var y: Int,
    var z: Int,
    var size: Double,
    var material: String
) : DbObject {
	companion object : OidDbObjectCompanion<Star>(Star::class, setup = {
		ensureUniqueIndex(Star::name)
	}) {
		fun create(name: String, spaceWorld: String, x: Int, y: Int, z: Int, mat: String, size: Double): Oid<Star> {
			val id = objId<Star>()
			col.insertOne(Star(id, name, spaceWorld, x, y, z, size, mat))
			return id
		}

		fun setPos(id: Oid<Star>, spaceWorld: String, x: Int, y: Int, z: Int) {
			updateById(id, set(Star::spaceWorld setTo spaceWorld, Star::x setTo x, Star::y setTo y, Star::z setTo z))
		}
	}
}
