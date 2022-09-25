package net.horizonsend.ion.common.database

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.findOneById
import org.litote.kmongo.replaceOneById
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.idValue
import java.util.UUID
import kotlin.reflect.KClass

abstract class Collection<D : Document>(private val kClass: KClass<D>) {
	protected lateinit var collection: MongoCollection<D>

	internal fun initialize(database: MongoDatabase) {
		collection = database.getCollection(KMongoUtil.defaultCollectionName(kClass), kClass.java)
	}

	protected abstract fun construct(id: UUID): D

	protected fun update(document: D) {
		collection.replaceOneById(document.idValue!!, document)
	}

	operator fun get(id: UUID): D {
		var document = collection.findOneById(id)

		if (document == null) {
			document = construct(id)
			collection.insertOne(document)
		}

		return document
	}
}