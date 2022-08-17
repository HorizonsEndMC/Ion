package net.horizonsend.ion.common

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import net.horizonsend.ion.common.CommonConfiguration.DatabaseType
import net.horizonsend.ion.common.database.PlayerDataTable
import net.horizonsend.ion.common.utilities.loadConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Initializes common code including configuration and the database
 * @param dataDirectory Location of the plugin data directory
 * @return Common server configuration
 * @see Path
 * @see CommonConfiguration
 */
fun initializeCommon(dataDirectory: Path): CommonConfiguration {
	val configuration: CommonConfiguration = loadConfiguration(dataDirectory)

	when (configuration.databaseType) {
		DatabaseType.SQLITE ->
			Database.connect(
				"jdbc:sqlite:${dataDirectory.resolve("shared/database.db").absolutePathString()}",
				"org.sqlite.JDBC"
			)

		DatabaseType.MYSQL -> configuration.databaseDetails.run {
			Database.connect("jdbc:mysql://$host:$port/$database", "com.mysql.cj.jdbc.Driver", username, password)
		}
	}

	transaction {
		SchemaUtils.createMissingTablesAndColumns(PlayerDataTable)
	}

	return configuration
}