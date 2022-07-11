package net.horizonsend.ion.common.managers

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import net.horizonsend.ion.common.CommonConfiguration
import net.horizonsend.ion.common.CommonConfiguration.DatabaseType
import net.horizonsend.ion.common.database.PlayerDataTable
import net.horizonsend.ion.common.utilities.loadConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object CommonManager {
	private var commonConfiguration: CommonConfiguration = CommonConfiguration()

	fun init(dataDirectory: Path) {
		commonConfiguration = loadConfiguration(dataDirectory)

		when (commonConfiguration.databaseType) {
			DatabaseType.SQLITE ->
				Database.connect("jdbc:sqlite:${dataDirectory.resolve("database.db").absolutePathString()}", "org.sqlite.JDBC")

			DatabaseType.MYSQL ->
				commonConfiguration.databaseDetails.run {
					Database.connect("jdbc:mysql://$host:$port/$database", "com.mysql.cj.jdbc.Driver", username, password)
				}
		}

		transaction {
			SchemaUtils.createMissingTablesAndColumns(PlayerDataTable)
		}
	}
}