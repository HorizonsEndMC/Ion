package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.OriginalDatabaseConfiguration.DatabaseType
import java.io.File
import net.horizonsend.ion.common.loadConfiguration
import org.jetbrains.exposed.sql.Database

fun initializeDatabase(dataDirectory: File) {
	val originalConfiguration: OriginalDatabaseConfiguration = loadConfiguration(dataDirectory.resolve("shared"), "common.conf")

	val databaseConfiguration: DatabaseConfiguration = loadConfiguration(dataDirectory.resolve("shared"), "database.conf")

	when (originalConfiguration.databaseType) {
		DatabaseType.SQLITE ->
			Database.connect(
				"jdbc:sqlite:${dataDirectory.resolve("shared/database.db").absolutePath}",
				"org.sqlite.JDBC"
			)

		DatabaseType.MYSQL -> originalConfiguration.databaseDetails.run {
			Database.connect("jdbc:mysql://$host:$port/$database", "com.mysql.cj.jdbc.Driver", username, password)
		}
	}
}