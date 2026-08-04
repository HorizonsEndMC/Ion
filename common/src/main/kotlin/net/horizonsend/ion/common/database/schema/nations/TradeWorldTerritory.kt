package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

data class TradeWorldTerritory(
	override val _id: Oid<TradeWorldTerritory> = objId(),
	var world: String,
	var name: String,
	var color: Int,
	var backingTerritory: Oid<Territory>,
) : DbObject {
	companion object : OidDbObjectCompanion<TradeWorldTerritory>(TradeWorldTerritory::class, setup = {
		ensureUniqueIndex(TradeWorldTerritory::world)
		ensureUniqueIndex(TradeWorldTerritory::name)
		ensureUniqueIndex(TradeWorldTerritory::backingTerritory)
	}) {
		fun create(world: String, name: String, color: Int, backingTerritory: Oid<Territory>): Oid<TradeWorldTerritory> = trx { sess ->
			val id = objId<TradeWorldTerritory>()
			col.insertOne(sess, TradeWorldTerritory(
				_id = id,
				world = world,
				name = name,
				color = color,
				backingTerritory = backingTerritory,
			))
			return@trx id
		}

		fun findByWorld(world: String): TradeWorldTerritory? = trx { sess ->
			col.findOne(sess, TradeWorldTerritory::world eq world)
		}
	}
}
