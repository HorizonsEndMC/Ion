package net.horizonsend.ion.common.database

import com.mongodb.client.MongoCollection
import net.horizonsend.ion.common.Connectivity
import org.litote.kmongo.findOneById
import org.litote.kmongo.replaceOneById
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.idValue
import java.util.UUID
import kotlin.reflect.KClass

internal abstract class Collection<D : Document>(kClass: KClass<D>) {
	internal var collection: MongoCollection<D> =
		Connectivity.mongoDatabase.getCollection(KMongoUtil.defaultCollectionName(kClass), kClass.java)

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
