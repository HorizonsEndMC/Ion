package net.horizonsend.ion.common.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@Deprecated("")
@ConfigSerializable
data class OriginalDatabaseConfiguration(
	@Deprecated("")
	val databaseType: DatabaseType = DatabaseType.MYSQL,

	@Deprecated("")
	val databaseDetails: DatabaseDetails = DatabaseDetails()
) {
	@Deprecated("")
	enum class DatabaseType {
		@Deprecated("")
		SQLITE,

		@Deprecated("")
		MYSQL
	}

	@Deprecated("")
	@ConfigSerializable
	data class DatabaseDetails(
		@Deprecated("")
		val host: String = "localhost",

		@Deprecated("")
		val port: Int = 3306,

		@Deprecated("")
		val database: String = "ion",

		@Deprecated("")
		val username: String = "ion",

		@Deprecated("")
		val password: String = "password"
	)
}