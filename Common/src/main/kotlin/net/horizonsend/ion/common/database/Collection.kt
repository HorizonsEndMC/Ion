package net.horizonsend.ion.common.database

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.findOneById
import org.litote.kmongo.replaceOneById
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass

abstract class Collection<D : Document<T>, T: Any>(
	private val kClass: KClass<D>
) {
	internal fun initialize(database: MongoDatabase) {
		database.getCollection(KMongoUtil.defaultCollectionName(kClass), kClass.java)
	}

	protected lateinit var collection: MongoCollection<D>

	protected abstract fun construct(id: T): D

	protected fun update(document: D) {
		collection.replaceOneById(document._id, document)
	}

	operator fun get(id: T): D {
		var document = collection.findOneById(id)

		if (document == null) {
			document = construct(id)
			collection.insertOne(document)
		}

		return document
	}
}