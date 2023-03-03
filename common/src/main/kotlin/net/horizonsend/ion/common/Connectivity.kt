package net.horizonsend.ion.common

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.PlayerAchievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerVoteTime
import org.bson.UuidRepresentation
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.KMongo.createClient
import org.litote.kmongo.util.KMongoJacksonFeature
import redis.clients.jedis.JedisPooled
import java.io.File
import java.lang.System.setProperty

object Connectivity {
	private lateinit var database: Database
	private lateinit var datasource: HikariDataSource

	internal lateinit var mongoDatabase: MongoDatabase private set
	private lateinit var mongoClient: MongoClient

	private lateinit var jedisPool: JedisPooled

	fun open(dataDirectory: File) {
		val configuration: DatabaseConfiguration = Configuration.load(dataDirectory, "database.json")

		// Manually loaded because classloaders are weird
		Class.forName("org.mariadb.jdbc.Driver")
		Class.forName("org.sqlite.JDBC")

		val hikariConfiguration = HikariConfig()
		hikariConfiguration.jdbcUrl = configuration.connectionUri

		datasource = HikariDataSource(hikariConfiguration)
		database = Database.connect(datasource)

		transaction {
			SchemaUtils.create(PlayerData.Table)
			SchemaUtils.create(PlayerVoteTime.Table)
			SchemaUtils.create(PlayerAchievement.Table)
		}

		setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")

		KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)

		mongoClient = createClient(
			MongoClientSettings
				.builder()
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.applyConnectionString(ConnectionString(configuration.mongoConnectionUri))
				.build()
		)

		mongoDatabase = mongoClient.getDatabase(configuration.databaseName)

		jedisPool = JedisPooled(configuration.redisConnectionUri)
	}

	fun close() {
		jedisPool.close()
		mongoClient.close()
		datasource.close()
	}

	@Serializable
	internal data class DatabaseConfiguration(
		internal val connectionUri: String = "jdbc:sqlite:plugins/Ion/database.db",
		internal val mongoConnectionUri: String = "mongodb://test:test@mongo",
		internal val redisConnectionUri: String = "redis",
		internal val databaseName: String = "ion"
	)
}
