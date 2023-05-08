package net.starlegacy.database

import com.mongodb.MongoException
import com.mongodb.client.ChangeStreamIterable
import com.mongodb.client.ClientSession
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Filters
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import com.mongodb.client.result.UpdateResult
import java.io.Closeable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import net.starlegacy.INITIALIZATION_COMPLETE
import net.starlegacy.database.MongoManager.getCollection
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.WrappedObjectId
import org.litote.kmongo.json
import org.litote.kmongo.match
import org.litote.kmongo.updateOneById
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.slf4j.LoggerFactory

interface DbObject {
	@Suppress("PropertyName")
	val _id: Id<*>
}

typealias Oid<T> = WrappedObjectId<T>

fun <T> objId(): Oid<T> = WrappedObjectId(ObjectId())

abstract class OidDbObjectCompanion<T : DbObject>(clazz: KClass<T>, setup: MongoCollection<T>.() -> Unit = {}) :
	DbObjectCompanion<T, Oid<T>>(clazz, setup)

abstract class DbObjectCompanion<T : DbObject, ID : Id<T>>(
	private val clazz: KClass<T>,
	private val setup: MongoCollection<T>.() -> Unit = {}
) {
	companion object {
		private val log = LoggerFactory.getLogger(DbObjectCompanion::class.java)
	}

	lateinit var col: MongoCollection<T>

	fun init() {
		col = getCollection(clazz).apply(setup)
	}

	fun all(): List<T> = col.find().toList()

	fun find(filter: Bson): FindIterable<T> = col.find(filter)
	fun find(filter: String): FindIterable<T> = col.find(filter)

	fun findOne(filter: Bson): T? = col.findOne(filter)
	fun findOne(filter: String): T? = col.findOne(filter)

	fun findProps(filter: Bson, vararg properties: KProperty<*>): MongoIterable<ProjectedResults> =
		col.findValues(filter, *properties)

	fun findOneProps(filter: Bson, vararg properties: KProperty<*>): ProjectedResults? =
		col.findOneValue(filter, *properties)

	fun findPropsById(id: ID, vararg properties: KProperty<*>): ProjectedResults? =
		col.findValuesById(id, *properties)

	inline fun <reified R> findProp(query: Bson = EMPTY_BSON, property: KProperty<R>): MongoIterable<R> =
		col.findValues(query, property).map { it[property] }

	inline fun <reified R> findOneProp(query: Bson = EMPTY_BSON, property: KProperty<R>): R? =
//        col.projection(property, query).firstOrNull()
		col.findOneValue(query, property)?.get(property)

	inline fun <reified R> findPropById(id: ID, property: KProperty<R>): R? =
		col.findOneValue(Filters.eq("_id", id), property)?.get(property)

	@Suppress("UNCHECKED_CAST")
	fun allIds(): MongoIterable<ID> = findProp(EMPTY_BSON, DbObject::_id) as MongoIterable<ID>

	fun findById(id: ID): T? = col.findOneById(id)

	fun findById(sess: ClientSession, id: ID) = col.find(sess, idFilterQuery(id)).firstOrNull()

	fun updateById(id: ID, vararg updates: Bson): UpdateResult =
		if (updates.size == 1) {
			col.updateOneById(id, updates.single())
		} else {
			col.updateOneById(id, combine(*updates))
		}

	internal fun updateById(sess: ClientSession, id: ID, vararg updates: Bson): UpdateResult =
		if (updates.size == 1) {
			col.updateOneById(sess, id, updates.single())
		} else {
			col.updateOneById(sess, id, combine(*updates))
		}

	fun matches(id: ID, filter: Bson): Boolean = col.countDocuments(and(idFilterQuery(id), filter)) == 1L

	fun matches(sess: ClientSession, id: ID, filter: Bson): Boolean =
		col.countDocuments(sess, and(idFilterQuery(id), filter)) == 1L

	fun count(query: Bson): Long = col.countDocuments(query)

	fun count(sess: ClientSession, query: Bson): Long = col.countDocuments(sess, query)

	fun none(query: Bson) = count(query) == 0L

	fun none(sess: ClientSession, query: Bson) = count(sess, query) == 0L

	internal fun exists(id: ID): Boolean = count(idFilterQuery(id)) == 1L

	internal fun exists(sess: ClientSession, id: ID): Boolean = count(sess, idFilterQuery(id)) == 1L

	fun watch(
		fullDocument: Boolean = false,
		pipelines: List<Bson>,
		onReceive: (ChangeStreamDocument<T>) -> Unit
	): Closeable {
		val changeStreamIterable: ChangeStreamIterable<T> = col
			.watch(pipelines)
			.fullDocument(if (fullDocument) FullDocument.UPDATE_LOOKUP else FullDocument.DEFAULT)

		val cursor: MongoCursor<ChangeStreamDocument<T>> = changeStreamIterable.iterator()
		@Suppress("UNCHECKED_CAST")
		MongoManager.registerWatching(cursor as MongoCursor<ChangeStreamDocument<*>>)

		MongoManager.threadPool.submit {
			while (true) {
				val change: ChangeStreamDocument<T>
				try {
					if (!cursor.hasNext()) {
						continue
					}
					change = cursor.next()
				} catch (e: MongoException) {
					if (e.message == "state should be: open") {
						// TODO: Find better solution to this
						break
					} else {
						e.printStackTrace()
						continue
					}
				}
				try {
					if (fullDocument && change.fullDocument == null) {
						log.warn("Full document set to true but not received.")
					}

					// to prevent weird things from happening, delay all update handling till initialization is complete
					// however, we still need to listen immediately so we don't miss any updates
					while (!INITIALIZATION_COMPLETE);

					onReceive.invoke(change)
				} catch (exception: Exception) {
					log.error("Error while processing change: ${change.json}", exception)
				}
			}
		}

		return Closeable {
			cursor.close()
			@Suppress("UNCHECKED_CAST")
			MongoManager.closeWatch(cursor as MongoCursor<ChangeStreamDocument<*>>)
		}
	}

	fun watchOperations(
		fullDocument: Boolean = false,
		operationTypes: List<OperationType>,
		onReceive: (ChangeStreamDocument<T>) -> Unit
	): Closeable = watch(
		fullDocument,
		listOf(match(Filters.`in`("operationType", operationTypes.map { it.value }))),
		onReceive
	)

	fun watchInserts(onReceive: (ChangeStreamDocument<T>) -> Unit): Closeable =
		watchOperations(true, listOf(OperationType.INSERT), onReceive)

	fun watchDeletes(fullDocument: Boolean = false, onReceive: (ChangeStreamDocument<T>) -> Unit): Closeable =
		watchOperations(fullDocument, listOf(OperationType.DELETE), onReceive)

	fun watchUpdates(fullDocument: Boolean = false, onReceive: (ChangeStreamDocument<T>) -> Unit): Closeable =
		watchOperations(fullDocument, listOf(OperationType.UPDATE), onReceive)
}
