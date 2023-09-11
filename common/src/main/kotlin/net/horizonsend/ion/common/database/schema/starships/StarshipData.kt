package net.horizonsend.ion.common.database.schema.starships

import com.mongodb.client.MongoCollection
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.StarshipTypeDB
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface StarshipData: DbObject {
	var starshipType: StarshipTypeDB
	var serverName: String?
	var levelName: String
	var blockKey: Long

	var containedChunks: Set<Long>?

	var lastUsed: Long
	var isLockEnabled: Boolean
}

abstract class StarshipDataCompanion<T: StarshipData>(
	clazz: KClass<T>,
	serverNameProperty: KProperty<String?>,
	levelNameProperty: KProperty<String>,
	blockKeyProperty: KProperty<Long>,
	private val setup: MongoCollection<T>.() -> Unit = {}
) : OidDbObjectCompanion<T>(clazz, setup = {
	ensureIndex(serverNameProperty)
	ensureIndex(levelNameProperty)
	ensureUniqueIndex(levelNameProperty, blockKeyProperty)
	setup(this)
}) {
	fun add(data: T) {
		col.insertOne(data)
	}

	fun remove(dataId: Oid<T>) {
		col.deleteOneById(dataId)
	}
}
