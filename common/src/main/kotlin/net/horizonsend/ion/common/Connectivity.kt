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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

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

		println("Performing Data Migration")
		val startTime = System.nanoTime()

		net.horizonsend.ion.common.database.collections.PlayerData.collection.find().forEach {
			if (PlayerData[it.uuid] != null) return@forEach
			if (it.minecraftUsername == null) return@forEach // who tf decided username was String?

			val playerData = PlayerData.new(it.uuid) {
				username = it.minecraftUsername!!
				snowflake = it.discordId
				acceptedBounty = it.acceptedBounty
				bounty = it.bounty
				particle = it.particle
				color = it.color
			}

			it.achievements.forEach {
				PlayerAchievement.new {
					player = playerData
					achievement = it
				}
			}

			it.voteTimes.forEach { (site, datetime) ->
				PlayerVoteTime.new {
					player = playerData
					serviceName = site
					dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.systemDefault())
				}
			}
		}

		val endTime = System.nanoTime()
		val deltaTime = endTime - startTime
		val timeMS = deltaTime / 1_000_000

		println("Migration took ${timeMS}ms")
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
