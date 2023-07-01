package net.horizonsend.ion.server.database.schema.nations.territories

import com.mongodb.client.MongoCollection
import com.mongodb.client.result.InsertOneResult
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.trx
import org.bson.conversions.Bson
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class AbstractTerritoryCompanion<T: TerritoryInterface>(
	clazz: KClass<T>,
	val nameProperty: KProperty<String>,
	val worldKProperty: KProperty<String>,
	val polygonDataProperty: KProperty<ByteArray>,
	val nationProperty: KProperty<Oid<Nation>?>,
	setup: MongoCollection<T>.() -> Unit = {}
) : OidDbObjectCompanion<T>(
	clazz,
	setup = {
		ensureUniqueIndexCaseInsensitive(nameProperty)
		ensureUniqueIndex(worldKProperty, polygonDataProperty)
		setup()
	}
) {
	abstract val unclaimedQuery: Bson

	abstract fun new(id: Oid<T>, name: String, world: String, polygonData: ByteArray): T

	fun create(name: String, world: String, polygonData: ByteArray): InsertOneResult = trx { session ->
		val id = objId<T>()

		col.insertOne(
			session,
			new(id, name, world, polygonData)
		)
	}

	fun setPolygonData(id: Oid<T>, polygonData: ByteArray) = trx { sess ->
		col.updateOneById(sess, id, setValue(polygonDataProperty, polygonData))
	}

	fun findByName(name: String): T? = trx { sess ->
		col.findOne(sess, Territory::name eq name)
	}

	fun setNation(id: Oid<Territory>, nation: Oid<Nation>?): Unit = trx { sess ->
		if (nation != null) {
			require(Territory.matches(sess, id, unclaimedQuery))
			require(Nation.exists(sess, nation))
		}
		Territory.updateById(sess, id, setValue(nationProperty, nation))
	}
}

interface TerritoryInterface: DbObject {
	val world: String
	val polygonData: ByteArray
}
