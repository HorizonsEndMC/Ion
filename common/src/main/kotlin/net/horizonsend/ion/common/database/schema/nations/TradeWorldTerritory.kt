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
	var centerX: Int = 0,
	var centerZ: Int = 0
) : DbObject {
	companion object : OidDbObjectCompanion<TradeWorldTerritory>(TradeWorldTerritory::class, setup = {
		ensureUniqueIndex(TradeWorldTerritory::world)
		ensureUniqueIndex(TradeWorldTerritory::name)
		ensureUniqueIndex(TradeWorldTerritory::backingTerritory)
	}) {
		fun create(world: String, name: String, color: Int, backingTerritory: Oid<Territory>, centerX: Int = 0, centerZ: Int = 0): Oid<TradeWorldTerritory> = trx { sess ->
			val id = objId<TradeWorldTerritory>()
			col.insertOne(sess, TradeWorldTerritory(
				_id = id,
				world = world,
				name = name,
				color = color,
				backingTerritory = backingTerritory,
				centerX = centerX,
				centerZ = centerZ
			))
			return@trx id
		}

		fun setCenterX(id: Oid<TradeWorldTerritory>, x: Int) {
			updateById(id, org.litote.kmongo.setValue(TradeWorldTerritory::centerX, x))
		}

		fun setCenterZ(id: Oid<TradeWorldTerritory>, z: Int) {
			updateById(id, org.litote.kmongo.setValue(TradeWorldTerritory::centerZ, z))
		}

		fun findByWorld(world: String): TradeWorldTerritory? = trx { sess ->
			col.findOne(sess, TradeWorldTerritory::world eq world)
		}
	}
}
