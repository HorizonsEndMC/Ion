package net.horizonsend.ion.common.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.CargoCrate
import net.horizonsend.ion.common.database.schema.economy.CargoCrateShipment
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.economy.CollectedItem
import net.horizonsend.ion.common.database.schema.economy.EcoStation
import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.Shuttle
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.CapturableStationSiege
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.SettlementZone
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.common.database.schema.space.Planet
import net.horizonsend.ion.common.database.schema.space.Star
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonReader
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdGenerator
import org.litote.kmongo.util.KMongoUtil
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Protocol
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.reflect.KClass

object DBManager : IonComponent() {
	var INITIALIZATION_COMPLETE: Boolean = false

	private val watching = mutableListOf<MongoCursor<ChangeStreamDocument<*>>>()

	internal lateinit var client: MongoClient

	lateinit var jedisPool: JedisPool

	@PublishedApi // to allow it to be used in inline functions
	internal lateinit var database: MongoDatabase

	val threadPool: ExecutorService = Executors.newCachedThreadPool(
		object : ThreadFactory {
			private var counter: Int = 0

			override fun newThread(r: Runnable): Thread {
				return Thread(r, "ion-mongodb-pool-${counter++}")
			}
		}
	)

	override fun onEnable() {
		jedisPool = JedisPool(CommonConfig.redis.host, Protocol.DEFAULT_PORT)

		IdGenerator.defaultGenerator = ObjectIdGenerator

		System.setProperty(
			"org.litote.mongo.test.mapping.service",
			"org.litote.kmongo.jackson.JacksonClassMappingTypeService"
		)

		val username = CommonConfig.db.username
		val password = CommonConfig.db.password
		val host = CommonConfig.db.host
		val port = CommonConfig.db.port
		val authDb = CommonConfig.db.database
		val connectionString = ConnectionString("mongodb://$username:$password@$host:$port/$authDb")
		client = KMongo.createClient(
			MongoClientSettings
				.builder()
				.codecRegistry(KMongoUtil.defaultCodecRegistry)
				.applyConnectionString(connectionString)
				.uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
				.build()
		)

		database = client.getDatabase(CommonConfig.db.database)

		// ##### Load classes of all collections #####

		// misc
		SLPlayer.init()
		Shuttle.init()

		// nations
		CapturableStation.init()
		CapturableStationSiege.init()
		Nation.init()
		NationRelation.init()
		NPCTerritoryOwner.init()
		SettlementRole.init()
		NationRole.init()
		Settlement.init()
		SettlementZone.init()
		Territory.init()

		NationSpaceStation.init()
		SettlementSpaceStation.init()
		PlayerSpaceStation.init()

		// space
		Planet.init()
		Star.init()

		// economy
		CargoCrate.init()
		CargoCrateShipment.init()
		CityNPC.init()
		CollectedItem.init()
		EcoStation.init()
		BazaarItem.init()

		// starships
		PlayerStarshipData.init()
		Blueprint.init()

		Cryopod.init()
		ClaimedBounty.init()
	}

	override fun onDisable() {
		jedisPool.close()

		if (::client.isInitialized) {
			client.close()
		}
	}

	inline fun <reified T> decode(document: Document): T =
		decode(document.toBsonDocument(T::class.java, database.codecRegistry))

	inline fun <reified T> decode(document: BsonDocument): T {
		val codecRegistry: CodecRegistry = database.codecRegistry
		val clazz: Class<T> = T::class.java
		BsonDocumentReader(document).use { reader ->
			return codecRegistry.get(clazz).decode(reader, DecoderContext.builder().build())
		}
	}

	inline fun <reified T> decode(json: String): T {
		val codecRegistry: CodecRegistry = database.codecRegistry

		val clazz: Class<T> = T::class.java

		JsonReader(json).use { reader ->
			return codecRegistry.get(clazz).decode(reader, DecoderContext.builder().build())
		}
	}

	internal fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T> {
		try {
			val collectionName: String = KMongoUtil.defaultCollectionName(clazz)

			if (!database.listCollectionNames().contains(collectionName)) {
				database.createCollection(collectionName)
				println("Created collection $collectionName")
			}

			require(database.listCollectionNames().contains(collectionName))

			return database.getCollection(collectionName, clazz.java)
		} catch (e: Exception) {
			e.printStackTrace()
			throw e
		}
	}

	internal fun registerWatching(cursor: MongoCursor<ChangeStreamDocument<*>>) {
		watching.add(cursor)
	}

	fun closeWatch(cursor: MongoCursor<ChangeStreamDocument<*>>) {
		watching.remove(cursor)
		cursor.close()
	}
}
