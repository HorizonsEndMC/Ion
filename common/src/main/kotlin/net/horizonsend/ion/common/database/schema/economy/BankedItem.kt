package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex

data class BankedItem(
	override val _id: Oid<BankedItem>,
	val frontierNation: Oid<FrontierNation>,
	var itemString: String,
	var quantity: Int
) : DbObject {
	companion object : OidDbObjectCompanion<BankedItem>(BankedItem::class, setup = {
		ensureIndex(BankedItem::frontierNation)
		ensureIndex(BankedItem::itemString)
	}) {
		fun create(frontierNation: Oid<FrontierNation>, itemString: String, quantity: Int): Oid<BankedItem> = trx { sess ->
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
