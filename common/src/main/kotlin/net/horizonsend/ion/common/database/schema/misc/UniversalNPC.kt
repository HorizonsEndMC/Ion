package net.horizonsend.ion.common.database.schema.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import java.util.UUID

class UniversalNPC(
	override val _id: Oid<UniversalNPC>,

	var worldKey: String,
	var x: Double,
	var y: Double,
	var z: Double,

	var skinData: ByteArray,
	var typeId: String,

	val npcID: UUID,
	var jsonMetadata: String
) : DbObject {
	companion object : OidDbObjectCompanion<UniversalNPC>(
		UniversalNPC::class,
		setup = {
			ensureIndex(UniversalNPC::worldKey)
			ensureIndex(UniversalNPC::typeId)
		}
	) {
		fun create(
			worldKey: String,
			x: Double,
			y: Double,
			z: Double,
			skinData: ByteArray,
			type: String,
			jsonMetadata: String = "{}"
		): Oid<UniversalNPC> {
			val id = objId<UniversalNPC>()
			col.insertOne(UniversalNPC(id, worldKey, x, y, z, skinData, type, UUID.randomUUID(), jsonMetadata))
			return id
		}

		fun delete(npcId: Oid<UniversalNPC>) {
			col.deleteOneById(npcId)
		}
	}
}
