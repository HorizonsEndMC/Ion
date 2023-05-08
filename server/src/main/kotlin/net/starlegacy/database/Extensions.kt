package net.starlegacy.database

import com.google.gson.Gson
import com.mongodb.MongoException
import com.mongodb.client.ClientSession
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.changestream.ChangeStreamDocument
import com.mongodb.client.result.UpdateResult
import java.util.UUID
import kotlin.collections.set
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import net.starlegacy.database.schema.misc.SLPlayerId
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BsonValue
import org.bson.Document
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.Id
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.IdTransformer
import org.litote.kmongo.id.WrappedObjectId
import org.litote.kmongo.json
import org.litote.kmongo.path
import org.litote.kmongo.projection
import org.litote.kmongo.util.KMongoUtil
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.withDocumentClass

/** Runs code with multi document transaction, only things that use the clientsession use the transaction,
 * can throw error from concurrency, if the code must retry upon write concern then use trx */
fun <T> session(transactional: (ClientSession) -> T): T {
	MongoManager.client.startSession().use { session ->
		try {
			session.startTransaction()
			val ret = session.let(transactional)
			session.commitTransaction()
			return ret
		} catch (exception: Exception) {
			session.abortTransaction()
			throw exception
		}
	}
}

/** Everything in here MUST be done through the clientsession, lest duplicate events occur */
fun <T> trx(transactional: (ClientSession) -> T): T = MongoManager.client.startSession().use { session ->
	val start = System.currentTimeMillis()

	while (true) {
		try {
			session.startTransaction()
			val ret = transactional(session)
			session.commitTransaction()
			return ret
		} catch (e: MongoException) {
			session.abortTransaction()
			println("Transaction aborted. Caught exception during transaction.")

			if (System.currentTimeMillis() - start < 15000 && e.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) {
				println("TransientTransactionError, retrying ...")
				continue
			} else {
				throw e
			}
		}
	}
	// Kotlin compiler doesn't like this not being here
	@Suppress("UNREACHABLE_CODE")
	error("Unreachable code!")
}

operator fun <T : DbObject> MongoCollection<T>.get(id: Oid<T>): T? = findOneById(id)

val Player.slPlayerId: SLPlayerId get() = uniqueId.slPlayerId

fun UpdateResult.requireModifiedOne(): UpdateResult = apply { require(modifiedCount == 1L) }

fun <T> MongoCollection<T>.ensureUniqueIndexCaseInsensitive(
	vararg properties: KProperty<*>,
	indexOptions: IndexOptions = IndexOptions()
): String = ensureIndex(
	properties = *properties,
	indexOptions = indexOptions
		.unique(true)
		.collation(Collation.builder().locale("en").collationStrength(CollationStrength.PRIMARY).build())
)

val SLPlayerId.uuid: UUID get() = UUID.fromString(this.toString())
val UUID.slPlayerId: SLPlayerId
	get() = SLPlayerId(
		this.toString()
	)

fun <T> MongoCollection<T>.updateAll(update: Bson): UpdateResult = updateMany(EMPTY_BSON, update)

fun <T> MongoCollection<T>.updateAll(session: ClientSession, update: Bson): UpdateResult =
	updateMany(session, EMPTY_BSON, update)

fun <T> MongoCollection<T>.none(filter: Bson): Boolean = countDocuments(filter) == 0L

fun <T> MongoCollection<T>.none(session: ClientSession, filter: Bson): Boolean = countDocuments(session, filter) == 0L

class ProjectedResults(document: Document, vararg properties: KProperty<*>) {
	val map = LinkedHashMap<String, Any?>()

	init {
		map["_id"] = document["_id"]

		for (property: KProperty<*> in properties) {
			val path: String = property.path()

			when {
				path.contains(".") -> {
					val split: List<String> = path.split(".")

					var currentDocument: Document = document

					for ((depth: Int, string: String) in split.withIndex()) {
						if (depth == split.lastIndex) {
							require(currentDocument.containsKey(string))
							map[path] = currentDocument[string]
						} else {
							currentDocument = currentDocument.get(string, Document::class.java)
						}
					}
				}

				else -> {
					map[path] = document[path]
				}
			}
		}
	}

	inline operator fun <reified R> get(path: String): R {
		require(map.contains(path)) { "Property $path not in collection $map" }

		val value: Any? = map[path]

		if (value is R) {
			return value
		}

		if (R::class.isSubclassOf(Id::class) && value != null) {
			return IdTransformer.wrapId(value) as R
		}

		try {
			return when (value) {
				is Document -> MongoManager.decode(value)
				else -> Gson().fromJson(value?.json, R::class.java)
			}
		} catch (exception: Exception) {
			throw Exception(
				"Failed to parse for path $path. \nValue: ${value?.json} \nProjected Results: $map",
				exception
			)
		}
	}

	inline operator fun <reified R> get(property: KProperty<R>): R = get(property.path())

	inline fun <reified I, reified R> convertProperty(path: String, convert: (I) -> R): R {
		require(map.contains(path)) { "Property $path not in collection $map" }
		val value: Any? = map[path]
		return convert.invoke(value as I)
	}

	inline fun <reified I, reified R> convertProperty(property: KProperty<R>, convert: (I) -> R): R =
		convertProperty(property.path(), convert)

	override fun toString(): String {
		return KMongoUtil.toExtendedJson(this)
	}
}

val <T : DbObject> ChangeStreamDocument<T>.oid: Oid<T>
	get() {
		val value = documentKey["_id"] ?: error("No ID value in ${KMongoUtil.toExtendedJson(this)}")
		return value.oid()
	}

val <T : DbObject> ChangeStreamDocument<T>.slPlayerId: SLPlayerId
	get() {
		val value = documentKey["_id"] ?: error("No ID value in ${KMongoUtil.toExtendedJson(this)}")
		return SLPlayerId(value.string())
	}

fun <T, R> ChangeStreamDocument<T>.wasRemoved(property: KProperty<R>): Boolean {
	return updateDescription.removedFields?.contains(property.path()) == true
}

fun <T, R> ChangeStreamDocument<T>.containsUpdated(property: KProperty<R>): Boolean {
	return updateDescription.updatedFields?.contains(property.path()) == true
}

/** Returns null if the value wasn't present, otherwise the BsonValue.
 * The BsonValue itself may represent a null value. */
inline operator fun <T, reified R> ChangeStreamDocument<T>.get(property: KProperty<R>): BsonValue? =
	updateDescription.updatedFields?.get(property)

inline operator fun <reified R> BsonDocument.get(property: KProperty<R>): BsonValue? = get(property.path())

fun BsonValue.nullable(): BsonValue? = if (isNull) null else this
fun BsonValue.int(): Int = asInt32().intValue()
fun BsonValue.double(): Double = asDouble().doubleValue()
fun BsonValue.boolean(): Boolean = asBoolean().value
fun BsonValue.string(): String = asString().value
fun BsonValue.binary(): ByteArray = asBinary().data
fun BsonValue.array(): BsonArray = asArray()
fun <T> BsonValue.mappedList(function: (BsonValue) -> T): List<T> = array().map(function)
fun <T> BsonValue.mappedSet(function: (BsonValue) -> T): Set<T> = array().asSequence().map(function).toSet()
inline fun <reified T : Enum<T>> BsonValue.enumValue(): T = enumValueOf(string())
inline fun <reified T> BsonValue.document(): T = MongoManager.decode(asDocument())
fun <T> BsonValue.oid(): Oid<T> = when {
	isObjectId -> WrappedObjectId(asObjectId().value)
	else -> error("Unrecognized object id type $json")
}

fun BsonValue.slPlayerId(): SLPlayerId = when {
	isString -> SLPlayerId(asString().value)
	else -> error("Unrecognized object id type $json")
}

fun <T> MongoCollection<T>.findValues(filter: Bson, vararg properties: KProperty<*>): MongoIterable<ProjectedResults> =
	withDocumentClass<Document>().find(filter).projected(*properties)

fun <T> MongoCollection<T>.findOneValue(filter: Bson, vararg properties: KProperty<*>): ProjectedResults? =
	findValues(filter, *properties).firstOrNull()

fun FindIterable<Document>.projected(vararg properties: KProperty<*>): MongoIterable<ProjectedResults> =
	projection(*properties).map { document -> document.projected(*properties) }

fun Document.projected(vararg properties: KProperty<*>): ProjectedResults = ProjectedResults(this, *properties)

fun <T> MongoCollection<T>.findValuesById(id: Any, vararg properties: KProperty<*>): ProjectedResults? =
	findValues(idFilterQuery(id), *properties).singleOrNull()
