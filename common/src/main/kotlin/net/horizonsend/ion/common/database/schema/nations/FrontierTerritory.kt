package net.horizonsend.ion.common.database.schema.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.trx
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

data class FrontierTerritory(
	override val _id: Oid<FrontierTerritory> = objId(),
	var name: String,
	var world: String,
	var polygonData: ByteArray,
	var frontierNation: Oid<FrontierNation>? = null,
	var alias: String? = null,
	var isCapital: Boolean = false
) : DbObject {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FrontierTerritory

		if (_id != other._id) return false
		if (name != other.name) return false
		if (world != other.world) return false
		if (!polygonData.contentEquals(other.polygonData)) return false
		if (frontierNation != other.frontierNation) return false

		return true
	}

	override fun hashCode(): Int {
		return _id.hashCode()
	}

	companion object : OidDbObjectCompanion<FrontierTerritory>(FrontierTerritory::class, setup = {
		ensureUniqueIndexCaseInsensitive(FrontierTerritory::name)
		ensureUniqueIndex(FrontierTerritory::world, FrontierTerritory::polygonData)
		ensureIndex(FrontierTerritory::frontierNation)
	}) {
		fun setFrontierNation(id: Oid<FrontierTerritory>, nation: Oid<FrontierNation>?): Unit = trx { sess ->
			if (nation != null) {
				require(matches(sess, id, unclaimedQuery))
				require(FrontierNation.exists(sess, nation))
			}

			if (nation == null) {
				updateById(sess, id, org.litote.kmongo.setValue(FrontierTerritory::alias, null))
				updateById(sess, id, org.litote.kmongo.setValue(FrontierTerritory::isCapital, null))
			}

			updateById(sess, id, org.litote.kmongo.setValue(FrontierTerritory::frontierNation, nation))
		}

		fun create(name: String, world: String, polygonData: ByteArray): Oid<FrontierTerritory> = trx { sess ->
			val id = objId<FrontierTerritory>()
			col.insertOne(sess, FrontierTerritory(
				_id = id,
				name = name,
				world = world,
				polygonData = polygonData
			))
			return@trx id
		}

		fun findByName(name: String): FrontierTerritory? = trx { sess ->
			col.findOne(sess, FrontierTerritory::name eq name)
		}

		fun setPolygonData(id: Oid<FrontierTerritory>, polygonData: ByteArray) = trx { sess ->
			updateById(sess, id, org.litote.kmongo.setValue(FrontierTerritory::polygonData, polygonData))
		}

		val unclaimedQuery = FrontierTerritory::frontierNation eq null
	}
}
