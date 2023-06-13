package net.horizonsend.ion.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import redis.clients.jedis.JedisPooled

object Connectivity {
	lateinit var database: Database
	lateinit var datasource: HikariDataSource

	lateinit var jedisPool: JedisPooled

	fun open(dataDirectory: File) {
		val configuration: DatabaseConfiguration = Configuration.load(dataDirectory, "database.json")

		// Manually loaded because classloaders are weird
		Class.forName("org.h2.Driver")

		val hikariConfiguration = HikariConfig()
		hikariConfiguration.jdbcUrl = configuration.connectionUri

		datasource = HikariDataSource(hikariConfiguration)
		database = Database.connect(datasource)

		transaction {
			SchemaUtils.createMissingTablesAndColumns(
				Nation.Table,
				PlayerAchievement.Table,
				PlayerData.Table,
				Cryopod.Table
			)
		}

		jedisPool = JedisPooled(configuration.redisConnectionUri)
	}

	fun close() {
		jedisPool.close()
		datasource.close()
	}

	@Serializable
	internal data class DatabaseConfiguration(
		internal val connectionUri: String = "jdbc:h2:./plugins/Ion/database",
		internal val redisConnectionUri: String = "redis"
	)
}
