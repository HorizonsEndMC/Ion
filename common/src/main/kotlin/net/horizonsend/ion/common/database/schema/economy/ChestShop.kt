package net.horizonsend.ion.common.database.schema.economy

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.utils.DBVec3i
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById

data class ChestShop(
	override val _id: Oid<ChestShop>,
	var owner: SLPlayerId,
	var world: String,
	var location: DBVec3i,
	var soldItem: String?,
	var price: Double,
	var selling: Boolean,
) : DbObject {
	companion object : OidDbObjectCompanion<ChestShop>(ChestShop::class, setup = {
		ensureIndex(ChestShop::owner)
		ensureIndex(ChestShop::location)
		ensureUniqueIndex(ChestShop::location, ChestShop::owner)
	}) {
		fun create(owner: SLPlayerId, location: DBVec3i, world: String, soldItem: String?, price: Double, selling: Boolean): Oid<ChestShop> = trx { session ->
			val id = objId<ChestShop>()
			col.insertOne(session, ChestShop(id, owner, world, location, soldItem, price, selling))

			return@trx id
		}

		fun setItem(shopId: Oid<ChestShop>, item: String) {
			col.updateOneById(shopId, setValue(ChestShop::soldItem, item))
		}

		fun delete(shopId: Oid<ChestShop>) = trx { session ->
			col.deleteOneById(session, shopId)
		}
	}
}
