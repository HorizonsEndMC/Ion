package net.horizonsend.ion.server.database.schema.nations

import com.mongodb.client.MongoCollection
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.OidDbObjectCompanion
import net.horizonsend.ion.server.database.ensureUniqueIndexCaseInsensitive
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.trx
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
	setup: MongoCollection<T>.() -> Unit = {}
) : OidDbObjectCompanion<T>(
	clazz,
	setup = {
		ensureUniqueIndexCaseInsensitive(nameProperty)
		ensureUniqueIndex(worldKProperty, polygonDataProperty)
		setup()
	}
) {
	abstract fun new(id: Oid<T>, name: String, world: String, polygonData: ByteArray): T

	fun create(name: String, world: String, polygonData: ByteArray) = trx { session ->
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
}

interface TerritoryInterface: DbObject {
	val world: String
	val polygonData: ByteArray
}
