package net.horizonsend.ion.common.database.schema.misc

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
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
		): Oid<UniversalNPC> = trx { sess ->
			val id = objId<UniversalNPC>()
			col.insertOne(sess, UniversalNPC(id, worldKey, x, y, z, skinData, type, UUID.randomUUID(), jsonMetadata))
			id
		}

		fun delete(npcId: Oid<UniversalNPC>) = trx { sess ->
			col.deleteOneById(sess, npcId)
		}

		fun updateMetaData(npcId: Oid<UniversalNPC>, new: String) {
			col.updateOneById(npcId, setValue(UniversalNPC::jsonMetadata, new))
		}

		fun updateSkinData(npcId: Oid<UniversalNPC>, new: ByteArray) {
			col.updateOneById(npcId, setValue(UniversalNPC::skinData, new))
		}
	}
}
