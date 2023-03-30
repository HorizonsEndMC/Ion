package net.starlegacy.database.schema.economy

import com.mongodb.client.FindIterable
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.objId
import net.starlegacy.database.schema.nations.Territory
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq

data class CityNPC(
	override val _id: Oid<CityNPC> = objId(),
	var territory: Oid<Territory>,
	var x: Double,
	var y: Double,
	var z: Double,
	var skinData: ByteArray,
	var type: Type
) : DbObject {
	companion object : OidDbObjectCompanion<CityNPC>(CityNPC::class, setup = {
		ensureIndex(CityNPC::territory)
	}) {
		fun create(
			territory: Oid<Territory>,
			x: Double,
			y: Double,
			z: Double,
			skinData: ByteArray,
			type: Type
		): Oid<CityNPC> {
			val id = objId<CityNPC>()
			col.insertOne(CityNPC(id, territory, x, y, z, skinData, type))
			return id
		}

		fun delete(npcId: Oid<CityNPC>) {
			col.deleteOneById(npcId)
		}

		fun deleteAt(territory: Oid<Territory>) {
			col.deleteMany(CityNPC::territory eq territory)
		}

		fun findAt(territory: Oid<Territory>): FindIterable<CityNPC> = col.find(CityNPC::territory eq territory)
	}

	enum class Type(val displayName: String) {
		IMPORTER("Importer"), EXPORTER("Exporter"), BAZAAR("Bazaar"),
		MERCHANT("Merchant")
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as CityNPC

		if (_id != other._id) return false
		if (territory != other.territory) return false
		if (x != other.x) return false
		if (y != other.y) return false
		if (z != other.z) return false
		if (!skinData.contentEquals(other.skinData)) return false
		if (type != other.type) return false

		return true
	}

	override fun hashCode(): Int {
		return _id.hashCode()
	}
}
