package net.horizonsend.ion.common

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo.createClient
import org.litote.kmongo.util.KMongoJacksonFeature
import redis.clients.jedis.JedisPooled
import java.io.File
import java.lang.System.setProperty

object Connectivity {
	internal lateinit var database: MongoDatabase private set
	private lateinit var mongoClient: MongoClient

	private lateinit var jedisPool: JedisPooled

	fun open(dataDirectory: File) {
		val configuration: SharedConfiguration = Configuration.load(dataDirectory, "database.json")

		setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")

		KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)

		mongoClient = createClient(
			MongoClientSettings
				.builder()
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.applyConnectionString(ConnectionString(configuration.mongoConnectionUri))
				.build()
		)

		database = mongoClient.getDatabase(configuration.databaseName)

		jedisPool = JedisPooled(configuration.redisConnectionUri)
	}

	fun close() {
		jedisPool.close()
		mongoClient.close()
	}
}
