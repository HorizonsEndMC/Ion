package net.horizonsend.ion.server.database

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.server.database.schema.Cryopod
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import net.starlegacy.SETTINGS
import net.starlegacy.SLComponent
import net.horizonsend.ion.server.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.database.schema.economy.CargoCrate
import net.horizonsend.ion.server.database.schema.economy.CargoCrateShipment
import net.horizonsend.ion.server.database.schema.economy.CityNPC
import net.horizonsend.ion.server.database.schema.economy.CollectedItem
import net.horizonsend.ion.server.database.schema.economy.EcoStation
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.misc.Shuttle
import net.horizonsend.ion.server.database.schema.nations.CapturableStation
import net.horizonsend.ion.server.database.schema.nations.CapturableStationSiege
import net.horizonsend.ion.server.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.NationRole
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.SettlementRole
import net.horizonsend.ion.server.database.schema.nations.SettlementZone
import net.horizonsend.ion.server.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.server.database.schema.nations.Territory
import net.horizonsend.ion.server.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStation
import net.horizonsend.ion.server.database.schema.space.Planet
import net.horizonsend.ion.server.database.schema.space.Star
import net.horizonsend.ion.server.database.schema.starships.Blueprint
import net.horizonsend.ion.server.database.schema.starships.PlayerStarshipData
import net.starlegacy.util.Tasks
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.Document
import org.bson.codecs.DecoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.json.JsonReader
import org.litote.kmongo.KMongo
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdGenerator
import org.litote.kmongo.util.KMongoUtil

object MongoManager : SLComponent() {
	private val watching = mutableListOf<MongoCursor<ChangeStreamDocument<*>>>()

	internal lateinit var client: MongoClient

	@PublishedApi // to allow it to be used in inline functions
	internal lateinit var database: MongoDatabase

	val threadPool = Executors.newCachedThreadPool(Tasks.namedThreadFactory("starlegacy-mongodb-cache"))

	override fun onEnable() {
		IdGenerator.defaultGenerator = ObjectIdGenerator

		System.setProperty(
			"org.litote.mongo.test.mapping.service",
			"org.litote.kmongo.jackson.JacksonClassMappingTypeService"
		)

		val username = SETTINGS.mongo.username
		val password = SETTINGS.mongo.password
		val host = SETTINGS.mongo.host
		val port = SETTINGS.mongo.port
		val authDb = SETTINGS.mongo.database
		val connectionString = ConnectionString("mongodb://$username:$password@$host:$port/$authDb")
		client = KMongo.createClient(connectionString)

		database = client.getDatabase(SETTINGS.mongo.database)

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

		SpaceStation.init() // Deprecated
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
	}

	override fun onDisable() {
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
				log.info("Created collection $collectionName")
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
