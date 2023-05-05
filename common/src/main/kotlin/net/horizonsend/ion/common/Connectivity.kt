package net.horizonsend.ion.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.PlayerAchievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.PlayerVoteTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import redis.clients.jedis.JedisPooled
import java.io.File

object Connectivity {
	private lateinit var database: Database
	private lateinit var datasource: HikariDataSource

	private lateinit var jedisPool: JedisPooled

	fun open(dataDirectory: File) {
		val configuration: DatabaseConfiguration = Configuration.load(dataDirectory, "database.json")

		// Manually loaded because classloaders are weird
		Class.forName("org.h2.Driver")

		val hikariConfiguration = HikariConfig()
		hikariConfiguration.jdbcUrl = configuration.connectionUri

		datasource = HikariDataSource(hikariConfiguration)
		database = Database.connect(datasource)

		transaction {
			SchemaUtils.createMissingTablesAndColumns(PlayerData.Table, PlayerVoteTime.Table, PlayerAchievement.Table)
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
