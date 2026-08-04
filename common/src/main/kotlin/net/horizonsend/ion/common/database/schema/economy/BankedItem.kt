package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex

data class BankedItem(
	override val _id: Oid<BankedItem>,
	val nation: Oid<Nation>,
	var itemString: String,
	var quantity: Int
) : DbObject {
	companion object : OidDbObjectCompanion<BankedItem>(BankedItem::class, setup = {
		ensureIndex(BankedItem::nation)
		ensureIndex(BankedItem::itemString)
	}) {
		fun create(frontierNation: Oid<Nation>, itemString: String, quantity: Int): Oid<BankedItem> = trx { sess ->
			val id = objId<BankedItem>()
			val item = BankedItem(id, frontierNation, itemString, quantity)
			col.insertOne(sess, item)
			return@trx id
		}

		fun delete(id: Oid<BankedItem>) = trx { sess ->
			col.deleteOneById(sess, id)
		}
	}
}
